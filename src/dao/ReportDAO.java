package dao;

import service.FeeCalculator;
import util.DBConnection;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class ReportDAO {

    private FeeCalculator calc = new FeeCalculator();

    // The latest site survey for each institution
    private static final String LATEST =
        "s.survey_id = (SELECT MAX(x.survey_id) FROM site_survey x WHERE x.institution_id = s.institution_id)";

    private DefaultTableModel emptyModel(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    // Runs any SELECT and turns the result into a table model
    private DefaultTableModel run(String sql) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            int n = md.getColumnCount();
            String[] cols = new String[n];
            for (int i = 0; i < n; i++) cols[i] = md.getColumnLabel(i + 1).replace('_', ' ');
            DefaultTableModel m = emptyModel(cols);
            while (rs.next()) {
                Object[] row = new Object[n];
                for (int i = 0; i < n; i++) row[i] = rs.getObject(i + 1);
                m.addRow(row);
            }
            return m;
        }
    }

    // 3(a)
    public DefaultTableModel registered() throws SQLException {
        return run("SELECT i.name AS Institution, i.category AS Category, i.location AS Location, "
                 + "i.reg_date AS Registered, i.status AS Status, c.full_name AS Contact, c.phone AS Phone "
                 + "FROM institution i LEFT JOIN contact_person c ON i.institution_id = c.institution_id "
                 + "ORDER BY i.name");
    }

    // 3(b): unpaid after the end of the bill month
    public DefaultTableModel defaulters() throws SQLException {
        double rate = FeeCalculator.LATE_FINE_RATE;
        return run("SELECT i.name AS Institution, DATE_FORMAT(b.bill_month,'%Y-%m') AS Bill_month, "
                 + "b.amount_due AS Amount_due, ROUND(b.amount_due * " + rate + ", 2) AS Fine, "
                 + "ROUND(b.amount_due * " + (1 + rate) + ", 2) AS Total_owed "
                 + "FROM monthly_bill b JOIN institution i ON i.institution_id = b.institution_id "
                 + "WHERE b.amount_paid < b.amount_due + b.fine AND CURDATE() > LAST_DAY(b.bill_month) "
                 + "ORDER BY i.name, b.bill_month");
    }

    // 3(c): unpaid after the 10th of the next month
    public DefaultTableModel disconnections() throws SQLException {
        double rate = FeeCalculator.LATE_FINE_RATE;
        double reconnect = FeeCalculator.RECONNECTION_FEE;
        return run("SELECT i.name AS Institution, i.status AS Status, DATE_FORMAT(b.bill_month,'%Y-%m') AS Bill_month, "
                 + "b.amount_due AS Amount_due, ROUND(b.amount_due * " + rate + ", 2) AS Fine, "
                 + reconnect + " AS Reconnection_fee, "
                 + "ROUND(b.amount_due * " + (1 + rate) + " + " + reconnect + ", 2) AS Total_to_reconnect "
                 + "FROM monthly_bill b JOIN institution i ON i.institution_id = b.institution_id "
                 + "WHERE b.amount_paid < b.amount_due + b.fine "
                 + "AND CURDATE() > DATE_ADD(LAST_DAY(b.bill_month), INTERVAL 10 DAY) "
                 + "ORDER BY i.name, b.bill_month");
    }

    // 3(d)
    public DefaultTableModel infrastructure() throws SQLException {
        return run("SELECT i.name AS Institution, s.num_users AS Users, s.pcs_existing AS PCs_owned, "
                 + "IF(s.lan_ready, 'Yes', 'No') AS LAN_ready, s.pcs_to_buy AS PCs_to_buy, "
                 + "s.nodes_to_buy AS Nodes_to_buy, s.survey_date AS Surveyed "
                 + "FROM site_survey s JOIN institution i ON i.institution_id = s.institution_id "
                 + "WHERE " + LATEST + " ORDER BY i.name");
    }

    // 4(a): installation fee + PCs + LAN
    public DefaultTableModel installationCosts() throws SQLException {
        DefaultTableModel m = emptyModel("Institution", "PCs", "LAN nodes",
                "Installation fee", "PC cost", "LAN cost", "Total");
        String sql = "SELECT i.name, s.pcs_to_buy, s.nodes_to_buy "
                   + "FROM site_survey s JOIN institution i ON i.institution_id = s.institution_id "
                   + "WHERE " + LATEST + " ORDER BY i.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int pcs = rs.getInt(2);
                int nodes = rs.getInt(3);
                double pcCost = pcs * FeeCalculator.PC_PRICE;
                double lan = calc.lanCost(nodes);
                m.addRow(new Object[]{rs.getString(1), pcs, nodes,
                        FeeCalculator.INSTALLATION_FEE, pcCost, lan,
                        FeeCalculator.INSTALLATION_FEE + pcCost + lan});
            }
        }
        return m;
    }

    // 4(b): PCs and LAN only, for institutions that bought something
    public DefaultTableModel pcAndLanCosts() throws SQLException {
        DefaultTableModel m = emptyModel("Institution", "PCs", "LAN nodes", "PC cost", "LAN cost", "Total");
        String sql = "SELECT i.name, s.pcs_to_buy, s.nodes_to_buy "
                   + "FROM site_survey s JOIN institution i ON i.institution_id = s.institution_id "
                   + "WHERE " + LATEST + " AND (s.pcs_to_buy > 0 OR s.nodes_to_buy > 0) ORDER BY i.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int pcs = rs.getInt(2);
                int nodes = rs.getInt(3);
                double pcCost = pcs * FeeCalculator.PC_PRICE;
                double lan = calc.lanCost(nodes);
                m.addRow(new Object[]{rs.getString(1), pcs, nodes, pcCost, lan, pcCost + lan});
            }
        }
        return m;
    }

    // 4(c): upgraded institutions pay the new package less 10%
    public DefaultTableModel upgradedCharges() throws SQLException {
        DefaultTableModel m = emptyModel("Institution", "Package (MBPS)", "Standard price",
                "Discounted charge", "Saving");
        String sql = "SELECT i.name, b.mbps, b.monthly_cost FROM institution i "
                   + "JOIN bandwidth_package b ON i.bandwidth_id = b.bandwidth_id "
                   + "WHERE i.upgraded = 1 ORDER BY i.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double price = rs.getDouble(3);
                double charge = calc.upgradedMonthlyCharge(price);
                m.addRow(new Object[]{rs.getString(1), rs.getInt(2), price, charge, price - charge});
            }
        }
        return m;
    }

    // 4(d): totals per institution category
    public DefaultTableModel categorySummary() throws SQLException {
        return run("SELECT i.category AS Category, SUM(b.amount_due) AS Monthly_charges, "
                 + "SUM(b.fine) AS Overdue_fines, SUM(b.reconnection_fee) AS Reconnection_fees, "
                 + "SUM(b.amount_due + b.fine + b.reconnection_fee) AS Total "
                 + "FROM monthly_bill b JOIN institution i ON i.institution_id = b.institution_id "
                 + "GROUP BY i.category ORDER BY i.category");
    }

    // 4(e): money received for each service, sorted by institution
    public DefaultTableModel serviceTotals() throws SQLException {
        return run("SELECT i.name AS Institution, "
                 + "SUM(CASE WHEN p.payment_type='REGISTRATION' THEN p.amount ELSE 0 END) AS Registration, "
                 + "SUM(CASE WHEN p.payment_type='INSTALLATION' THEN p.amount ELSE 0 END) AS Installation, "
                 + "SUM(CASE WHEN p.payment_type='PC' THEN p.amount ELSE 0 END) AS PCs, "
                 + "SUM(CASE WHEN p.payment_type='LAN' THEN p.amount ELSE 0 END) AS LAN, "
                 + "SUM(CASE WHEN p.payment_type='MONTHLY' THEN p.amount ELSE 0 END) AS Monthly, "
                 + "SUM(CASE WHEN p.payment_type='FINE' THEN p.amount ELSE 0 END) AS Fines, "
                 + "SUM(CASE WHEN p.payment_type='RECONNECTION' THEN p.amount ELSE 0 END) AS Reconnection, "
                 + "SUM(p.amount) AS Total "
                 + "FROM payment p JOIN institution i ON i.institution_id = p.institution_id "
                 + "GROUP BY i.name ORDER BY i.name");
    }
}