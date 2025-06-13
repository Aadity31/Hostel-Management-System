package com.hms.views.users.student;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.util.Duration;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

public class Contact implements Initializable {

    @FXML
    private Label txtPara;

    @FXML
    private Label txtLocation;

    // Optional: Add labels for date and time display if needed
    @FXML
    private Label dateLabel;

    @FXML
    private Label timeLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentDate();
        showTime();
        addText();
    }

    public static String now(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    public void currentDate() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd yyyy");
        String dateString = sdf.format(d);
        System.out.println("Current Date: " + dateString); // For debugging

        // If you have a date label in FXML, uncomment the line below:
        // if (dateLabel != null) dateLabel.setText(dateString);
    }

    public void showTime() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
            String time = sdf.format(d);
            System.out.println("Current Time: " + time); // For debugging

            // If you have a time label in FXML, uncomment the line below:
            // if (timeLabel != null) timeLabel.setText(time);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void addText() {
        String myText1 = "If you have any question or need further information, please feel free to contact us. "
                + "Our dedicated team is here to assist you with any inquiries related to our programs, admissions "
                + "and services. We look forward to helping you on your educational journey.";

        if (txtPara != null) {
            txtPara.setText(myText1);
        }

        String myText2 = "Graphic Era Hill University, Clementown, Dehradun, Uttarakhand";
        if (txtLocation != null) {
            txtLocation.setText(myText2);
        }
    }
}
