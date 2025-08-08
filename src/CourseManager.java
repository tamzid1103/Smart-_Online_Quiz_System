package service;

import model.Course;
import model.DBConfig;
import model.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseManager {

    // Get all courses
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY course_name";

        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Course course = new Course(
                        rs.getInt("course_id"),
                        rs.getString("course_name"),
                        rs.getString("course_code"),
                        rs.getString("description")
                );
                courses.add(course);
            }
        } catch (SQLException e) {
            System.out.println("Error loading courses: " + e.getMessage());
        }
        return courses;
    }

    // Add new course
    public boolean addCourse(String courseName, String courseCode, String description) {
        String sql = "INSERT INTO courses (course_name, course_code, description) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courseName);
            pstmt.setString(2, courseCode);
            pstmt.setString(3, description);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding course: " + e.getMessage());
            return false;
        }
    }

    // Get questions by course
    public List<Question> getQuestionsByCourse(int courseId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE course_id = ? ORDER BY id";

        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String text = rs.getString("question_text");
                String[] opts = {
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3"),
                        rs.getString("option4")
                };
                int correct = rs.getInt("correct_option") + 1; // convert 0-based to 1-based
                questions.add(new Question(text, opts, correct));
            }
        } catch (SQLException e) {
            System.out.println("Error loading questions for course: " + e.getMessage());
        }
        return questions;
    }

    // Add question to specific course
    public boolean addQuestionToCourse(int courseId, String questionText, String option1,
                                       String option2, String option3, String option4,
                                       int correctOption, String difficulty) {
        String sql = "INSERT INTO questions (course_id, question_text, option1, option2, option3, option4, correct_option, difficulty_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            pstmt.setString(2, questionText);
            pstmt.setString(3, option1);
            pstmt.setString(4, option2);
            pstmt.setString(5, option3);
            pstmt.setString(6, option4);
            pstmt.setInt(7, correctOption - 1); // convert 1-based to 0-based for DB
            pstmt.setString(8, difficulty);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding question to course: " + e.getMessage());
            return false;
        }
    }

    // Get course by ID
    public Course getCourseById(int courseId) {
        String sql = "SELECT * FROM courses WHERE course_id = ?";

        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Course(
                        rs.getInt("course_id"),
                        rs.getString("course_name"),
                        rs.getString("course_code"),
                        rs.getString("description")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error loading course: " + e.getMessage());
        }
        return null;
    }

    // Delete course
    public boolean deleteCourse(int courseId) {
        String sql = "DELETE FROM courses WHERE course_id = ?";

        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting course: " + e.getMessage());
            return false;
        }
    }

    // Count questions in course
    public int getQuestionCountByCourse(int courseId) {
        String sql = "SELECT COUNT(*) FROM questions WHERE course_id = ?";

        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error counting questions: " + e.getMessage());
        }
        return 0;
    }
}
