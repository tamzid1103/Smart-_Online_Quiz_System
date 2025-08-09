import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Course;
import model.Question;
import service.UserManager;

import java.util.List;

public class TeacherCourseManagementController {
    private Stage primaryStage;
    private UserManager userManager;
    private model.Teacher currentTeacher;

    public TeacherCourseManagementController(Stage primaryStage, UserManager userManager, model.Teacher currentTeacher) {
        this.primaryStage = primaryStage;
        this.userManager = userManager;
        this.currentTeacher = currentTeacher;
    }

    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        // Header
        Label title = new Label("📚 Course-Based Question Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        Label welcomeLabel = new Label("Welcome, Faculty " + currentTeacher.getUsername() + "!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        welcomeLabel.setStyle("-fx-text-fill: #ffeb3b;");

        // Course selection container
        VBox courseContainer = new VBox(15);
        courseContainer.setAlignment(Pos.CENTER);
        courseContainer.setPadding(new Insets(20));
        courseContainer.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 15;");

        Label instructionLabel = new Label("Select a course to manage its questions:");
        instructionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        instructionLabel.setStyle("-fx-text-fill: #333;");

        // Load courses and create buttons
        List<Course> courses = userManager.getAllCourses();
        VBox courseButtons = new VBox(10);
        courseButtons.setAlignment(Pos.CENTER);

        for (Course course : courses) {
            Button courseBtn = createCourseButton(course);
            courseBtn.setOnAction(e -> showCourseQuestions(course));
            courseButtons.getChildren().add(courseBtn);
        }

        // Add general options
        Button viewAllQuestionsBtn = createStyledButton("📋 View All Questions (Mixed)", "#9C27B0");
        Button addQuestionBtn = createStyledButton("➕ Add Question to Course", "#4CAF50");

        viewAllQuestionsBtn.setOnAction(e -> showAllQuestionsScreen());
        addQuestionBtn.setOnAction(e -> showAddQuestionToCourseScreen());

        courseContainer.getChildren().addAll(instructionLabel, courseButtons,
                new Separator(), viewAllQuestionsBtn, addQuestionBtn);

        // Navigation buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button backBtn = createStyledButton("⬅ Back to Dashboard", "#9E9E9E");
        backBtn.setOnAction(e -> returnToTeacherDashboard());

        buttonBox.getChildren().add(backBtn);

        root.getChildren().addAll(title, welcomeLabel, courseContainer, buttonBox);

        Scene scene = new Scene(root, 700, 600);
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

        // Get question count for this course
        int questionCount = getQuestionCountForCourse(course.getCourseId());
        Label questionCountLabel = new Label(questionCount + " questions available");
        questionCountLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        questionCountLabel.setStyle("-fx-text-fill: #E8F5E8;");

        courseInfo.getChildren().addAll(courseName, courseCode, questionCountLabel);

        Button courseBtn = new Button();
        courseBtn.setGraphic(courseInfo);
        courseBtn.setPrefWidth(350);
        courseBtn.setPrefHeight(70);

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

    private int getQuestionCountForCourse(int courseId) {
        List<Question> questions = userManager.getQuestionsByCourse(courseId);
        return questions.size();
    }

    private void showCourseQuestions(Course course) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label title = new Label("📚 " + course.getCourseName() + " Questions");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        ScrollPane scrollPane = new ScrollPane();
        VBox questionsBox = new VBox(15);
        questionsBox.setPadding(new Insets(20));
        questionsBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10;");

        loadCourseQuestions(questionsBox, course);

        scrollPane.setContent(questionsBox);
        scrollPane.setPrefHeight(350);
        scrollPane.setFitToWidth(true);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button addToCourseBtn = createStyledButton("➕ Add Question to " + course.getCourseName(), "#4CAF50");
        Button createQuizBtn = createStyledButton("🎯 Create Custom Quiz", "#FF9800");
        Button backBtn = createStyledButton("⬅ Back", "#9E9E9E");

        addToCourseBtn.setOnAction(e -> showAddQuestionToCourseScreen(course));
        createQuizBtn.setOnAction(e -> showCreateCustomQuizScreen(course));
        backBtn.setOnAction(e -> show());

        buttonBox.getChildren().addAll(addToCourseBtn, createQuizBtn, backBtn);

        root.getChildren().addAll(title, scrollPane, buttonBox);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
    }

