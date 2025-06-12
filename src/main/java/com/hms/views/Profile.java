package com.hms.views;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import com.hms.utils.DB;
import com.hms.utils.Emp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class Profile implements Initializable {

    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;

    @FXML
    private TextField txtuserid;
    @FXML
    private TextField txtusername;
    @FXML
    private TextField txtpassword;
    @FXML
    private TextField txtusertype;
    @FXML
    private Button update_btn;
    @FXML
    private Button clear_btn;
    @FXML
    private Text username;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DB.connect();
        txtuserid.setText(String.valueOf(Emp.UserId));
        get_data();
        txtusername.requestFocus();
    }

    private void get_data() {
        String tmp = txtuserid.getText();
        String sql = "select * from user where User_id=?";

        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, tmp);
            rs = pst.executeQuery();

            if (rs.next()) {
                String add1 = rs.getString("User_id");
                txtuserid.setText(add1);

                String add2 = rs.getString("Username");
                txtusername.setText(add2);
                username.setText(add2);

                String add3 = rs.getString("Password");
                txtpassword.setText(add3);

                String add4 = rs.getString("User_type");
                txtusertype.setText(add4);
            }
        } catch (SQLException e) {
            showAlert("Database Error", e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }

    @FXML
    private void update_btnActionPerformed(ActionEvent evt) {
        if (!(txtuserid.getText().isEmpty() || txtusername.getText().isEmpty() ||
                txtpassword.getText().isEmpty() || txtusertype.getText().isEmpty())) {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Update Account");
            alert.setHeaderText("Are you sure you want to update?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    String value1 = txtuserid.getText();
                    String value2 = txtusername.getText();
                    String value3 = txtpassword.getText();
                    String value4 = txtusertype.getText();

                    String sql = "update user set User_id=?, Username=?, Password=?, User_type=? where User_id=?";
                    pst = conn.prepareStatement(sql);
                    pst.setString(1, value1);
                    pst.setString(2, value2);
                    pst.setString(3, value3);
                    pst.setString(4, value4);
                    pst.setString(5, value1);
                    pst.execute();

                    showAlert("Success", "Account Updated", Alert.AlertType.INFORMATION);

                    // Log the update
                    Date currentDate = new Date();
                    DateFormat df = DateFormat.getDateInstance();
                    String dateString = df.format(currentDate);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    String timeString = sdf.format(currentDate);

                    int value = Emp.UserId;
                    String reg = "insert into logs (User_id, Date, Status) values (?,?,?)";
                    pst = conn.prepareStatement(reg);
                    pst.setInt(1, value);
                    pst.setString(2, timeString + " / " + dateString);
                    pst.setString(3, "User Account Updated");
                    pst.execute();

                } catch (SQLException e) {
                    showAlert("Database Error", e.getMessage(), Alert.AlertType.ERROR);
                } finally {
                    try {
                        if (pst != null) pst.close();
                    } catch (SQLException e) {
                        // Ignore
                    }
                }
                get_data();
            }
        } else {
            showAlert("Error", "One or more required fields are empty", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clear_btnActionPerformed(ActionEvent evt) {
        txtusername.clear();
        txtpassword.clear();
        txtusertype.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
