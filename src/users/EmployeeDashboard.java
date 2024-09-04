package users;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.DatabaseConnector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeDashboard extends Application {

    private final String loggedInUsername;
    private String firstName;
    private String lastName;
    private Image employeeImage;

    private ImageView imageView; // Declare ImageView here to access globally
    private DashboardView dashboardView; // DashboardView instance
    private MyAccount myAccountView; // MyAccount instance

    public EmployeeDashboard(String username) {
        this.loggedInUsername = username;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Employee Dashboard");

        // Fetch user details including first name, last name, and image URL
        fetchUserDetails();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        VBox sidebar = new VBox();
        sidebar.setPadding(new Insets(10));
        sidebar.setSpacing(10);
        sidebar.getStyleClass().add("sidebar");

        // Create square frame for employee image
        imageView = new ImageView(employeeImage);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true); // Maintain image aspect ratio
        imageView.getStyleClass().add("profile-image");

        HBox profileBox = new HBox(imageView);
        profileBox.getStyleClass().add("profile-box");

        // My Account section
        VBox myAccountBox = new VBox();
        myAccountBox.setSpacing(10);

        Button btnUploadImage = new Button("Upload Image");
        btnUploadImage.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Image File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(primaryStage); // Pass primaryStage here
            if (selectedFile != null) {
                try {
                    uploadImage(selectedFile);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        });

        myAccountBox.getChildren().addAll(
                new Label("Welcome, " + firstName + " " + lastName),
                btnUploadImage
        );

        sidebar.getChildren().addAll(profileBox, myAccountBox);

        Button btnAcc = new Button("My Account");
        btnAcc.getStyleClass().add("button");
        Button btnDashboard = new Button("Overview");
        btnDashboard.getStyleClass().add("button");
       
        Button btnAttendance = new Button("Attendance");
        btnAttendance.getStyleClass().add("button");
        Button btnLeaveButton = new Button("Leave");
        btnLeaveButton.getStyleClass().add("button");

        Button btnPaySlip = new Button("Pay Slip");
        btnPaySlip.getStyleClass().add("button");

        // Logout button
        Button btnLogout = new Button("Logout");
        btnLogout.getStyleClass().addAll("button", "button-logout");
        btnLogout.setOnAction(e -> handleLogout(primaryStage));

        sidebar.getChildren().addAll(
                btnDashboard,
                btnAcc,
                btnAttendance,
                btnLeaveButton,
                btnPaySlip,
                btnLogout // Add logout button to the sidebar
        );

        // Initialize DashboardView, MyAccount and other views
        dashboardView = new DashboardView(firstName, lastName); // Initialize DashboardView
        myAccountView = new MyAccount(loggedInUsername); // Initialize MyAccount
        AttendanceView attendanceView = new AttendanceView(loggedInUsername, 0);
        EmployeePayslip paySlipView = new EmployeePayslip(firstName, lastName);
        Leave leaveView = new Leave(loggedInUsername);


        root.setLeft(sidebar);
        root.setCenter(dashboardView.getView()); // Initially show Dashboard view

        btnDashboard.setOnAction(e -> root.setCenter(dashboardView.getView()));
        btnAcc.setOnAction(e -> root.setCenter(myAccountView.getView())); // Set MyAccount view in the center
        btnAttendance.setOnAction(e -> root.setCenter(attendanceView.getView()));
        btnLeaveButton.setOnAction(e -> root.setCenter(leaveView.getView())); // Set Leave view in the center
        btnPaySlip.setOnAction(e -> root.setCenter(paySlipView.getView()));

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("employeedash.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("leave.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("myaccount.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("attendance.css").toExternalForm());


        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        DatabaseConnector.closeConnection();
    }

    private void fetchUserDetails() {
        // Fetch first name, last name, and image URL based on loggedInUsername from the database
        String query = "SELECT first_name, last_name, image_url FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, loggedInUsername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                firstName = rs.getString("first_name");
                lastName = rs.getString("last_name");
                String imageUrl = rs.getString("image_url");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    employeeImage = new Image(imageUrl); // Load employee image from URL
                } else {
                    // Set default image if no image URL is provided
                    employeeImage = new Image(getClass().getResourceAsStream("/users/default.jpg"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void uploadImage(File imageFile) throws FileNotFoundException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String updateQuery = "UPDATE users SET image_url = ? WHERE email = ?";
            PreparedStatement pstmt = conn.prepareStatement(updateQuery);
            pstmt.setString(1, imageFile.toURI().toString());
            pstmt.setString(2, loggedInUsername);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                // Update employeeImage and refresh UI
                employeeImage = new Image(new FileInputStream(imageFile));
                imageView.setImage(employeeImage); // Update the ImageView directly
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleLogout(Stage primaryStage) {
        // Add logic here to handle logout, e.g., return to login screen or close application
        primaryStage.close(); // Closing the application for simplicity
    }

    public static void main(String[] args) {
        launch(args);
    }
}