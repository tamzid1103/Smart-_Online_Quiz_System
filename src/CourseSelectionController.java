import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Course;
import model.User;
import service.UserManager;

import java.util.List;

public class CourseSelectionController {
    private Stage primaryStage;
    private UserManager userManager;
    private User student;

    public CourseSelectionController(Stage primaryStage, UserManager userManager, User student) {
        this.primaryStage = primaryStage;
        this.userManager = userManager;
        this.student = student;
    }

    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        // Header
        Label title = new Label("📚 Select Course for Quiz");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        Label welcomeLabel = new Label("Welcome, " + student.getUsername() + "!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        welcomeLabel.setStyle("-fx-text-fill: #ffeb3b;");

        // Course selection container
        VBox courseContainer = new VBox(15);
        courseContainer.setAlignment(Pos.CENTER);
        courseContainer.setPadding(new Insets(20));
        courseContainer.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 15;");

        Label instructionLabel = new Label("Choose a course to start your quiz:");
        instructionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        instructionLabel.setStyle("-fx-text-fill: #333;");

        // Load courses and create buttons
        List<Course> courses = userManager.getAllCourses();
        VBox courseButtons = new VBox(10);
        courseButtons.setAlignment(Pos.CENTER);

        for (Course course : courses) {
            Button courseBtn = createCourseButton(course);
            courseBtn.setOnAction(e -> startCourseQuiz(course));
            courseButtons.getChildren().add(courseBtn);
        }

        courseContainer.getChildren().addAll(instructionLabel, courseButtons);

        // Navigation buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button backBtn = createStyledButton("⬅ Back to Dashboard", "#9E9E9E");
        Button logoutBtn = createStyledButton("🚪 Logout", "#f44336");

        backBtn.setOnAction(e -> returnToStudentDashboard());
        logoutBtn.setOnAction(e -> showMainScreen());

        buttonBox.getChildren().addAll(backBtn, logoutBtn);

        root.getChildren().addAll(title, welcomeLabel, courseContainer, buttonBox);

        Scene scene = new Scene(root, 600, 550);
        primaryStage.setScene(scene);
    }

    private Button createCourseButton(Course course) {
        VBox courseInfo = new VBox(5);
        courseInfo.setAlignment(Pos.CENTER);

        Label courseName = new Label(course.getCourseName());
        courseName.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        courseName.setStyle("-fx-text-fill: white;");

        Label courseCode = new Label("(" + course.getCourseCode() + ")");
        courseCode.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        courseCode.setStyle("-fx-text-fill: #E8F5E8;");

        Label description = new Label(course.getDescription());
        description.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        description.setStyle("-fx-text-fill: #E8F5E8;");
        description.setWrapText(true);
        description.setMaxWidth(300);

        courseInfo.getChildren().addAll(courseName, courseCode, description);

        Button courseBtn = new Button();
        courseBtn.setGraphic(courseInfo);
        courseBtn.setPrefWidth(350);
        courseBtn.setPrefHeight(80);

        String color = getCourseColor(course.getCourseId());
        courseBtn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        );

        courseBtn.setOnMouseEntered(e -> courseBtn.setStyle(
                "-fx-background-color: derive(" + color + ", 20%);" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 8, 0, 0, 3);" +
                        "-fx-cursor: hand;"
        ));

        courseBtn.setOnMouseExited(e -> courseBtn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        ));

        return courseBtn;
    }

    private String getCourseColor(int courseId) {
        switch (courseId) {
            case 1: return "#FF6B6B"; // Mathematics - Red
            case 2: return "#4ECDC4"; // OOP - Teal
            case 3: return "#45B7D1"; // English - Blue
            case 4: return "#96CEB4"; // General Knowledge - Green
            default: return "#9B59B6"; // Default - Purple
        }
    }

    private void startCourseQuiz(Course course) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Start Quiz");
        confirmAlert.setHeaderText("Start " + course.getCourseName() + " Quiz?");
        confirmAlert.setContentText("You are about to start a quiz for " + course.getCourseName() +
                "\n\nQuiz Rules:\n• 10 random questions\n• 20 seconds per question\n• No going back to previous questions\n\nAre you ready?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            CourseBasedQuizController quizController = new CourseBasedQuizController(primaryStage, userManager, student, course);
            quizController.show();
        }
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

        takeQuizBtn.setOnAction(e -> show()); // Return to course selection
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

        // Create leaderboard table (simplified version)
        ScrollPane scrollPane = new ScrollPane();
        VBox leaderboardContent = new VBox(10);

        // Load leaderboard data and display
        // This would connect to the database to show current rankings
        Label comingSoon = new Label("🔄 Leaderboard functionality will be enhanced\nwith course-specific rankings!");
        comingSoon.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        comingSoon.setStyle("-fx-text-fill: #333; -fx-text-alignment: center;");
        leaderboardContent.getChildren().add(comingSoon);

        scrollPane.setContent(leaderboardContent);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 15;");

        Button backBtn = createStyledButton("⬅ Back", "#9E9E9E");
        backBtn.setOnAction(e -> returnToStudentDashboard());

        root.getChildren().addAll(title, scrollPane, backBtn);

        Scene scene = new Scene(root, 500, 500);
        primaryStage.setScene(scene);
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

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(40);
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        );

        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: derive(" + color + ", 20%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 8, 0, 0, 3);" +
                        "-fx-cursor: hand;"
        ));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        ));

        return button;
    }
}
