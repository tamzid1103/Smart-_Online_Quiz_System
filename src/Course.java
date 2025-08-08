package model;

public class Course {
    private int courseId;
    private String courseName;
    private String courseCode;
    private String description;

    public Course() {}

    public Course(int courseId, String courseName, String courseCode, String description) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.description = description;
    }

    public Course(String courseName, String courseCode, String description) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.description = description;
    }

    // Getters and Setters
    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return courseName + " (" + courseCode + ")";
    }
}
