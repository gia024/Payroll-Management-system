package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class EmployeeManagement {
    public static ObservableList<Employee> employees = FXCollections.observableArrayList();
    public static TextField firstNameField;
    public static TextField lastNameField;
    public static TextField emailField;
    public static TextField phoneField;
    public static TextField addressField;
    public static DatePicker dateOfBirthPicker;
    public static DatePicker dateHiredPicker;
    public static TextField salaryField;
    public static TextField positionField;
    public static TextField companyIdField;
    public static TextField searchCompanyIdField;

    public static ScrollPane getView() {
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10));

        Label titleLabel = new Label("Employee Management Section");
        titleLabel.setFont(Font.font("Arial", 18));
        titleLabel.setTextFill(Color.DARKBLUE);

        // New section for searching by company ID
        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(10, 0, 0, 0));
        searchCompanyIdField = new TextField();
        searchCompanyIdField.setPromptText("Search by Company ID");

        Button searchByCompanyIdButton = new Button("Search");
        searchByCompanyIdButton.setOnAction(e -> searchEmployeesByCompanyId());

        searchBox.getChildren().addAll(searchCompanyIdField, searchByCompanyIdButton);

        HBox controlsBox = new HBox(10);
        controlsBox.setPadding(new Insets(10, 0, 0, 0));

        firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        emailField = new TextField();
        emailField.setPromptText("Email");

        phoneField = new TextField();
        phoneField.setPromptText("Phone");

        VBox controlsBox1 = new VBox(10);
        controlsBox1.setPadding(new Insets(10, 0, 0, 0));

        addressField = new TextField();
        addressField.setPromptText("Address");

        salaryField = new TextField();
        salaryField.setPromptText("Salary");

        dateOfBirthPicker = new DatePicker();
        dateOfBirthPicker.setPromptText("Date of Birth");

        dateHiredPicker = new DatePicker();
        dateHiredPicker.setPromptText("Date Hired");

        positionField = new TextField();
        positionField.setPromptText("Position");

        companyIdField = new TextField();
        companyIdField.setPromptText("Company ID");

        HBox controlsBox2 = new HBox(10);
        controlsBox2.setPadding(new Insets(10, 0, 0, 0));

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> addEmployee());

        Button updateButton = new Button("Update");
        updateButton.setOnAction(e -> updateEmployee());

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteEmployee());

        controlsBox.getChildren().addAll(firstNameField, lastNameField, emailField, phoneField);
        controlsBox1.getChildren().addAll(addressField, dateOfBirthPicker, dateHiredPicker, salaryField, positionField, companyIdField);
        controlsBox2.getChildren().addAll(addButton, updateButton, deleteButton);

        ListView<Employee> employeeList = new ListView<>(employees);
        employeeList.setPrefHeight(200);
        employeeList.setCellFactory(param -> new ListCell<Employee>() {
            @Override
            protected void updateItem(Employee employee, boolean empty) {
                super.updateItem(employee, empty);
                if (empty || employee == null) {
                    setText(null);
                } else {
                    setText(employee.getFirstName() + " " + employee.getLastName());
                }
            }
        });

        vbox.getChildren().addAll(titleLabel, searchBox, controlsBox, controlsBox1, controlsBox2, employeeList);

        // Wrap VBox with ScrollPane
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true); // Fit the width of the content to the width of the ScrollPane
        scrollPane.setFitToHeight(true); // Fit the height of the content to the height of the ScrollPane
        scrollPane.setPadding(new Insets(10)); // Add some padding around the content

        // Load employees from database
        loadEmployeesFromDatabase();

        // Show employee details on selection
        employeeList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showEmployeeDetails(newValue));

        return scrollPane;
    }

    public static void loadEmployeesFromDatabase() {
        employees.clear();
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connection = DatabaseConnector.getConnection();
            String query = "SELECT * FROM employees";
            stmt = connection.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Employee employee = new Employee(
                    rs.getInt("employee_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address"),
                    rs.getDate("date_of_birth"),
                    rs.getDate("date_hired"),
                    rs.getBigDecimal("salary"),
                    rs.getString("position"),
                    rs.getInt("company_id")
                );
                employees.add(employee);
            }
        } catch (SQLException e) {
            showErrorDialog("Database Error", "An error occurred while loading employees from the database.");
        } finally {
            closeResources(stmt, rs);
        }
    }

    public static void addEmployee() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String address = addressField.getText();
        LocalDate dateOfBirth = dateOfBirthPicker.getValue();
        LocalDate dateHired = dateHiredPicker.getValue();
        String salaryStr = salaryField.getText();
        String position = positionField.getText();
        String companyIdStr = companyIdField.getText();

        if (validateFields(firstName, lastName, email, phone, address, dateOfBirth, dateHired, salaryStr, position, companyIdStr)) {
            Connection connection = null;
            PreparedStatement stmt = null;
            try {
                connection = DatabaseConnector.getConnection();
                String query = "INSERT INTO employees (first_name, last_name, email, phone, address, date_of_birth, date_hired, salary, position, company_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                stmt = connection.prepareStatement(query);
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, email);
                stmt.setString(4, phone);
                stmt.setString(5, address);
                stmt.setDate(6, Date.valueOf(dateOfBirth));
                stmt.setDate(7, Date.valueOf(dateHired));
                stmt.setBigDecimal(8, new BigDecimal(salaryStr));
                stmt.setString(9, position);
                stmt.setInt(10, Integer.parseInt(companyIdStr));

                stmt.executeUpdate();
                showInfoDialog("Success", "Employee added successfully.");
                loadEmployeesFromDatabase();
                clearFields();
            } catch (SQLException e) {
                showErrorDialog("Database Error", "An error occurred while adding the employee.");
            } finally {
                closeResources(stmt);
            }
        }
    }

    public static void updateEmployee() {
        Employee selectedEmployee = employees.stream()
            .filter(employee -> employee.getFirstName().equals(firstNameField.getText()) && employee.getLastName().equals(lastNameField.getText()))
            .findFirst()
            .orElse(null);

        if (selectedEmployee != null) {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String address = addressField.getText();
            LocalDate dateOfBirth = dateOfBirthPicker.getValue();
            LocalDate dateHired = dateHiredPicker.getValue();
            String salaryStr = salaryField.getText();
            String position = positionField.getText();
            String companyIdStr = companyIdField.getText();

            if (validateFields(firstName, lastName, email, phone, address, dateOfBirth, dateHired, salaryStr, position, companyIdStr)) {
                Connection connection = null;
                PreparedStatement stmt = null;
                try {
                    connection = DatabaseConnector.getConnection();
                    String query = "UPDATE employees SET first_name = ?, last_name = ?, email = ?, phone = ?, address = ?, date_of_birth = ?, date_hired = ?, salary = ?, position = ?, company_id = ? WHERE employee_id = ?";
                    stmt = connection.prepareStatement(query);
                    stmt.setString(1, firstName);
                    stmt.setString(2, lastName);
                    stmt.setString(3, email);
                    stmt.setString(4, phone);
                    stmt.setString(5, address);
                    stmt.setDate(6, Date.valueOf(dateOfBirth));
                    stmt.setDate(7, Date.valueOf(dateHired));
                    stmt.setBigDecimal(8, new BigDecimal(salaryStr));
                    stmt.setString(9, position);
                    stmt.setInt(10, Integer.parseInt(companyIdStr));
                    stmt.setInt(11, selectedEmployee.getEmployeeId());

                    stmt.executeUpdate();
                    showInfoDialog("Success", "Employee updated successfully.");
                    loadEmployeesFromDatabase();
                    clearFields();
                } catch (SQLException e) {
                    showErrorDialog("Database Error", "An error occurred while updating the employee.");
                } finally {
                    closeResources(stmt);
                }
            }
        } else {
            showErrorDialog("Update Error", "No employee selected to update.");
        }
    }

    public static void deleteEmployee() {
        Employee selectedEmployee = employees.stream()
            .filter(employee -> employee.getFirstName().equals(firstNameField.getText()) && employee.getLastName().equals(lastNameField.getText()))
            .findFirst()
            .orElse(null);

        if (selectedEmployee != null) {
            Connection connection = null;
            PreparedStatement stmt = null;
            try {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Dialog");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to delete this employee?");
                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    connection = DatabaseConnector.getConnection();
                    String query = "DELETE FROM employees WHERE employee_id = ?";
                    stmt = connection.prepareStatement(query);
                    stmt.setInt(1, selectedEmployee.getEmployeeId());

                    stmt.executeUpdate();
                    showInfoDialog("Success", "Employee deleted successfully.");
                    loadEmployeesFromDatabase();
                    clearFields();
                }
            } catch (SQLException e) {
                showErrorDialog("Database Error", "An error occurred while deleting the employee.");
            } finally {
                closeResources(stmt);
            }
        } else {
            showErrorDialog("Delete Error", "No employee selected to delete.");
        }
    }

    public static void searchEmployeesByCompanyId() {
        String companyIdStr = searchCompanyIdField.getText();
        if (companyIdStr.isEmpty()) {
            showErrorDialog("Search Error", "Company ID field cannot be empty.");
            return;
        }

        try {
            int companyId = Integer.parseInt(companyIdStr);
            Connection connection = DatabaseConnector.getConnection();
            String query = "SELECT * FROM employees WHERE company_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();

            employees.clear();
            while (rs.next()) {
                Employee employee = new Employee(
                    rs.getInt("employee_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address"),
                    rs.getDate("date_of_birth"),
                    rs.getDate("date_hired"),
                    rs.getBigDecimal("salary"),
                    rs.getString("position"),
                    rs.getInt("company_id")
                );
                employees.add(employee);
            }
            showInfoDialog("Search Results", employees.size() + " employees found.");
        } catch (NumberFormatException e) {
            showErrorDialog("Search Error", "Invalid Company ID format.");
        } catch (SQLException e) {
            showErrorDialog("Database Error", "An error occurred while searching for employees.");
        }
    }

    public static boolean validateFields(String firstName, String lastName, String email, String phone, String address, LocalDate dateOfBirth, LocalDate dateHired, String salaryStr, String position, String companyIdStr) {
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || dateOfBirth == null || dateHired == null || salaryStr.isEmpty() || position.isEmpty() || companyIdStr.isEmpty()) {
            showErrorDialog("Validation Error", "All fields are required.");
            return false;
        }

        try {
            new BigDecimal(salaryStr);
            Integer.parseInt(companyIdStr);
        } catch (NumberFormatException e) {
            showErrorDialog("Validation Error", "Salary and Company ID must be numeric.");
            return false;
        }

        return true;
    }

    public static void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        dateOfBirthPicker.setValue(null);
        dateHiredPicker.setValue(null);
        salaryField.clear();
        positionField.clear();
        companyIdField.clear();
    }

    public static void showEmployeeDetails(Employee employee) {
        if (employee != null) {
            firstNameField.setText(employee.getFirstName());
            lastNameField.setText(employee.getLastName());
            emailField.setText(employee.getEmail());
            phoneField.setText(employee.getPhone());
            addressField.setText(employee.getAddress());
            dateOfBirthPicker.setValue(employee.getDateOfBirth().toLocalDate());
            dateHiredPicker.setValue(employee.getDateHired().toLocalDate());
            salaryField.setText(employee.getSalary().toString());
            positionField.setText(employee.getPosition());
            companyIdField.setText(String.valueOf(employee.getCompanyId()));
        }
    }

    public static void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            try {
                if (resource != null) {
                    resource.close();
                }
            } catch (Exception e) {
                // Handle resource closing exception
            }
        }
    }
}
