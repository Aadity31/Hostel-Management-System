package com.hms.views.users.admin;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

import com.hms.utils.DB;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

public class Activity implements Initializable {

    @FXML private TextField txt_search;
    @FXML private Button btnreset;
    @FXML private TableView<LogEntry> tbl_3;
    @FXML private TableColumn<LogEntry, String> colLogsId;
    @FXML private TableColumn<LogEntry, String> colUserId;
    @FXML private TableColumn<LogEntry, String> colDate;
    @FXML private TableColumn<LogEntry, String> colStatus;

    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;
    private Timeline timeline;
    private ObservableList<LogEntry> logsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DB.connect();
        setupTableColumns();
        currentDate();
        startClock();
        updateTable();
        // tbl_3.setSortPolicy(null); // enable default sorting
    }

    private void setupTableColumns() {
        colLogsId.setCellValueFactory(cellData -> cellData.getValue().logsIdProperty());
        colUserId.setCellValueFactory(cellData -> cellData.getValue().userIdProperty());
        colDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        tbl_3.setItems(logsList);
    }

    public static String now(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    public void currentDate() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd yyyy");
        // Optional: use the date string if needed
    }

    public void startClock() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
            // Optional: use the time string if needed
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void stopClock() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    private void updateTable() {
        logsList.clear();
        try {
            String sql = "SELECT * FROM logs";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            while (rs.next()) {
                String logsId = rs.getString("logs_id");
                String userId = rs.getString("User_id");
                String date = rs.getString("date");
                String status = rs.getString("status");

                LogEntry entry = new LogEntry(
                        logsId != null ? logsId : "",
                        userId != null ? userId : "",
                        date != null ? date : "",
                        status != null ? status : ""
                );
                logsList.add(entry);
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Error fetching logs data: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    @FXML
    private void onSearchKeyReleased(KeyEvent event) {
        String searchText = txt_search.getText().trim();
        if (searchText.isEmpty()) {
            updateTable();
            return;
        }

        if (!searchText.matches("\\d+")) {
            showAlert("Input Error", "Please enter a valid numeric User ID");
            return;
        }

        logsList.clear();
        try {
            String sql = "SELECT * FROM logs WHERE CAST(User_id AS TEXT) LIKE ?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + searchText + "%");
            rs = pst.executeQuery();

            while (rs.next()) {
                String logsId = rs.getString("logs_id");
                String userId = rs.getString("User_id");
                String date = rs.getString("date");
                String status = rs.getString("status");

                LogEntry entry = new LogEntry(
                        logsId != null ? logsId : "",
                        userId != null ? userId : "",
                        date != null ? date : "",
                        status != null ? status : ""
                );
                logsList.add(entry);
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Error searching logs: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    @FXML
    private void onResetClicked() {
        txt_search.setText("");
        updateTable();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (SQLException e) {
            showAlert("Database Error", "Error closing resources: " + e.getMessage());
        }
    }

    public void refreshData() {
        updateTable();
    }

    public void cleanup() {
        stopClock();
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
            if (conn != null) conn.close();
        } catch (SQLException ex) {
            showAlert("Database Error", "Error closing database connection: " + ex.getMessage());
        }
    }

    public static class LogEntry {
        private final SimpleStringProperty logsId;
        private final SimpleStringProperty userId;
        private final SimpleStringProperty date;
        private final SimpleStringProperty status;

        public LogEntry(String logsId, String userId, String date, String status) {
            this.logsId = new SimpleStringProperty(logsId);
            this.userId = new SimpleStringProperty(userId);
            this.date = new SimpleStringProperty(date);
            this.status = new SimpleStringProperty(status);
        }

        public SimpleStringProperty logsIdProperty() {
            return logsId;
        }

        public SimpleStringProperty userIdProperty() {
            return userId;
        }

        public SimpleStringProperty dateProperty() {
            return date;
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }

        public String getLogsId() {
            return logsId.get();
        }

        public String getUserId() {
            return userId.get();
        }

        public String getDate() {
            return date.get();
        }

        public String getStatus() {
            return status.get();
        }
    }
}