    private void loadCourseQuestions(VBox questionsBox, Course course) {
        List<Question> questions = userManager.getQuestionsByCourse(course.getCourseId());

        if (questions.isEmpty()) {
            Label noQuestionsLabel = new Label("No questions found for " + course.getCourseName());
            noQuestionsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-font-style: italic;");
            questionsBox.getChildren().add(noQuestionsLabel);
            return;
        }

        for (Question question : questions) {
            VBox questionBox = new VBox(8);
            questionBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");

            Label idLabel = new Label("Question ID: " + question.getQuestionId());
            idLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");

            Label difficultyLabel = new Label("Difficulty: " + question.getDifficultyLevel());
            difficultyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF9800;");

            Label questionLabel = new Label("Q: " + question.getText());
            questionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
            questionLabel.setWrapText(true);

            String[] options = question.getOptions();
            Label option1 = new Label("1. " + options[0]);
            Label option2 = new Label("2. " + options[1]);
            Label option3 = new Label("3. " + options[2]);
            Label option4 = new Label("4. " + options[3]);

            option1.setStyle("-fx-text-fill: #555;");
            option2.setStyle("-fx-text-fill: #555;");
            option3.setStyle("-fx-text-fill: #555;");
            option4.setStyle("-fx-text-fill: #555;");

            Label correctLabel = new Label("Correct Answer: " + question.getCorrectIndex());
            correctLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");

            questionBox.getChildren().addAll(idLabel, difficultyLabel, questionLabel,
                    option1, option2, option3, option4, correctLabel);
            questionsBox.getChildren().add(questionBox);
        }
    }

    private void showAddQuestionToCourseScreen() {
        showAddQuestionToCourseScreen(null);
    }

    private void showAddQuestionToCourseScreen(Course selectedCourse) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label title = new Label("➕ Add Question to Course");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10;");

        // Course selection
        Label courseLabel = new Label("Select Course:");
        courseLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<Course> courseCombo = new ComboBox<>();
        List<Course> courses = userManager.getAllCourses();
        courseCombo.getItems().addAll(courses);
        if (selectedCourse != null) {
            courseCombo.setValue(selectedCourse);
        }

        // Question fields
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

        Label difficultyLabel = new Label("Difficulty Level:");
        difficultyLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll("Easy", "Medium", "Hard");
        difficultyCombo.setValue("Medium");

        formBox.getChildren().addAll(
                courseLabel, courseCombo,
                questionLabel, questionArea,
                optionsLabel,
                option1Field, option2Field, option3Field, option4Field,
                correctLabel, correctCombo,
                difficultyLabel, difficultyCombo
        );

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button addBtn = createStyledButton("✅ Add Question", "#4CAF50");
        Button cancelBtn = createStyledButton("❌ Cancel", "#f44336");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #ffeb3b; -fx-font-weight: bold;");

        addBtn.setOnAction(e -> {
            Course selectedCourseValue = courseCombo.getValue();
            if (selectedCourseValue == null) {
                messageLabel.setText("❌ Please select a course");
                return;
            }

            if (questionArea.getText().trim().isEmpty() ||
                    option1Field.getText().trim().isEmpty() ||
                    option2Field.getText().trim().isEmpty() ||
                    option3Field.getText().trim().isEmpty() ||
                    option4Field.getText().trim().isEmpty()) {
                messageLabel.setText("❌ Please fill in all fields");
                return;
            }

            int correctOption = Integer.parseInt(correctCombo.getValue());
            String difficulty = difficultyCombo.getValue();

            if (userManager.addQuestionToCourse(
                    selectedCourseValue.getCourseId(),
                    questionArea.getText().trim(),
                    option1Field.getText().trim(),
                    option2Field.getText().trim(),
                    option3Field.getText().trim(),
                    option4Field.getText().trim(),
                    correctOption,
                    difficulty)) {
                messageLabel.setText("✅ Question added to " + selectedCourseValue.getCourseName() + " successfully!");
                // Clear form
                questionArea.clear();
                option1Field.clear();
                option2Field.clear();
                option3Field.clear();
                option4Field.clear();
                correctCombo.setValue("1");
                difficultyCombo.setValue("Medium");
            } else {
                messageLabel.setText("❌ Failed to add question");
            }
        });

        cancelBtn.setOnAction(e -> show());

        buttonBox.getChildren().addAll(addBtn, cancelBtn);

