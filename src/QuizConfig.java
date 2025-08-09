package model;

public class QuizConfig {
    private int courseId;
    private int questionLimit;
    private int timePerQuestion;
    private boolean isActive;

    public QuizConfig() {
        // Default values
        this.questionLimit = 10;
        this.timePerQuestion = 20;
        this.isActive = true;
    }

    public QuizConfig(int courseId, int questionLimit, int timePerQuestion) {
        this.courseId = courseId;
        this.questionLimit = questionLimit;
        this.timePerQuestion = timePerQuestion;
        this.isActive = true;
    }

    // Getters and Setters
    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getQuestionLimit() {
        return questionLimit;
    }

    public void setQuestionLimit(int questionLimit) {
        this.questionLimit = questionLimit;
    }

    public int getTimePerQuestion() {
        return timePerQuestion;
    }

    public void setTimePerQuestion(int timePerQuestion) {
        this.timePerQuestion = timePerQuestion;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "QuizConfig{" +
                "courseId=" + courseId +
                ", questionLimit=" + questionLimit +
                ", timePerQuestion=" + timePerQuestion +
                ", isActive=" + isActive +
                '}';
    }
}
