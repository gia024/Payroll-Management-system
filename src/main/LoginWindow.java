package main;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import users.EmployeeDashboard;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginWindow extends Application {

    public static final String DB_URL = "jdbc:mysql://localhost:3306/payrollms";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "";

    private TextField usernameField;
    private PasswordField passwordField;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");

        // Create root pane using GridPane for better layout control
        GridPane root = new GridPane();
        root.setPrefSize(800, 450);
        root.setHgap(20); // Horizontal gap between columns
        root.setVgap(20); // Vertical gap between rows
        root.setPadding(new Insets(20)); // Padding around the edges

        // Load the image
        Image image = new Image(getClass().getResourceAsStream("/main/logo.jpg"));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(400);
        imageView.setFitHeight(400);
        GridPane.setConstraints(imageView, 0, 0, 1, 7); //image in columns 0 (spanning 1 row and 7 columns)

        // Welcome label
        Label welcomeLabel = new Label("Welcome!");
        welcomeLabel.setFont(new Font(24));
        welcomeLabel.setTextFill(Color.web("#007bff"));
        GridPane.setConstraints(welcomeLabel, 1, 0); // label in column 1, row 0

        // Email Address label and text field
        Label emailLabel = new Label("Email Address");
        emailLabel.setFont(new Font(14));
        GridPane.setConstraints(emailLabel, 1, 1); // label in column 1, row 1

        usernameField = new TextField();
        usernameField.setPrefSize(300, 31);
        GridPane.setConstraints(usernameField, 1, 2); // text field in column 1, row 2

        // Password label and password field
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(new Font(14));
        GridPane.setConstraints(passwordLabel, 1, 3); // label in column 1, row 3

        passwordField = new PasswordField();
        passwordField.setPrefSize(300, 31);
        GridPane.setConstraints(passwordField, 1, 4); // password field in column 1, row 4

        // Sign in button
        Button loginButton = new Button("Sign in");
        loginButton.setTextFill(Color.WHITE);
        loginButton.setStyle("-fx-background-color: black;");
        loginButton.setPrefSize(100, 30);
        GridPane.setConstraints(loginButton, 1, 5); // Place button in column 1, row 5

        // Signup button
        Button signupButton = new Button("Are you an employee? Sign up");
        signupButton.setTextFill(Color.WHITE);
        signupButton.setStyle("-fx-background-color: grey;");
        signupButton.setPrefSize(200, 30);
        GridPane.setConstraints(signupButton, 1, 7); // Place button in column 1, row 7

        // Add all elements to the root pane
        root.getChildren().addAll(
                imageView,
                welcomeLabel,
                emailLabel,
                usernameField,
                passwordLabel,
                passwordField,
                loginButton,
                signupButton
        );

        // Event handler for signup button
        signupButton.setOnAction(event -> {
            SignupWindow signupWindow = new SignupWindow();
            signupWindow.showSignupWindow();
        });

        // Event handler for login button
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            try {
                // Connect to the database
                Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                // Query to check user credentials and role
                String query = "SELECT role FROM users WHERE email = ? AND password = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username);
                pstmt.setString(2, password);

                // Execute query
                ResultSet rs = pstmt.executeQuery();

                // Inside loginButton.setOnAction(...)
if (rs.next()) {
    String role = rs.getString("role");
    Session.setLoggedInUsername(username);
    Session.setRole(role);
    if ("admin".equalsIgnoreCase(role)) {
        openAdminDashboard(primaryStage);
    } else if ("employee".equalsIgnoreCase(role)) {
        openEmployeeDashboard(primaryStage, username);
    } else {
        showAlert(Alert.AlertType.ERROR, "Login Error", "Unknown role: " + role);
    }
}


                // Clean up
                rs.close();
                pstmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        // Create scene and set on primary stage
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/main/login.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openAdminDashboard(Stage primaryStage) {
        primaryStage.close(); // Close login window

        // Open the PayrollDashboard as admin
        PayrollDashboard payrollDashboard = new PayrollDashboard();
        payrollDashboard.start(new Stage());
    }

    private void openEmployeeDashboard(Stage primaryStage, String username) {
        primaryStage.close(); // Close login window

        EmployeeDashboard employeeDashboard = new EmployeeDashboard(username);
        employeeDashboard.start(new Stage());
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}