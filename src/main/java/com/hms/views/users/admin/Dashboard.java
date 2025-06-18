package com.hms.views.users.admin;

import com.hms.utils.DB;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Dashboard extends Application implements Initializable {

    // FXML Components
    @FXML private Button btnProfile;
    @FXML private Button btnLogout;
    @FXML private StackPane contentArea;

    // Sidebar buttons
    @FXML private HBox btnDashboard;
    @FXML private HBox btnRooms;
    @FXML private HBox btnStudent;
    @FXML private HBox btnFees;
    @FXML private HBox btnAllStudent;
    @FXML private HBox btnStaff;
    @FXML private HBox btnActivity;

    // Database connections
    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;

    // Current active button
    private HBox currentActiveButton = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/admin/dashboard.fxml"));
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize database connection
        try {
            conn = DB.connect(); // Assuming you have a database connection class
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set initial active button
        setActiveButton(btnDashboard);

        // Load initial dashboard content
        loadDashboardContent();
    }

    // Method to set active button styling
    private void setActiveButton(HBox button) {
        // Reset all buttons to default style
        if (currentActiveButton != null) {
            currentActiveButton.setStyle("-fx-background-color: #191B52; -fx-padding: 12 0; -fx-cursor: hand; -fx-spacing: 0; -fx-pref-height: 50;");
            // Reset indicator to transparent
            currentActiveButton.getChildren().get(0).setStyle("-fx-background-color: transparent; -fx-pref-width: 3; -fx-pref-height: 43;");
        }

        // Set new button as active
        button.setStyle("-fx-background-color: #2D3091; -fx-padding: 12 0; -fx-cursor: hand; -fx-spacing: 0; -fx-pref-height: 50;");
        // Set indicator to white
        button.getChildren().getFirst().setStyle("-fx-background-color: #FFFFFF; -fx-pref-width: 3; -fx-pref-height: 43;");
        currentActiveButton = button;
    }

    // Load different content based on selection
    private void loadDashboardContent() {
        try {
            // Clear existing content
            contentArea.getChildren().clear();

            // Load the Inner Dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/admin/InnerDashboard.fxml"));
            Parent dashboardRoot = loader.load();

            // Get the controller for future reference (optional)
            InnerDashboard dashboardController = loader.getController();

            // Add the loaded content to the content area
            contentArea.getChildren().add(dashboardRoot);

            // Optional: Store controller reference for cleanup later
            // You might want to add this as a class field if you need to access it elsewhere
            // this.dashboardController = dashboardController;

        } catch (Exception e) {
            e.printStackTrace();

            // Optional: Show error message to user
            showErrorAlert("Failed to load dashboard content: " + e.getMessage());

            // Optional: Load a fallback content or show error label
            loadFallbackContent();
        }
    }

    // Optional helper method for error handling
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Loading Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Optional fallback content method
    private void loadFallbackContent() {
        Label errorLabel = new Label("Dashboard content could not be loaded.");
        errorLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: red;");
        contentArea.getChildren().add(errorLabel);
    }

    // Event Handlers for sidebar buttons
    @FXML
    private void handleDashboardClick(MouseEvent event) {
        setActiveButton(btnDashboard);
        loadDashboardContent();
    }

    @FXML
    private void handleRoomsClick(MouseEvent event) {
        setActiveButton(btnRooms);
        loadRoomsContent();
    }

    @FXML
    private void handleStudentClick(MouseEvent event) {
        setActiveButton(btnStudent);
        loadStudentContent();
    }

    @FXML
    private void handleFeesClick(MouseEvent event) {
        setActiveButton(btnFees);
        loadFeesContent();
    }

    @FXML
    private void handleAllStudentClick(MouseEvent event) {
        setActiveButton(btnAllStudent);
        loadAllStudentContent();
    }

    @FXML
    private void handleStaffClick(MouseEvent event) {
        setActiveButton(btnStaff);
        loadStaffContent();
    }

    @FXML
    private void handleActivityClick(MouseEvent event) {
        setActiveButton(btnActivity);
        loadActivityContent();
    }

    @FXML
    private void handleAboutClick(MouseEvent event) {
        try {
            // Load About dialog/window
            // hms.About about = new hms.About();
            // about.show();
            showAlert("About", "HMS - Hostel Management System", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfileAction() {
        try {
            // Load Profile window
            // Profile profile = new Profile();
            // profile.show();
            showAlert("Profile", "Profile functionality will be implemented here.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleRequestsClick() {
        String fxml = "/com/hms/fxml/admin/requests.fxml";
        URL url = getClass().getResource(fxml);
        if (url == null) {
            System.err.println("❌  Cannot find " + fxml);
            return;                       // nothing to load
        }
        try {
            Parent view = FXMLLoader.load(url);
            contentArea.getChildren().setAll(view);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    @FXML
    private void handleLogoutAction() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("This will end your current session.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            logUserActivity("Logged out");

            try {
                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/hms/fxml/login.fxml")));

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Login");
                // Close current window
                ((Stage) btnLogout.getScene().getWindow()).close();
                stage.show();

                // Open login window
                // hms.Login login = new hms.Login();
                // login.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Content loading methods
    private void loadRoomsContent() {
        try {
            contentArea.getChildren().clear();
            // Load Rooms FXML
             FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/admin/rooms.fxml"));
             Parent roomsContent = loader.load();
             contentArea.getChildren().add(roomsContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStudentContent() {
        try {
            contentArea.getChildren().clear();
            // Load Student FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/admin/students.fxml"));
            Parent dashboardRoot = loader.load();
            contentArea.getChildren().add(dashboardRoot);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFeesContent() {
        try {
            contentArea.getChildren().clear();
            // Load Fees FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/admin/fees.fxml"));
            Parent dashboardRoot = loader.load();
            contentArea.getChildren().add(dashboardRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAllStudentContent() {
        try {
            contentArea.getChildren().clear();
            // Load All Student FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/admin/allStudents.fxml"));
            Parent dashboardRoot = loader.load();
            contentArea.getChildren().add(dashboardRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStaffContent() {
        try {
            contentArea.getChildren().clear();
            // Load Staff FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/admin/manageStaff.fxml"));
            Parent dashboardRoot = loader.load();
            contentArea.getChildren().add(dashboardRoot);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadActivityContent() {
        try {
            contentArea.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hms/fxml/admin/activity.fxml"));
            Parent dashboardRoot = loader.load();

            contentArea.getChildren().add(dashboardRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Utility methods
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void logUserActivity(String activity) {
        try {
            if (conn != null) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss / yyyy-MM-dd");
                String timestamp = now.format(formatter);

                // Assuming you have a user ID available
                int userId = getUserId(); // You need to implement this method

                String query = "INSERT INTO logs (User_id, Date, Status) VALUES (?, ?, ?)";
                pst = conn.prepareStatement(query);
                pst.setInt(1, userId);
                pst.setString(2, timestamp);
                pst.setString(3, activity);
                pst.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to log user activity: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private int getUserId() {
        // Return the current user ID
        // This should be implemented based on your login system
        // For now, returning a placeholder
        return 1; // Replace with actual user ID retrieval
    }

    public static String getCurrentDateTime(String format) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return now.format(formatter);
    }

    // Cleanup method
    public void cleanup() {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to handle window closing
    public void handleWindowClose() {
        cleanup();
    }

    public static void main(String[] args) {
        launch(args);
    }
}