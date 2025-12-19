package database;

import model.SportFacility;
import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:sport_objects.db";

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public void initDatabase() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS regions (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS facilities (id INTEGER PRIMARY KEY, name TEXT, region_id INTEGER, address TEXT, entry_date TEXT, FOREIGN KEY(region_id) REFERENCES regions(id))");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public Map<String, Integer> saveRegions(Set<String> names) {
        Map<String, Integer> map = new HashMap<>();
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement("INSERT OR IGNORE INTO regions(name) VALUES(?)")) {
            for (String name : names) {
                if (name != null && !name.isEmpty()) {
                    pstmt.setString(1, name);
                    pstmt.executeUpdate();
                }
            }
            ResultSet rs = conn.createStatement().executeQuery("SELECT id, name FROM regions");
            while (rs.next()) map.put(rs.getString("name"), rs.getInt("id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    public void saveFacilities(List<SportFacility> facilities) {
        try (Connection conn = connect()) {
            conn.createStatement().execute("DELETE FROM facilities");
            String sql = "INSERT OR REPLACE INTO facilities(id, name, region_id, address, entry_date) VALUES(?,?,?,?,?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (SportFacility f : facilities) {
                pstmt.setInt(1, f.id());
                pstmt.setString(2, f.name());
                pstmt.setInt(3, f.regionId());
                pstmt.setString(4, f.address());
                pstmt.setString(5, f.date());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public int getRecordCount() {
        try (Connection conn = connect(); ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM facilities")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }
}