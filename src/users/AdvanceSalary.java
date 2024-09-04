package users;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import main.DatabaseConnector;

public class AdvanceSalary extends VBox {

    public Node getView() {
        setPadding(new Insets(10));
        setSpacing(10);

        Label titleLabel = new Label("Advance Salary");
        titleLabel.getStyleClass().add("title");

        // Input fields for amount, date, and reason
        TextField amountField = new TextField();
        amountField.setPromptText("Enter Amount");

        TextField dateField = new TextField();
        dateField.setPromptText("Enter Date (YYYY-MM-DD)");

        TextField reasonField = new TextField();
        reasonField.setPromptText("Enter Reason");

        // Submit button
        Button btnSubmit = new Button("Submit");
        btnSubmit.getStyleClass().add("button");

        // Handle submission action
        btnSubmit.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String dateStr = dateField.getText();
                String reason = reasonField.getText();

                if (!isValidDate(dateStr)) {
                    showAlert(AlertType.ERROR, "Invalid Date", "Please enter a valid date in YYYY-MM-DD format.");
                    return;
                }

                LocalDate date = LocalDate.parse(dateStr);

                // Get logged-in user's email (you need to fetch this from your authentication mechanism)
                String loggedInEmail = "ira@gmail.com"; // Replace with actual logged-in user's email

                // Insert into the database
                insertAdvanceSalary(loggedInEmail, amount, date, reason);

                showAlert(AlertType.INFORMATION, "Success", "Advance salary request submitted successfully.");
                // Optionally clear the fields after submission
                amountField.clear();
                dateField.clear();
                reasonField.clear();

            } catch (NumberFormatException ex) {
                showAlert(AlertType.ERROR, "Invalid Input", "Please enter valid amount.");
            } catch (SQLException ex) {
                showAlert(AlertType.ERROR, "Database Error", "Failed to submit advance salary request. Please try again.");
                ex.printStackTrace();
            }
        });

        // Add components to the VBox
        getChildren().addAll(
                titleLabel,
                new Label("Amount:"),
                amountField,
                new Label("Date:"),
                dateField,
                new Label("Reason:"),
                reasonField,
                btnSubmit
        );

        return this;
    }

    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void insertAdvanceSalary(String loggedInEmail, double amount, LocalDate date, String reason) throws SQLException {
        // Fetch user details based on logged-in email
        String fetchUserQuery = "SELECT id, first_name, last_name FROM users WHERE email = ?";
        int employeeId = 0;
        String firstName = null;
        String lastName = null;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmtFetch = conn.prepareStatement(fetchUserQuery)) {
            pstmtFetch.setString(1, loggedInEmail);
            ResultSet rs = pstmtFetch.executeQuery();
            
            if (rs.next()) {
                employeeId = rs.getInt("id");
                firstName = rs.getString("first_name");
                lastName = rs.getString("last_name");
            }
        }

        // Insert into the database
        String sql = "INSERT INTO advance_salary (employee_id, first_name, last_name, amount, date, reason) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setDouble(4, amount);
            pstmt.setDate(5, java.sql.Date.valueOf(date));
            pstmt.setString(6, reason);
            pstmt.executeUpdate();
        }
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
