package service;

import model.DBConfig;
import model.User;
import model.Teacher;
import java.sql.*;

public class UserManager {

    // Get a fresh database connection for each operation
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
    }

    // Helper method for consistent error handling
    private void handleSQLError(String operation, SQLException e) {
        System.out.println("❌ " + operation + " error: " + e.getMessage());
    }

    public boolean register(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            handleSQLError("Registration", e);
            return false;
        }
    }

    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int score = rs.getInt("score");
                return new User(username, password, score);
            }
        } catch (SQLException e) {
            handleSQLError("Login", e);
        }
        return null;
    }

    public void updateUserScore(User user, int newScore) {
        String sql = "UPDATE users SET score = ? WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newScore);
            ps.setString(2, user.getUsername());
            ps.executeUpdate();
        } catch (SQLException e) {
            handleSQLError("Score update", e);
        }
    }

    public void showLeaderboard() {
        String sql = "SELECT username, score FROM users ORDER BY score DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n📊 Leaderboard:");
            System.out.printf("%-15s %s\n", "Username", "Score");
            System.out.println("------------------------");
            while (rs.next()) {
                System.out.printf("%-15s %d\n", rs.getString("username"), rs.getInt("score"));
            }
            System.out.println("------------------------\n");
        } catch (SQLException e) {
            handleSQLError("Leaderboard", e);
        }
    }

    // Teacher authentication method
    public Teacher teacherLogin(String username, String password) {
        String sql = "SELECT * FROM teachers WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Teacher(username, password);
            }
        } catch (SQLException e) {
            handleSQLError("Teacher Login", e);
        }
        return null;
    }

    // Register a new teacher
    public boolean registerTeacher(String username, String password) {
        String sql = "INSERT INTO teachers (username, password) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            handleSQLError("Teacher Registration", e);
            return false;
        }
    }

    // Show all students list for teachers
    public void showStudentsList() {
        String sql = "SELECT username FROM users ORDER BY username ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n👥 Students List (Alphabetical Order):");
            System.out.printf("%-5s %-20s\n", "S/N", "Student Name");
            System.out.println("--------------------------------");
            int serialNumber = 1;
            while (rs.next()) {
                System.out.printf("%-5d %-20s\n", serialNumber++, rs.getString("username"));
            }
            System.out.println("--------------------------------");
            System.out.println("Total Students: " + (serialNumber - 1) + "\n");
        } catch (SQLException e) {
            handleSQLError("Students List", e);
        }
    }

    // Enhanced leaderboard for teachers with more details
    public void showDetailedLeaderboard() {
        String sql = "SELECT username, score FROM users ORDER BY score DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n🏆 Detailed Leaderboard:");
            System.out.printf("%-5s %-20s %s\n", "Rank", "Username", "Score");
            System.out.println("------------------------------------");
            int rank = 1;
            while (rs.next()) {
                System.out.printf("%-5d %-20s %d\n", rank++, rs.getString("username"), rs.getInt("score"));
            }
            System.out.println("------------------------------------\n");
        } catch (SQLException e) {
            handleSQLError("Detailed Leaderboard", e);
        }
    }








}
