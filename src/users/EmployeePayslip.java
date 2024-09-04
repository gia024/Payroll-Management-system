package users;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import main.DatabaseConnector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeePayslip extends VBox {
    private TableView<Payslip> tableView;
    private ObservableList<Payslip> payslipData = FXCollections.observableArrayList();
    private static String loggedInFirstName;
    private static String loggedInLastName;

    public EmployeePayslip(String firstName, String lastName) {
        loggedInFirstName = firstName;
        loggedInLastName = lastName;
    
        initializeTable();
        loadPayslipData();
        initializeView();
    }

    public Node getView() {
        return this;
    }

    @SuppressWarnings("unchecked")
    private void initializeTable() {
        tableView = new TableView<>();

        TableColumn<Payslip, String> companyCol = new TableColumn<>("Company ID");
        companyCol.setCellValueFactory(new PropertyValueFactory<>("companyId"));

        TableColumn<Payslip, Double> basicSalaryCol = new TableColumn<>("Basic Salary");
        basicSalaryCol.setCellValueFactory(new PropertyValueFactory<>("basicSalary"));

        TableColumn<Payslip, Double> taxCol = new TableColumn<>("Tax");
        taxCol.setCellValueFactory(new PropertyValueFactory<>("tax"));

        TableColumn<Payslip, Double> ssfCol = new TableColumn<>("SSF");
        ssfCol.setCellValueFactory(new PropertyValueFactory<>("ssf"));

        TableColumn<Payslip, Double> overtimeCol = new TableColumn<>("Overtime Salary");
        overtimeCol.setCellValueFactory(new PropertyValueFactory<>("overtimeSalary"));

        TableColumn<Payslip, Double> netSalaryCol = new TableColumn<>("Net Salary");
        netSalaryCol.setCellValueFactory(new PropertyValueFactory<>("netSalary"));

        TableColumn<Payslip, Double> companySsfCol = new TableColumn<>("Company SSF");
        companySsfCol.setCellValueFactory(new PropertyValueFactory<>("companySsf"));

       

        tableView.getColumns().addAll(companyCol, basicSalaryCol, taxCol, ssfCol, overtimeCol, netSalaryCol, companySsfCol);
    }

    private void loadPayslipData() {
        String query = "SELECT * FROM pay_slips WHERE first_name = ? AND last_name = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, loggedInFirstName);
            pstmt.setString(2, loggedInLastName);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String companyId = rs.getString("company_id");
                double basicSalary = rs.getDouble("basic_salary");
                double tax = rs.getDouble("tax");
                double ssf = rs.getDouble("ssf");
                double overtimeSalary = rs.getDouble("overtime_salary");
                double netSalary = rs.getDouble("net_salary");
                double companySsf = rs.getDouble("company_ssf");

                payslipData.add(new Payslip(companyId, basicSalary, tax, ssf, overtimeSalary, netSalary, companySsf));
            }

            tableView.setItems(payslipData);

        } catch (SQLException e) {
            showErrorDialog("Error loading payslip data.");
            e.printStackTrace();
        }
    }

    private void initializeView() {
        setPadding(new Insets(10));
        setSpacing(10);

        Label title = new Label("Your Payslip");
        title.getStyleClass().add("title");

        getChildren().addAll(title, tableView);
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Payslip {
        private String companyId;
        private double basicSalary;
        private double tax;
        private double ssf;
        private double overtimeSalary;
        private double netSalary;
        private double companySsf;
        private String position;

        public Payslip(String companyId, double basicSalary, double tax, double ssf, double overtimeSalary, double netSalary, double companySsf) {
            this.companyId = companyId;
            this.basicSalary = basicSalary;
            this.tax = tax;
            this.ssf = ssf;
            this.overtimeSalary = overtimeSalary;
            this.netSalary = netSalary;
            this.companySsf = companySsf;

        }

        public String getCompanyId() {
            return companyId;
        }

        public void setCompanyId(String companyId) {
            this.companyId = companyId;
        }

        public double getBasicSalary() {
            return basicSalary;
        }

        public void setBasicSalary(double basicSalary) {
            this.basicSalary = basicSalary;
        }

        public double getTax() {
            return tax;
        }

        public void setTax(double tax) {
            this.tax = tax;
        }

        public double getSsf() {
            return ssf;
        }

        public void setSsf(double ssf) {
            this.ssf = ssf;
        }

        public double getOvertimeSalary() {
            return overtimeSalary;
        }

        public void setOvertimeSalary(double overtimeSalary) {
            this.overtimeSalary = overtimeSalary;
        }

        public double getNetSalary() {
            return netSalary;
        }

        public void setNetSalary(double netSalary) {
            this.netSalary = netSalary;
        }

        public double getCompanySsf() {
            return companySsf;
        }

        public void setCompanySsf(double companySsf) {
            this.companySsf = companySsf;
        }

        public String getPosition() {
            return position;
        }

    }
}
