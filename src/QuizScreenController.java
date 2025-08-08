import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.DBConfig;
import model.Question;
import model.User;
import service.UserManager;

import java.sql.*;
import java.util.*;

public class QuizScreenController {
    private Stage primaryStage;
    private UserManager userManager;
    private User student;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private Timeline timer;
    private Label timerLabel;
    private int timeRemaining = 30;
    private ToggleGroup answerGroup;
    private VBox questionContainer;

    public QuizScreenController(Stage primaryStage, UserManager userManager, User student) {
        this.primaryStage = primaryStage;
        this.userManager = userManager;
        this.student = student;
        this.questions = new ArrayList<>();
        loadQuestionsFromDB();
    }

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

    public void show() {
        if (questions.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Questions");
            alert.setHeaderText("Quiz Unavailable");
            alert.setContentText("No questions available for the quiz. Please contact faculty.");
            alert.showAndWait();
            return;
        }

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        Label quizTitle = new Label("🧠 Quiz - " + student.getUsername());
        quizTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        quizTitle.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        timerLabel = new Label("⏰ 30s");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        timerLabel.setStyle("-fx-text-fill: #ffeb3b; -fx-background-color: rgba(0,0,0,0.3); -fx-padding: 5 10; -fx-background-radius: 15;");

        header.getChildren().addAll(quizTitle, spacer, timerLabel);

        // Question container
        questionContainer = new VBox(15);
        questionContainer.setAlignment(Pos.CENTER);
        questionContainer.setPadding(new Insets(20));
        questionContainer.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 15;");

        // Navigation buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button submitBtn = createStyledButton("✅ Submit Answer", "#4CAF50");
        Button skipBtn = createStyledButton("⏭ Skip Question", "#FF9800");
        Button quitBtn = createStyledButton("🚪 Quit Quiz", "#f44336");

        submitBtn.setOnAction(e -> submitAnswer());
        skipBtn.setOnAction(e -> skipQuestion());
        quitBtn.setOnAction(e -> quitQuiz());

        buttonBox.getChildren().addAll(submitBtn, skipBtn, quitBtn);

        root.getChildren().addAll(header, questionContainer, buttonBox);

        Scene scene = new Scene(root, 700, 500);
        primaryStage.setScene(scene);

        showCurrentQuestion();
        startTimer();
    }

