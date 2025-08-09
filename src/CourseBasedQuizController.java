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
import model.Course;
import model.Question;
import model.User;
import model.QuizConfig;
import service.UserManager;

import java.util.Collections;
import java.util.List;

public class CourseBasedQuizController {
    private Stage primaryStage;
    private UserManager userManager;
    private User student;
    private Course course;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private Timeline timer;
    private Label timerLabel;
    private int timeRemaining;
    private ToggleGroup answerGroup;
    private VBox questionContainer;
    private QuizConfig quizConfig;

    public CourseBasedQuizController(Stage primaryStage, UserManager userManager, User student, Course course) {
        this.primaryStage = primaryStage;
        this.userManager = userManager;
        this.student = student;
        this.course = course;

        // Load quiz configuration for this course
        this.quizConfig = userManager.getQuizConfig(course.getCourseId());
        this.timeRemaining = quizConfig.getTimePerQuestion();

        loadQuestionsFromCourse();
    }

    private void loadQuestionsFromCourse() {
        questions = userManager.getQuestionsByCourse(course.getCourseId());
        Collections.shuffle(questions);

        // Use configurable question limit instead of hardcoded 10
        int questionLimit = quizConfig.getQuestionLimit();
        if (questions.size() > questionLimit) {
            questions = questions.subList(0, questionLimit);
        }
    }

    public void show() {
        if (questions.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Questions");
            alert.setHeaderText("Quiz Unavailable");
            alert.setContentText("No questions available for " + course.getCourseName() + ". Please contact faculty.");
            alert.showAndWait();
            returnToCourseSelection();
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

        Label quizTitle = new Label("🧠 " + course.getCourseName() + " Quiz - " + student.getUsername());
        quizTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        quizTitle.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        timerLabel = new Label("⏰ " + quizConfig.getTimePerQuestion() + "s");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
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

        Label courseLabel = new Label("📚 " + course.getCourseName());
        courseLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        courseLabel.setStyle("-fx-text-fill: #888;");

        Label difficultyLabel = new Label("🎯 Difficulty: " + (currentQuestion.getDifficultyLevel() != null ? currentQuestion.getDifficultyLevel() : "Medium"));
        difficultyLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        difficultyLabel.setStyle("-fx-text-fill: #888;");

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

        questionContainer.getChildren().addAll(questionNumber, courseLabel, difficultyLabel, questionText, optionsBox);

        // Reset timer to configurable value
        timeRemaining = quizConfig.getTimePerQuestion();
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
        if (timeRemaining <= 5) {
            timerLabel.setStyle("-fx-text-fill: #f44336; -fx-background-color: rgba(244,67,54,0.2); -fx-padding: 5 10; -fx-background-radius: 15; -fx-font-weight: bold;");
        } else if (timeRemaining <= 10) {
            timerLabel.setStyle("-fx-text-fill: #FF9800; -fx-background-color: rgba(255,152,0,0.2); -fx-padding: 5 10; -fx-background-radius: 15; -fx-font-weight: bold;");
        } else {
            timerLabel.setStyle("-fx-text-fill: #ffeb3b; -fx-background-color: rgba(0,0,0,0.3); -fx-padding: 5 10; -fx-background-radius: 15; -fx-font-weight: bold;");
        }
    }

    private void submitAnswer() {
        if (timer != null) {
            timer.stop();
        }

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

        // Check if this was the last question
        if (currentQuestionIndex >= questions.size()) {
            // Make sure timer is stopped and won't restart
            if (timer != null) {
                timer.stop();
                timer = null; // Clear the timer reference
            }
            // Small delay to show feedback then end quiz
            Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> endQuiz()));
            delay.play();
        } else {
            // Small delay before showing next question
            Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
                showCurrentQuestion();
                startTimer();
            }));
            delay.play();
        }
    }

    private void skipQuestion() {
        if (timer != null) {
            timer.stop();
        }

        showSkipMessage();
        currentQuestionIndex++;

        // Check if this was the last question
        if (currentQuestionIndex >= questions.size()) {
            // Small delay to show skip message then end quiz
            Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1), e -> endQuiz()));
            delay.play();
        } else {
            // Small delay before showing next question
            Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                showCurrentQuestion();
                startTimer();
            }));
            delay.play();
        }
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
        alert.setContentText("Do you want to quit the " + course.getCourseName() + " quiz? Your current progress will be lost.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            returnToCourseSelection();
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
        alert.setHeaderText("🎉 " + course.getCourseName() + " Quiz Complete!");
        alert.setContentText("Quiz completed!\n\nCourse: " + course.getCourseName() +
                "\nYour Score: " + score + "/" + questions.size() +
                "\nPercentage: " + String.format("%.1f", (score * 100.0 / questions.size())) + "%");
        alert.showAndWait();

        returnToCourseSelection();
    }

    private void returnToCourseSelection() {
        CourseSelectionController courseSelection = new CourseSelectionController(primaryStage, userManager, student);
        courseSelection.show();
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
