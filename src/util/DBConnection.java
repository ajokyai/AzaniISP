package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL  = "jdbc:mysql://localhost:3306/azani_db";
private static final String USER = System.getenv().getOrDefault("AZANI_DB_USER", "azani");
private static final String PASS = System.getenv().getOrDefault("AZANI_DB_PASS", "Azani2026");
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
