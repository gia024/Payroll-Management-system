package users;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import main.DatabaseConnector;

import java.sql.*;
import java.time.LocalDate;

public class Leave {

    private String loggedInFirstName;
    private String loggedInLastName;
    private String loggedInEmail;
    private TableView<LeaveRequest> leaveHistoryTable;
    private VBox leaveInfoBox;

    public Leave(String loggedInEmail) {
        this.loggedInEmail = loggedInEmail;
        fetchUserDetails();

        leaveHistoryTable = new TableView<>();
        leaveHistoryTable.setPadding(new Insets(10));
        leaveHistoryTable.setMinSize(600, 400);

        refreshLeaveHistoryTable();
    }

    public Node getView() {
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));

        GridPane gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(10);

        Image image = new Image(getClass().getResourceAsStream("/users/leave123.jpg"));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(400);
        imageView.setFitHeight(400);

        gridPane.add(imageView, 1, 0);
        leaveInfoBox = createLeaveInfoBox();
        gridPane.add(leaveInfoBox, 0, 0);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle(
        "-fx-background-color: transparent; " +  // Make TabPane background transparent
        "-fx-padding: 0; " +                     // Remove padding
        "-fx-border-color: transparent; " +     // Remove border
        "-fx-border-width: 0; "                 // Remove border width
    );

        Tab leaveTab = new Tab("Leave Management");
        leaveTab.setContent(gridPane);

        Tab historyTab = new Tab("Leave History");
        VBox leaveHistoryBox = createLeaveHistoryBox();
        historyTab.setContent(leaveHistoryBox);

        tabPane.getTabs().addAll(leaveTab, historyTab);

        borderPane.setCenter(tabPane);

        return borderPane;
    }

    private VBox createLeaveInfoBox() {
        VBox leaveInfoBox = new VBox();
        leaveInfoBox.setSpacing(10);

        Label titleLabel = new Label("Leave Management");
        titleLabel.setId("titleLabel");

        ComboBox<String> leaveTypeComboBox = new ComboBox<>();
        leaveTypeComboBox.setItems(FXCollections.observableArrayList(
                "Vacation Leave", "Sick Leave", "Maternity Leave", "Paternity Leave",
                "Parental Leave", "Funeral Leave", "Compassionate Leave", "Study Leave"
        ));
        leaveTypeComboBox.setPromptText("Select Leave Type");

        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String leaveType = leaveTypeComboBox.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (leaveType == null) {
                showAlert("Please select a leave type.");
            } else if (startDate == null || endDate == null) {
                showAlert("Please select both start and end dates.");
            } else if (endDate.isBefore(startDate)) {
                showAlert("End date cannot be before start date.");
            } else {
                requestLeave(leaveType, startDate.toString(), endDate.toString());
            }
        });

        leaveInfoBox.getChildren().addAll(
                titleLabel,
                leaveTypeComboBox,
                new Label("Start Date:"), startDatePicker,
                new Label("End Date:"), endDatePicker,
                submitButton
        );

        return leaveInfoBox;
    }

    @SuppressWarnings("unchecked")
    private VBox createLeaveHistoryBox() {
        VBox leaveHistoryBox = new VBox();
        leaveHistoryBox.setSpacing(10);

        leaveHistoryTable.setPlaceholder(new Label("No leave requests found."));

        TableColumn<LeaveRequest, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<LeaveRequest, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<LeaveRequest, String> leaveTypeCol = new TableColumn<>("Leave Type");
        leaveTypeCol.setCellValueFactory(new PropertyValueFactory<>("leaveType"));

        TableColumn<LeaveRequest, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        TableColumn<LeaveRequest, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        TableColumn<LeaveRequest, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        leaveHistoryTable.getColumns().addAll(firstNameCol, lastNameCol, leaveTypeCol, startDateCol, endDateCol, statusCol);

        leaveHistoryBox.getChildren().addAll(
                new Separator(),
                new Label("Leave Request History"),
                leaveHistoryTable
        );

        return leaveHistoryBox;
    }
    private void requestLeave(String leaveType, String startDate, String endDate) {
        String query = "INSERT INTO leave_requests (leave_type, start_date, end_date, status, request_date, first_name, last_name, username) VALUES (?, ?, ?, 'Pending', CURDATE(), ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement leaveStmt = conn.prepareStatement(query)) {
            leaveStmt.setString(1, leaveType);
            leaveStmt.setString(2, startDate);
            leaveStmt.setString(3, endDate);
            leaveStmt.setString(4, loggedInFirstName);
            leaveStmt.setString(5, loggedInLastName);
            leaveStmt.setString(6, loggedInEmail);

            int rowsAffected = leaveStmt.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Leave request submitted successfully.");
                refreshLeaveHistoryTable();
            } else {
                showAlert("Failed to submit leave request.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error occurred while submitting leave request.");
        }
    }

    private void refreshLeaveHistoryTable() {
        ObservableList<LeaveRequest> leaveRequests = FXCollections.observableArrayList();

        String query = "SELECT first_name, last_name, leave_type, start_date, end_date, status " +
                       "FROM leave_requests " +
                       "WHERE first_name = ? AND last_name = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, loggedInFirstName);
            stmt.setString(2, loggedInLastName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LeaveRequest request = new LeaveRequest(
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("leave_type"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getString("status")
                );
                leaveRequests.add(request);
            }

            leaveHistoryTable.setItems(leaveRequests);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error occurred while fetching leave request history.");
        }
    }

    private void fetchUserDetails() {
        String query = "SELECT e.first_name, e.last_name " +
                       "FROM users u " +
                       "JOIN employees e ON u.email = e.email " +
                       "WHERE u.email = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, loggedInEmail);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                loggedInFirstName = rs.getString("first_name");
                loggedInLastName = rs.getString("last_name");
            } else {
                showAlert("Error: User details not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error occurred while fetching user details.");
        }
    }

    private void showAlert(String message) {
        Stage alertStage = new Stage();
        alertStage.setTitle("Leave Request");
        VBox alertBox = new VBox();
        alertBox.setPadding(new Insets(10));
        alertBox.setSpacing(10);
        Label alertMessage = new Label(message);
        Button okButton = new Button("OK");
        okButton.setOnAction(e -> alertStage.close());
        alertBox.getChildren().addAll(alertMessage, okButton);
        Scene alertScene = new Scene(alertBox, 300, 100);
        alertStage.setScene(alertScene);
        alertStage.show();
    }

    public static class LeaveRequest {
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty leaveType;
        private final SimpleStringProperty startDate;
        private final SimpleStringProperty endDate;
        private final SimpleStringProperty status;

        public LeaveRequest(String firstName, String lastName, String leaveType, String startDate, String endDate, String status) {
            this.firstName = new SimpleStringProperty(firstName);
            this.lastName = new SimpleStringProperty(lastName);
            this.leaveType = new SimpleStringProperty(leaveType);
            this.startDate = new SimpleStringProperty(startDate);
            this.endDate = new SimpleStringProperty(endDate);
            this.status = new SimpleStringProperty(status);
        }

        public String getFirstName() {
            return firstName.get();
        }

        public String getLastName() {
            return lastName.get();
        }

        public String getLeaveType() {
            return leaveType.get();
        }

        public String getStartDate() {
            return startDate.get();
        }

        public String getEndDate() {
            return endDate.get();
        }

        public String getStatus() {
            return status.get();
        }
    }
}
