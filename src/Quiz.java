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

        for (Question q : questions) {
            q.display();
            System.out.print("Your answer (1-4): ");

            Integer answer = getTimedInput(scanner);

            if (answer != null) {
                if (q.checkAnswer(answer)) {
                    System.out.println("✅ Correct!\n");
                    score++;
                } else {
                    System.out.println("❌ Wrong!\n");
                }
            } else {
                System.out.println("⏰ Time's up! Skipping question.\n");
            }
        }

        System.out.println("Quiz Over! Final score: " + score + "/" + questions.size());
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
                    // Use a more responsive input reading approach
                    StringBuilder input = new StringBuilder();
                    long startTime = System.currentTimeMillis();

                    while (System.currentTimeMillis() - startTime < TIME_LIMIT * 1000L) {
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
                        Thread.sleep(50); // Small delay to prevent busy waiting
                    }
                    // Timeout occurred
                    return null;
                } catch (Exception e) {
                    return null;
                }
            };

            Future<Integer> future = executor.submit(inputTask);
            try {
                return future.get(TIME_LIMIT + 1, TimeUnit.SECONDS); // Give extra second for cleanup
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
}
