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

    @FXML
    private Label txtrooms;

    @FXML
    private Label txtstudent;

    @FXML
    private Label txtdate;

    @FXML
    private Label txttime;

    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;
    private Timeline timeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize database connection
        conn = DB.connect();

        // Initialize UI components
        currentDate();
        startTimeUpdate();
        totalRooms();
        totalStudent();
        totalStaff();
    }

    /**
     * Get current date/time with specified format
     */
    public static String now(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    /**
     * Set current date
     */
    public void currentDate() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd yyyy");
        txtdate.setText(sdf.format(d));
    }

    /**
     * Start timeline for updating time every second
     */
    public void startTimeUpdate() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
            txttime.setText(sdf.format(d));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Get total number of rooms from database
     */
    public void totalRooms() {
        try {
            pst = conn.prepareStatement("SELECT COUNT(*) AS RoomsCount FROM rooms");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int count = rs.getInt("RoomsCount");
                txtrooms.setText(String.valueOf(count));
            }
            rs.close();
            pst.close();
        } catch (SQLException ex) {
            System.err.println("Error fetching room count: " + ex.getMessage());
            txtrooms.setText("Error");
        }
    }

    /**
     * Get total number of students from database
     */
    public void totalStudent() {
        try {
            pst = conn.prepareStatement("SELECT COUNT(*) AS StudentCount FROM student");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int count = rs.getInt("StudentCount");
                txtstudent.setText(String.valueOf(count));
            }
            rs.close();
            pst.close();
        } catch (SQLException ex) {
            System.err.println("Error fetching student count: " + ex.getMessage());
            txtstudent.setText("Error");
        }
    }

    /**
     * Get total number of staff from database
     */
    public void totalStaff() {
        try {
            pst = conn.prepareStatement("SELECT COUNT(*) AS StaffCount FROM user WHERE User_type = 'Staff'");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int count = rs.getInt("StaffCount");
                // Note: There's no txtstaff label in the original code, so this is just for database query
                System.out.println("Total Staff: " + count);
            }
            rs.close();
            pst.close();
        } catch (SQLException ex) {
            System.err.println("Error fetching staff count: " + ex.getMessage());
        }
    }

    /**
     * Cleanup resources when controller is destroyed
     */
    public void cleanup() {
        if (timeline != null) {
            timeline.stop();
        }
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
            if (conn != null) conn.close();
        } catch (SQLException ex) {
            System.err.println("Error closing database resources: " + ex.getMessage());
        }
    }
}