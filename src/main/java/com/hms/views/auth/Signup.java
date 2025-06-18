package com.hms.views.auth;

import com.hms.utils.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.regex.Pattern;

public class Signup {

    @FXML private TextField txtStudentId;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtMobileNumber;
    @FXML private TextField txtEmailId;
    @FXML private ComboBox<String> combGender;
    @FXML private ComboBox<String> combStudyProgram;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtComPassword;
    @FXML private TextField txtVisiblePassword;
    @FXML private TextField txtVisibleComPassword;
    @FXML private Button btnRegister;
    @FXML private ImageView backIcon;
    @FXML private ImageView showPasswordIcon;
    @FXML private ImageView hidePasswordIcon;
    @FXML private ImageView showComPasswordIcon;
    @FXML private ImageView hideComPasswordIcon;
    @FXML private Hyperlink linkBackToLogin;

    // Error labels for validation
    @FXML private Label lblStudentIdError;
    @FXML private Label lblFirstNameError;
    @FXML private Label lblLastNameError;
    @FXML private Label lblMobileError;
    @FXML private Label lblEmailError;
    @FXML private Label lblGenderError;
    @FXML private Label lblProgramError;
    @FXML private Label lblPasswordError;
    @FXML private Label lblComPasswordError;

    private Connection conn;
    private PreparedStatement pst;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    public void initialize() {
        // Initialize gender dropdown
        ObservableList<String> genders = FXCollections.observableArrayList(
                "Select Gender", "Male", "Female", "Other"
        );
        combGender.setItems(genders);
        combGender.setValue("Select Gender");

        // Initialize study program dropdown
        ObservableList<String> programs = FXCollections.observableArrayList(
                "Select Program",
                "Computer Science",
                "Information Technology",
                "Software Engineering",
                "Data Science",
                "Cybersecurity",
                "Artificial Intelligence",
                "Business Administration",
                "Engineering",
                "Medicine",
                "Law",
                "Arts"
        );
        combStudyProgram.setItems(programs);
        combStudyProgram.setValue("Select Program");

        conn = DB.connect();

        // Hide password visibility icons initially
        hidePasswordIcon.setVisible(false);
        hideComPasswordIcon.setVisible(false);

        // Hide visible password fields initially
        txtVisiblePassword.setVisible(false);
        txtVisiblePassword.setManaged(false);
        txtVisibleComPassword.setVisible(false);
        txtVisibleComPassword.setManaged(false);

        // Initialize error labels as invisible
        hideAllErrorLabels();
    }

    private void hideAllErrorLabels() {
        lblStudentIdError.setVisible(false);
        lblFirstNameError.setVisible(false);
        lblLastNameError.setVisible(false);
        lblMobileError.setVisible(false);
        lblEmailError.setVisible(false);
        lblGenderError.setVisible(false);
        lblProgramError.setVisible(false);
        lblPasswordError.setVisible(false);
        lblComPasswordError.setVisible(false);
    }

