package com.hms.views.users.admin;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

import com.hms.utils.DB;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class InnerDashboard implements Initializable {

    @FXML
    private Label txtrooms;

    @FXML
    private Label txtstudent;

    @FXML
    private Label txtstaff;

    @FXML
    private Label txtdate;

    @FXML
    private Label txttime;

    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;
    private Timeline timeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize database connection
        conn = DB.connect();

        // Initialize UI components
        currentDate();
        startClock();
        totalRooms();
        totalStudent();
        totalStaff();

        // Set initial time and date
        txtdate.setText(now("MMMM dd yyyy"));
        txttime.setText(now("hh:mm aaa"));
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

    public void startClock() {
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    Date d = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
                    txttime.setText(sdf.format(d));
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void stopClock() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    public void totalRooms() {
        try {
            pst = conn.prepareStatement("SELECT COUNT(*) AS RoomsCount FROM rooms");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int count = rs.getInt("RoomsCount");
                txtrooms.setText(String.valueOf(count));
            }
        } catch (SQLException ex) {
            showAlert("Database Error", "Error fetching total rooms: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException ex) {
                showAlert("Database Error", "Error closing resources: " + ex.getMessage());
            }
        }
    }

    public void totalStudent() {
        try {
            pst = conn.prepareStatement("SELECT COUNT(*) AS StudentCount FROM student");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int count = rs.getInt("StudentCount");
                txtstudent.setText(String.valueOf(count));
            }
        } catch (SQLException ex) {
            showAlert("Database Error", "Error fetching total students: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException ex) {
                showAlert("Database Error", "Error closing resources: " + ex.getMessage());
            }
        }
    }

    public void totalStaff() {
        try {
            pst = conn.prepareStatement("SELECT COUNT(*) AS StaffCount FROM user WHERE User_type = 'Staff'");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int count = rs.getInt("StaffCount");
                txtstaff.setText(String.valueOf(count));
            }
        } catch (SQLException ex) {
            showAlert("Database Error", "Error fetching total staff: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException ex) {
                showAlert("Database Error", "Error closing resources: " + ex.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to refresh all data
    public void refreshData() {
        totalRooms();
        totalStudent();
        totalStaff();
        currentDate();
    }

    // Cleanup method to be called when closing the dashboard
    public void cleanup() {
        stopClock();
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
            if (conn != null) conn.close();
        } catch (SQLException ex) {
            showAlert("Database Error", "Error closing database connection: " + ex.getMessage());
        }
    }
}
