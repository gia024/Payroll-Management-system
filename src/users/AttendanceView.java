package users;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AttendanceView {

    private static boolean isClockedIn = false;
    private static LocalDateTime clockInTime;
    private static LocalDateTime clockOutTime;

    private static Label statusLabel;
    private static Label clockInLabel;
    private static Label clockOutLabel;
    private static Label workingHoursLabel;

    private static String loggedInUsername;
    private static int companyId;

    public AttendanceView(String loggedInUsername, int companyId) {
        AttendanceView.loggedInUsername = loggedInUsername;
        AttendanceView.companyId = companyId;
    }

    public BorderPane getView() {
        BorderPane root = new BorderPane();

        TabPane tabPane = new TabPane();
        tabPane.setStyle(
        "-fx-background-color: transparent; " +  // Make TabPane background transparent
        "-fx-padding: 0; " +                     // Remove padding
        "-fx-border-color: transparent; " +     // Remove border
        "-fx-border-width: 0; "                 // Remove border width
    );

        Tab attendanceTab = new Tab("Attendance");
        attendanceTab.setContent(createAttendanceContent());

        Tab reportTab = new Tab("Attendance Report");
        reportTab.setContent(createReportContent());

        tabPane.getTabs().addAll(attendanceTab, reportTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        root.setCenter(tabPane);

        return root;
    }

    private VBox createAttendanceContent() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(20));
        vbox.setSpacing(20);
    
    
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
    
        Button clockInButton = new Button("Clock In");
        setButtonStyle(clockInButton);
    
        Button clockOutButton = new Button("Clock Out");
        setButtonStyle(clockOutButton);
    
        Button closeButton = new Button("Close");
        setButtonStyle(closeButton);
    
        buttonBox.getChildren().addAll(clockInButton, clockOutButton, closeButton);
    
        statusLabel = new Label("Status: Not Clocked In");
        setLabelStyle(statusLabel);
    
        clockInLabel = new Label("Clock In Time: ");
        setLabelStyle(clockInLabel);
    
        clockOutLabel = new Label("Clock Out Time: ");
        setLabelStyle(clockOutLabel);
    
        workingHoursLabel = new Label("Working Hours: ");
        setLabelStyle(workingHoursLabel);
    
        if (isClockedIn) {
            statusLabel.setText("Status: Clocked In at " + formatDateTime(clockInTime));
            clockInLabel.setText("Clock In Time: " + formatDateTime(clockInTime));
        }
    
        clockInButton.setOnAction(e -> clockIn());
        clockOutButton.setOnAction(e -> clockOut());
        closeButton.setOnAction(e -> close());
    
        // Create VBox for attendance content
        VBox attendanceBox = new VBox();
        attendanceBox.setSpacing(10);
        attendanceBox.getChildren().addAll( statusLabel, buttonBox, clockInLabel, clockOutLabel, workingHoursLabel);
    
        // Add the image to the ImageView
        Image image = new Image(getClass().getResourceAsStream("/users/attendance.jpg"));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(400);
        imageView.setFitHeight(400);
    
        // Create a GridPane for layout
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setHgap(20);
        gridPane.setVgap(20);
    
        // Add attendance content VBox to the GridPane (left side)
        gridPane.add(attendanceBox, 0, 0);
    
        // Add ImageView to the GridPane (right side)
        gridPane.add(imageView, 1, 0);
    
        // Add GridPane to the main VBox
        vbox.getChildren().add(gridPane);
    
        return vbox;
    }
    


    @SuppressWarnings("unchecked")
    private static VBox createReportContent() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(20));
        vbox.setSpacing(20);

        Label titleLabel = new Label("Attendance Report");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TableView<AttendanceRecord> tableView = new TableView<>();
        tableView.setPrefWidth(600);

        TableColumn<AttendanceRecord, LocalDateTime> eventTimeCol = new TableColumn<>("Event Time");
        eventTimeCol.setCellValueFactory(new PropertyValueFactory<>("eventTime"));

        TableColumn<AttendanceRecord, String> eventTypeCol = new TableColumn<>("Event Type");
        eventTypeCol.setCellValueFactory(new PropertyValueFactory<>("eventType"));

        TableColumn<AttendanceRecord, String> workingHoursCol = new TableColumn<>("Working Hours");
        workingHoursCol.setCellValueFactory(new PropertyValueFactory<>("workingHours"));

        tableView.getColumns().addAll(eventTimeCol, eventTypeCol, workingHoursCol);

        fetchAndPopulateTable(tableView);

        vbox.getChildren().addAll(titleLabel, tableView);

        return vbox;
    }

    private static void close() {
        statusLabel.setText("Status: Not Clocked In");
        clockInLabel.setText("Clock In Time: ");
        clockOutLabel.setText("Clock Out Time: ");
        workingHoursLabel.setText("Working Hours: ");
        isClockedIn = false;
    }

    private static void clockIn() {
        if (!isClockedIn) {
            isClockedIn = true;
            clockInTime = LocalDateTime.now();
            statusLabel.setText("Status: Clocked In at " + formatDateTime(clockInTime));
            clockInLabel.setText("Clock In Time: " + formatDateTime(clockInTime));
            storeClockTime(clockInTime, true);
        }
    }

    private static void clockOut() {
        if (isClockedIn) {
            clockOutTime = LocalDateTime.now();
            statusLabel.setText("Status: Clocked Out at " + formatDateTime(clockOutTime));
            clockOutLabel.setText("Clock Out Time: " + formatDateTime(clockOutTime));
            calculateAndStoreWorkingHours();
            displayWorkingHours();
            isClockedIn = false;
        }
    }

    private static void calculateAndStoreWorkingHours() {
        long minutes = calculateMinutesBetween(clockInTime, clockOutTime);
        storeClockTime(clockOutTime, false);
        storeWorkingHours(minutes);
    }

    private static long calculateMinutesBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toMinutes();
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    private static void storeClockTime(LocalDateTime dateTime, boolean isClockIn) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/payrollms", "root", "")) {
            String sql = "INSERT INTO attendance_log (username, company_id, event_time, event_type) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, loggedInUsername); // Ensure loggedInUsername is not null
                statement.setInt(2, companyId);
                statement.setTimestamp(3, Timestamp.valueOf(dateTime));
                statement.setString(4, isClockIn ? "Clock In" : "Clock Out");
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static void storeWorkingHours(long minutes) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/payrollms", "root", "")) {
            String sql = "UPDATE attendance_log SET working_hours_minutes = ? WHERE username = ? AND company_id = ? AND event_time = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setLong(1, minutes);
                statement.setString(2, loggedInUsername); // Ensure loggedInUsername is not null
                statement.setInt(3, companyId);
                statement.setTimestamp(4, Timestamp.valueOf(clockInTime));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static void displayWorkingHours() {
        long hours = calculateMinutesBetween(clockInTime, clockOutTime) / 60;
        long minutes = calculateMinutesBetween(clockInTime, clockOutTime) % 60;
        workingHoursLabel.setText("Working Hours: " + hours + " hours " + minutes);
    }

    private static void fetchAndPopulateTable(TableView<AttendanceRecord> tableView) {
        tableView.getItems().clear();
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/payrollms", "root", "");
            String sql = "SELECT event_time, event_type, working_hours_minutes FROM attendance_log WHERE username = ? AND company_id = ? ORDER BY event_time DESC";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, loggedInUsername);
            statement.setInt(2, companyId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                LocalDateTime eventTime = resultSet.getTimestamp("event_time").toLocalDateTime();
                String eventType = resultSet.getString("event_type");
                long workingHoursMinutes = resultSet.getLong("working_hours_minutes");
                String formattedDateTime = formatDateTime(eventTime);
                String formattedWorkingHours = formatWorkingHours(workingHoursMinutes);
                AttendanceRecord record = new AttendanceRecord(formattedDateTime, eventType, formattedWorkingHours);
                tableView.getItems().add(record);
            }
            resultSet.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }
    
    private static String formatWorkingHours(long minutes) {
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        return hours + " hours " + remainingMinutes + " minutes";
    }

    private static void handleSQLException(SQLException e) {
        if (e instanceof SQLIntegrityConstraintViolationException) {
            System.out.println("Duplicate entry error: " + e.getMessage());
        } else {
            e.printStackTrace();
        }
    }

    private static void setButtonStyle(Button button) {
        button.setStyle("-fx-font-size: 16px; -fx-padding: 10px 20px; -fx-background-color:black; -fx-text-fill: white; -fx-border-radius: 5px;");
    }

    private static void setLabelStyle(Label label) {
        label.setStyle("-fx-font-size: 18px; -fx-padding: 5px 0;");
    }

    public static class AttendanceRecord {
        private final String eventTime;
        private final String eventType;
        private final String workingHours;

        public AttendanceRecord(String eventTime, String eventType, String workingHours) {
            this.eventTime = eventTime;
            this.eventType = eventType;
            this.workingHours = workingHours;
        }

        public String getEventTime() {
            return eventTime;
        }

        public String getEventType() {
            return eventType;
        }

        public String getWorkingHours() {
            return workingHours;
        }
    }
}
