package com.hms.views.users.staff;

import com.hms.utils.DB;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

public class InnerDashboard implements Initializable {

    @FXML private Label txtrooms;
    @FXML private Label txtstudent;
    @FXML private Label txtstaff;   // Add this only if you're showing staff count
    @FXML private Label txtdate;
    @FXML private Label txttime;

    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;
    private Timeline timeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conn = DB.connect();

        currentDate();
        startTimeUpdate();
        totalRooms();
        totalStudent();
        totalStaff();
    }

    public static String now(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    public void currentDate() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd yyyy");
        txtdate.setText(sdf.format(d));
    }

    public void startTimeUpdate() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
            txttime.setText(sdf.format(d));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void totalRooms() {
        try {
            pst = conn.prepareStatement("SELECT COUNT(*) AS RoomsCount FROM rooms");
            rs = pst.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("RoomsCount");
                txtrooms.setText(String.valueOf(count));
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching room count: " + ex.getMessage());
            txtrooms.setText("Error");
        } finally {
            closeResultSetAndStatement();
        }
    }

    public void totalStudent() {
        try {
            pst = conn.prepareStatement("SELECT COUNT(*) AS StudentCount FROM student");
            rs = pst.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("StudentCount");
                txtstudent.setText(String.valueOf(count));
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching student count: " + ex.getMessage());
            txtstudent.setText("Error");
        } finally {
            closeResultSetAndStatement();
        }
    }

    public void totalStaff() {
        try {
            pst = conn.prepareStatement("SELECT COUNT(*) AS StaffCount FROM staff");  // ✅ Use staff table
            rs = pst.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("StaffCount");
                if (txtstaff != null) {
                    txtstaff.setText(String.valueOf(count));
                } else {
                    System.out.println("Total Staff: " + count);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching staff count: " + ex.getMessage());
            if (txtstaff != null) txtstaff.setText("Error");
        } finally {
            closeResultSetAndStatement();
        }
    }

    private void closeResultSetAndStatement() {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (SQLException ex) {
            System.err.println("Error closing resources: " + ex.getMessage());
        }
    }

    public void cleanup() {
        if (timeline != null) timeline.stop();
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException ex) {
            System.err.println("Error closing connection: " + ex.getMessage());
        }
    }
}
