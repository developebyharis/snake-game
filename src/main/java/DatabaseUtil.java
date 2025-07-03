package src.main.java;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/snake_db"; // Change DB name
    private static final String USER = "root"; 
    private static final String PASSWORD = "haris123";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void saveGameResult(String player1Name, String player2Name, int player1Score, int player2Score, String winner) {
        String sql = "INSERT INTO game_history (player1_name, player2_name, player1_score, player2_score, winner) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player1Name);
            ps.setString(2, player2Name);
            ps.setInt(3, player1Score);
            ps.setInt(4, player2Score);
            ps.setString(5, winner);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving game result: " + e.getMessage());
        }
    }

    public static List<String[]> fetchGameHistory() {
        List<String[]> history = new ArrayList<>();
        String sql = "SELECT player1_name, player2_name, player1_score, player2_score, winner, played_at FROM game_history ORDER BY played_at DESC LIMIT 20";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String[] row = new String[6];
                row[0] = rs.getString("player1_name");
                row[1] = rs.getString("player2_name");
                row[2] = String.valueOf(rs.getInt("player1_score"));
                row[3] = String.valueOf(rs.getInt("player2_score"));
                row[4] = rs.getString("winner");
                row[5] = rs.getTimestamp("played_at").toString();
                history.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching game history: " + e.getMessage());
        }
        return history;
    }
} 