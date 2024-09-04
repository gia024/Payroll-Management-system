package main;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignupWindow {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/payrollms";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public void showSignupWindow() {
        Stage signupStage = new Stage();
        signupStage.setTitle("Employee Signup");

        // Create root pane using GridPane for signup window
        GridPane signupRoot = new GridPane();
        signupRoot.setPrefSize(600, 400);
        signupRoot.setHgap(10); // Horizontal gap between columns
        signupRoot.setVgap(10); // Vertical gap between rows
        signupRoot.setPadding(new Insets(20)); // Padding around the edges

        // First Name label and text field
        Label firstNameLabel = new Label("First Name");
        firstNameLabel.setFont(new Font(14));
        GridPane.setConstraints(firstNameLabel, 0, 0); // Place label in column 0, row 0

        TextField firstNameField = new TextField();
        firstNameField.setPrefSize(300, 31);
        GridPane.setConstraints(firstNameField, 1, 0); // Place text field in column 1, row 0

        // Last Name label and text field
        Label lastNameLabel = new Label("Last Name");
        lastNameLabel.setFont(new Font(14));
        GridPane.setConstraints(lastNameLabel, 0, 1); // Place label in column 0, row 1

        TextField lastNameField = new TextField();
        lastNameField.setPrefSize(300, 31);
        GridPane.setConstraints(lastNameField, 1, 1); // Place text field in column 1, row 1

        // Email Address label and text field
        Label emailLabel = new Label("Email Address");
        emailLabel.setFont(new Font(14));
        GridPane.setConstraints(emailLabel, 0, 2); // Place label in column 0, row 2

        TextField emailField = new TextField();
        emailField.setPrefSize(300, 31);
        GridPane.setConstraints(emailField, 1, 2); // Place text field in column 1, row 2

        // Password label and password field
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(new Font(14));
        GridPane.setConstraints(passwordLabel, 0, 3); // Place label in column 0, row 3

        PasswordField passwordField = new PasswordField();
        passwordField.setPrefSize(300, 31);
        GridPane.setConstraints(passwordField, 1, 3); // Place password field in column 1, row 3

        // Repeat Password label and password field
        Label repeatPasswordLabel = new Label("Repeat Password");
        repeatPasswordLabel.setFont(new Font(14));
        GridPane.setConstraints(repeatPasswordLabel, 0, 4); // Place label in column 0, row 4

        PasswordField repeatPasswordField = new PasswordField();
        repeatPasswordField.setPrefSize(300, 31);
        GridPane.setConstraints(repeatPasswordField, 1, 4); // Place password field in column 1, row 4

        // Sign up button
        Button signupConfirmButton = new Button("Sign up");
        signupConfirmButton.setTextFill(Color.WHITE);
        signupConfirmButton.setStyle("-fx-background-color: black;");
        signupConfirmButton.setPrefSize(100, 30);
        GridPane.setConstraints(signupConfirmButton, 1, 5); // Place button in column 1, row 5

        // Add all elements to the signup root pane
        signupRoot.getChildren().addAll(
                firstNameLabel,
                firstNameField,
                lastNameLabel,
                lastNameField,
                emailLabel,
                emailField,
                passwordLabel,
                passwordField,
                repeatPasswordLabel,
                repeatPasswordField,
                signupConfirmButton
        );

        // Event handler for signup confirmation
        signupConfirmButton.setOnAction(event -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String repeatPassword = repeatPasswordField.getText();

            if (!password.equals(repeatPassword)) {
                // Handle password mismatch
                showAlert(Alert.AlertType.ERROR, "Signup Error", "Passwords do not match!");
                return;
            }

            try {
                // Connect to the database
                Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                // Prepare statement to insert user
                String insertUserQuery = "INSERT INTO users (first_name, last_name, email, password, role) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertUserQuery);
                pstmt.setString(1, firstName);
                pstmt.setString(2, lastName);
                pstmt.setString(3, email);
                pstmt.setString(4, password);
                pstmt.setString(5, "employee"); // Assigning employee role for signup

                // Execute insert query
                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Signup Successful", "Employee signed up successfully!");
                    signupStage.close(); // Close signup window
                } else {
                    showAlert(Alert.AlertType.ERROR, "Signup Error", "Failed to sign up employee!");
                }

                // Clean up
                pstmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        // Create scene and set on signup stage
        Scene signupScene = new Scene(signupRoot);
        signupStage.setScene(signupScene);
        signupStage.show();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

