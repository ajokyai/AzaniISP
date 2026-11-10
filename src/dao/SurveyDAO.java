package dao;

import java.sql.*;
import model.Survey;
import util.DBConnection;

public class SurveyDAO {

    public void save(Survey s) throws SQLException {
        String sql = "INSERT INTO site_survey(institution_id, num_users, pcs_existing, pcs_to_buy, "
                   + "lan_ready, nodes_to_buy, survey_date) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getInstitutionId());
            ps.setInt(2, s.getNumUsers());
            ps.setInt(3, s.getPcsExisting());
            ps.setInt(4, s.getPcsToBuy());
            ps.setBoolean(5, s.isLanReady());
            ps.setInt(6, s.getNodesToBuy());
            ps.setDate(7, Date.valueOf(s.getSurveyDate()));
            ps.executeUpdate();
        }
    }
}