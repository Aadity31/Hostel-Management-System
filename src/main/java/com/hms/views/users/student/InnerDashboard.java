package com.hms.views.users.student;

import com.hms.utils.DB;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
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

        // Initialize dashboard data
        currentDate();
        startTimeUpdater();
        totalRooms();
        totalStudent();

        // Set initial date and time
        txtdate.setText(now("MMMM dd yyyy"));
        txttime.setText(now("hh:mm a"));
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

    public void startTimeUpdater() {
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
            pst = conn.prepareStatement("select count(*) as RoomsCount from rooms");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int count = rs.getInt("RoomsCount");
                txtrooms.setText(String.valueOf(count));
            }
        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage());
        }
    }

    public void totalStudent() {
        try {
            pst = conn.prepareStatement("select count(*) as StudentCount from student");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int count = rs.getInt("StudentCount");
                txtstudent.setText(String.valueOf(count));
            }
        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to stop the timeline when the controller is no longer needed
    public void cleanup() {
        if (timeline != null) {
            timeline.stop();
        }
    }
}