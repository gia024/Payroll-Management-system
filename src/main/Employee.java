package main;

import java.math.BigDecimal;
import java.sql.Date;

public class Employee {
    private int employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private Date dateOfBirth;
    private Date dateHired;
    private BigDecimal salary;
    private String position; // New field
    private int companyId; // New field

    public Employee(int employeeId, String firstName, String lastName, String email, String phone, String address, Date dateOfBirth, Date dateHired, BigDecimal salary, String position, int companyId) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.dateHired = dateHired;
        this.salary = salary;
        this.position = position; // New field
        this.companyId = companyId; // New field
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public Date getDateHired() {
        return dateHired;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public String getPosition() { // New getter
        return position;
    }

    public int getCompanyId() { // New getter
        return companyId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setDateHired(Date dateHired) {
        this.dateHired = dateHired;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public void setPosition(String position) { // New setter
        this.position = position;
    }

    public void setCompanyId(int companyId) { // New setter
        this.companyId = companyId;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
