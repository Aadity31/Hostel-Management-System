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

public class Signup {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtComPassword;
    @FXML private TextField txtVisiblePassword;
    @FXML private TextField txtVisibleComPassword;
    @FXML private ComboBox<String> combUserType;
    @FXML private Button btnRegister;
    @FXML private ImageView backIcon;
    @FXML private ImageView showPasswordIcon;
    @FXML private ImageView hidePasswordIcon;
    @FXML private ImageView showComPasswordIcon;
    @FXML private ImageView hideComPasswordIcon;

    private Connection conn;
    private PreparedStatement pst;

    public void initialize() {
        ObservableList<String> userTypes = FXCollections.observableArrayList("Select", "Student");
        combUserType.setItems(userTypes);
        combUserType.setValue("Select");

        conn = DB.connect();

        hidePasswordIcon.setVisible(false);
    }

    @FXML
    private void handleRegister() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String comPassword = txtComPassword.getText();
        String userType = combUserType.getValue();

        if (username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please Enter Username");
            return;
        }

        if (!username.matches("^[a-zA-Z0-9]+$")) {
            showAlert(Alert.AlertType.ERROR, "Error", "Username can only contain letters and numbers");
            return;
        }

        if (password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please Enter Password");
            return;
        }

        if (!password.equals(comPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password Not Matched");
            return;
        }

        if (userType == null || userType.equals("Select")) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please Select Usertype");
            return;
        }

        try {
            pst = conn.prepareStatement("INSERT INTO user(Username,Password,User_type) VALUES(?,?,?)");
            pst.setString(1, username);
            pst.setString(2, password);
            pst.setString(3, userType);
            pst.executeUpdate();

            // Log the account creation
            logAccountCreation(username);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Account Created");

            // Clear fields
            txtUsername.clear();
            txtPassword.clear();
            txtComPassword.clear();
            combUserType.getSelectionModel().select(0);
            txtUsername.requestFocus();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void logAccountCreation(String username) throws SQLException {
        Date currentDate = GregorianCalendar.getInstance().getTime();
        DateFormat df = DateFormat.getDateInstance();
        String dateString = df.format(currentDate);

        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String timeString = sdf.format(d);

        String reg = "INSERT INTO logs (User_id, Date, Status) VALUES ((SELECT User_id FROM user WHERE Username = ?), ?, ?)";
        pst = conn.prepareStatement(reg);
        pst.setString(1, username);
        pst.setString(2, timeString + " / " + dateString);
        pst.setString(3, "New account created");
        pst.execute();
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

    @FXML
    private void handleShowComPassword() {
        txtVisibleComPassword.setText(txtComPassword.getText());
        txtComPassword.setVisible(false);
        txtComPassword.setManaged(false);

        txtVisibleComPassword.setVisible(true);
        txtVisibleComPassword.setManaged(true);

        txtVisibleComPassword.setFocusTraversable(false); // 🔐 Prevent focus
        txtVisibleComPassword.getParent().requestFocus();  // 🔐 Transfer focus

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