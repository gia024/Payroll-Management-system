package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AttendanceManagement {

    private static TableView<AttendanceRecord> table;

    @SuppressWarnings("unchecked")
    public static VBox getView() {
        // Create table and columns
        table = new TableView<>();
        TableColumn<AttendanceRecord, String> nameCol = new TableColumn<>("Employee Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("employeeName"));

        TableColumn<AttendanceRecord, String> eventTypeCol = new TableColumn<>("Event Type");
        eventTypeCol.setCellValueFactory(new PropertyValueFactory<>("eventType"));

        TableColumn<AttendanceRecord, String> eventTimeCol = new TableColumn<>("Event Time");
        eventTimeCol.setCellValueFactory(new PropertyValueFactory<>("eventTime"));

        TableColumn<AttendanceRecord, Long> workingHoursCol = new TableColumn<>("Working Hours (minutes)");
        workingHoursCol.setCellValueFactory(new PropertyValueFactory<>("workingHours"));

        TableColumn<AttendanceRecord, Integer> companyIdCol = new TableColumn<>("Company ID");
        companyIdCol.setCellValueFactory(new PropertyValueFactory<>("companyId"));

        table.getColumns().addAll(nameCol, eventTypeCol, eventTimeCol, workingHoursCol, companyIdCol);

        // Populate table with data from database
        table.setItems(getAttendanceData());

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);
        vbox.getChildren().add(table);

        return vbox;
    }

    private static ObservableList<AttendanceRecord> getAttendanceData() {
        ObservableList<AttendanceRecord> data = FXCollections.observableArrayList();
    
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/payrollms", "root", "");
             Statement stmt = conn.createStatement()) {
    
            String sql = "SELECT al.id, al.username, al.event_time, al.event_type, al.working_hours_minutes, e.company_id " +
                         "FROM attendance_log al " +
                         "JOIN employees e ON al.username = e.email";  // Assuming 'username' in attendance_log matches 'email' in employees
            ResultSet rs = stmt.executeQuery(sql);
    
            while (rs.next()) {
                rs.getInt("id");
                String username = rs.getString("username");
                String eventTime = rs.getTimestamp("event_time").toString();
                String eventType = rs.getString("event_type");
                long workingHours = rs.getLong("working_hours_minutes");
                int companyId = rs.getInt("company_id");
    
                data.add(new AttendanceRecord(username, eventTime, eventType, workingHours, companyId));
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return data;
    }
    

    public static class AttendanceRecord {
        private final String employeeName;
        private final String eventType;
        private final String eventTime;
        private final long workingHours;
        private final int companyId;

        public AttendanceRecord(String employeeName, String eventType, String eventTime, long workingHours, int companyId) {
            this.employeeName = employeeName;
            this.eventType = eventType;
            this.eventTime = eventTime;
            this.workingHours = workingHours;
            this.companyId = companyId;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public String getEventType() {
            return eventType;
        }

        public String getEventTime() {
            return eventTime;
        }

        public long getWorkingHours() {
            return workingHours;
        }

        public int getCompanyId() {
            return companyId;
        }
    }
}
