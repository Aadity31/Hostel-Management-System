package com.hms.views;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

import com.hms.utils.DB;
import com.hms.views.auth.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;

public class Profile implements Initializable {

    /* ────── DB handles ────── */
    private Connection conn;

    /* ────── FXML controls ────── */
    @FXML private TextField txtuserid;
    @FXML private TextField txtusername;
    @FXML private TextField txtusertype;

    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtMobile;
    @FXML private TextField txtGender;
    @FXML private TextField txtProgram;
    @FXML private TextField txtRoomNo;

    @FXML private Text        username;
    @FXML private Button      update_btn;
    @FXML private Button      clear_btn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DB.connect();

        int sid = Session.getLoggedInStudentId();
        if (sid <= 0) {
            showAlert("Not logged in", "Please log in again.", Alert.AlertType.ERROR);
            return;
        }

        txtuserid.setEditable(false);
        txtusername.setEditable(false);
        txtusertype.setEditable(false);
        txtFirstName.setEditable(false);
        txtLastName.setEditable(false);
        txtEmail.setEditable(false);
        txtMobile.setEditable(false);
        txtGender.setEditable(false);
        txtProgram.setEditable(false);
        txtRoomNo.setEditable(false);

        txtusertype.setText("Student");
        loadProfile(sid);
    }

    private void loadProfile(int sid) {
        String sql = """
            SELECT student_id, username,
                   first_name, last_name, email,
                   mobile_number, gender, study_program,
                   COALESCE(room_no,'') AS room_no
            FROM   student
            WHERE  student_id = ?
        """;

        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, sid);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    txtuserid   .setText(r.getString("student_id"));
                    txtusername .setText(r.getString("username"));
                    username    .setText(r.getString("username"));

                    txtFirstName.setText(r.getString("first_name"));
                    txtLastName .setText(r.getString("last_name"));
                    txtEmail    .setText(r.getString("email"));
                    txtMobile   .setText(r.getString("mobile_number"));
                    txtGender   .setText(r.getString("gender"));
                    txtProgram  .setText(r.getString("study_program"));
                    txtRoomNo   .setText(r.getString("room_no"));
                }
            }
        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void update_btnActionPerformed() {
        showAlert("Not allowed", "Profile is read-only.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void clear_btnActionPerformed() {
        showAlert("Not allowed", "Profile is read-only.", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
