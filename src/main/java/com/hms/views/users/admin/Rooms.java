package com.hms.views.users.admin;

import com.hms.utils.DB;
import com.hms.utils.Emp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.ResourceBundle;

public class Rooms implements Initializable {

    // FXML Components
    @FXML private TextField txtroomid;
    @FXML private ComboBox<String> txtroomtype;
    @FXML private TextField txtcapacity;
    @FXML private ComboBox<String> txtfacility;
    @FXML private ComboBox<String> txtstatus;
    @FXML private Button add_btn;
    @FXML private Button update_btn;
    @FXML private Button delete_btn;
    @FXML private Button clear_btn;
    @FXML private TableView<Room> room_table;
    @FXML private TableColumn<Room, Integer> roomIdColumn;
    @FXML private TableColumn<Room, String> roomTypeColumn;
    @FXML private TableColumn<Room, Integer> capacityColumn;
    @FXML private TableColumn<Room, String> facilityColumn;
    @FXML private TableColumn<Room, String> roomStatusColumn;

    // Database components
    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;

    // Observable list for table data
    private ObservableList<Room> roomList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize database connection
        conn = DB.connect();
        // Set up table columns
        setupTableColumns();
        updateTable();

        ObservableList<String> roomTypes = FXCollections.observableArrayList("Select", "Single", "Shared");
        txtroomtype.setItems(roomTypes);
        txtroomtype.setValue("Select");

        ObservableList<String> roomFacilityTypes = FXCollections.observableArrayList("Select", "AC", "Non AC");
        txtfacility.setItems(roomFacilityTypes);
        txtfacility.setValue("Select");

        ObservableList<String> roomStatusType = FXCollections.observableArrayList("Select", "Available", "Full");
        txtstatus.setItems(roomStatusType);
        txtstatus.setValue("Select");