    private void showCurrentQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            endQuiz();
            return;
        }

        questionContainer.getChildren().clear();

        Question currentQuestion = questions.get(currentQuestionIndex);

        // Question number and text
        Label questionNumber = new Label("Question " + (currentQuestionIndex + 1) + " of " + questions.size());
        questionNumber.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        questionNumber.setStyle("-fx-text-fill: #666;");

        Label questionText = new Label(currentQuestion.getText());
        questionText.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        questionText.setStyle("-fx-text-fill: #333;");
        questionText.setWrapText(true);

        // Answer options
        answerGroup = new ToggleGroup();
        VBox optionsBox = new VBox(10);

        String[] options = currentQuestion.getOptions();
        for (int i = 0; i < options.length; i++) {
            RadioButton option = new RadioButton((i + 1) + ". " + options[i]);
            option.setToggleGroup(answerGroup);
            option.setUserData(i + 1); // Store 1-based index
            option.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            option.setStyle("-fx-text-fill: #333;");
            optionsBox.getChildren().add(option);
        }

        questionContainer.getChildren().addAll(questionNumber, questionText, optionsBox);

        // Reset timer
        timeRemaining = 30;
        updateTimerDisplay();
    }

    private void startTimer() {
        if (timer != null) {
            timer.stop();
        }

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            updateTimerDisplay();

            if (timeRemaining <= 0) {
                timer.stop();
                skipQuestion();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void updateTimerDisplay() {
        timerLabel.setText("⏰ " + timeRemaining + "s");
        if (timeRemaining <= 10) {
            timerLabel.setStyle("-fx-text-fill: #f44336; -fx-background-color: rgba(244,67,54,0.2); -fx-padding: 5 10; -fx-background-radius: 15; -fx-font-weight: bold;");
        } else {
            timerLabel.setStyle("-fx-text-fill: #ffeb3b; -fx-background-color: rgba(0,0,0,0.3); -fx-padding: 5 10; -fx-background-radius: 15; -fx-font-weight: bold;");
        }
    }

    private void submitAnswer() {
        if (timer != null) {
            timer.stop();
        }

        // Check if we still have valid questions
        if (currentQuestionIndex >= questions.size()) {
            endQuiz();
            return;
        }

        RadioButton selectedOption = (RadioButton) answerGroup.getSelectedToggle();
        if (selectedOption != null) {
            int answer = (Integer) selectedOption.getUserData();
            Question currentQuestion = questions.get(currentQuestionIndex);

            if (currentQuestion.checkAnswer(answer)) {
                score++;
                showFeedback(true);
            } else {
                showFeedback(false);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Answer Selected");
            alert.setHeaderText("Please select an answer");
            alert.setContentText("You must select an answer before submitting.");
            alert.showAndWait();
            startTimer(); // Restart timer
            return;
        }

        currentQuestionIndex++;

        // Small delay before showing next question
        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
            if (currentQuestionIndex < questions.size()) {
                showCurrentQuestion();
                startTimer();
            } else {
                endQuiz();
            }
        }));
        delay.play();
    }

    private void skipQuestion() {
        if (timer != null) {
            timer.stop();
        }

        showSkipMessage();
        currentQuestionIndex++;

        // Small delay before showing next question
        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (currentQuestionIndex < questions.size()) {
                showCurrentQuestion();
                startTimer();
            } else {
                endQuiz();
            }
        }));
        delay.play();
    }

    private void showFeedback(boolean correct) {
        Label feedback = new Label(correct ? "✅ Correct!" : "❌ Wrong!");
        feedback.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        feedback.setStyle(correct ? "-fx-text-fill: #4CAF50;" : "-fx-text-fill: #f44336;");
        questionContainer.getChildren().add(feedback);
    }

    private void showSkipMessage() {
        Label skipMsg = new Label("⏰ Time's up! Question skipped.");
        skipMsg.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        skipMsg.setStyle("-fx-text-fill: #FF9800;");
        questionContainer.getChildren().add(skipMsg);
    }

    private void quitQuiz() {
        if (timer != null) {
            timer.stop();
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quit Quiz");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Do you want to quit the quiz? Your current progress will be lost.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            // Return to student dashboard
            returnToStudentDashboard();
        } else {
            startTimer(); // Resume timer
        }
    }

    private void endQuiz() {
        if (timer != null) {
            timer.stop();
        }

        userManager.updateUserScore(student, score);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Quiz Completed");
        alert.setHeaderText("🎉 Congratulations!");
        alert.setContentText("Quiz completed!\n\nYour Score: " + score + "/" + questions.size() +
                "\nPercentage: " + String.format("%.1f", (score * 100.0 / questions.size())) + "%");
        alert.showAndWait();

        // Return to student dashboard
        returnToStudentDashboard();
    }

    private void returnToStudentDashboard() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(50));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label title = new Label("👨‍🎓 Student Dashboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        Label welcomeLabel = new Label("Welcome, " + student.getUsername() + "!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        welcomeLabel.setStyle("-fx-text-fill: #ffeb3b;");

        Button takeQuizBtn = createStyledButton("🧠 Take Quiz", "#4CAF50");
        Button leaderboardBtn = createStyledButton("🏆 View Leaderboard", "#2196F3");
        Button logoutBtn = createStyledButton("🚪 Logout", "#f44336");

        takeQuizBtn.setOnAction(e -> {
            QuizScreenController newQuiz = new QuizScreenController(primaryStage, userManager, student);
            newQuiz.show();
        });

        leaderboardBtn.setOnAction(e -> showLeaderboardScreen());
        logoutBtn.setOnAction(e -> showMainScreen());

        root.getChildren().addAll(title, welcomeLabel, takeQuizBtn, leaderboardBtn, logoutBtn);

        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
    }

    private void showLeaderboardScreen() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label title = new Label("🏆 Leaderboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        // Create table for leaderboard
        TableView<LeaderboardEntry> table = new TableView<>();
        table.setPrefHeight(300);

        TableColumn<LeaderboardEntry, String> rankCol = new TableColumn<>("Rank");
        rankCol.setPrefWidth(80);
        rankCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().rank));

        TableColumn<LeaderboardEntry, String> nameCol = new TableColumn<>("Username");
        nameCol.setPrefWidth(200);
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().username));

        TableColumn<LeaderboardEntry, String> scoreCol = new TableColumn<>("Score");
        scoreCol.setPrefWidth(100);
        scoreCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().score));

        table.getColumns().addAll(rankCol, nameCol, scoreCol);

        // Load leaderboard data
        loadLeaderboardData(table);

        Button backBtn = createStyledButton("⬅ Back", "#9E9E9E");
        backBtn.setOnAction(e -> returnToStudentDashboard());

        root.getChildren().addAll(title, table, backBtn);

        Scene scene = new Scene(root, 500, 500);
        primaryStage.setScene(scene);
    }

    private void loadLeaderboardData(TableView<LeaderboardEntry> table) {
        String sql = "SELECT username, score FROM users ORDER BY score DESC";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int rank = 1;
            while (rs.next()) {
                table.getItems().add(new LeaderboardEntry(
                        String.valueOf(rank++),
                        rs.getString("username"),
                        String.valueOf(rs.getInt("score"))
                ));
            }
        } catch (SQLException e) {
            System.out.println("Leaderboard load error: " + e.getMessage());
        }
    }

    private void showMainScreen() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(50));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label title = new Label("🧠 Smart Online Quiz System");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: white;");

        Button studentBtn = createStyledButton("👨‍🎓 Student Portal", "#4CAF50");
        Button facultyBtn = createStyledButton("👨‍🏫 Faculty Portal", "#2196F3");
        Button exitBtn = createStyledButton("🚪 Exit", "#f44336");

        studentBtn.setOnAction(e -> {
            QuizApp app = new QuizApp();
            try {
                app.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        facultyBtn.setOnAction(e -> {
            QuizApp app = new QuizApp();
            try {
                app.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        exitBtn.setOnAction(e -> primaryStage.close());

        root.getChildren().addAll(title, studentBtn, facultyBtn, exitBtn);

        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
    }

    // Helper classes for table data
    public static class LeaderboardEntry {
        public final String rank;
        public final String username;
        public final String score;

        public LeaderboardEntry(String rank, String username, String score) {
            this.rank = rank;
            this.username = username;
            this.score = score;
        }
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefWidth(150);
        button.setPrefHeight(35);
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        );

        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: derive(" + color + ", 20%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 8, 0, 0, 3);" +
                        "-fx-cursor: hand;"
        ));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        ));

        return button;
    }
}
