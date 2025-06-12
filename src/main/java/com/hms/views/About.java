package com.hms.views;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.Objects;

public class About {

    @FXML
    private Label txt_details;

    @FXML
    private Label txt_dev;

    @FXML
    private ImageView aboutImageView;

    private Stage stage;

    @FXML
    public void initialize() {
        // Set the window icon (would be set in the main class)

        // Add text
        String myText1 = "Hostel Management System is designed to manage all hostel activities like hostel admissions, fees, room, "
                + "and generates related reports for smooth transactions.";
        txt_details.setText(myText1);

        String myText2 = "Aditya Dimri, Priyanshi Bhatt, Pranjali Rathore, Shwetanshu Bhatt";
        txt_dev.setText(myText2);

        // Set the about image
        aboutImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/hms/images/about_img.png"))));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleMouseClicked() {
        stage.close();
    }
}