package com.hms.views.users.student;


import com.hms.utils.DB;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

public class Rules implements Initializable {

    @FXML
    private TextArea rulesTextArea;

    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;
    private Timeline timeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize database connection
        conn = DB.connect();

        // Set the rules text
        setRulesText();

        // Initialize date and time functions
        currentDate();
        showTime();
    }

    private void setRulesText() {
        String rulesText = " \n" +
                "  Hostel management rules aim to create a secure, comfortable, and respectful environment for residents, particularly students and workers.\n\n" +
                "  1. Entry and Exit Rules\n\n" +
                "   - Set a curfew for residents; late entry may require permission.\n" +
                "   - Allow visitors only during specific hours and in common areas.\n" +
                "   - Use entry cards for security at the gate.\n\n" +
                "  2. Room and Property Management\n\n" +
                "   - Maintain cleanliness in personal and shared spaces with regular inspections.\n" +
                "   - Prohibit alterations to room structures, such as painting or adding shelves.\n" +
                "   - Limit appliances in rooms to ensure safety and energy efficiency.\n\n" +
                "  3. Code of Conduct\n\n" +
                "   - Respect fellow residents' privacy and property; harassment is not tolerated.\n" +
                "   - Enforce a no-smoking and no-alcohol rule in designated areas.\n" +
                "   - Implement noise restrictions, especially during study hours and at night.\n\n" +
                "  4. Safety and Security\n\n" +
                "   - Prohibit weapons, flammable materials, and narcotics.\n" +
                "   - Outline fire safety procedures and provide emergency contact numbers.\n" +
                "   - Report suspicious activities to management immediately.\n\n" +
                "  5. Penalties and Enforcement\n\n" +
                "   - Impose fines, warnings, or expulsion for rule violations.\n" +
                "   - Hold regular meetings to keep residents informed about rules.\n\n" +
                "  While rules differ across institutions, their goal remains the same: to foster a safe and organized living environment for everyone.\n";

        rulesTextArea.setText(rulesText);
    }

    public static String now(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    public void currentDate() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd yyyy");
        // You can use this formatted date as needed
        String formattedDate = sdf.format(d);
    }

    public void showTime() {
        timeline = new Timeline(new KeyFrame(Duration.millis(1000), e -> {
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
            // You can use this formatted time as needed
            String formattedTime = sdf.format(d);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // Clean up resources when the controller is destroyed
    public void cleanup() {
        if (timeline != null) {
            timeline.stop();
        }
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
            if (conn != null) conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
