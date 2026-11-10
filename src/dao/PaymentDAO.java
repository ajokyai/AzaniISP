package dao;

import model.Payment;
import util.DBConnection;
import java.sql.*;

public class PaymentDAO {

    public void save(Payment p) throws SQLException {
        String sql = "INSERT INTO payment(institution_id, payment_type, amount, payment_date) VALUES(?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getInstitutionId());
            ps.setString(2, p.getPaymentType());
            ps.setDouble(3, p.getAmount());
            ps.setDate(4, Date.valueOf(p.getPaymentDate()));
            ps.executeUpdate();
        }
    }

    public boolean hasPayment(int institutionId, String type) throws SQLException {
    String sql = "SELECT COUNT(*) FROM payment WHERE institution_id = ? AND payment_type = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, institutionId);
        ps.setString(2, type);
        try (ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }
}

}
