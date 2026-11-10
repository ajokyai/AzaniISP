package dao;

import model.BandwidthPackage;
import model.Bill;
import service.FeeCalculator;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    private FeeCalculator calc = new FeeCalculator();

    public List<BandwidthPackage> findPackages() throws SQLException {
        List<BandwidthPackage> list = new ArrayList<>();
        String sql = "SELECT bandwidth_id, mbps, monthly_cost FROM bandwidth_package ORDER BY mbps";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new BandwidthPackage(rs.getInt(1), rs.getInt(2), rs.getDouble(3)));
            }
        }
        return list;
    }

    // Connect a new institution, or change its package (upgrade gets the discount)
    public String assignBandwidth(int institutionId, BandwidthPackage pkg) throws SQLException {
        String read = "SELECT i.bandwidth_id, b.monthly_cost FROM institution i "
                    + "LEFT JOIN bandwidth_package b ON i.bandwidth_id = b.bandwidth_id "
                    + "WHERE i.institution_id = ?";
        String write = "UPDATE institution SET bandwidth_id = ?, upgraded = ?, "
                     + "status = IF(status = 'DISCONNECTED', 'DISCONNECTED', 'CONNECTED') "
                     + "WHERE institution_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement r = conn.prepareStatement(read)) {
            r.setInt(1, institutionId);
            boolean hadPackage = false;
            double oldCost = 0;
            try (ResultSet rs = r.executeQuery()) {
                if (rs.next()) {
                    rs.getInt("bandwidth_id");
                    hadPackage = !rs.wasNull();
                    oldCost = rs.getDouble("monthly_cost");
                }
            }
            if (hadPackage && pkg.getCost() == oldCost) {
                return "The institution is already on that package.";
            }
            boolean upgraded = hadPackage && pkg.getCost() > oldCost;
            try (PreparedStatement w = conn.prepareStatement(write)) {
                w.setInt(1, pkg.getId());
                w.setBoolean(2, upgraded);
                w.setInt(3, institutionId);
                w.executeUpdate();
            }
            if (!hadPackage) return "Connected on " + pkg.getMbps() + " MBPS.";
            if (upgraded) return "Upgraded to " + pkg.getMbps() + " MBPS. The 10% upgrade discount applies.";
            return "Changed to " + pkg.getMbps() + " MBPS.";
        }
    }

    // Creates one bill per connected institution that has none for that month
    public int generateBills(LocalDate month) throws SQLException {
        LocalDate first = month.withDayOfMonth(1);
        String select = "SELECT i.institution_id, i.bandwidth_id, i.upgraded, b.monthly_cost "
                      + "FROM institution i JOIN bandwidth_package b ON i.bandwidth_id = b.bandwidth_id "
                      + "WHERE i.status = 'CONNECTED' AND NOT EXISTS ("
                      + "SELECT 1 FROM monthly_bill m WHERE m.institution_id = i.institution_id AND m.bill_month = ?)";
        String insert = "INSERT INTO monthly_bill(institution_id, bill_month, bandwidth_id, amount_due) VALUES(?,?,?,?)";
        int count = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(select);
             PreparedStatement ins = conn.prepareStatement(insert)) {
            ps.setDate(1, Date.valueOf(first));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double cost = rs.getDouble("monthly_cost");
                    double due = rs.getBoolean("upgraded") ? calc.upgradedMonthlyCharge(cost) : cost;
                    ins.setInt(1, rs.getInt("institution_id"));
                    ins.setDate(2, Date.valueOf(first));
                    ins.setInt(3, rs.getInt("bandwidth_id"));
                    ins.setDouble(4, due);
                    ins.executeUpdate();
                    count++;
                }
            }
        }
        return count;
    }

    // Adds 15% fines after month end, and disconnects after the 10th of the next month
    public int[] applyPenalties() throws SQLException {
        String fineSql = "UPDATE monthly_bill SET fine = ROUND(amount_due * ?, 2) "
                       + "WHERE amount_paid < amount_due AND fine = 0 AND CURDATE() > LAST_DAY(bill_month)";
        String cutSql = "UPDATE institution SET status = 'DISCONNECTED' "
                      + "WHERE status = 'CONNECTED' AND institution_id IN ("
                      + "SELECT institution_id FROM monthly_bill "
                      + "WHERE amount_paid < amount_due + fine "
                      + "AND CURDATE() > DATE_ADD(LAST_DAY(bill_month), INTERVAL 10 DAY))";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement f = conn.prepareStatement(fineSql);
             PreparedStatement c = conn.prepareStatement(cutSql)) {
            f.setDouble(1, FeeCalculator.LATE_FINE_RATE);
            int fined = f.executeUpdate();
            int cut = c.executeUpdate();
            return new int[]{fined, cut};
        }
    }

    public List<Bill> findUnpaid() throws SQLException {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT m.bill_id, m.institution_id, i.name, m.bill_month, "
                   + "m.amount_due, m.fine, m.amount_paid "
                   + "FROM monthly_bill m JOIN institution i ON i.institution_id = m.institution_id "
                   + "WHERE m.amount_paid < m.amount_due + m.fine ORDER BY i.name, m.bill_month";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Bill(rs.getInt(1), rs.getInt(2), rs.getString(3),
                        rs.getDate(4).toLocalDate(), rs.getDouble(5), rs.getDouble(6), rs.getDouble(7)));
            }
        }
        return list;
    }

    // Pays the full balance: bill + fine (if late) + reconnection fee (if disconnected)
    public String payBill(Bill bill, LocalDate payDate) throws SQLException {
        LocalDate month = bill.getBillMonth();
        LocalDate lastDay = month.withDayOfMonth(month.lengthOfMonth());
        double due = bill.getAmountDue();
        double fine = bill.getFine();
        if (fine == 0 && payDate.isAfter(lastDay)) {
            fine = Math.round(calc.lateFine(due) * 100) / 100.0;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String status = "";
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT status FROM institution WHERE institution_id = ?")) {
                    ps.setInt(1, bill.getInstitutionId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) status = rs.getString(1);
                    }
                }
                double reconnection = "DISCONNECTED".equals(status) ? FeeCalculator.RECONNECTION_FEE : 0;

                addPayment(conn, bill.getInstitutionId(), "MONTHLY", due, payDate, month);
                if (fine > 0) addPayment(conn, bill.getInstitutionId(), "FINE", fine, payDate, month);
                if (reconnection > 0) addPayment(conn, bill.getInstitutionId(), "RECONNECTION", reconnection, payDate, month);

                try (PreparedStatement up = conn.prepareStatement(
                        "UPDATE monthly_bill SET amount_paid = ?, fine = ?, reconnection_fee = ?, paid_date = ? WHERE bill_id = ?")) {
                    up.setDouble(1, due + fine);
                    up.setDouble(2, fine);
                    up.setDouble(3, reconnection);
                    up.setDate(4, Date.valueOf(payDate));
                    up.setInt(5, bill.getBillId());
                    up.executeUpdate();
                }
                try (PreparedStatement st = conn.prepareStatement(
                        "UPDATE institution SET status = 'CONNECTED' WHERE institution_id = ?")) {
                    st.setInt(1, bill.getInstitutionId());
                    st.executeUpdate();
                }
                conn.commit();
                return String.format("Paid KSh %,.2f (bill %,.2f + fine %,.2f + reconnection %,.2f).",
                        due + fine + reconnection, due, fine, reconnection);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void addPayment(Connection conn, int institutionId, String type,
                            double amount, LocalDate date, LocalDate billMonth) throws SQLException {
        String sql = "INSERT INTO payment(institution_id, payment_type, amount, payment_date, bill_month) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, institutionId);
            ps.setString(2, type);
            ps.setDouble(3, amount);
            ps.setDate(4, Date.valueOf(date));
            ps.setDate(5, Date.valueOf(billMonth));
            ps.executeUpdate();
        }
    }
}
