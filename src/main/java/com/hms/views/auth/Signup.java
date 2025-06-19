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
import java.util.Objects;
import java.util.regex.Pattern;

public class Signup {

    @FXML private TextField txtStudentId;
    @FXML private TextField txtUsername;
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
    @FXML private ImageView showPasswordIcon;
    @FXML private ImageView hidePasswordIcon;
    @FXML private ImageView showComPasswordIcon;
    @FXML private ImageView hideComPasswordIcon;
    @FXML private Hyperlink linkBackToLogin;

    @FXML private Label lblStudentIdError;
    @FXML private Label lblUsernameError;
    @FXML private Label lblFirstNameError;
    @FXML private Label lblLastNameError;
    @FXML private Label lblMobileError;
    @FXML private Label lblEmailError;
    @FXML private Label lblGenderError;
    @FXML private Label lblProgramError;
    @FXML private Label lblPasswordError;
    @FXML private Label lblComPasswordError;

    private final Connection conn = DB.connect();

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{6,}$");
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9._-]{4,20}$");

    public void initialize() {
        combGender.setItems(FXCollections.observableArrayList("Select Gender", "Male", "Female", "Other"));
        combGender.setValue("Select Gender");

        combStudyProgram.setItems(FXCollections.observableArrayList(
                "Select Program", "Computer Science", "Information Technology", "Software Engineering",
                "Data Science", "Cybersecurity", "Artificial Intelligence",
                "Business Administration", "Engineering", "Medicine", "Law", "Arts"
        ));
        combStudyProgram.setValue("Select Program");

        hidePasswordIcon.setVisible(false);
        hideComPasswordIcon.setVisible(false);
        txtVisiblePassword.setVisible(false);
        txtVisiblePassword.setManaged(false);
        txtVisibleComPassword.setVisible(false);
        txtVisibleComPassword.setManaged(false);

        hideAllErrorLabels();
    }

    private void hideAllErrorLabels() {
        lblStudentIdError.setVisible(false);
        lblUsernameError.setVisible(false);
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
        if (conn == null) {
            showAlert(Alert.AlertType.ERROR, "Connection Error", "Database connection not available.");
            return;
        }

        boolean isValid = true;

        String studentId = txtStudentId.getText().trim();
        String username = txtUsername.getText().trim();
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String mobile = txtMobileNumber.getText().trim();
        String email = txtEmailId.getText().trim();
        String gender = combGender.getValue();
        String program = combStudyProgram.getValue();
        String password = txtPassword.getText();
        String comPassword = txtComPassword.getText();

        // Validation
        if (username.isEmpty()) {
            showError(lblUsernameError, "Username is required");
            isValid = false;
        } else if (!USERNAME_PATTERN.matcher(username).matches()) {
            showError(lblUsernameError, "4–20 chars: letters, digits, . _ -");
            isValid = false;
        } else if (exists("SELECT Username FROM student WHERE Username = ?", username)) {
            showError(lblUsernameError, "Username already taken");
            isValid = false;
        }

        if (studentId.isEmpty()) {
            showError(lblStudentIdError, "Student ID is required");
            isValid = false;
        } else if (!studentId.matches("\\d+")) {
            showError(lblStudentIdError, "Only digits allowed");
            isValid = false;
        } else if (exists("SELECT Student_id FROM student WHERE Student_id = ?", studentId)) {
            showError(lblStudentIdError, "Student ID already exists");
            isValid = false;
        }

        if (firstName.isEmpty()) {
            showError(lblFirstNameError, "First name required");
            isValid = false;
        } else if (!firstName.matches("[a-zA-Z\\s]+")) {
            showError(lblFirstNameError, "Only letters allowed");
            isValid = false;
        }

        if (lastName.isEmpty()) {
            showError(lblLastNameError, "Last name required");
            isValid = false;
        } else if (!lastName.matches("[a-zA-Z\\s]+")) {
            showError(lblLastNameError, "Only letters allowed");
            isValid = false;
        }

        if (mobile.isEmpty()) {
            showError(lblMobileError, "Mobile required");
            isValid = false;
        } else if (!mobile.matches("\\d{10}")) {
            showError(lblMobileError, "Must be 10 digits");
            isValid = false;
        } else if (exists("SELECT Mobile_number FROM student WHERE Mobile_number = ?", mobile)) {
            showError(lblMobileError, "Mobile already registered");
            isValid = false;
        }

        if (email.isEmpty()) {
            showError(lblEmailError, "Email required");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError(lblEmailError, "Invalid email");
            isValid = false;
        } else if (exists("SELECT Email FROM student WHERE Email = ?", email)) {
            showError(lblEmailError, "Email already used");
            isValid = false;
        }

        if (gender == null || gender.equals("Select Gender")) {
            showError(lblGenderError, "Select gender");
            isValid = false;
        }

        if (program == null || program.equals("Select Program")) {
            showError(lblProgramError, "Select program");
            isValid = false;
        }

        if (password.isEmpty()) {
            showError(lblPasswordError, "Password required");
            isValid = false;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            showError(lblPasswordError, "Weak password");
            isValid = false;
        }

        if (comPassword.isEmpty()) {
            showError(lblComPasswordError, "Confirm password");
            isValid = false;
        } else if (!password.equals(comPassword)) {
            showError(lblComPasswordError, "Passwords do not match");
            isValid = false;
        }

        if (!isValid) return;

        // Insert
        String sql = "INSERT INTO student(Student_id, Username, First_name, Last_name, Mobile_number, Email, Gender, Study_program, Password) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, studentId);
            pst.setString(2, username);
            pst.setString(3, firstName);
            pst.setString(4, lastName);
            pst.setString(5, mobile);
            pst.setString(6, email);
            pst.setString(7, gender);
            pst.setString(8, program);
            pst.setString(9, password); 
            pst.executeUpdate();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
            return;
        }

        logAccountCreation(studentId);
        showAlert(Alert.AlertType.INFORMATION, "Success", "Account created!");
        clearAllFields();
    }

    private void logAccountCreation(String studentId) {
        String logSql = "INSERT INTO logs (User_id, Date, Status) VALUES ((SELECT User_id FROM student WHERE Student_id = ?), ?, ?)";
        Date now = new Date();
        String dateStr = DateFormat.getDateInstance().format(now);
        String timeStr = new SimpleDateFormat("HH:mm:ss").format(now);
        try (PreparedStatement pst = conn.prepareStatement(logSql)) {
            pst.setString(1, studentId);
            pst.setString(2, timeStr + " / " + dateStr);
            pst.setString(3, "New account created");
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean exists(String sql, String param) {
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, param);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private void showError(Label label, String msg) {
        label.setText(msg);
        label.setVisible(true);
    }

    private void clearAllFields() {
        txtStudentId.clear();
        txtUsername.clear();
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
            InputStream icon = getClass().getResourceAsStream("/com/hms/images/HMS.png");
            if (icon != null) stage.getIcons().add(new Image(icon));
            stage.show();
            ((Stage) linkBackToLogin.getScene().getWindow()).close();
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

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
