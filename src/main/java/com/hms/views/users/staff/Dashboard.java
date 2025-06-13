package com.hms.views.users.staff;

import com.hms.utils.DB;
import com.hms.utils.Emp;
import com.hms.views.auth.Login;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
import java.util.*;

public class Dashboard extends Application implements Initializable {

    @FXML private AnchorPane rootPane;
    @FXML private Button btnProfile;
    @FXML private Button btnLogout;
    @FXML private StackPane desktopPane;

    // Navigation buttons
    @FXML private HBox btn1, btn2, btn3, btn4, btn5;

    // Indicators
    @FXML private Pane ind1, ind2, ind3, ind4, ind5;

    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;
    private Timer timeTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize database connection
        conn = DB.connect();

        // Set initial state - Dashboard selected
        setActiveButton(btn1, ind1);

        // Load default dashboard content
        loadDashboardContent();

        // Start time display
        startTimeDisplay();

        // Center the window
        Platform.runLater(this::centerWindow);
    }

    private void centerWindow() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.centerOnScreen();
    }

    private void startTimeDisplay() {
        timeTimer = new Timer(true);
        timeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    // Update time display if needed
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
                    String timeString = now.format(formatter);
                    // You can update a time label here if you add one to the FXML
                });
            }
        }, 0, 1000);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleProfileAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/Profile.fxml"));
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
    private void handleRoomsClick(MouseEvent event) {
        setActiveButton(btn2, ind2);
        loadRoomsContent();
    }

    @FXML
    private void handleStudentClick(MouseEvent event) {
        setActiveButton(btn3, ind3);
        loadStudentContent();
    }

    @FXML
    private void handleFeesClick(MouseEvent event) {
        setActiveButton(btn4, ind4);
        loadFeesContent();
    }

    @FXML
    private void handleAllStudentClick(MouseEvent event) {
        setActiveButton(btn5, ind5);
        loadAllStudentContent();
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

    private void setActiveButton(HBox activeButton, Pane activeIndicator) {
        // Reset all buttons
        resetAllButtons();

        // Set active button
        activeButton.setStyle("-fx-background-color: #2d3091; -fx-cursor: hand;");
        activeIndicator.setStyle("-fx-background-color: #ffffff;");
    }

    private void resetAllButtons() {
        // Reset all navigation buttons
        btn1.setStyle("-fx-background-color: #191b52; -fx-cursor: hand;");
        btn2.setStyle("-fx-background-color: #191b52; -fx-cursor: hand;");
        btn3.setStyle("-fx-background-color: #191b52; -fx-cursor: hand;");
        btn4.setStyle("-fx-background-color: #191b52; -fx-cursor: hand;");
        btn5.setStyle("-fx-background-color: #191b52; -fx-cursor: hand;");

        // Reset all indicators
        ind1.setStyle("-fx-background-color: transparent;");
        ind2.setStyle("-fx-background-color: transparent;");
        ind3.setStyle("-fx-background-color: transparent;");
        ind4.setStyle("-fx-background-color: transparent;");
        ind5.setStyle("-fx-background-color: transparent;");
    }

    private void loadDashboardContent() {
        try {
            desktopPane.getChildren().clear();
            // Load inner dashboard content
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/staff/innerDashboard.fxml"));
            Parent dashboardRoot = loader.load();

            // You can create Inner_Dashboard as a JavaFX component or load FXML
            InnerDashboard dashboard = new InnerDashboard();
            // Convert Swing component to JavaFX if needed, or create new JavaFX version
            desktopPane.getChildren().add(dashboardRoot);

        } catch (Exception e) {
            showErrorAlert("Error loading dashboard", e.getMessage());
        }
    }

    private void loadRoomsContent() {
        try {
            desktopPane.getChildren().clear();
            // Load rooms content
            loadContentPlaceholder("Rooms Management Content");
        } catch (Exception e) {
            showErrorAlert("Error loading rooms", e.getMessage());
        }
    }

    private void loadStudentContent() {
        try {
            desktopPane.getChildren().clear();
            // Load student content
            loadContentPlaceholder("Student Management Content");
        } catch (Exception e) {
            showErrorAlert("Error loading student content", e.getMessage());
        }
    }

    private void loadFeesContent() {
        try {
            desktopPane.getChildren().clear();
            // Load fees content
            loadContentPlaceholder("Student Fees Content");
        } catch (Exception e) {
            showErrorAlert("Error loading fees content", e.getMessage());
        }
    }

    private void loadAllStudentContent() {
        try {
            desktopPane.getChildren().clear();
            // Load all student content
            loadContentPlaceholder("All Students Living Content");
        } catch (Exception e) {
            showErrorAlert("Error loading all students content", e.getMessage());
        }
    }

    private void loadContentPlaceholder(String content) {
        // This is a placeholder method - replace with actual content loading
        javafx.scene.control.Label label = new javafx.scene.control.Label(content);
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #333333;");
        desktopPane.getChildren().add(label);
    }

    private void logActivity(String action) {
        try {
            Date currentDate = GregorianCalendar.getInstance().getTime();
            DateFormat df = DateFormat.getDateInstance();
            String dateString = df.format(currentDate);

            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String timeString = sdf.format(d);

            int userId = Emp.UserId; // Assuming this exists
            String query = "INSERT INTO logs (User_id, Date, Status) VALUES (?, ?, ?)";
            pst = conn.prepareStatement(query);
            pst.setInt(1, userId);
            pst.setString(2, timeString + " / " + dateString);
            pst.setString(3, action);
            pst.executeUpdate();

        } catch (SQLException e) {
            showErrorAlert("Database Error", e.getMessage());
        } finally {
            try {
                if (pst != null) pst.close();
            } catch (SQLException e) {
                showErrorAlert("Database Error", e.getMessage());
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

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void cleanup() {
        if (timeTimer != null) {
            timeTimer.cancel();
        }
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/staff/dashboard.fxml"));
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
}