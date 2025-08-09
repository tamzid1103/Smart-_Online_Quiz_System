package service;

import model.DBConfig;
import model.Question;
import model.User;
import model.Course;
import model.QuizConfig;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class CourseBasedQuiz {
    private List<Question> questions;
    private Course selectedCourse;
    private QuizConfig quizConfig;
    private UserManager userManager;

    public CourseBasedQuiz() {
        questions = new ArrayList<>();
        userManager = new UserManager();
    }

    // Load questions from specific course
    public void loadQuestionsFromCourse(int courseId) {
        questions.clear();

        // Load quiz configuration for this course
        quizConfig = userManager.getQuizConfig(courseId);

        String sql = "SELECT q.*, c.course_name FROM questions q JOIN courses c ON q.course_id = c.course_id WHERE q.course_id = ? ORDER BY RAND()";

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
                int correct = rs.getInt("correct_option") + 1; // convert 0-based DB to 1-based app
                int questionId = rs.getInt("id");
                String difficulty = rs.getString("difficulty_level");

                if (selectedCourse == null) {
                    selectedCourse = new Course(courseId, rs.getString("course_name"), "", "");
                }

                questions.add(new Question(questionId, text, opts, correct, courseId, difficulty));
            }
        } catch (SQLException e) {
            System.out.println("Question load error: " + e.getMessage());
        }

        // Use configurable question limit instead of hardcoded 10
        int questionLimit = quizConfig.getQuestionLimit();
        if (questions.size() > questionLimit) {
            questions = questions.subList(0, questionLimit);
        }
    }

    public int startQuiz(User user, int courseId) {
        loadQuestionsFromCourse(courseId);

        if (questions.isEmpty()) {
            System.out.println("❌ No questions available for this course!");
            return 0;
        }

        Scanner scanner = new Scanner(System.in);
        int score = 0;
        System.out.println("\n🧠 " + selectedCourse.getCourseName() + " Quiz starts for " + user.getUsername());
        System.out.println("📚 Course: " + selectedCourse.getCourseName());
        System.out.println("⏱️ Time per question: " + quizConfig.getTimePerQuestion() + " seconds");
        System.out.println("❓ Total questions: " + questions.size() + "\n");

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.println("Question " + (i + 1) + "/" + questions.size() + " [" + q.getDifficultyLevel() + "]");
            q.display();
            System.out.print("Your answer (1-4): ");

            Integer answer = getTimedInput(scanner);

            if (answer != null) {
                if (q.checkAnswer(answer)) {
                    System.out.println("✅ Correct!\n");
                    score++;
                } else {
                    System.out.println("❌ Wrong! Correct answer was: " + q.getCorrectIndex() + "\n");
                }
            } else {
                System.out.println("⏰ Time's up! Skipping question. Correct answer was: " + q.getCorrectIndex() + "\n");
            }
        }

        System.out.println("🎉 " + selectedCourse.getCourseName() + " Quiz Complete!");
        System.out.println("📊 Final score: " + score + "/" + questions.size());
        System.out.println("📈 Percentage: " + String.format("%.1f", (score * 100.0 / questions.size())) + "%\n");

        return score;
    }

    private Integer getTimedInput(Scanner scanner) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            // Clear any pending input
            while (System.in.available() > 0) {
                System.in.read();
            }

            Callable<Integer> inputTask = () -> {
                try {
                    StringBuilder input = new StringBuilder();
                    long startTime = System.currentTimeMillis();
                    int timeLimit = quizConfig.getTimePerQuestion() * 1000; // Convert to milliseconds

                    while (System.currentTimeMillis() - startTime < timeLimit) {
                        if (System.in.available() > 0) {
                            int ch = System.in.read();
                            if (ch == '\n' || ch == '\r') {
                                String inputStr = input.toString().trim();
                                if (!inputStr.isEmpty()) {
                                    try {
                                        int answer = Integer.parseInt(inputStr);
                                        if (answer >= 1 && answer <= 4) {
                                            return answer;
                                        } else {
                                            System.out.println("Please enter a number between 1-4");
                                            return null;
                                        }
                                    } catch (NumberFormatException e) {
                                        System.out.println("Please enter a valid number");
                                        return null;
                                    }
                                }
                            } else if (ch != '\r') {
                                input.append((char) ch);
                            }
                        }
                        Thread.sleep(50);
                    }
                    return null;
                } catch (Exception e) {
                    return null;
                }
            };

            Future<Integer> future = executor.submit(inputTask);
            try {
                return future.get(quizConfig.getTimePerQuestion() + 1, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                return null;
            } catch (Exception e) {
                future.cancel(true);
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            executor.shutdownNow();
        }
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public Course getSelectedCourse() {
        return selectedCourse;
    }
}
