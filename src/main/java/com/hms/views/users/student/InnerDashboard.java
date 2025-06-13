package com.hms.views.users.student;

import com.hms.utils.DB;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class InnerDashboard implements Initializable {

    @FXML private Label txtrooms, txtstudent, txtdate, txttime;
    @FXML private Button hostelListBtn, applyBtn, requestsBtn, changeRoomBtn;

    private Connection conn = null;
    private Timeline timeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conn = DB.connect();

        // Set current date and start time updater
        setCurrentDate();
        startTimeUpdater();

        // Fetch dashboard stats
        totalRooms();
        totalStudent();

        // Button actions (examples)
        hostelListBtn.setOnAction(e -> showAlert("Hostel List", "Show hostel list here."));
        applyBtn.setOnAction(e -> showAlert("Apply", "Hostel application form here."));
        requestsBtn.setOnAction(e -> showAlert("Requests", "Show your hostel/room requests here."));
        changeRoomBtn.setOnAction(e -> showAlert("Change Room", "Change room functionality here."));
    }

    private void setCurrentDate() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd yyyy");
        txtdate.setText(sdf.format(d));
    }

    private void startTimeUpdater() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
            txttime.setText(sdf.format(d));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void totalRooms() {
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = conn.prepareStatement("select count(*) as RoomsCount from rooms");
            rs = pst.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("RoomsCount");
                txtrooms.setText(String.valueOf(count));
            }
        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (pst != null) pst.close(); } catch (SQLException ignored) {}
        }
    }

    private void totalStudent() {
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = conn.prepareStatement("select count(*) as StudentCount from student");
            rs = pst.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("StudentCount");
                txtstudent.setText(String.valueOf(count));
            }
        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (pst != null) pst.close(); } catch (SQLException ignored) {}
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Call this when closing the dashboard to avoid memory leaks
    public void cleanup() {
        if (timeline != null) {
            timeline.stop();
        }
        try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
    }
}