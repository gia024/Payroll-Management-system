package users;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardView {

    private String firstName;
    private String lastName;

    public DashboardView(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Node getView() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Label welcomeLabel = new Label("Welcome, " + firstName + " " + lastName + "!");
        welcomeLabel.getStyleClass().add("welcome-label");

        HBox welcomeBox = new HBox(welcomeLabel);
        welcomeBox.getStyleClass().add("welcome-box");
        welcomeBox.setPadding(new Insets(10));
        welcomeBox.setSpacing(10);

        Label titleLabel = new Label("Dashboard");
        titleLabel.getStyleClass().add("title-label");

        PieChart pieChart = createPieChart();
        BarChart<String, Number> barChart = createAttendanceChart();
        Label leaveSummaryLabel = createLeaveSummary();

        // Add charts to grid
        gridPane.add(createChartContainer(pieChart, "Payroll Summary"), 0, 0);
        gridPane.add(createChartContainer(barChart, "Attendance Summary"), 1, 0);
        gridPane.add(createChartContainer(leaveSummaryLabel, "Leave Summary"), 0, 1);
        gridPane.add(createChartContainer(new Label("More Info Here"), "Additional Info"), 1, 1);

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        vbox.getChildren().addAll(welcomeBox, titleLabel, gridPane);

        return vbox;
    }

    private VBox createChartContainer(Node chart, String title) {
        VBox container = new VBox();
        container.setPadding(new Insets(10));
        container.setSpacing(10);

        Label chartTitle = new Label(title);
        chartTitle.getStyleClass().add("chart-title");

        container.getChildren().addAll(chartTitle, chart);
        return container;
    }

    private PieChart createPieChart() {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Payroll Management Summary");

        String query = "SELECT basic_salary, tax, ssf, overtime_salary, net_salary, company_ssf FROM pay_slips WHERE first_name = ? AND last_name = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double basicSalary = rs.getDouble("basic_salary");
                double tax = rs.getDouble("tax");
                double ssf = rs.getDouble("ssf");
                double overtimeSalary = rs.getDouble("overtime_salary");
                double netSalary = rs.getDouble("net_salary");
                double companySsf = rs.getDouble("company_ssf");

                pieChart.getData().add(new PieChart.Data("Basic Salary", basicSalary));
                pieChart.getData().add(new PieChart.Data("Tax", tax));
                pieChart.getData().add(new PieChart.Data("SSF", ssf));
                pieChart.getData().add(new PieChart.Data("Overtime Salary", overtimeSalary));
                pieChart.getData().add(new PieChart.Data("Net Salary", netSalary));
                pieChart.getData().add(new PieChart.Data("Company SSF", companySsf));
            }

        } catch (SQLException e) {
            showErrorDialog("Error loading payroll data.");
            e.printStackTrace();
        }

        return pieChart;
    }

    private BarChart<String, Number> createAttendanceChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Total Working Hours");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Monthly Attendance Summary");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Working Hours");

        String query = "SELECT DATE(event_time) AS date, SUM(working_hours_minutes) / 60 AS total_hours " +
                       "FROM attendance_log " +
                       "WHERE username = ? " +
                       "GROUP BY DATE(event_time)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, firstName);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String date = rs.getString("date");
                double totalHours = rs.getDouble("total_hours");
                series.getData().add(new XYChart.Data<>(date, totalHours));
            }

        } catch (SQLException e) {
            showErrorDialog("Error loading attendance data.");
            e.printStackTrace();
        }

        barChart.getData().add(series);
        return barChart;
    }

    private Label createLeaveSummary() {
        Label leaveSummary = new Label();
        leaveSummary.setText("Loading leave summary...");

        String query = "SELECT COUNT(*) AS leave_count FROM leave_requests WHERE first_name = ? AND last_name = ? AND status = 'Approved'";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int leaveCount = rs.getInt("leave_count");
                leaveSummary.setText("Approved Leave Requests: " + leaveCount);
            }

        } catch (SQLException e) {
            showErrorDialog("Error loading leave summary.");
            e.printStackTrace();
        }

        return leaveSummary;
    }

    private void showErrorDialog(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
