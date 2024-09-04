package main;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class PaySlipApp {

    public static TextField searchField;
    public static Label firstNameLabel, lastNameLabel, positionLabel, basicSalaryLabel, taxLabel, ssfLabel, companySsfLabel, overtimeSalaryLabel, netSalaryLabel;
    public static TableView<PaySlipRecord> paySlipsTable;

    public static VBox getView() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        Label titleLabel = new Label("Generate Payslip");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        searchField = new TextField();
        searchField.setPromptText("Enter Company Employee ID");

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchEmployee());

        Button doneButton = new Button("Done");
        doneButton.setOnAction(e -> {
            showAlert("Pay slip added successfully.");
            clearFields();
            refreshPaySlipsTable();
        });

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(searchButton, doneButton);

        GridPane detailsGrid = createDetailsGrid();

        vbox.getChildren().addAll(
                titleLabel,
                searchField,
                buttonBox,
                detailsGrid,
                createPaySlipsTablePane()
        );

        refreshPaySlipsTable(); // Load data at initialization

        return vbox;
    }

    public static GridPane createDetailsGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
        // grid.setGridLinesVisible(true);


        Label firstNameText = new Label("First Name:");
        firstNameLabel = new Label();
        Label lastNameText = new Label("Last Name:");
        lastNameLabel = new Label();
        Label positionText = new Label("Position:");
        positionLabel = new Label();
        Label basicSalaryText = new Label("Basic Salary:");
        basicSalaryLabel = new Label();
        Label taxText = new Label("Tax (10%):");
        taxLabel = new Label();
        Label ssfText = new Label("SSF (5%):");
        ssfLabel = new Label();
        Label companySsfText = new Label("Company SSF:");
        companySsfLabel = new Label();
        Label overtimeSalaryText = new Label("Overtime Salary:");
        overtimeSalaryLabel = new Label();
        Label netSalaryText = new Label("Net Salary:");
        netSalaryLabel = new Label();

        grid.add(firstNameText, 0, 0);
        grid.add(firstNameLabel, 1, 0);
        grid.add(lastNameText, 0, 1);
        grid.add(lastNameLabel, 1, 1);
        grid.add(positionText, 0, 2);
        grid.add(positionLabel, 1, 2);
        grid.add(basicSalaryText, 0, 3);
        grid.add(basicSalaryLabel, 1, 3);
        grid.add(taxText, 0, 4);
        grid.add(taxLabel, 1, 4);
        grid.add(ssfText, 0, 5);
        grid.add(ssfLabel, 1, 5);
        grid.add(companySsfText, 0, 6);
        grid.add(companySsfLabel, 1, 6);
        grid.add(overtimeSalaryText, 0, 7);
        grid.add(overtimeSalaryLabel, 1, 7);
        grid.add(netSalaryText, 0, 8);
        grid.add(netSalaryLabel, 1, 8);

        return grid;
    }

    @SuppressWarnings("unchecked")
    public static VBox createPaySlipsTablePane() {
        VBox tablePane = new VBox(10);
        tablePane.setPadding(new Insets(10));
        tablePane.setMaxHeight(200);

        TableColumn<PaySlipRecord, Integer> paySlipIdCol = new TableColumn<>("Pay Slip ID");
        paySlipIdCol.setCellValueFactory(new PropertyValueFactory<>("paySlipId"));

        TableColumn<PaySlipRecord, Integer> employeeIdCol = new TableColumn<>("Employee ID");
        employeeIdCol.setCellValueFactory(new PropertyValueFactory<>("employeeId"));

        TableColumn<PaySlipRecord, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<PaySlipRecord, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<PaySlipRecord, String> companyIdCol = new TableColumn<>("Company ID");
        companyIdCol.setCellValueFactory(new PropertyValueFactory<>("companyId"));

        TableColumn<PaySlipRecord, Double> basicSalaryCol = new TableColumn<>("Basic Salary");
        basicSalaryCol.setCellValueFactory(new PropertyValueFactory<>("basicSalary"));

        TableColumn<PaySlipRecord, Double> taxCol = new TableColumn<>("Tax");
        taxCol.setCellValueFactory(new PropertyValueFactory<>("tax"));

        TableColumn<PaySlipRecord, Double> ssfCol = new TableColumn<>("SSF");
        ssfCol.setCellValueFactory(new PropertyValueFactory<>("ssf"));

        TableColumn<PaySlipRecord, Double> companySsfCol = new TableColumn<>("Company SSF");
        companySsfCol.setCellValueFactory(new PropertyValueFactory<>("companySsf"));

        TableColumn<PaySlipRecord, Double> overtimeSalaryCol = new TableColumn<>("Overtime Salary");
        overtimeSalaryCol.setCellValueFactory(new PropertyValueFactory<>("overtimeSalary"));

        TableColumn<PaySlipRecord, Double> netSalaryCol = new TableColumn<>("Net Salary");
        netSalaryCol.setCellValueFactory(new PropertyValueFactory<>("netSalary"));

        TableColumn<PaySlipRecord, LocalDate> paySlipDateCol = new TableColumn<>("Pay Slip Date");
        paySlipDateCol.setCellValueFactory(new PropertyValueFactory<>("paySlipDate"));

        paySlipsTable = new TableView<>();
        paySlipsTable.getColumns().addAll(
                paySlipIdCol, employeeIdCol, firstNameCol, lastNameCol, companyIdCol, basicSalaryCol, taxCol,
                ssfCol, companySsfCol, overtimeSalaryCol, netSalaryCol, paySlipDateCol
        );

        tablePane.getChildren().add(paySlipsTable);

        return tablePane;
    }

    public static void searchEmployee() {
        String companyId = searchField.getText();
        if (companyId.isEmpty()) {
            showAlert("Please enter a Company Employee ID.");
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            String query = "SELECT * FROM employees WHERE company_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, companyId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int employeeId = rs.getInt("employee_id");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String position = rs.getString("position");
                    double basicSalary = rs.getDouble("salary");

                    firstNameLabel.setText("" + firstName);
                    lastNameLabel.setText("" + lastName);
                    positionLabel.setText("" + position);
                    basicSalaryLabel.setText("" + basicSalary);

                    double tax = basicSalary * 0.1;
                    double ssf = basicSalary * 0.05;
                    double companySsf = basicSalary * 0.1;

                    taxLabel.setText("" + tax);
                    ssfLabel.setText("" + ssf);
                    companySsfLabel.setText("" + companySsf);

                    double netSalary = calculateNetSalary(conn, employeeId, basicSalary, tax, ssf, companySsf, companyId);
                    netSalaryLabel.setText("" + netSalary);

                } else {
                    showAlert("Employee not found.");
                    clearFields();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error occurred while searching for employee.");
        }
    }
    public static double calculateNetSalary(Connection conn, int employeeId, double basicSalary, double tax, double ssf, double companySsf, String companyId) {
        double netSalary = basicSalary - tax - ssf;
        double overtimeSalary = 0.0;

        try {
            String query = "SELECT SUM(working_hours_minutes) AS total_minutes FROM attendance_log WHERE company_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, companyId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    double totalMinutes = rs.getDouble("total_minutes");
                    double normalMinutes = 20 * 24 * 60; // 20 days * 24 hours * 60 minutes

                    if (totalMinutes > normalMinutes) {
                        double overtimeMinutes = totalMinutes - normalMinutes;
                        double dailySalary = basicSalary / 30;
                        double hourlySalary = dailySalary / 9 / 60;
                        overtimeSalary = hourlySalary * 1.5 * overtimeMinutes;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error occurred while fetching attendance data.");
        }

        overtimeSalaryLabel.setText("" + overtimeSalary);
        netSalary += overtimeSalary;
        savePaySlip(conn, employeeId, companyId, basicSalary, tax, ssf, companySsf, overtimeSalary, netSalary);
        refreshPaySlipsTable(); // Refresh data after insert
        return netSalary;
    }

    public static void savePaySlip(Connection conn, int employeeId, String companyId, double basicSalary, double tax, double ssf, double companySsf, double overtimeSalary, double netSalary) {
        try {
            String insertQuery = "INSERT INTO pay_slips (employee_id, company_id, basic_salary, tax, ssf, company_ssf, overtime_salary, net_salary, pay_slip_date, first_name, last_name) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setInt(1, employeeId);
                stmt.setString(2, companyId);
                stmt.setDouble(3, basicSalary);
                stmt.setDouble(4, tax);
                stmt.setDouble(5, ssf);
                stmt.setDouble(6, companySsf);
                stmt.setDouble(7, overtimeSalary);
                stmt.setDouble(8, netSalary);
                stmt.setDate(9, java.sql.Date.valueOf(LocalDate.now()));

                // Fetch first_name and last_name from employees table
                String employeeQuery = "SELECT first_name, last_name FROM employees WHERE employee_id = ?";
                try (PreparedStatement empStmt = conn.prepareStatement(employeeQuery)) {
                    empStmt.setInt(1, employeeId);
                    ResultSet empRs = empStmt.executeQuery();
                    if (empRs.next()) {
                        String firstName = empRs.getString("first_name");
                        String lastName = empRs.getString("last_name");
                        stmt.setString(10, firstName);
                        stmt.setString(11, lastName);
                    }
                }

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error occurred while saving pay slip.");
        }
    }

    public static void refreshPaySlipsTable() {
        ObservableList<PaySlipRecord> paySlips = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnector.getConnection()) {
            String query = "SELECT * FROM pay_slips";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    PaySlipRecord record = new PaySlipRecord(
                            rs.getInt("pay_slip_id"),
                            rs.getInt("employee_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("company_id"),
                            rs.getDouble("basic_salary"),
                            rs.getDouble("tax"),
                            rs.getDouble("ssf"),
                            rs.getDouble("company_ssf"),
                            rs.getDouble("overtime_salary"),
                            rs.getDouble("net_salary"),
                            rs.getDate("pay_slip_date").toLocalDate()
                    );
                    paySlips.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error occurred while fetching pay slips.");
        }

        paySlipsTable.setItems(paySlips);
    }
    public static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void clearFields() {
        searchField.clear();
        firstNameLabel.setText("");
        lastNameLabel.setText("");
        positionLabel.setText("");
        basicSalaryLabel.setText("");
        taxLabel.setText("");
        ssfLabel.setText("");
        companySsfLabel.setText("");
        overtimeSalaryLabel.setText("");
        netSalaryLabel.setText("");
    }


    public static class PaySlipRecord {
        private final SimpleIntegerProperty paySlipId;
        private final SimpleIntegerProperty employeeId;
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty companyId;
        private final SimpleDoubleProperty basicSalary;
        private final SimpleDoubleProperty tax;
        private final SimpleDoubleProperty ssf;
        private final SimpleDoubleProperty companySsf;
        private final SimpleDoubleProperty overtimeSalary;
        private final SimpleDoubleProperty netSalary;
        private final SimpleObjectProperty<LocalDate> paySlipDate;

        public PaySlipRecord(int paySlipId, int employeeId, String firstName, String lastName, String companyId,
                             double basicSalary, double tax, double ssf, double companySsf, double overtimeSalary,
                             double netSalary, LocalDate paySlipDate) {
            this.paySlipId = new SimpleIntegerProperty(paySlipId);
            this.employeeId = new SimpleIntegerProperty(employeeId);
            this.firstName = new SimpleStringProperty(firstName);
            this.lastName = new SimpleStringProperty(lastName);
            this.companyId = new SimpleStringProperty(companyId);
            this.basicSalary = new SimpleDoubleProperty(basicSalary);
            this.tax = new SimpleDoubleProperty(tax);
            this.ssf = new SimpleDoubleProperty(ssf);
            this.companySsf = new SimpleDoubleProperty(companySsf);
            this.overtimeSalary = new SimpleDoubleProperty(overtimeSalary);
            this.netSalary = new SimpleDoubleProperty(netSalary);
            this.paySlipDate = new SimpleObjectProperty<>(paySlipDate);
        }

        public int getPaySlipId() {
            return paySlipId.get();
        }

        public int getEmployeeId() {
            return employeeId.get();
        }

        public String getFirstName() {
            return firstName.get();
        }

        public String getLastName() {
            return lastName.get();
        }

        public String getCompanyId() {
            return companyId.get();
        }

        public double getBasicSalary() {
            return basicSalary.get();
        }

        public double getTax() {
            return tax.get();
        }

        public double getSsf() {
            return ssf.get();
        }

        public double getCompanySsf() {
            return companySsf.get();
        }

        public double getOvertimeSalary() {
            return overtimeSalary.get();
        }

        public double getNetSalary() {
            return netSalary.get();
        }

        public LocalDate getPaySlipDate() {
            return paySlipDate.get();
        }
    }

    public static void main(String[] args) {
        // Dummy main method for testing purposes
        Stage stage = new Stage();
        stage.setTitle("Pay Slip Application");
        stage.setScene(new javafx.scene.Scene(getView(), 800, 600));
        stage.show();
    }
}
