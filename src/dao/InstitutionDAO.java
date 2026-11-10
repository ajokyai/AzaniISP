package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.ContactPerson;
import model.Institution;
import util.DBConnection;

public class InstitutionDAO {

    public void register(Institution inst, ContactPerson cp) throws SQLException {
        String sql1 = "INSERT INTO institution(name, category, location, reg_date) VALUES(?,?,?,CURDATE())";
        String sql2 = "INSERT INTO contact_person(institution_id, full_name, phone, email, position) VALUES(?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement ps2 = conn.prepareStatement(sql2)) {

                ps1.setString(1, inst.getName());
                ps1.setString(2, inst.getCategory());
                ps1.setString(3, inst.getLocation());
                ps1.executeUpdate();

                ResultSet keys = ps1.getGeneratedKeys();
                keys.next();
                int newId = keys.getInt(1);

                ps2.setInt(1, newId);
                ps2.setString(2, cp.getFullName());
                ps2.setString(3, cp.getPhone());
                ps2.setString(4, cp.getEmail());
                ps2.setString(5, cp.getPosition());
                ps2.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Institution> findAll() throws SQLException {
        List<Institution> list = new ArrayList<>();
        String sql = "SELECT institution_id, name, category, location FROM institution ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Institution i = new Institution(
                    rs.getString("name"), rs.getString("category"), rs.getString("location"));
                i.setInstitutionId(rs.getInt("institution_id"));
                list.add(i);
            }
        }
        return list;
    }
}