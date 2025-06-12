package com.hms.views.users.admin;

import com.hms.utils.DB;
import com.hms.utils.Emp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.ResourceBundle;

public class ManageStaff implements Initializable {

    @FXML private TextField txtuserid;
    @FXML private TextField txtusername;
    @FXML private TextField txtpassword;
    @FXML private ComboBox<String> txtusertype;
    @FXML private Button add_btn;
    @FXML private Button update_btn;
    @FXML private Button delete_btn;
    @FXML private Button clear_btn;
    @FXML private TableView<StaffRecord> staff_table;
    @FXML private TableColumn<StaffRecord, Integer> colUserId;
    @FXML private TableColumn<StaffRecord, String> colUsername;
    @FXML private TableColumn<StaffRecord, String> colPassword;
    @FXML private TableColumn<StaffRecord, String> colUserType;

    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;
    private ObservableList<StaffRecord> staffData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize database connection
        conn = DB.connect();

        // Setup table columns
        setupTableColumns();

        ObservableList<String> userTypes = FXCollections.observableArrayList("Select", "Staff");
        txtusertype.setItems(userTypes);
        // Initialize ComboBox
        txtusertype.setValue("Select");

        // Load initial data
        updateTable();

        // Set focus to username field
        txtusername.requestFocus();

        // Disable user ID field
        txtuserid.setDisable(true);
    }

    private void setupTableColumns() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        colUserType.setCellValueFactory(new PropertyValueFactory<>("userType"));

        staff_table.setItems(staffData);
    }

    private void updateTable() {
        staffData.clear();
        try {
            String sql = "SELECT * FROM user WHERE User_type = 'Staff'";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            while (rs.next()) {
                StaffRecord record = new StaffRecord(
                        rs.getInt("User_id"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("User_type")
                );
                staffData.add(record);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", e.getMessage());
        } finally {
            closeResources();
        }
    }

    @FXML
    private void addStaff() {
        if (validateFields()) {
            try {
                String sql = "INSERT INTO user (Username, Password, User_type) VALUES (?, ?, ?)";
                pst = conn.prepareStatement(sql);
                pst.setString(1, txtusername.getText());
                pst.setString(2, txtpassword.getText());
                pst.setString(3, txtusertype.getValue());

                pst.execute();
                showInfoAlert("New Staff Account Created");

                // Log the action
                logAction("New Staff Account Created");

                updateTable();
                clearFields();

            } catch (SQLException e) {
                showErrorAlert("Database Error", e.getMessage());
            } finally {
                closeResources();
            }
        } else {
            showErrorAlert("Validation Error", "One or more required fields are empty");
        }
    }

    @FXML
    private void updateStaff() {
        if (validateAllFields()) {
            Optional<ButtonType> result = showConfirmationAlert("Update Record",
                    "Are you sure you want to update this record?");

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    String sql = "UPDATE user SET Username=?, Password=?, User_type=? WHERE User_id=?";
                    pst = conn.prepareStatement(sql);
                    pst.setString(1, txtusername.getText());
                    pst.setString(2, txtpassword.getText());
                    pst.setString(3, txtusertype.getValue());
                    pst.setString(4, txtuserid.getText());

                    pst.execute();
                    showInfoAlert("Account Updated");

                    // Log the action
                    logAction("Staff Details Updated");

                    updateTable();

                } catch (SQLException e) {
                    showErrorAlert("Database Error", e.getMessage());
                } finally {
                    closeResources();
                }
            }
        } else {
            showErrorAlert("Validation Error", "One or more required fields are empty");
        }
    }

    @FXML
    private void deleteStaff() {
        if (validateAllFields()) {
            Optional<ButtonType> result = showConfirmationAlert("Delete Account",
                    "Are you sure you want to delete this account?");

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // Log the action first
                    logAction("Staff Account Removed");

                    String sql = "DELETE FROM user WHERE User_id=?";
                    pst = conn.prepareStatement(sql);
                    pst.setString(1, txtuserid.getText());
                    pst.execute();

                    showInfoAlert("Account Removed");
                    updateTable();
                    clearFields();

                } catch (SQLException e) {
                    showErrorAlert("Database Error", e.getMessage());
                } finally {
                    closeResources();
                }
            }
        } else {
            showErrorAlert("Validation Error", "One or more required fields are empty");
        }
    }

    @FXML
    private void clearFields() {
        txtuserid.clear();
        txtusername.clear();
        txtpassword.clear();
        txtusertype.setValue("Select");
        txtusername.requestFocus();
    }

    @FXML
    private void handleTableClick(MouseEvent event) {
        StaffRecord selectedRecord = staff_table.getSelectionModel().getSelectedItem();
        if (selectedRecord != null) {
            txtuserid.setText(String.valueOf(selectedRecord.getUserId()));
            txtusername.setText(selectedRecord.getUsername());
            txtpassword.setText(selectedRecord.getPassword());
            txtusertype.setValue(selectedRecord.getUserType());
        }
    }

    private boolean validateFields() {
        return !txtusertype.getValue().equals("Select") &&
                !txtusername.getText().trim().isEmpty() &&
                !txtpassword.getText().trim().isEmpty();
    }

    private boolean validateAllFields() {
        return validateFields() && !txtuserid.getText().trim().isEmpty();
    }

    private void logAction(String action) {
        try {
            Date currentDate = GregorianCalendar.getInstance().getTime();
            DateFormat df = DateFormat.getDateInstance();
            String dateString = df.format(currentDate);

            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String timeString = sdf.format(d);

            String timestamp = timeString + " / " + dateString;
            int userId = Emp.UserId;

            String sql = "INSERT INTO logs (User_id, Date, Status) VALUES (?, ?, ?)";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, userId);
            pst.setString(2, timestamp);
            pst.setString(3, action);
            pst.execute();

        } catch (SQLException e) {
            showErrorAlert("Logging Error", e.getMessage());
        }
    }

    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (SQLException e) {
            showErrorAlert("Resource Error", e.getMessage());
        }
    }

    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Optional<ButtonType> showConfirmationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    // Inner class for table data
    public static class StaffRecord {
        private int userId;
        private String username;
        private String password;
        private String userType;

        public StaffRecord(int userId, String username, String password, String userType) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.userType = userType;
        }

        // Getters and setters
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getUserType() { return userType; }
        public void setUserType(String userType) { this.userType = userType; }
    }
}