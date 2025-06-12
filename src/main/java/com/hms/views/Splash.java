package com.hms.views;

import com.hms.HMS;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * JavaFX controller for <strong>Splash.fxml</strong>.
 * <p>
 * Replicates the Swing behaviour of showing a logo, version label and a
 * percentage counter that ticks from <code>0&nbsp;%</code> to <code>100&nbsp;%</code>
 * before handing control to the Login scene.
 */
public class Splash implements Initializable {


    @FXML
    private ImageView logoImage;

    @FXML
    private Label versionLabel;

    @FXML
    private Label progressLabel; // shows "0% .. 100%"

    private static final int DURATION_MS = 40;  // match original Thread.sleep(40)

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        versionLabel.setText("V1.0");
        startFakeLoader();
    }

    /**
     * Simulates loading work by updating <code>progressLabel</code> every
     * {@value #DURATION_MS} ms until 100&nbsp;% is reached, then switches to the
     * Login view.
     */
    private void startFakeLoader() {
        Timeline tl = new Timeline();
        tl.setCycleCount(101); // 0..100 inclusive
        tl.getKeyFrames().add(
                new KeyFrame(Duration.millis(DURATION_MS), e -> {
                    int value = Integer.parseInt(progressLabel.getText().replace("%", ""));
                    if (value < 100) {
                        progressLabel.setText((value + 1) + "%");
                    } else {
                        tl.stop();
                        HMS.switchScene("/com/hms/fxml/login.fxml", "Login");
                    }
                })
        );
        tl.play();
    }
}
