package com.hms.views.users.student;


import com.hms.utils.DB;
import com.hms.utils.Emp;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.event.ActionEvent;

import java.awt.Toolkit;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Student implements Initializable {

    // FXML Controls
    @FXML private TextField txtname;
    @FXML private TextField txtaddress;
    @FXML private ComboBox<String> txtgencom;
    @FXML private TextField txtguardian;
    @FXML private TextField txtstuid;
    @FXML private TextField txtstuyear;
    @FXML private TextField txtnic;
    @FXML private TextField txtcontact;
    @FXML private TextField txtemail;
    @FXML private TextField txtemgcontact;
    @FXML private TextField txtprogramme;
    @FXML private ComboBox<String> txtroomcom;
    @FXML private Button add_btn;
    @FXML private Button clear_btn;

    // Database connection
    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;

    // Email validation pattern
    private static final String EMAIL_PATTERN
            = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize database connection
        conn = DB.connect();

        // Set focus to name field
        Platform.runLater(() -> txtname.requestFocus());

        // Initialize combo boxes
        setupComboBoxes();

        // Fill room combo box with available rooms
        fillRoomComboBox();

        // Start time display (if needed)
        showTime();
    }

    private void setupComboBoxes() {
        // Setup gender combo box
        txtgencom.setItems(FXCollections.observableArrayList("Select", "Male", "Female"));
        txtgencom.getSelectionModel().selectFirst();

        // Setup room combo box with default value
        txtroomcom.setItems(FXCollections.observableArrayList("Select"));
        txtroomcom.getSelectionModel().selectFirst();
    }

    private void fillRoomComboBox() {
        try {
            String sql = "select * from rooms where Room_status='Available'";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            while (rs.next()) {
                String roomno = rs.getString("Room_id");
                txtroomcom.getItems().add(roomno);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            }
        }
    }

    private void showTime() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
                // You can update a time label here if needed
            }
        }, 0, 1000);
    }

    @FXML
    private void clear_btnActionPerformed(ActionEvent event) {
        txtname.clear();
        txtaddress.clear();
        txtcontact.clear();
        txtemail.clear();
        txtemgcontact.clear();
        txtguardian.clear();
        txtnic.clear();
        txtprogramme.clear();
        txtstuid.clear();
        txtstuyear.clear();
        txtgencom.getSelectionModel().selectFirst();
        txtroomcom.getSelectionModel().selectFirst();
    }

    @FXML
    private void add_btnActionPerformed(ActionEvent event) {
        if (validateFields()) {
            if (txtcontact.getText().length() == 10 && txtemgcontact.getText().length() == 10) {
                try {
                    String sql = "insert into student"
                            + "(Name,Address,Gender,Guardian,Student_id,Study_year,NIC,Contact_no,Email,Emg_contact,Programme,Room_no) "
                            + "values (?,?,?,?,?,?,?,?,?,?,?,?)";

                    pst = conn.prepareStatement(sql);
                    pst.setString(1, txtname.getText());
                    pst.setString(2, txtaddress.getText());
                    pst.setString(3, txtgencom.getSelectionModel().getSelectedItem());
                    pst.setString(4, txtguardian.getText());
                    pst.setString(5, txtstuid.getText());
                    pst.setString(6, txtstuyear.getText());
                    pst.setString(7, txtnic.getText());
                    pst.setString(8, txtcontact.getText());
                    pst.setString(9, txtemail.getText());
                    pst.setString(10, txtemgcontact.getText());
                    pst.setString(11, txtprogramme.getText());
                    pst.setString(12, txtroomcom.getSelectionModel().getSelectedItem());

                    pst.execute();

                    // Play system beep (cross-platform alternative)
                    try {
                        Toolkit.getDefaultToolkit().beep();
                    } catch (Exception e) {
                        // Ignore if beep is not available
                    }

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Data is saved successfully");

                    // Log the activity
                    logActivity();

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
                }
            } else {
                try {
                    Toolkit.getDefaultToolkit().beep();
                } catch (Exception e) {
                    // Ignore if beep is not available
                }
                showAlert(Alert.AlertType.ERROR, "Error", "Enter valid contact number");
            }
        } else {
            try {
                Toolkit.getDefaultToolkit().beep();
            } catch (Exception e) {
                // Ignore if beep is not available
            }
            showAlert(Alert.AlertType.ERROR, "Error", "One or more required fields are empty");
        }
    }

    private boolean validateFields() {
        return !(txtname.getText().isEmpty() || txtaddress.getText().isEmpty() ||
                txtcontact.getText().isEmpty() || txtguardian.getText().isEmpty() ||
                txtemgcontact.getText().isEmpty() || txtnic.getText().isEmpty() ||
                txtprogramme.getText().isEmpty() || txtstuid.getText().isEmpty() ||
                txtstuyear.getText().isEmpty() || txtemail.getText().isEmpty() ||
                txtgencom.getSelectionModel().getSelectedItem().equals("Select") ||
                txtroomcom.getSelectionModel().getSelectedItem().equals("Select"));
    }

    private void logActivity() {
        Date currentDate = GregorianCalendar.getInstance().getTime();
        DateFormat df = DateFormat.getDateInstance();
        String dateString = df.format(currentDate);

        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String timeString = sdf.format(d);

        String value0 = timeString;
        String value1 = dateString;
        int value = Emp.UserId;

        try {
            String reg = "insert into logs (User_id, Date, Status) values ('" + value + "','" + value0 + " / " + value1 + "','New Student Added')";
            pst = conn.prepareStatement(reg);
            pst.execute();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            }
        }
    }

    @FXML
    private void txtcontactKeyReleased(KeyEvent event) {
        try {
            if (!txtcontact.getText().isEmpty()) {
                Integer.parseInt(txtcontact.getText());
            }
        } catch (NumberFormatException e) {
            try {
                Toolkit.getDefaultToolkit().beep();
            } catch (Exception ex) {
                // Ignore if beep is not available
            }
            txtcontact.clear();
            showAlert(Alert.AlertType.ERROR, "Error", "You can add numbers only");
        }
    }

    @FXML
    private void txtemgcontactKeyReleased(KeyEvent event) {
        try {
            if (!txtemgcontact.getText().isEmpty()) {
                Integer.parseInt(txtemgcontact.getText());
            }
        } catch (NumberFormatException e) {
            try {
                Toolkit.getDefaultToolkit().beep();
            } catch (Exception ex) {
                // Ignore if beep is not available
            }
            txtemgcontact.clear();
            showAlert(Alert.AlertType.ERROR, "Error", "You can add numbers only");
        }
    }

    @FXML
    private void txtemailFocusLost(ActionEvent event) {
        String email = txtemail.getText();
        if (!email.isEmpty()) {
            Matcher matcher = pattern.matcher(email);
            if (!matcher.matches()) {
                try {
                    Toolkit.getDefaultToolkit().beep();
                } catch (Exception e) {
                    // Ignore if beep is not available
                }
                txtemail.clear();
                showAlert(Alert.AlertType.ERROR, "Error", "Enter valid email");
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}