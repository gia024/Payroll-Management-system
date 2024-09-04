package users;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import main.DatabaseConnector;

public class MyAccount {
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField emailField;
    private TextField phoneField;
    private TextField addressField;
    private ImageView profileImageView;
    private File profileImageFile;
    private PasswordField passwordField;
    private String loggedInUsername;

    public MyAccount(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
    }

    public GridPane getView() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setHgap(20); // Increased spacing between columns
        gridPane.setVgap(20); // Increased spacing between rows
        gridPane.getStyleClass().add("grid-pane"); // Add this line
        // gridPane.setGridLinesVisible(true);

        // Title label
        Label titleLabel = new Label("My Account");
        titleLabel.getStyleClass().add("title-label");
        titleLabel.setFont(new Font(20));
        gridPane.add(titleLabel, 0, 0, 2, 1); // Span across 2 columns

        // Profile Image
        profileImageView = new ImageView();
        profileImageView.setFitWidth(100);
        profileImageView.setFitHeight(100);
        profileImageView.setPreserveRatio(true);
        profileImageView.setSmooth(true);
        profileImageView.setOnMouseClicked(event -> selectProfileImage());
        gridPane.add(profileImageView, 2, 1, 1, 6); // Span across multiple rows

        // First Name
        Label firstNameLabel = new Label("First Name:");
        firstNameLabel.setFont(new Font(14));
        firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        gridPane.add(firstNameLabel, 0, 1);
        gridPane.add(firstNameField, 1, 1);
        GridPane.setHalignment(firstNameLabel, HPos.LEFT);
        GridPane.setHalignment(firstNameField, HPos.LEFT);

        // Last Name
        Label lastNameLabel = new Label("Last Name:");
        lastNameLabel.setFont(new Font(14));
        lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        gridPane.add(lastNameLabel, 0, 2);
        gridPane.add(lastNameField, 1, 2);
        GridPane.setHalignment(lastNameLabel, HPos.LEFT);
        GridPane.setHalignment(lastNameField, HPos.LEFT);

        // Email
        Label emailLabel = new Label("Email:");
        emailLabel.setFont(new Font(14));
        emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setEditable(false); // Assuming email cannot be edited
        gridPane.add(emailLabel, 0, 3);
        gridPane.add(emailField, 1, 3);
        GridPane.setHalignment(emailLabel, HPos.LEFT);
        GridPane.setHalignment(emailField, HPos.LEFT);

        // Phone
        Label phoneLabel = new Label("Phone:");
        phoneLabel.setFont(new Font(14));
        phoneField = new TextField();
        phoneField.setPromptText("Phone");
        gridPane.add(phoneLabel, 0, 4);
        gridPane.add(phoneField, 1, 4);
        GridPane.setHalignment(phoneLabel, HPos.LEFT);
        GridPane.setHalignment(phoneField, HPos.LEFT);

        // Address
        Label addressLabel = new Label("Address:");
        addressLabel.setFont(new Font(14));
        addressField = new TextField();
        addressField.setPromptText("Address");
        gridPane.add(addressLabel, 0, 5);
        gridPane.add(addressField, 1, 5);
        GridPane.setHalignment(addressLabel, HPos.LEFT);
        GridPane.setHalignment(addressField, HPos.LEFT);

        // Password
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(new Font(14));
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        gridPane.add(passwordLabel, 0, 6);
        gridPane.add(passwordField, 1, 6);
        GridPane.setHalignment(passwordLabel, HPos.LEFT);
        GridPane.setHalignment(passwordField, HPos.LEFT);

        // Buttons
        Button editButton = new Button("Edit Profile");
        editButton.setOnAction(event -> enableEditing());

        Button updateButton = new Button("Update Profile");
        updateButton.setOnAction(event -> updateEmployeeDetails());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(editButton, updateButton);
        gridPane.add(buttonBox, 0, 7, 2, 1); // Span across 2 columns

        // Side Image
        ImageView sideImageView = new ImageView();
        sideImageView.setFitWidth(400);
        sideImageView.setFitHeight(400);
        sideImageView.setPreserveRatio(true);
        try {
                       sideImageView.setImage(new Image(new FileInputStream("src\\users\\myaccount.jpg")));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        gridPane.add(sideImageView, 2, 1, 1, 7); // Span across multiple rows

        loadEmployeeDetails();

        return gridPane;
    }

    private void loadEmployeeDetails() {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connection = DatabaseConnector.getConnection();
            String query = "SELECT * FROM employees WHERE email = ?";
            stmt = connection.prepareStatement(query);
            stmt.setString(1, loggedInUsername);
            rs = stmt.executeQuery();
            if (rs.next()) {
                firstNameField.setText(rs.getString("first_name"));
                lastNameField.setText(rs.getString("last_name"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone"));
                addressField.setText(rs.getString("address"));
                String imageUrl = rs.getString("image_url");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    profileImageView.setImage(new Image(new FileInputStream(imageUrl)));
                }
            }
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeResources(connection, stmt, rs);
        }
    }

    private void enableEditing() {
        firstNameField.setEditable(true);
        lastNameField.setEditable(true);
        phoneField.setEditable(true);
        addressField.setEditable(true);
        passwordField.setEditable(true);
    }

    private void updateEmployeeDetails() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String phone = phoneField.getText();
        String address = addressField.getText();
        String password = passwordField.getText();
        String imageUrl = (profileImageFile != null) ? profileImageFile.getAbsolutePath() : null;

        if (!firstName.isEmpty() && !lastName.isEmpty() && !phone.isEmpty() && !address.isEmpty()) {
            Connection connection = null;
            PreparedStatement empStmt = null;
            PreparedStatement userStmt = null;
            try {
                connection = DatabaseConnector.getConnection();
                // Update employees table
                String empQuery = "UPDATE employees SET first_name = ?, last_name = ?, phone = ?, address = ?, image_url = ? WHERE email = ?";
                empStmt = connection.prepareStatement(empQuery);
                empStmt.setString(1, firstName);
                empStmt.setString(2, lastName);
                empStmt.setString(3, phone);
                empStmt.setString(4, address);
                empStmt.setString(5, imageUrl);
                empStmt.setString(6, loggedInUsername);
                empStmt.executeUpdate();

                // Update users table (assuming 'password' is in the users table)
                String userQuery = "UPDATE users SET password = ? WHERE email = ?";
                userStmt = connection.prepareStatement(userQuery);
                userStmt.setString(1, password);
                userStmt.setString(2, loggedInUsername);
                userStmt.executeUpdate();

                showSuccessAlert("Profile Updated Successfully!");

                // Reset editable fields after update
                firstNameField.setEditable(false);
                lastNameField.setEditable(false);
                phoneField.setEditable(false);
                addressField.setEditable(false);
                passwordField.setEditable(false);

            } catch (SQLException e) {
                e.printStackTrace();
                showErrorAlert("Failed to update profile!");
            } finally {
                closeResources(connection, empStmt, null);
                closeResources(null, userStmt, null);
            }
        } else {
            showErrorAlert("Please fill in all required fields.");
        }
    }

    private void selectProfileImage() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            profileImageFile = selectedFile;
            profileImageView.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeResources(Connection connection, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