    @FXML
    private void handleRegister() {
        hideAllErrorLabels();
        boolean isValid = true;

        String studentId = txtStudentId.getText().trim();
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String mobileNumber = txtMobileNumber.getText().trim();
        String emailId = txtEmailId.getText().trim();
        String gender = combGender.getValue();
        String studyProgram = combStudyProgram.getValue();
        String password = txtPassword.getText();
        String comPassword = txtComPassword.getText();

        // Validate Student ID
        if (studentId.isEmpty()) {
            lblStudentIdError.setText("Student ID is required");
            lblStudentIdError.setVisible(true);
            isValid = false;
        } else if (!studentId.matches("^[0-9]+$")) {
            lblStudentIdError.setText("Student ID should contain only numbers");
            lblStudentIdError.setVisible(true);
            isValid = false;
        } else if (isStudentIdExists(studentId)) {
            lblStudentIdError.setText("Student ID already exists");
            lblStudentIdError.setVisible(true);
            isValid = false;
        }

        // Validate First Name
        if (firstName.isEmpty()) {
            lblFirstNameError.setText("First name is required");
            lblFirstNameError.setVisible(true);
            isValid = false;
        } else if (!firstName.matches("^[a-zA-Z\\s]+$")) {
            lblFirstNameError.setText("First name should contain only letters");
            lblFirstNameError.setVisible(true);
            isValid = false;
        }

        // Validate Last Name
        if (lastName.isEmpty()) {
            lblLastNameError.setText("Last name is required");
            lblLastNameError.setVisible(true);
            isValid = false;
        } else if (!lastName.matches("^[a-zA-Z\\s]+$")) {
            lblLastNameError.setText("Last name should contain only letters");
            lblLastNameError.setVisible(true);
            isValid = false;
        }

        // Validate Mobile Number
        if (mobileNumber.isEmpty()) {
            lblMobileError.setText("Mobile number is required");
            lblMobileError.setVisible(true);
            isValid = false;
        } else if (!mobileNumber.matches("^[0-9]{10}$")) {
            lblMobileError.setText("Mobile number should be 10 digits");
            lblMobileError.setVisible(true);
            isValid = false;
        }

        // Validate Email
        if (emailId.isEmpty()) {
            lblEmailError.setText("Email is required");
            lblEmailError.setVisible(true);
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(emailId).matches()) {
            lblEmailError.setText("Please enter a valid email address");
            lblEmailError.setVisible(true);
            isValid = false;
        }

        // Validate Gender
        if (gender == null || gender.equals("Select Gender")) {
            lblGenderError.setText("Please select gender");
            lblGenderError.setVisible(true);
            isValid = false;
        }

        // Validate Study Program
        if (studyProgram == null || studyProgram.equals("Select Program")) {
            lblProgramError.setText("Please select study program");
            lblProgramError.setVisible(true);
            isValid = false;
        }

        // Validate Password
        if (password.isEmpty()) {
            lblPasswordError.setText("Password is required");
            lblPasswordError.setVisible(true);
            isValid = false;
        } else if (password.length() < 6) {
            lblPasswordError.setText("Password must be at least 6 characters");
            lblPasswordError.setVisible(true);
            isValid = false;
        }

        // Validate Confirm Password
        if (comPassword.isEmpty()) {
            lblComPasswordError.setText("Please confirm your password");
            lblComPasswordError.setVisible(true);
            isValid = false;
        } else if (!password.equals(comPassword)) {
            lblComPasswordError.setText("Passwords do not match");
            lblComPasswordError.setVisible(true);
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        try {
            // Insert into user table with new fields
            String sql = "INSERT INTO user(Student_id, First_name, Last_name, Mobile_number, Email, Gender, Study_program, Password, User_type) VALUES(?,?,?,?,?,?,?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setString(1, studentId);
            pst.setString(2, firstName);
            pst.setString(3, lastName);
            pst.setString(4, mobileNumber);
            pst.setString(5, emailId);
            pst.setString(6, gender);
            pst.setString(7, studyProgram);
            pst.setString(8, password);
            pst.setString(9, "Student");
            pst.executeUpdate();

            // Log the account creation
            logAccountCreation(studentId);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Account Created Successfully!");

            // Clear all fields
            clearAllFields();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error creating account: " + e.getMessage());
        }
    }

    private boolean isStudentIdExists(String studentId) {
        try {
            pst = conn.prepareStatement("SELECT Student_id FROM user WHERE Student_id = ?");
            pst.setString(1, studentId);
            ResultSet rs = pst.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void logAccountCreation(String studentId) throws SQLException {
        Date currentDate = GregorianCalendar.getInstance().getTime();
        DateFormat df = DateFormat.getDateInstance();
        String dateString = df.format(currentDate);

        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String timeString = sdf.format(d);

        String reg = "INSERT INTO logs (User_id, Date, Status) VALUES ((SELECT User_id FROM user WHERE Student_id = ?), ?, ?)";
        pst = conn.prepareStatement(reg);
        pst.setString(1, studentId);
        pst.setString(2, timeString + " / " + dateString);
        pst.setString(3, "New account created");
        pst.execute();
    }

    private void clearAllFields() {
        txtStudentId.clear();
        txtFirstName.clear();
        txtLastName.clear();
        txtMobileNumber.clear();
        txtEmailId.clear();
        txtPassword.clear();
        txtComPassword.clear();
        txtVisiblePassword.clear();
        txtVisibleComPassword.clear();
        combGender.getSelectionModel().select(0);
        combStudyProgram.getSelectionModel().select(0);
        txtStudentId.requestFocus();
        hideAllErrorLabels();
    }

    @FXML
    private void handleBackToLogin() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/hms/fxml/login.fxml")));

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");

            InputStream iconStream = getClass().getResourceAsStream("/com/hms/images/HMS.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            } else {
                System.err.println("⚠️ HMS.png icon not found at /com/hms/images/HMS.png");
            }

            stage.show();

            // Close current window
            ((Stage) backIcon.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShowPassword() {
        txtVisiblePassword.setText(txtPassword.getText());
        txtPassword.setVisible(false);
        txtPassword.setManaged(false);

        txtVisiblePassword.setVisible(true);
        txtVisiblePassword.setManaged(true);

        showPasswordIcon.setVisible(false);
        showPasswordIcon.setManaged(false);

        hidePasswordIcon.setVisible(true);
        hidePasswordIcon.setManaged(true);
    }

    @FXML
    private void handleHidePassword() {
        txtPassword.setText(txtVisiblePassword.getText());
        txtVisiblePassword.setVisible(false);
        txtVisiblePassword.setManaged(false);

        txtPassword.setVisible(true);
        txtPassword.setManaged(true);

        hidePasswordIcon.setVisible(false);
        hidePasswordIcon.setManaged(false);

        showPasswordIcon.setVisible(true);
        showPasswordIcon.setManaged(true);
    }

    @FXML
    private void handleShowComPassword() {
        txtVisibleComPassword.setText(txtComPassword.getText());
        txtComPassword.setVisible(false);
        txtComPassword.setManaged(false);

        txtVisibleComPassword.setVisible(true);
        txtVisibleComPassword.setManaged(true);

        showComPasswordIcon.setVisible(false);
        showComPasswordIcon.setManaged(false);

        hideComPasswordIcon.setVisible(true);
        hideComPasswordIcon.setManaged(true);
    }

    @FXML
    private void handleHideComPassword() {
        txtComPassword.setText(txtVisibleComPassword.getText());
        txtVisibleComPassword.setVisible(false);
        txtVisibleComPassword.setManaged(false);

        txtComPassword.setVisible(true);
        txtComPassword.setManaged(true);

        hideComPasswordIcon.setVisible(false);
        hideComPasswordIcon.setManaged(false);

        showComPasswordIcon.setVisible(true);
        showComPasswordIcon.setManaged(true);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}