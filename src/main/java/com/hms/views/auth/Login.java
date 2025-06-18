package com.hms.views.auth;

import com.hms.utils.DB;
import com.hms.utils.Emp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Login screen controller.
 * Each user‑type (Admin / Staff / Student) is authenticated
 * against its own table:  admin, staff, student.
 * Those tables contain only two columns: username VARCHAR, password VARCHAR.
 */
public class Login implements Initializable {

    /* ───────── UI ───────── */
    @FXML private TextField        txtUsername;
    @FXML private PasswordField    txtPassword;          // hidden input
    @FXML private TextField        txtVisiblePassword;   // visible when eye‑open
    @FXML private ComboBox<String> combUserType;
    @FXML private Button           btnLogin;
    @FXML private Label            signUpLabel;
    @FXML private ImageView        showPasswordIcon;
    @FXML private ImageView        hidePasswordIcon;

    /* ───────── DB ───────── */
    private Connection conn;

    /* ───────── INITIALISE ───────── */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        combUserType.setItems(
                FXCollections.observableArrayList("Select", "Admin", "Staff", "Student"));
        combUserType.setValue("Select");

        conn = DB.connect();

        hidePasswordIcon.setVisible(false);          // start with hidden “eye”

        // keep visible + hidden password fields in sync
        txtVisiblePassword.textProperty()
                          .bindBidirectional(txtPassword.textProperty());
    }

    /* ───────── LOGIN ───────── */
    @FXML
    private void handleLogin() {

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();     // same text as visible field
        String userType = combUserType.getValue();

        if (username.isEmpty() || password.isEmpty() || "Select".equals(userType)) {
            showAlert(Alert.AlertType.ERROR, "Error",
                      "Username, password or user‑type is empty.");
            return;
        }

        // pick the credential table
        String table = switch (userType) {
            case "Admin"   -> "admin";
            case "Staff"   -> "staff";
            case "Student" -> "student";
            default        -> null;
        };
        if (table == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid user type."); return;
        }

        String sql = "SELECT username FROM " + table +
                     " WHERE BINARY username=? AND BINARY password=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, username);
            pst.setString(2, password);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {                      // ✅ credentials valid
                    Emp.UserId   = 0;                // no numeric id in this schema
                    Emp.UserName = username;

                    logLogin(username);
                    openDashboard(userType);
                    ((Stage) btnLogin.getScene().getWindow()).close();
                } else {
                    showAlert(Alert.AlertType.ERROR,
                              "Login Failed", "Username or password incorrect.");
                }
            }

        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR,
                      "Database Error", ex.getMessage());
        }
    }

    /* ───────── LOG ACTIVITY ───────── */
    private void logLogin(String userName) {

        Date now = new Date();
        String date = DateFormat.getDateInstance().format(now);
        String time = new SimpleDateFormat("HH:mm:ss").format(now);

        String sql = "INSERT INTO logs (User_name, Date, Status) VALUES (?, ?, ?)";

        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, userName);
            p.setString(2, time + " / " + date);
            p.setString(3, "Logged in");
            p.execute();
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Logging Error", ex.getMessage());
        }
    }

    /* ───────── OPEN DASHBOARD ───────── */
    private void openDashboard(String userType) {
        try {
            String fxmlPath = switch (userType) {
                case "Admin"   -> "/com/hms/fxml/admin/dashboard.fxml";
                case "Staff"   -> "/com/hms/fxml/staff/dashboard.fxml";
                case "Student" -> "/com/hms/fxml/student/dashboard.fxml";
                default        -> "";
            };
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource(fxmlPath)));

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(userType + " Dashboard");

            InputStream icon = getClass().getResourceAsStream("/com/hms/images/HMS.png");
            if (icon != null) stage.getIcons().add(new Image(icon));

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ───────── SIGN‑UP / LINK EFFECTS ───────── */
    @FXML
    private void handleSignUp() {
        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource(
                            "/com/hms/fxml/signup.fxml")));

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Sign Up");

            InputStream icon = getClass().getResourceAsStream("/com/hms/images/HMS.png");
            if (icon != null) stage.getIcons().add(new Image(icon));

            stage.show();
            ((Stage) signUpLabel.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML private void handleSignUpHover() { signUpLabel.setStyle("-fx-text-fill:#00e870;"); }
    @FXML private void handleSignUpExit () { signUpLabel.setStyle("-fx-text-fill:#00a650;"); }

    /* ───────── PASSWORD EYE ICON ───────── */
    @FXML
    private void handleShowPassword() {
        txtPassword.setVisible(false);
        txtPassword.setManaged(false);

        txtVisiblePassword.setVisible(true);
        txtVisiblePassword.setManaged(true);

        showPasswordIcon.setVisible(false);
        hidePasswordIcon.setVisible(true);
    }
    @FXML
    private void handleHidePassword() {
        txtVisiblePassword.setVisible(false);
        txtVisiblePassword.setManaged(false);

        txtPassword.setVisible(true);
        txtPassword.setManaged(true);

        hidePasswordIcon.setVisible(false);
        showPasswordIcon.setVisible(true);
    }

    /* ───────── ALERT UTIL ───────── */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
