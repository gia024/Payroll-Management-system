package main;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;

public class PayrollDashboard extends Application {

    @SuppressWarnings("unused")
    private Connection connection;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Payroll Management Dashboard");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        VBox sidebar = new VBox();
        sidebar.setPadding(new Insets(10));
        sidebar.setSpacing(10);
        sidebar.getStyleClass().add("sidebar");

        // Load the image
        Image image = new Image("file:src\\main\\user.jpg"); // Replace with your image path
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(300); // Increase width
        imageView.setFitHeight(300); // Increase height
        imageView.setPreserveRatio(true); // Maintain aspect ratio
        imageView.setSmooth(true); // Smoothing for better quality
        imageView.setCache(true); // Cache the image for performance

        // Apply inline CSS to the ImageView
        imageView.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-padding: 10;");

        // Create a StackPane to center the image
        StackPane imagePane = new StackPane(imageView);
        imagePane.setPadding(new Insets(10, 0, 10, 0)); // Add padding if needed

        // Add the image pane to the sidebar
        sidebar.getChildren().add(imagePane);

        Button btnDashboard = new Button("Dashboard");
        btnDashboard.getStyleClass().add("button");
        Button btnEmployeeManagement = new Button("Employee Management");
        btnEmployeeManagement.getStyleClass().add("button");
        Button btnLeaveManagement = new Button("Leave Management");
        btnLeaveManagement.getStyleClass().add("button");
        Button btnPayslip = new Button("Payslip");
        btnPayslip.getStyleClass().add("button");
        // Button btnRebateManagement = new Button("Rebate");
        // btnRebateManagement.getStyleClass().add("button");
        Button btnAttendanceManagement = new Button("Attendance");
        btnAttendanceManagement.getStyleClass().add("button");
        Button btnLogout = new Button("Logout");
        btnLogout.getStyleClass().add("button");

        sidebar.getChildren().addAll(
                btnDashboard,
                btnEmployeeManagement,
                btnLeaveManagement,
                btnPayslip,
                // btnRebateManagement,
                btnAttendanceManagement,
                btnLogout
        );

        root.setLeft(sidebar);
        root.setCenter(Dashboard.getView());

        btnDashboard.setOnAction(e -> root.setCenter(Dashboard.getView()));
        btnEmployeeManagement.setOnAction(e -> root.setCenter(EmployeeManagement.getView()));
        btnLeaveManagement.setOnAction(e -> root.setCenter(LeaveManagement.getView()));
        btnPayslip.setOnAction(e -> root.setCenter(PaySlipApp.getView()));
        // btnRebateManagement.setOnAction(e -> root.setCenter(RebateManagement.getView()));
        btnAttendanceManagement.setOnAction(e -> root.setCenter(AttendanceManagement.getView()));
        btnLogout.setOnAction(e -> logout(primaryStage));

        Scene scene = new Scene(root, 1000, 600);

        scene.getStylesheets().add(getClass().getResource("payroll.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("dashboard.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("leave.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("pay.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("employee.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("paySlip.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialize database connection
        try {
            connection = DatabaseConnector.getConnection();
        } catch (SQLException ex) {
            System.err.println("Error connecting to database: " + ex.getMessage());
            ex.printStackTrace();
            showAlert("Database Connection Error", "Failed to connect to the database. Please check your connection settings.");
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        // Close the database connection when the application is stopped
        DatabaseConnector.closeConnection();
    }

    private void logout(Stage stage) {
        // Close the current stage to log out
        stage.close();

        // Optionally, you could also open a login window here if you have one
        // For example:
        // new LoginWindow().start(new Stage());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
