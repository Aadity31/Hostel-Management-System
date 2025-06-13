package com.hms.views.users.student;

import com.hms.utils.DB;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

public class Rooms implements Initializable {

    @FXML
    private Label txtavailable;

    @FXML
    private Label txtfilled;

    @FXML
    private TableView<Room> room_table;

    @FXML
    private TableColumn<Room, String> roomIdColumn;

    @FXML
    private TableColumn<Room, String> roomTypeColumn;

    @FXML
    private TableColumn<Room, String> capacityColumn;

    @FXML
    private TableColumn<Room, String> facilityColumn;

    @FXML
    private TableColumn<Room, String> roomStatusColumn;

    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;
    private Timeline timeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize database connection
        conn = DB.connect();
        if (conn == null) {
            showAlert("Error", "Failed to connect to database");
            return;
        }

        // Setup table columns
        setupTableColumns();

        // Load data
        availableRooms();
//        filledRooms();
        updateTable();

        // Start time updates
        startTimeUpdates();
    }

    private void setupTableColumns() {
        roomIdColumn.setCellValueFactory(cellData -> cellData.getValue().roomIdProperty());
        roomTypeColumn.setCellValueFactory(cellData -> cellData.getValue().roomTypeProperty());
        capacityColumn.setCellValueFactory(cellData -> cellData.getValue().capacityProperty());
        facilityColumn.setCellValueFactory(cellData -> cellData.getValue().facilityProperty());
        roomStatusColumn.setCellValueFactory(cellData -> cellData.getValue().roomStatusProperty());
    }

    public static String now(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    public void currentDate() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd yyyy");
        // If you need to display date somewhere, add a label and set it here
        // dateLabel.setText(sdf.format(d));
    }

    public void startTimeUpdates() {
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    Date d = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
                    // If you need to display time somewhere, add a label and set it here
                    // timeLabel.setText(sdf.format(d));
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void availableRooms() {
        try {
            pst = conn.prepareStatement("SELECT COUNT(*) AS AvailableRooms FROM rooms WHERE Room_status = 'Available'");
            rs = pst.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("AvailableRooms");
                txtavailable.setText(String.valueOf(count));
            }
        } catch (SQLException ex) {
            showAlert("Database Error", "Error fetching available rooms: " + ex.getMessage());
        } finally {
            closeResources();
        }
    }

//    public void filledRooms() {
//        try {
//            pst = conn.prepareStatement("SELECT COUNT(*) AS FilledRooms FROM rooms WHERE Room_status = 'Full'");
//            rs = pst.executeQuery();
//            if (rs.next()) {
//                int count = rs.getInt("FilledRooms");
//                txtfilled.setText(String.valueOf(count));
//            }
//        } catch (SQLException ex) {
//            showAlert("Database Error", "Error fetching filled rooms: " + ex.getMessage());
//        } finally {
//            closeResources();
//        }
//    }

    private void updateTable() {
        try {
            String sql = "SELECT * FROM rooms";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            ObservableList<Room> roomList = FXCollections.observableArrayList();

            while (rs.next()) {
                Room room = new Room(
                        rs.getString("Room_id"),
                        rs.getString("Room_type"),
                        rs.getString("Capacity"),
                        rs.getString("Facility"),
                        rs.getString("Room_status")
                );
                roomList.add(room);
            }

            room_table.setItems(roomList);

        } catch (SQLException e) {
            showAlert("Database Error", "Error updating table: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (SQLException e) {
            // Log error
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void cleanup() {
        if (timeline != null) {
            timeline.stop();
        }
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error closing connection: " + e.getMessage());
        }
    }


    static class Room {
        private final StringProperty roomId;
        private final StringProperty roomType;
        private final StringProperty capacity;
        private final StringProperty facility;
        private final StringProperty roomStatus;

        public Room(String roomId, String roomType, String capacity,
                    String facility, String roomStatus) {
            this.roomId = new SimpleStringProperty(roomId);
            this.roomType = new SimpleStringProperty(roomType);
            this.capacity = new SimpleStringProperty(capacity);
            this.facility = new SimpleStringProperty(facility);
            this.roomStatus = new SimpleStringProperty(roomStatus);
        }

        // Property getters
        public StringProperty roomIdProperty() { return roomId; }
        public StringProperty roomTypeProperty() { return roomType; }
        public StringProperty capacityProperty() { return capacity; }
        public StringProperty facilityProperty() { return facility; }
        public StringProperty roomStatusProperty() { return roomStatus; }

        // Regular getters
        public String getRoomId() { return roomId.get(); }
        public String getRoomType() { return roomType.get(); }
        public String getCapacity() { return capacity.get(); }
        public String getFacility() { return facility.get(); }
        public String getRoomStatus() { return roomStatus.get(); }
    }

}