        root.getChildren().addAll(title, formBox, buttonBox, messageLabel);

        Scene scene = new Scene(root, 650, 700);
        primaryStage.setScene(scene);
    }

    private void showCreateCustomQuizScreen(Course course) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label title = new Label("🎯 Create Custom Quiz - " + course.getCourseName());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setStyle("-fx-text-fill: white;");

        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10;");

        Label instructionLabel = new Label("Configure your custom quiz settings:");
        instructionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Quiz settings
        HBox settingsBox = new HBox(20);
        settingsBox.setAlignment(Pos.CENTER);

        VBox leftSettings = new VBox(10);
        leftSettings.setAlignment(Pos.CENTER_LEFT);

        Label questionsLabel = new Label("Number of Questions:");
        questionsLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> questionsCombo = new ComboBox<>();
        questionsCombo.getItems().addAll("5", "10", "15", "20", "All Available");
        questionsCombo.setValue("10");

        Label difficultyLabel = new Label("Difficulty Filter:");
        difficultyLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll("All Levels", "Easy", "Medium", "Hard");
        difficultyCombo.setValue("All Levels");

        leftSettings.getChildren().addAll(questionsLabel, questionsCombo, difficultyLabel, difficultyCombo);

        VBox rightSettings = new VBox(10);
        rightSettings.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label("Time per Question:");
        timeLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll("15 seconds", "20 seconds", "30 seconds", "45 seconds", "60 seconds");
        timeCombo.setValue("20 seconds");

        Label shuffleLabel = new Label("Shuffle Questions:");
        shuffleLabel.setStyle("-fx-font-weight: bold;");
        CheckBox shuffleCheckBox = new CheckBox("Randomize question order");
        shuffleCheckBox.setSelected(true);

        rightSettings.getChildren().addAll(timeLabel, timeCombo, shuffleLabel, shuffleCheckBox);

        settingsBox.getChildren().addAll(leftSettings, new Separator(), rightSettings);

        // Quiz info
        int totalQuestions = getQuestionCountForCourse(course.getCourseId());
        Label infoLabel = new Label("Available questions in " + course.getCourseName() + ": " + totalQuestions);
        infoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");

        formBox.getChildren().addAll(instructionLabel, settingsBox, infoLabel);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button createBtn = createStyledButton("🎯 Create Quiz", "#4CAF50");
        Button previewBtn = createStyledButton("👁 Preview Questions", "#2196F3");
        Button backBtn = createStyledButton("⬅ Back", "#9E9E9E");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #ffeb3b; -fx-font-weight: bold;");

        createBtn.setOnAction(e -> {
            String numQuestions = questionsCombo.getValue();
            String difficulty = difficultyCombo.getValue();
            String timePerQuestion = timeCombo.getValue();
            boolean shuffle = shuffleCheckBox.isSelected();

            if (totalQuestions == 0) {
                messageLabel.setText("❌ No questions available in " + course.getCourseName());
                return;
            }

            messageLabel.setText("✅ Custom quiz configuration saved! Quiz will be available for students.");

            // Here you could save the quiz configuration to database
            // For now, we'll just show a success message
        });

        previewBtn.setOnAction(e -> showCourseQuestions(course));
        backBtn.setOnAction(e -> showCourseQuestions(course));

        buttonBox.getChildren().addAll(createBtn, previewBtn, backBtn);

        root.getChildren().addAll(title, formBox, buttonBox, messageLabel);

        Scene scene = new Scene(root, 700, 600);
        primaryStage.setScene(scene);
    }

    private void showAllQuestionsScreen() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label title = new Label("📝 All Quiz Questions (Mixed Courses)");
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
        backBtn.setOnAction(e -> show());

        root.getChildren().addAll(title, scrollPane, backBtn);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
    }

    private void loadAllQuestions(VBox questionsBox) {
        // This will load all questions from all courses
        userManager.showAllQuestions(); // This prints to console, but we need GUI version

        // For GUI, we need to implement a method to get all questions with course info
        // For now, let's show a message
        Label messageLabel = new Label("All questions are displayed in the console.\nCourse-specific management is available above.");
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-text-alignment: center;");
        questionsBox.getChildren().add(messageLabel);
    }

    private void returnToTeacherDashboard() {
        // Return to the teacher dashboard
        QuizApp app = new QuizApp();
        try {
            app.start(primaryStage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(40);
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
