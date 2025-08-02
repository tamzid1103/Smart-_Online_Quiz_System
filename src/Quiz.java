package service;

import model.DBConfig;
import model.Question;
import model.User;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class Quiz {
    private List<Question> questions;
    private final int TIME_LIMIT = 30; // seconds per question

    public Quiz() {
        questions = new ArrayList<>();
        loadQuestionsFromDB(); // Load from DB
    }

    // Loads questions from the database and shuffles them
    private void loadQuestionsFromDB() {
        questions.clear();
        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM questions")) {
            while (rs.next()) {
                String text = rs.getString("question_text");
                String[] opts = {
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3"),
                        rs.getString("option4")
                };
                int correct = rs.getInt("correct_option") + 1; // convert 0-based DB to 1-based app
                questions.add(new Question(text, opts, correct));
            }
        } catch (SQLException e) {
            System.out.println("Question load error: " + e.getMessage());
        }
        Collections.shuffle(questions);
    }

    public int start(User user) {
        Scanner scanner = new Scanner(System.in);
        int score = 0;
        System.out.println("\n🧠 Quiz starts for " + user.getUsername() + ". Time per question: " + TIME_LIMIT + " seconds.\n");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        for (Question q : questions) {
            q.display();
            System.out.print("Your answer (1-4): ");

            Callable<Integer> inputTask = () -> {
                while (!scanner.hasNextInt()) {
                    scanner.next(); // consume invalid input
                }
                return scanner.nextInt();
            };

            Future<Integer> future = executor.submit(inputTask);
            Integer answer = null;
            try {
                answer = future.get(TIME_LIMIT, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                System.out.println("\n⏰ Time's up! Skipping question.\n");
                future.cancel(true);
            } catch (Exception e) {
                System.out.println("Input error: " + e.getMessage());
                future.cancel(true);
            }

            if (answer != null) {
                if (q.checkAnswer(answer)) {
                    System.out.println("✅ Correct!\n");
                    score++;
                } else {
                    System.out.println("❌ Wrong!\n");
                }
            }
        }

        executor.shutdownNow();
        System.out.println("Quiz Over! Final score: " + score + "/" + questions.size());
        return score;
    }
}
