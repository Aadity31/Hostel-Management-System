package com.hms.views.users.student;


import com.hms.utils.DB;
import com.hms.utils.Emp;
import com.hms.views.About;
import com.hms.views.Profile;
import com.hms.views.auth.Login;
import com.hms.views.users.staff.InnerDashboard;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.formula.functions.DVar;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.ResourceBundle;

public class Dashboard extends Application implements Initializable {

    @FXML private Button btnProfile;
    @FXML private Button btnLogout;
    @FXML private StackPane contentArea;
    @FXML private HBox btn1, btn2, btn3, btn4, btn5, btn6;
    @FXML private Region ind1, ind2, ind3, ind4, ind5, ind6;

    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize database connection
        conn = DB.connect();

        // Set initial state - Dashboard selected
        setActiveButton(btn1, ind1);

        // Load initial dashboard content
        loadDashboardContent();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/student/dashboard.fxml"));
//        loader.setController(this);
        Parent root = loader.load();

        Scene scene = new Scene(root, 1300, 660);

        primaryStage.setTitle("HMS Dashboard");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Center the window
        primaryStage.centerOnScreen();
    }

    @FXML
    private void handleProfileAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/profile.fxml"));
            Parent profileRoot = loader.load();

            Stage profileStage = new Stage();
            profileStage.setTitle("Profile");
            profileStage.setScene(new Scene(profileRoot));
            profileStage.initModality(Modality.APPLICATION_MODAL); // Optional: makes it modal
            profileStage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not open profile: " + e.getMessage());
        }
    }


    @FXML
    private void handleLogoutAction() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Log the logout activity
                logActivity("Logged out");

                // Close current window and open login
                Stage currentStage = (Stage) btnLogout.getScene().getWindow();
                currentStage.close();

                // Open login window
                openLoginWindow();

            } catch (Exception e) {
                showAlert("Error", "Logout failed: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDashboardClick(MouseEvent event) {
        setActiveButton(btn1, ind1);
        loadDashboardContent();
    }

    @FXML
    private void handleRoomDetailsClick(MouseEvent event) {
        setActiveButton(btn2, ind2);
        loadContent("Rooms");
    }

    @FXML
    private void handleRegistrationClick(MouseEvent event) {
        setActiveButton(btn3, ind3);
        loadContent("Student");
    }

    @FXML
    private void handleStudentFeesClick(MouseEvent event) {
        setActiveButton(btn4, ind4);
        loadContent("Fees");
    }

    @FXML
    private void handleHostelRulesClick(MouseEvent event) {
        setActiveButton(btn5, ind5);
        loadContent("Rules");
    }

    @FXML
    private void handleHostelContactsClick(MouseEvent event) {
        setActiveButton(btn6, ind6);
        loadContent("Contact");
    }

    @FXML
    private void handleAboutClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/about.fxml"));
            Parent aboutRoot = loader.load();

            Stage aboutStage = new Stage();
            aboutStage.setTitle("About");
            aboutStage.setScene(new Scene(aboutRoot));
            aboutStage.initModality(Modality.APPLICATION_MODAL); // Optional: makes it modal
            aboutStage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not open about: " + e.getMessage());
        }
    }



    private void setActiveButton(HBox activeButton, Region activeIndicator) {
        // Reset all buttons
        resetAllButtons();

        // Set active button
        activeButton.setStyle("-fx-cursor: hand; -fx-background-color: #2D3091;");
        activeIndicator.setStyle("-fx-background-color: #2D3091;");
    }

    private void resetAllButtons() {
        HBox[] buttons = {btn1, btn2, btn3, btn4, btn5, btn6};
        Region[] indicators = {ind1, ind2, ind3, ind4, ind5, ind6};

        for (HBox button : buttons) {
            button.setStyle("-fx-cursor: hand; -fx-background-color: #191B52;");
        }

        for (Region indicator : indicators) {
            indicator.setStyle("-fx-background-color: transparent;");
        }
    }

    private void loadDashboardContent() {
        try {
            contentArea.getChildren().clear();

            // Load Inner_Dashboard content
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/hms/fxml/student/innerDashboard.fxml"));
            Parent dashboardContent = loader.load();

            InnerDashboard dashboard = new InnerDashboard();

            contentArea.getChildren().add(dashboardContent);
        } catch (IOException e) {
            // If FXML doesn't exist, create a simple placeholder
            createPlaceholderContent("Dashboard");
        }
    }

    private void loadContent(String contentType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/student/" + contentType + ".fxml"));
            Node content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (IOException e) {
            // If FXML doesn't exist, create a simple placeholder
            createPlaceholderContent(contentType);
        }
    }

    private void createPlaceholderContent(String contentType) {
        contentArea.getChildren().clear();

        // Create a simple label as placeholder
        javafx.scene.control.Label placeholder = new javafx.scene.control.Label(contentType + " Content");
        placeholder.setStyle("-fx-font-size: 24px; -fx-text-fill: #333333;");
        contentArea.getChildren().add(placeholder);
    }

    private void logActivity(String activity) {
        try {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss / dd/MM/yyyy");
            String timestamp = now.format(formatter);

            int userId = Emp.UserId; // Assuming this static field exists
            String query = "INSERT INTO logs (User_id, Date, Status) VALUES (?, ?, ?)";
            pst = conn.prepareStatement(query);
            pst.setInt(1, userId);
            pst.setString(2, timestamp);
            pst.setString(3, activity);
            pst.execute();
        } catch (SQLException e) {
            showAlert("Database Error", "Could not log activity: " + e.getMessage());
        } finally {
            try {
                if (pst != null) pst.close();
            } catch (SQLException e) {
                showAlert("Database Error", "Could not close statement: " + e.getMessage());
            }
        }
    }

    private void openLoginWindow() {
        try {
            // Try to load JavaFX Login window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/login.fxml"));
            Parent loginRoot = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Login");
            loginStage.setScene(new Scene(loginRoot));

            InputStream iconStream = getClass().getResourceAsStream("/com/hms/images/HMS.png");
            if (iconStream != null) {
                loginStage.getIcons().add(new Image(iconStream));
            } else {
                System.err.println("⚠️ HMS.png icon not found at /com/hms/images/HMS.png");
            }

            loginStage.show();
        } catch (IOException e) {
            // Fallback to Swing login if JavaFX version doesn't exist
            Platform.runLater(() -> {
                Login loginFrame = new Login();
//                loginFrame.setVisible(true);
            });
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static String getCurrentDate(String format) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
    }

    public static void main(String[] args) {
        launch(args);
    }


    // Cleanup method to close database connections
    public void cleanup() {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            showAlert("Database Error", "Could not close database connections: " + e.getMessage());
        }
    }
}

