package com.hms;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class HMS extends Application {

    public static Stage primaryStageGlobal;

    @Override
    public void start(Stage primaryStage) {
        primaryStageGlobal = primaryStage;

        try {
            // Load splash
            Stage splashStage = new Stage();
            splashStage.initStyle(StageStyle.UNDECORATED);
            Parent splashRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/hms/fxml/splash.fxml")));
            splashStage.setScene(new Scene(splashRoot));

            // ✅ Set icon for splash screen
            splashStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/hms/images/HMS.png"))));
            splashStage.show();

            // Load main UI
            Task<Parent> loadMainUI = new Task<>() {
                @Override
                protected Parent call() throws Exception {
                    Thread.sleep(2000); // simulate loading
                    return FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/hms/fxml/login.fxml")));
                }
            };

            loadMainUI.setOnSucceeded(event -> {
                Parent loginRoot = loadMainUI.getValue();
                Stage loginStage = new Stage();
                loginStage.setScene(new Scene(loginRoot));
                loginStage.setTitle("Login");

                // ✅ Set icon for main window
                loginStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/hms/images/HMS.png"))));

                loginStage.show();
                splashStage.close();
            });

            new Thread(loadMainUI).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(HMS.class.getResource(fxmlPath));
            Parent root = loader.load();
            primaryStageGlobal.setScene(new Scene(root));
            primaryStageGlobal.setTitle(title);
            primaryStageGlobal.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
