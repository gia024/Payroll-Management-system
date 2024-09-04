package users;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import main.DatabaseConnector;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Loan extends VBox {

    private static final double COMPANY_INTEREST_RATE = 2.5; // Fixed company interest rate

    private TextField firstNameField;
    private TextField lastNameField;

    private ObservableList<LoanApplication> loanApplications = FXCollections.observableArrayList();
    private TableView<LoanApplication> loanTableView;

    public Loan() {
        // Constructor can be used for initialization if needed
    }

    @SuppressWarnings({ "deprecation", "unchecked" })
    public Node getView(String loggedInEmail) {
        setPadding(new Insets(10));
        setSpacing(10);

        Label titleLabel = new Label("Loan Application");
        titleLabel.getStyleClass().add("title");

        firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        TextField amountField = new TextField();
        amountField.setPromptText("Loan Amount");

        TextField durationField = new TextField();
        durationField.setPromptText("Loan Duration (months)");

        TextField startDateField = new TextField();
        startDateField.setPromptText("Start Date (YYYY-MM-DD)");

        Label feedbackLabel = new Label();
        feedbackLabel.getStyleClass().add("feedback-label");

        fetchUserDetails(loggedInEmail);

        Button btnSubmit = new Button("Submit Application");
        btnSubmit.getStyleClass().add("button");

        btnSubmit.setOnAction(e -> {
            try {
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                double amount = Double.parseDouble(amountField.getText());
                int duration = Integer.parseInt(durationField.getText());
                LocalDate startDate = LocalDate.parse(startDateField.getText());

                if (!isValidLoanDetails(firstName, lastName, amount, duration, startDate)) {
                    return;
                }

                double interestRate = COMPANY_INTEREST_RATE;
                double totalInterest = (amount * interestRate * duration) / 100;
                double monthlyPayment = (amount + totalInterest) / duration;

                showLoanDetails(firstName, lastName, amount, duration, interestRate, startDate, totalInterest, monthlyPayment);

                insertLoanApplication(firstName, lastName, amount, duration, interestRate, startDate);

                showAlert(AlertType.INFORMATION, "Success", "Loan application submitted successfully. Total Interest: $" + totalInterest);

                amountField.clear();
                durationField.clear();
                startDateField.clear();

                fetchLoanApplications(firstName, lastName); // Refresh loan applications

            } catch (NumberFormatException ex) {
                showAlert(AlertType.ERROR, "Invalid Input", "Please enter valid numbers for amount and duration.");
            } catch (SQLException ex) {
                showAlert(AlertType.ERROR, "Database Error", "Failed to submit loan application. Please try again.");
                ex.printStackTrace();
            }
        });

        loanTableView = new TableView<>();
        loanTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<LoanApplication, Integer> loanIdColumn = new TableColumn<>("Loan ID");
        loanIdColumn.setCellValueFactory(data -> data.getValue().loanIdProperty().asObject());

        TableColumn<LoanApplication, Double> amountColumn = new TableColumn<>("Amount ($)");
        amountColumn.setCellValueFactory(data -> data.getValue().amountProperty().asObject());

        TableColumn<LoanApplication, Integer> durationColumn = new TableColumn<>("Duration (months)");
        durationColumn.setCellValueFactory(data -> data.getValue().durationProperty().asObject());

        TableColumn<LoanApplication, Double> interestRateColumn = new TableColumn<>("Interest Rate (%)");
        interestRateColumn.setCellValueFactory(data -> data.getValue().interestRateProperty().asObject());

        TableColumn<LoanApplication, LocalDate> startDateColumn = new TableColumn<>("Start Date");
        startDateColumn.setCellValueFactory(data -> data.getValue().startDateProperty());

        TableColumn<LoanApplication, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty("Pending")); // Example status

        loanTableView.getColumns().addAll(loanIdColumn, amountColumn, durationColumn, interestRateColumn, startDateColumn, statusColumn);

        VBox loanList = new VBox(10);
        loanList.getChildren().addAll(new Label("Loan Applications:"), loanTableView);

        getChildren().addAll(
                titleLabel,
                firstNameField,
                lastNameField,
                new Label("Amount:"),
                amountField,
                new Label("Duration (months):"),
                durationField,
                new Label("Start Date:"),
                startDateField,
                btnSubmit,
                feedbackLabel,
                loanList
        );

        fetchLoanApplications(firstNameField.getText(), lastNameField.getText()); // Load initial loan applications

        return this;
    }

    private void fetchUserDetails(String loggedInEmail) {
        String sql = "SELECT first_name, last_name FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loggedInEmail);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                firstNameField.setText(firstName);
                lastNameField.setText(lastName);
            }
        } catch (SQLException ex) {
            showAlert(AlertType.ERROR, "Database Error", "Failed to fetch user details.");
            ex.printStackTrace();
        }
    }

    private boolean isValidLoanDetails(String firstName, String lastName, double amount, int duration, LocalDate startDate) {
        if (firstName == null || lastName == null || firstName.isEmpty() || lastName.isEmpty()) {
            showFeedback("Please check your profile to ensure your details are updated.");
            return false;
        }

        if (amount <= 0) {
            showFeedback("Loan amount must be greater than zero.");
            return false;
        }

        if (duration <= 0) {
            showFeedback("Loan duration must be greater than zero.");
            return false;
        }

        if (startDate == null) {
            showFeedback("Please enter a valid start date.");
            return false;
        }

        return true;
    }

    private void fetchLoanApplications(String firstName, String lastName) {
        loanApplications.clear();
        String sql = "SELECT * FROM loans WHERE first_name = ? AND last_name = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int loanId = rs.getInt("loan_id");
                double amount = rs.getDouble("amount");
                int duration = rs.getInt("duration");
                double interestRate = rs.getDouble("interest_rate");
                LocalDate startDate = rs.getDate("start_date").toLocalDate();
                LoanApplication loan = new LoanApplication(loanId, amount, duration, interestRate, startDate);
                loanApplications.add(loan);
            }
            loanTableView.setItems(loanApplications);
        } catch (SQLException ex) {
            showAlert(AlertType.ERROR, "Database Error", "Failed to fetch loan applications.");
            ex.printStackTrace();
        }
    }

    private void insertLoanApplication(String firstName, String lastName, double amount, int duration, double interestRate, LocalDate startDate) throws SQLException {
        String sql = "INSERT INTO loans (first_name, last_name, amount, duration, interest_rate, start_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setDouble(3, amount);
            pstmt.setInt(4, duration);
            pstmt.setDouble(5, interestRate);
            pstmt.setDate(6, java.sql.Date.valueOf(startDate));
            pstmt.executeUpdate();
        }
    }

    private void showLoanDetails(String firstName, String lastName, double amount, int duration, double interestRate, LocalDate startDate, double totalInterest, double monthlyPayment) {
        Label detailsLabel = new Label("Loan Details:\n" +
                "Name: " + firstName + " " + lastName + "\n" +
                "Loan Amount: $" + amount + "\n" +
                "Duration: " + duration + " months\n" +
                "Interest Rate: " + interestRate + "%\n" +
                "Start Date: " + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        getChildren().add(detailsLabel);
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showFeedback(String message) {
        Label feedbackLabel = (Label) getChildren().get(getChildren().size() - 2); // Feedback label is second last child
        feedbackLabel.setText(message);
    }

    public class LoanApplication {

        private final SimpleIntegerProperty loanId;
        private final DoubleProperty amount;
        private final SimpleIntegerProperty duration;
        private final DoubleProperty interestRate;
        private final ObjectProperty<LocalDate> startDate;

        public LoanApplication(int loanId, double amount, int duration, double interestRate, LocalDate startDate) {
            this.loanId = new SimpleIntegerProperty(loanId);
            this.amount = new SimpleDoubleProperty(amount);
            this.duration = new SimpleIntegerProperty(duration);
            this.interestRate = new SimpleDoubleProperty(interestRate);
            this.startDate = new SimpleObjectProperty<>(startDate);
        }

        public int getLoanId() {
            return loanId.get();
        }

        public SimpleIntegerProperty loanIdProperty() {
            return loanId;
        }

        public double getAmount() {
            return amount.get();
        }

        public DoubleProperty amountProperty() {
            return amount;
        }

        public int getDuration() {
            return duration.get();
        }

        public SimpleIntegerProperty durationProperty() {
            return duration;
        }

        public double getInterestRate() {
            return interestRate.get();
        }

        public DoubleProperty interestRateProperty() {
            return interestRate;
        }

        public LocalDate getStartDate() {
            return startDate.get();
        }

        public ObjectProperty<LocalDate> startDateProperty() {
            return startDate;
        }
    }
}
