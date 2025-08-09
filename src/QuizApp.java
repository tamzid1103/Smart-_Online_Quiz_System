import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import service.UserManager;
import model.User;
import model.Teacher;
import model.DBConfig;
import java.sql.*;

public class QuizApp extends Application {
    private Stage primaryStage;
    private UserManager userManager;
    // Add fields to track current logged-in users
    private User currentStudent;
    private Teacher currentTeacher;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.userManager = new UserManager();

        showMainScreen();

        primaryStage.setTitle("🧠 Smart Online Quiz System");
        primaryStage.setResizable(false);
        primaryStage.show();
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

        studentBtn.setOnAction(e -> showLoginScreen(false));
        facultyBtn.setOnAction(e -> showLoginScreen(true));
        exitBtn.setOnAction(e -> primaryStage.close());

        root.getChildren().addAll(title, studentBtn, facultyBtn, exitBtn);

        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
    }

    private void showLoginScreen(boolean isTeacher) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(50));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        String portalType = isTeacher ? "Faculty" : "Student";
        Label title = new Label(portalType + " Portal");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        // Login form
        GridPane loginForm = new GridPane();
        loginForm.setAlignment(Pos.CENTER);
        loginForm.setHgap(10);
        loginForm.setVgap(15);

        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        TextField userField = new TextField();
        userField.setPromptText("Enter username");
        userField.setPrefWidth(200);

        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");
        passField.setPrefWidth(200);

        loginForm.add(userLabel, 0, 0);
        loginForm.add(userField, 1, 0);
        loginForm.add(passLabel, 0, 1);
        loginForm.add(passField, 1, 1);

        Button loginBtn = createStyledButton("🔑 Login", "#4CAF50");
        Button registerBtn = createStyledButton("📝 Register", "#FF9800");
        Button backBtn = createStyledButton("⬅ Back", "#9E9E9E");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loginBtn, registerBtn, backBtn);

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #ffeb3b; -fx-font-size: 12px;");

        loginBtn.setOnAction(e -> handleLogin(userField.getText(), passField.getText(), isTeacher, messageLabel));
        registerBtn.setOnAction(e -> handleRegister(userField.getText(), passField.getText(), isTeacher, messageLabel));
        backBtn.setOnAction(e -> showMainScreen());

        root.getChildren().addAll(title, loginForm, buttonBox, messageLabel);

        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
    }

    private void handleLogin(String username, String password, boolean isTeacher, Label messageLabel) {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            messageLabel.setText("❌ Please fill in all fields");
            return;
        }

        if (isTeacher) {
            Teacher teacher = userManager.teacherLogin(username, password);
            if (teacher != null) {
                currentTeacher = teacher; // Track logged-in teacher
                messageLabel.setText("✅ Login successful!");
                showTeacherDashboard(teacher);
            } else {
                messageLabel.setText("❌ Invalid credentials");
            }
        } else {
            User user = userManager.login(username, password);
            if (user != null) {
                currentStudent = user; // Track logged-in student
                messageLabel.setText("✅ Login successful!");
                showStudentDashboard(user);
            } else {
                messageLabel.setText("❌ Invalid credentials");
            }
        }
    }

    private void handleRegister(String username, String password, boolean isTeacher, Label messageLabel) {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            messageLabel.setText("❌ Please fill in all fields");
            return;
        }

        boolean success;
        if (isTeacher) {
            success = userManager.registerTeacher(username, password);
        } else {
            success = userManager.register(username, password);
        }

        if (success) {
            messageLabel.setText("✅ Registration successful! You can now login.");
        } else {
            messageLabel.setText("❌ Username already exists");
        }
    }

    private void showStudentDashboard(User student) {
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

        // UPDATED: Use CourseSelectionController instead of old QuizScreenController
        takeQuizBtn.setOnAction(e -> showCourseSelection(student));
        leaderboardBtn.setOnAction(e -> showLeaderboardScreen(false));
        logoutBtn.setOnAction(e -> showMainScreen());

        root.getChildren().addAll(title, welcomeLabel, takeQuizBtn, leaderboardBtn, logoutBtn);

        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
    }

    // NEW METHOD: Show course selection instead of direct quiz
    private void showCourseSelection(User student) {
        CourseSelectionController courseSelection = new CourseSelectionController(primaryStage, userManager, student);
        courseSelection.show();
    }

    private void showTeacherDashboard(Teacher teacher) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(50));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label title = new Label("👨‍🏫 Faculty Dashboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        Label welcomeLabel = new Label("Welcome, Faculty " + teacher.getUsername() + "!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        welcomeLabel.setStyle("-fx-text-fill: #ffeb3b;");

        Button leaderboardBtn = createStyledButton("🏆 Detailed Leaderboard", "#2196F3");
        Button studentsBtn = createStyledButton("👥 Students List", "#9C27B0");
        Button questionsBtn = createStyledButton("📝 Manage Questions", "#FF9800");
        Button courseManagementBtn = createStyledButton("📚 Course-Based Management", "#4CAF50"); // NEW
        Button manageStudentsBtn = createStyledButton("👤 Manage Students", "#607D8B");
        Button logoutBtn = createStyledButton("🚪 Logout", "#f44336");

        leaderboardBtn.setOnAction(e -> showLeaderboardScreen(true));
        studentsBtn.setOnAction(e -> showStudentsListScreen());
        questionsBtn.setOnAction(e -> showQuestionManagementScreen());
        courseManagementBtn.setOnAction(e -> showCourseManagement(teacher)); // NEW
        manageStudentsBtn.setOnAction(e -> showStudentManagementScreen());
        logoutBtn.setOnAction(e -> showMainScreen());

        root.getChildren().addAll(title, welcomeLabel, leaderboardBtn, studentsBtn,
                questionsBtn, courseManagementBtn, manageStudentsBtn, logoutBtn);

        Scene scene = new Scene(root, 500, 600);
        primaryStage.setScene(scene);
    }

    // NEW METHOD: Show course-based management
    private void showCourseManagement(Teacher teacher) {
        TeacherCourseManagementController courseManagement = new TeacherCourseManagementController(primaryStage, userManager, teacher);
        courseManagement.show();
    }

    private void showQuizScreen(User student) {
        // Redirect to course selection instead
        showCourseSelection(student);
    }

    private void showLeaderboardScreen(boolean isDetailed) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label title = new Label("🏆 " + (isDetailed ? "Detailed " : "") + "Leaderboard");
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
        backBtn.setOnAction(e -> {
            // Return to appropriate dashboard based on user context
            if (isDetailed && currentTeacher != null) {
                showTeacherDashboard(currentTeacher);
            } else if (!isDetailed && currentStudent != null) {
                showStudentDashboard(currentStudent);
            } else {
                showMainScreen(); // Fallback
            }
        });

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

    private void showStudentsListScreen() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label title = new Label("👥 Students List");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        // Create table for students
        TableView<StudentEntry> table = new TableView<>();
        table.setPrefHeight(300);

        TableColumn<StudentEntry, String> serialCol = new TableColumn<>("S/N");
        serialCol.setPrefWidth(60);
        serialCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().serial));

        TableColumn<StudentEntry, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setPrefWidth(250);
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().name));

        table.getColumns().addAll(serialCol, nameCol);

        // Load students data
        loadStudentsData(table);

        Button backBtn = createStyledButton("⬅ Back", "#9E9E9E");
        backBtn.setOnAction(e -> {
            if (currentTeacher != null) {
                showTeacherDashboard(currentTeacher);
            } else {
                showMainScreen();
            }
        });

        root.getChildren().addAll(title, table, backBtn);

        Scene scene = new Scene(root, 400, 450);
        primaryStage.setScene(scene);
    }

    private void loadStudentsData(TableView<StudentEntry> table) {
        String sql = "SELECT username FROM users ORDER BY username ASC";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int serial = 1;
            while (rs.next()) {
                table.getItems().add(new StudentEntry(
                        String.valueOf(serial++),
                        rs.getString("username")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Students load error: " + e.getMessage());
        }
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefWidth(250);
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

    public static class StudentEntry {
        public final String serial;
        public final String name;

        public StudentEntry(String serial, String name) {
            this.serial = serial;
            this.name = name;
        }
    }

    private void showQuestionManagementScreen() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label title = new Label("📝 Question Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button viewBtn = createStyledButton("👁 View Questions", "#2196F3");
        Button addBtn = createStyledButton("➕ Add Question", "#4CAF50");
        Button deleteBtn = createStyledButton("🗑 Delete Question", "#f44336");
        Button backBtn = createStyledButton("⬅ Back", "#9E9E9E");

        viewBtn.setOnAction(e -> showAllQuestionsScreen());
        addBtn.setOnAction(e -> showAddQuestionScreen());
        deleteBtn.setOnAction(e -> showDeleteQuestionScreen());
        backBtn.setOnAction(e -> {
            if (currentTeacher != null) {
                showTeacherDashboard(currentTeacher);
            } else {
                showMainScreen();
            }
        });

        buttonBox.getChildren().addAll(viewBtn, addBtn);

        HBox buttonBox2 = new HBox(15);
        buttonBox2.setAlignment(Pos.CENTER);
        buttonBox2.getChildren().addAll(deleteBtn, backBtn);

        root.getChildren().addAll(title, buttonBox, buttonBox2);

        Scene scene = new Scene(root, 600, 300);
        primaryStage.setScene(scene);
    }

    private void showAllQuestionsScreen() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label title = new Label("📝 All Quiz Questions");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        ScrollPane scrollPane = new ScrollPane();
        VBox questionsBox = new VBox(15);
        questionsBox.setPadding(new Insets(20));
        questionsBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10;");

        loadAllQuestions(questionsBox);

        scrollPane.setContent(questionsBox);
        scrollPane.setPrefHeight(350);
        scrollPane.setFitToWidth(true);

        Button backBtn = createStyledButton("⬅ Back", "#9E9E9E");
        backBtn.setOnAction(e -> showQuestionManagementScreen());

        root.getChildren().addAll(title, scrollPane, backBtn);

        Scene scene = new Scene(root, 700, 500);
        primaryStage.setScene(scene);
    }

    private void loadAllQuestions(VBox questionsBox) {
        String sql = "SELECT * FROM questions ORDER BY id ASC";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                VBox questionBox = new VBox(8);
                questionBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");

                Label idLabel = new Label("Question ID: " + rs.getInt("id"));
                idLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");

                Label questionLabel = new Label("Q: " + rs.getString("question_text"));
                questionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
                questionLabel.setWrapText(true);

                Label option1 = new Label("1. " + rs.getString("option1"));
                Label option2 = new Label("2. " + rs.getString("option2"));
                Label option3 = new Label("3. " + rs.getString("option3"));
                Label option4 = new Label("4. " + rs.getString("option4"));

                option1.setStyle("-fx-text-fill: #555;");
                option2.setStyle("-fx-text-fill: #555;");
                option3.setStyle("-fx-text-fill: #555;");
                option4.setStyle("-fx-text-fill: #555;");

                Label correctLabel = new Label("Correct Answer: " + (rs.getInt("correct_option") + 1));
                correctLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");

                questionBox.getChildren().addAll(idLabel, questionLabel, option1, option2, option3, option4, correctLabel);
                questionsBox.getChildren().add(questionBox);
            }
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading questions: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #f44336;");
            questionsBox.getChildren().add(errorLabel);
        }
    }

    private void showAddQuestionScreen() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label title = new Label("➕ Add New Question");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10;");

        Label questionLabel = new Label("Question Text:");
        questionLabel.setStyle("-fx-font-weight: bold;");
        TextArea questionArea = new TextArea();
        questionArea.setPromptText("Enter your question here...");
        questionArea.setPrefRowCount(3);

        Label optionsLabel = new Label("Answer Options:");
        optionsLabel.setStyle("-fx-font-weight: bold;");

        TextField option1Field = new TextField();
        option1Field.setPromptText("Option 1");
        TextField option2Field = new TextField();
        option2Field.setPromptText("Option 2");
        TextField option3Field = new TextField();
        option3Field.setPromptText("Option 3");
        TextField option4Field = new TextField();
        option4Field.setPromptText("Option 4");

        Label correctLabel = new Label("Correct Option (1-4):");
        correctLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> correctCombo = new ComboBox<>();
        correctCombo.getItems().addAll("1", "2", "3", "4");
        correctCombo.setValue("1");

        formBox.getChildren().addAll(
                questionLabel, questionArea,
                optionsLabel,
                option1Field, option2Field, option3Field, option4Field,
                correctLabel, correctCombo
        );

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button addBtn = createStyledButton("✅ Add Question", "#4CAF50");
        Button cancelBtn = createStyledButton("❌ Cancel", "#f44336");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #ffeb3b; -fx-font-weight: bold;");

        addBtn.setOnAction(e -> {
            if (questionArea.getText().trim().isEmpty() ||
                    option1Field.getText().trim().isEmpty() ||
                    option2Field.getText().trim().isEmpty() ||
                    option3Field.getText().trim().isEmpty() ||
                    option4Field.getText().trim().isEmpty()) {
                messageLabel.setText("❌ Please fill in all fields");
                return;
            }

            int correctOption = Integer.parseInt(correctCombo.getValue());
            if (userManager.addQuestion(
                    questionArea.getText().trim(),
                    option1Field.getText().trim(),
                    option2Field.getText().trim(),
                    option3Field.getText().trim(),
                    option4Field.getText().trim(),
                    correctOption)) {
                messageLabel.setText("✅ Question added successfully!");
                // Clear form
                questionArea.clear();
                option1Field.clear();
                option2Field.clear();
                option3Field.clear();
                option4Field.clear();
                correctCombo.setValue("1");
            } else {
                messageLabel.setText("❌ Failed to add question");
            }
        });

        cancelBtn.setOnAction(e -> showQuestionManagementScreen());

        buttonBox.getChildren().addAll(addBtn, cancelBtn);

        root.getChildren().addAll(title, formBox, buttonBox, messageLabel);

        Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
    }

    private void showDeleteQuestionScreen() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label title = new Label("🗑 Delete Question");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setAlignment(Pos.CENTER);
        formBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10;");

        Label instructionLabel = new Label("Enter Question ID to delete:");
        instructionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField idField = new TextField();
        idField.setPromptText("Question ID");
        idField.setPrefWidth(200);

        formBox.getChildren().addAll(instructionLabel, idField);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button deleteBtn = createStyledButton("🗑 Delete", "#f44336");
        Button viewBtn = createStyledButton("👁 View Questions", "#2196F3");
        Button cancelBtn = createStyledButton("❌ Cancel", "#9E9E9E");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #ffeb3b; -fx-font-weight: bold;");

        deleteBtn.setOnAction(e -> {
            try {
                int questionId = Integer.parseInt(idField.getText().trim());
                if (questionId <= 0) {
                    messageLabel.setText("❌ Please enter a valid question ID");
                    return;
                }

                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirm Delete");
                confirmAlert.setHeaderText("Delete Question");
                confirmAlert.setContentText("Are you sure you want to delete question ID " + questionId + "?");

                if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                    if (userManager.deleteQuestion(questionId)) {
                        messageLabel.setText("✅ Question deleted successfully!");
                        idField.clear();
                    } else {
                        messageLabel.setText("❌ Question not found or delete failed");
                    }
                }
            } catch (NumberFormatException ex) {
                messageLabel.setText("❌ Please enter a valid number");
            }
        });

        viewBtn.setOnAction(e -> showAllQuestionsScreen());
        cancelBtn.setOnAction(e -> showQuestionManagementScreen());

        buttonBox.getChildren().addAll(deleteBtn, viewBtn, cancelBtn);

        root.getChildren().addAll(title, formBox, buttonBox, messageLabel);

        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
    }

    private void showStudentManagementScreen() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label title = new Label("👤 Student Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setAlignment(Pos.CENTER);
        formBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10;");

        Label instructionLabel = new Label("Enter student username to unregister:");
        instructionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Student Username");
        usernameField.setPrefWidth(200);

        formBox.getChildren().addAll(instructionLabel, usernameField);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button unregisterBtn = createStyledButton("🗑 Unregister", "#f44336");
        Button viewStudentsBtn = createStyledButton("👥 View Students", "#2196F3");
        Button cancelBtn = createStyledButton("❌ Cancel", "#9E9E9E");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #ffeb3b; -fx-font-weight: bold;");

        unregisterBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                messageLabel.setText("❌ Please enter a username");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Unregister");
            confirmAlert.setHeaderText("Unregister Student");
            confirmAlert.setContentText("Are you sure you want to unregister student '" + username + "'?");

            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                if (userManager.deleteStudent(username)) {
                    messageLabel.setText("✅ Student '" + username + "' unregistered successfully!");
                    usernameField.clear();
                } else {
                    messageLabel.setText("❌ Student not found or unregister failed");
                }
            }
        });

        viewStudentsBtn.setOnAction(e -> showStudentsListScreen());
        cancelBtn.setOnAction(e -> {
            if (currentTeacher != null) {
                showTeacherDashboard(currentTeacher);
            } else {
                showMainScreen();
            }
        });

        buttonBox.getChildren().addAll(unregisterBtn, viewStudentsBtn, cancelBtn);

        root.getChildren().addAll(title, formBox, buttonBox, messageLabel);

        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
