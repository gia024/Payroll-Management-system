package main;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.sql.*;

public class LeaveManagement {

    private static TableView<LeaveRequest> leaveRequestsTable;

    @SuppressWarnings({"unchecked", "deprecation"}) 
    public static VBox getView() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);
        vbox.getStyleClass().add("content-area");

        Label titleLabel = new Label("Leave Management Section");
        titleLabel.getStyleClass().add("title");

        leaveRequestsTable = new TableView<>();
        leaveRequestsTable.setPlaceholder(new Label("No leave requests found."));
        leaveRequestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        TableColumn<LeaveRequest, String> stateCol = new TableColumn<>("State");
        stateCol.setCellFactory(createStateCellFactory());

        leaveRequestsTable.getColumns().addAll(firstNameCol, lastNameCol, leaveTypeCol, startDateCol, endDateCol, statusCol,  stateCol);

        vbox.getChildren().addAll(titleLabel, leaveRequestsTable);

        refreshLeaveRequestsTable();

        return vbox;
    }

    private static Callback<TableColumn<LeaveRequest, String>, TableCell<LeaveRequest, String>> createStateCellFactory() {
        return col -> new TableCell<>() {
            private final ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList("Approved", "Rejected"));

            {
                comboBox.setOnAction(e -> updateLeaveRequestStatus(comboBox.getValue()));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    comboBox.setValue(getTableView().getItems().get(getIndex()).getStatus());
                    setGraphic(comboBox);
                }
            }

            private void updateLeaveRequestStatus(String newStatus) {
                LeaveRequest leaveRequest = getTableView().getItems().get(getIndex());
                if (leaveRequest != null) {
                    Platform.runLater(() -> { // Ensure this runs on JavaFX Application Thread
                        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/payrollms", "root", "");
                             PreparedStatement stmt = conn.prepareStatement("UPDATE leave_requests SET status = ? WHERE request_id = ?")) {

                            stmt.setString(1, newStatus);
                            stmt.setInt(2, leaveRequest.getRequestId());

                            stmt.executeUpdate();

                        } catch (SQLException e) {
                            e.printStackTrace();
                            showAlert("Error occurred while updating leave request status.");
                        }
                    });
                } else {
                    showAlert("Please select a leave request to update.");
                }
            }
        };
    }


    private static void refreshLeaveRequestsTable() {
        ObservableList<LeaveRequest> leaveRequests = FXCollections.observableArrayList();

        Platform.runLater(() -> { // Ensure this runs on JavaFX Application Thread
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/payrollms", "root", "");
                 PreparedStatement stmt = conn.prepareStatement("SELECT request_id, first_name, last_name, leave_type, start_date, end_date, status, comments FROM leave_requests")) {

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int requestId = rs.getInt("request_id");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String leaveType = rs.getString("leave_type");
                    String startDate = rs.getString("start_date");
                    String endDate = rs.getString("end_date");
                    String status = rs.getString("status");
                    String comments = rs.getString("comments");

                    leaveRequests.add(new LeaveRequest(requestId, firstName, lastName, leaveType, startDate, endDate, status, comments));
                }

                leaveRequestsTable.setItems(leaveRequests);

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error occurred while fetching leave requests.");
            }
        });
    }

    private static void showAlert(String message) {
        Platform.runLater(() -> { // Ensure this runs on JavaFX Application Thread
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Leave Management");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static class LeaveRequest {
        private final SimpleIntegerProperty requestId;
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty leaveType;
        private final SimpleStringProperty startDate;
        private final SimpleStringProperty endDate;
        private final SimpleStringProperty status;
        private final SimpleStringProperty comments;

        public LeaveRequest(int requestId, String firstName, String lastName, String leaveType, String startDate, String endDate, String status, String comments) {
            this.requestId = new SimpleIntegerProperty(requestId);
            this.firstName = new SimpleStringProperty(firstName);
            this.lastName = new SimpleStringProperty(lastName);
            this.leaveType = new SimpleStringProperty(leaveType);
            this.startDate = new SimpleStringProperty(startDate);
            this.endDate = new SimpleStringProperty(endDate);
            this.status = new SimpleStringProperty(status);
            this.comments = new SimpleStringProperty(comments);
        }

        public int getRequestId() {
            return requestId.get();
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

        public String getComments() {
            return comments.get();
        }

        public void setComments(String comments) {
            this.comments.set(comments);
        }
    }
}
