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

public class Login implements Initializable {

    @FXML private TextField      txtUsername;
    @FXML private PasswordField  txtPassword;        // hidden field
    @FXML private TextField      txtVisiblePassword; // visible field
    @FXML private ComboBox<String> combUserType;
    @FXML private Button         btnLogin;
    @FXML private Label          signUpLabel;
    @FXML private ImageView      showPasswordIcon;
    @FXML private ImageView      hidePasswordIcon;

    private Connection      conn;
    private PreparedStatement pst;
    private ResultSet       rs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // 1. populate user‑type combo
        ObservableList<String> userTypes =
                FXCollections.observableArrayList("Select", "Admin", "Staff", "Student");
        combUserType.setItems(userTypes);
        combUserType.setValue("Select");

        // 2. open DB connection
        conn = DB.connect();

        // 3. start with eye‑closed icon
        hidePasswordIcon.setVisible(false);

        /* ------------------------------------------------------------------
            KEY FIX: keep both password text fields in sync.
            Binding them bidirectionally means typing in either control
            updates the other automatically, so handleLogin() can read from
            txtPassword regardless of which control was visible.
           ------------------------------------------------------------------ */
        txtVisiblePassword.textProperty()
                          .bindBidirectional(txtPassword.textProperty());
    }

    /* ----------------------------- LOGIN --------------------------------- */
    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();  // safe: always in sync now
        String userType = combUserType.getValue();

        if (username.isEmpty() || password.isEmpty()
                || userType == null || userType.equals("Select")) {
            showAlert(Alert.AlertType.ERROR, "Error",
                      "One or more required fields are empty");
            return;
        }

        String sql = """
                     SELECT User_id, Username, Password, User_type
                     FROM user
                     WHERE (BINARY Username = ? AND BINARY Password = ? AND User_type = ?)
                     """;

        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);
            pst.setString(3, userType);

            rs   = pst.executeQuery();
            int count = 0;

            while (rs.next()) {
                Emp.UserId   = rs.getInt(1);
                Emp.UserName = rs.getString(2);
                count++;
            }

            if (count == 1) {
                logLogin(Emp.UserId);   // write to logs
                openDashboard(userType);
                ((Stage) btnLogin.getScene().getWindow()).close();

            } else if (count > 1) {
                showAlert(Alert.AlertType.WARNING, "Warning",
                          "Duplicate Username or Password - Access denied");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                          "Username, Password or Usertype is not correct");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        } finally {
            try { if (rs  != null) rs.close(); }
            catch (SQLException ignored) {}
            try { if (pst != null) pst.close(); }
            catch (SQLException ignored) {}
        }
    }

    /* --------------------------- LOG HISTORY ----------------------------- */
    private void logLogin(int userId) throws SQLException {
        Date currentDate   = GregorianCalendar.getInstance().getTime();
        DateFormat df      = DateFormat.getDateInstance();
        String dateString  = df.format(currentDate);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String timeString    = sdf.format(new Date());

        String reg = "INSERT INTO logs (User_id, Date, Status) VALUES (?, ?, ?)";
        pst = conn.prepareStatement(reg);
        pst.setInt   (1, userId);
        pst.setString(2, timeString + " / " + dateString);
        pst.setString(3, "Logged in");
        pst.execute();
    }

    /* ------------------------- OPEN DASHBOARD ---------------------------- */
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
            stage.getIcons().add(new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream(
                            "/com/hms/images/HMS.png"))));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* -------------------------- SIGN‑UP PAGE ----------------------------- */
    @FXML
    private void handleSignUp() {
        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource(
                            "/com/hms/fxml/signup.fxml")));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Sign Up");

            InputStream iconStream =
                    getClass().getResourceAsStream("/com/hms/images/HMS.png");
            if (iconStream != null) stage.getIcons().add(new Image(iconStream));
            else System.err.println("⚠️ HMS.png icon not found");

            stage.show();
            ((Stage) signUpLabel.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleSignUpHover() { signUpLabel.setStyle("-fx-text-fill: #00e870;"); }
    @FXML private void handleSignUpExit () { signUpLabel.setStyle("-fx-text-fill: #00a650;"); }

    /* -------------------- PASSWORD TOGGLE BUTTONS ------------------------ */
    @FXML
    private void handleShowPassword() {
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
        txtVisiblePassword.setVisible(false);
        txtVisiblePassword.setManaged(false);

        txtPassword.setVisible(true);
        txtPassword.setManaged(true);

        hidePasswordIcon.setVisible(false);
        hidePasswordIcon.setManaged(false);

        showPasswordIcon.setVisible(true);
        showPasswordIcon.setManaged(true);
    }

    /* ------------------------------ ALERT -------------------------------- */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