        // Set focus to room type field
        txtroomtype.requestFocus();
    }

    private void setupTableColumns() {
        roomIdColumn.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        facilityColumn.setCellValueFactory(new PropertyValueFactory<>("facility"));
        roomStatusColumn.setCellValueFactory(new PropertyValueFactory<>("roomStatus"));

        room_table.setItems(roomList);
    }

    private void updateTable() {
        roomList.clear();
        try {
            String sql = "SELECT * FROM rooms";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            while (rs.next()) {
                Room room = new Room(
                        rs.getInt("Room_id"),
                        rs.getString("Room_type"),
                        rs.getInt("Capacity"),
                        rs.getString("Facility"),
                        rs.getString("Room_status")
                );
                roomList.add(room);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading room data: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            closeResources();
        }
    }

    @FXML
    private void addRecord() {
        if (validateFields(false)) {
            try {
                String sql = "INSERT INTO rooms (Room_type, Capacity, Facility, Room_status) VALUES (?, ?, ?, ?)";

                pst = conn.prepareStatement(sql);
                pst.setString(1, txtroomtype.getValue());
                pst.setString(2, txtcapacity.getText());
                pst.setString(3, txtfacility.getValue());
                pst.setString(4, txtstatus.getValue());

                pst.execute();
                showAlert("Success", "Room added successfully!", Alert.AlertType.INFORMATION);

                // Log the activity
                logActivity("New Room Added");

                // Refresh table and clear fields
                updateTable();
                clearFields();

            } catch (SQLException e) {
                showAlert("Database Error", "Error adding room: " + e.getMessage(), Alert.AlertType.ERROR);
            } finally {
                closeResources();
            }
        } else {
            showAlert("Validation Error", "Please fill all required fields", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void updateRecord() {
        if (validateFields(true)) {
            // Show confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Update Record");
            confirmAlert.setHeaderText("Are you sure you want to update this record?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    String sql = "UPDATE rooms SET Room_type=?, Capacity=?, Facility=?, Room_status=? WHERE Room_id=?";

                    pst = conn.prepareStatement(sql);
                    pst.setString(1, txtroomtype.getValue());
                    pst.setString(2, txtcapacity.getText());
                    pst.setString(3, txtfacility.getValue());
                    pst.setString(4, txtstatus.getValue());
                    pst.setString(5, txtroomid.getText());

                    pst.execute();
                    showAlert("Success", "Room updated successfully!", Alert.AlertType.INFORMATION);

                    // Log the activity
                    logActivity("Room Details Updated");

                    // Refresh table
                    updateTable();

                } catch (SQLException e) {
                    showAlert("Database Error", "Error updating room: " + e.getMessage(), Alert.AlertType.ERROR);
                } finally {
                    closeResources();
                }
            }
        } else {
            showAlert("Validation Error", "Please fill all required fields", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void deleteRecord() {
        if (validateFields(true)) {
            // Show confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Record");
            confirmAlert.setHeaderText("Are you sure you want to delete this record?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // Log the activity first
                    logActivity("Room Removed");

                    String sql = "DELETE FROM rooms WHERE Room_id=?";
                    pst = conn.prepareStatement(sql);
                    pst.setString(1, txtroomid.getText());
                    pst.execute();

                    showAlert("Success", "Room deleted successfully!", Alert.AlertType.INFORMATION);

                    // Refresh table and clear fields
                    updateTable();
                    clearFields();

                } catch (SQLException e) {
                    showAlert("Database Error", "Error deleting room: " + e.getMessage(), Alert.AlertType.ERROR);
                } finally {
                    closeResources();
                }
            }
        } else {
            showAlert("Validation Error", "Please select a room to delete", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clearFields() {
        txtroomid.clear();
        txtcapacity.clear();
        txtroomtype.setValue("Select");
        txtfacility.setValue("Select");
        txtstatus.setValue("Select");
    }

    @FXML
    private void tableRowClicked(MouseEvent event) {
        Room selectedRoom = room_table.getSelectionModel().getSelectedItem();
        if (selectedRoom != null) {
            txtroomid.setText(String.valueOf(selectedRoom.getRoomId()));
            txtroomtype.setValue(selectedRoom.getRoomType());
            txtcapacity.setText(String.valueOf(selectedRoom.getCapacity()));
            txtfacility.setValue(selectedRoom.getFacility());
            txtstatus.setValue(selectedRoom.getRoomStatus());
        }
    }

    private boolean validateFields(boolean includeRoomId) {
        if (includeRoomId && (txtroomid.getText() == null || txtroomid.getText().trim().isEmpty())) {
            return false;
        }

        return !("Select".equals(txtroomtype.getValue()) ||
                txtcapacity.getText() == null || txtcapacity.getText().trim().isEmpty() ||
                "Select".equals(txtfacility.getValue()) ||
                "Select".equals(txtstatus.getValue()));
    }

    private void logActivity(String activity) {
        try {
            Date currentDate = GregorianCalendar.getInstance().getTime();
            DateFormat df = DateFormat.getDateInstance();
            String dateString = df.format(currentDate);

            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String timeString = sdf.format(d);

            int userId = Emp.UserId;

            String sql = "INSERT INTO logs (User_id, Date, Status) VALUES (?, ?, ?)";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, userId);
            pst.setString(2, timeString + " / " + dateString);
            pst.setString(3, activity);
            pst.execute();

        } catch (SQLException e) {
            System.err.println("Error logging activity: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
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
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    // Room model class
    public static class Room {
        private int roomId;
        private String roomType;
        private int capacity;
        private String facility;
        private String roomStatus;

        public Room(int roomId, String roomType, int capacity, String facility, String roomStatus) {
            this.roomId = roomId;
            this.roomType = roomType;
            this.capacity = capacity;
            this.facility = facility;
            this.roomStatus = roomStatus;
        }

        // Getters and setters
        public int getRoomId() { return roomId; }
        public void setRoomId(int roomId) { this.roomId = roomId; }

        public String getRoomType() { return roomType; }
        public void setRoomType(String roomType) { this.roomType = roomType; }

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }

        public String getFacility() { return facility; }
        public void setFacility(String facility) { this.facility = facility; }

        public String getRoomStatus() { return roomStatus; }
        public void setRoomStatus(String roomStatus) { this.roomStatus = roomStatus; }
    }
}