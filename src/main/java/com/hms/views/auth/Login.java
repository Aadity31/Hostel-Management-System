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

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtVisiblePassword;
    @FXML private ComboBox<String> combUserType;
    @FXML private Button btnLogin;
    @FXML private Label signUpLabel;
    @FXML private ImageView showPasswordIcon;
    @FXML private ImageView hidePasswordIcon;

    private Connection conn;
    private PreparedStatement pst;
    private ResultSet rs;


    public void initialize(URL location, ResourceBundle resources) {
        // Set ComboBox items
        ObservableList<String> userTypes = FXCollections.observableArrayList("Select", "Admin", "Staff", "Student");
        combUserType.setItems(userTypes);
        combUserType.setValue("Select");  // set default selected value
        // Initialize DB connection
        conn = DB.connect();

        // Set up password toggle visibility
        hidePasswordIcon.setVisible(false);
    }


    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String userType = combUserType.getValue();

        if (username.isEmpty() || password.isEmpty() || userType == null || userType.equals("Select")) {
            showAlert(Alert.AlertType.ERROR, "Error", "One or more required fields are empty");
            return;
        }

        String sql = "SELECT User_id, Username, Password, User_type FROM user WHERE (BINARY Username = ? AND BINARY Password = ? AND User_type = ?)";

        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);
            pst.setString(3, userType);

            rs = pst.executeQuery();

            int count = 0;
            while (rs.next()) {
                int id = rs.getInt(1);
                String un = rs.getString(2);
                Emp.UserId = id;
                Emp.UserName = un;
                count++;
            }

            if (count == 1) {
                // Log successful login
                logLogin(Emp.UserId);

                // Open appropriate dashboard
                openDashboard(userType);

                // Close login window
                ((Stage) btnLogin.getScene().getWindow()).close();
            } else if (count > 1) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Duplicate Username or Password - Access denied");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Username, Password or Usertype is not correct");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void logLogin(int userId) throws SQLException {
        Date currentDate = GregorianCalendar.getInstance().getTime();
        DateFormat df = DateFormat.getDateInstance();
        String dateString = df.format(currentDate);

        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String timeString = sdf.format(d);

        String reg = "INSERT INTO logs (User_id, Date, Status) VALUES (?, ?, ?)";
        pst = conn.prepareStatement(reg);
        pst.setInt(1, userId);
        pst.setString(2, timeString + " / " + dateString);
        pst.setString(3, "Logged in");
        pst.execute();
    }

    private void openDashboard(String userType) {
        try {
            String fxmlPath = switch (userType) {
                case "Admin" -> "/com/hms/fxml/admin/dashboard.fxml";
                case "Staff" -> "/com/hms/fxml/staff/dashboard.fxml";
                case "Student" -> "/com/hms/fxml/student/dashboard.fxml";
                default -> "";
            };

            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(userType + " Dashboard");
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/hms/images/HMS.png"))));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignUp() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/hms/fxml/signup.fxml")));

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Sign Up");

            InputStream iconStream = getClass().getResourceAsStream("/com/hms/images/HMS.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            } else {
                System.err.println("⚠️ HMS.png icon not found at /com/hms/images/HMS.png");
            }

            stage.show();

            // Close login window
            ((Stage) signUpLabel.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleSignUpHover() {
        signUpLabel.setStyle("-fx-text-fill: #00e870;");
    }

    @FXML
    private void handleSignUpExit() {
        signUpLabel.setStyle("-fx-text-fill: #00a650;");
    }

    @FXML
    private void handleShowPassword() {
        txtVisiblePassword.setText(txtPassword.getText());
        txtPassword.setVisible(false);
        txtPassword.setManaged(false);

        txtVisiblePassword.setVisible(true);
        txtVisiblePassword.setManaged(true);

        txtVisiblePassword.setFocusTraversable(false); // 🔐 Prevent focus
        txtVisiblePassword.getParent().requestFocus();  // 🔐 Transfer focus

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

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}