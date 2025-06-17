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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
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
    private Label selectedHostelLabel;

    // Hostel Table
    @FXML
    private TableView<Hostel> hostel_table;

    // Assuming these are defined in your FXML file
    @FXML private TableColumn<Hostel, String> hostelIdColumn;
    @FXML private TableColumn<Hostel, String> hostelNameColumn;
    @FXML private TableColumn<Hostel, String> hostelLocationColumn;
    @FXML private TableColumn<Hostel, String> totalRoomsColumn; // Renamed from hostelCapacityColumn
    @FXML private TableColumn<Hostel, String> availableRoomsColumn; // Renamed from hostelAvailableColumn
    @FXML private TableColumn<Hostel, String> hostelTypeColumn;
    @FXML private TableColumn<Hostel, String> contactPersonColumn; // New
    @FXML private TableColumn<Hostel, String> contactNumberColumn; // New
    @FXML private TableColumn<Hostel, String> emailColumn;         // New
    @FXML private TableColumn<Hostel, String> descriptionColumn;   // New

    // Room Table
    @FXML
    private TableView<Room> room_table;

    @FXML
    private TableColumn<Room, String> roomIdColumn;

    @FXML
    private TableColumn<Room, String> roomTypeColumn;

    @FXML
    private TableColumn<Room, String> capacityColumn;

    @FXML
    private TableColumn<Room, String> currentOccupantsColumn;

    @FXML
    private TableColumn<Room, String> facilityColumn;

    @FXML
    private TableColumn<Room, String> roomStatusColumn;

    @FXML
    private TableColumn<Room, String> monthlyRentColumn;

    @FXML
    private TableColumn<Room, Void> applyActionColumn;

    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;
    private Timeline timeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            conn = DB.connect();
            setupHostelTableColumns();
            setupRoomTableColumns();
            loadHostels();

            hostel_table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedHostelLabel.setText("Selected Hostel: " + newSelection.getHostelName());
                    loadRoomsForHostel(newSelection.getHostelId());
                    updateStatistics();
                }
            });

            room_table.setItems(FXCollections.observableArrayList());
            selectedHostelLabel.setText("Select a hostel to view rooms");
            startTimeUpdates();
        } catch (Exception e) {
            e.printStackTrace(); // Or showAlert(...) if needed
        }

    }

    /**
     * Sets up the cell value factories for the hostel TableView columns.
     * This method binds the columns to the properties of the updated Hostel class.
     */
    private void setupHostelTableColumns() {
        // Updated bindings for existing columns
        hostelIdColumn.setCellValueFactory(cellData -> cellData.getValue().hostelIdProperty());
        hostelNameColumn.setCellValueFactory(cellData -> cellData.getValue().hostelNameProperty());
        hostelLocationColumn.setCellValueFactory(cellData -> cellData.getValue().locationProperty());
        hostelTypeColumn.setCellValueFactory(cellData -> cellData.getValue().hostelTypeProperty());


        totalRoomsColumn.setCellValueFactory(cellData -> cellData.getValue().totalRoomsProperty());
        availableRoomsColumn.setCellValueFactory(cellData -> cellData.getValue().availableRoomsProperty());

        contactPersonColumn.setCellValueFactory(cellData -> cellData.getValue().contactPersonProperty());
        contactNumberColumn.setCellValueFactory(cellData -> cellData.getValue().contactNumberProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
    }

    private void setupRoomTableColumns() {
        roomIdColumn.setCellValueFactory(cellData -> cellData.getValue().roomIdProperty());
        roomTypeColumn.setCellValueFactory(cellData -> cellData.getValue().roomTypeProperty());
        capacityColumn.setCellValueFactory(cellData -> cellData.getValue().capacityProperty());
        currentOccupantsColumn.setCellValueFactory(cellData -> cellData.getValue().currentOccupantsProperty());
        facilityColumn.setCellValueFactory(cellData -> cellData.getValue().facilityProperty());
        roomStatusColumn.setCellValueFactory(cellData -> cellData.getValue().roomStatusProperty());
        monthlyRentColumn.setCellValueFactory(cellData -> cellData.getValue().monthlyRentProperty());

        // Setup the "Apply" button column
        applyActionColumn.setCellFactory(param -> new TableCell<Room, Void>() {
            private final Button applyButton = new Button("Apply");

            {
                applyButton.getStyleClass().add("apply-button");
                applyButton.setOnAction(_ -> {
                    Room room = getTableView().getItems().get(getIndex());
                    Hostel selectedHostel = hostel_table.getSelectionModel().getSelectedItem();
                    if (selectedHostel != null) {
                        handleRoomApplication(room, selectedHostel);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Room room = getTableView().getItems().get(getIndex());
                    boolean canApply = canApplyForRoom(room);
                    applyButton.setDisable(!canApply);

                    if (canApply) {
                        applyButton.setText("Apply");
                        applyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    } else {
                        if ("Full".equalsIgnoreCase(room.getRoomStatus())) {
                            applyButton.setText("Room Full");
                        } else {
                            applyButton.setText("Not Available");
                        }
                        applyButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                    }
                    setGraphic(applyButton);
                }
            }
        });
    }

    private boolean canApplyForRoom(Room room) {
        try {
            int capacity = Integer.parseInt(room.getCapacity());
            int occupants = Integer.parseInt(room.getCurrentOccupants());
            return occupants < capacity && "Available".equalsIgnoreCase(room.getRoomStatus());
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void handleRoomApplication(Room room, Hostel hostel) {
        // Here you can implement the actual application logic
        // For now, just show a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Room Application");
        alert.setHeaderText("Apply for Room");
        alert.setContentText(String.format(
                """     
                        Do you want to apply for:
                        Hostel: %s
                        Room ID: %s
                        Room Type: %s
                        Monthly Rent: ₹%s
                        Available Spots: %d""",
                hostel.getHostelName(),
                room.getRoomId(),
                room.getRoomType(),
                room.getMonthlyRent(),
                Integer.parseInt(room.getCapacity()) - Integer.parseInt(room.getCurrentOccupants())
        ));

        alert.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                // Implement actual application submission here
                showAlert(Alert.AlertType.INFORMATION, "Application Submitted",
                        "Your application for Room " + room.getRoomId() + " has been submitted successfully!");
            }
        });
    }

    private void loadHostels() {
        // The list now holds Hostel objects which should be updated to accept the new fields.
        ObservableList<Hostel> hostelList = FXCollections.observableArrayList();
        try {
            // Updated SQL query to include the new columns and calculations.
            String sql = """
            SELECT
                h.Hostel_ID,
                h.Hostel_Name,
                h.Location,
                h.Hostel_Type,
                h.Contact_Person,
                h.Contact_Number,
                h.Email,
                h.Description,
                COALESCE(r_counts.total_rooms, 0) AS Total_Rooms,
                COALESCE(r_counts.available_rooms, 0) AS Available_Rooms
            FROM
                hostels h
            LEFT JOIN (
                SELECT
                    Hostel_Name,
                    COUNT(Room_id) AS total_rooms,
                    SUM(CASE WHEN Capacity > COALESCE(Current_Occupants, 0) THEN 1 ELSE 0 END) AS available_rooms
                FROM
                    rooms
                GROUP BY
                    Hostel_Name
            ) r_counts ON h.Hostel_Name = r_counts.Hostel_Name
            ORDER BY
                h.Hostel_Name;
            """;

            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            while (rs.next()) {
                // Make sure your Hostel class constructor is updated to match these parameters.
                Hostel hostel = new Hostel(
                        rs.getString("Hostel_ID"),
                        rs.getString("Hostel_Name"),
                        rs.getString("Location"),
                        rs.getString("Total_Rooms"),
                        rs.getString("Available_Rooms"),
                        rs.getString("Hostel_Type"),
                        rs.getString("Contact_Person"),
                        rs.getString("Contact_Number"),
                        rs.getString("Email"),
                        rs.getString("Description")
                );
                hostelList.add(hostel);
            }

            hostel_table.setItems(hostelList);


        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading hostels: " + e.getMessage());
        } finally {
            closeResources(rs, pst);
        }

    }



    private void loadRoomsForHostel(String hostelId) {
        ObservableList<Room> roomList = FXCollections.observableArrayList();
        try {
            String sql = """
            SELECT r.Room_id, r.Room_type, r.Capacity,
                   COALESCE(r.Current_Occupants, 0) AS Current_Occupants,
                   r.Facility, r.Monthly_Rent,
                   CASE
                       WHEN COALESCE(r.Current_Occupants, 0) >= r.Capacity THEN 'Full'
                       WHEN r.Room_status = 'Available' AND COALESCE(r.Current_Occupants, 0) < r.Capacity THEN 'Available'
                       ELSE 'Not Available'
                   END AS Room_status
            FROM rooms r
            JOIN hostels h ON r.Hostel_ID = h.Hostel_ID
            WHERE h.Hostel_ID = ?
            ORDER BY r.Room_id
        """;

            pst = conn.prepareStatement(sql);
            pst.setString(1, hostelId);
            rs = pst.executeQuery();

            while (rs.next()) {
                Room room = new Room(
                        rs.getString("Room_id"),
                        rs.getString("Room_type"),
                        rs.getString("Capacity"),
                        rs.getString("Current_Occupants"),
                        rs.getString("Facility"),
                        rs.getString("Room_status"),
                        rs.getString("Monthly_Rent")
                );
                roomList.add(room);
            }

            room_table.setItems(roomList);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading rooms: " + e.getMessage());
        } finally {
            closeResources(rs, pst);
        }
    }


    private void updateStatistics() {
        Hostel selectedHostel = hostel_table.getSelectionModel().getSelectedItem();
        if (selectedHostel == null) return;

        try {
            // Count available rooms
            String availableSql = """
                SELECT COUNT(*) as available_count
                FROM rooms r
                JOIN hostels h ON r.Hostel_Name = h.Hostel_Name
                WHERE h.Hostel_id = ? AND COALESCE(r.Current_Occupants, 0) < r.Capacity AND r.Room_status = 'Available'
                """;

            pst = conn.prepareStatement(availableSql);
            pst.setString(1, selectedHostel.getHostelId());
            rs = pst.executeQuery();

            int availableCount = 0;
            if (rs.next()) {
                availableCount = rs.getInt("available_count");
            }

            closeResources(rs, pst);

            // Count filled rooms
            String filledSql = """
                SELECT COUNT(*) as filled_count
                FROM rooms r
                JOIN hostels h ON r.Hostel_Name = h.Hostel_Name
                WHERE h.Hostel_id = ? AND COALESCE(r.Current_Occupants, 0) >= r.Capacity
                """;

            pst = conn.prepareStatement(filledSql);
            pst.setString(1, selectedHostel.getHostelId());
            rs = pst.executeQuery();

            int filledCount = 0;
            if (rs.next()) {
                filledCount = rs.getInt("filled_count");
            }

            txtavailable.setText(String.valueOf(availableCount));
            txtfilled.setText(String.valueOf(filledCount));

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating statistics: " + e.getMessage());
        } finally {
            closeResources(rs, pst);
        }
    }

    public void startTimeUpdates() {
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), _ -> {
                    Date d = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
                    // If you have a time label, update it here
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void closeResources(ResultSet rs, PreparedStatement pst) {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
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
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error closing connection: " + e.getMessage());
        }
    }

    // --- Hostel Model Class ---
    public static class Hostel {
        private final StringProperty hostelId;
        private final StringProperty hostelName;
        private final StringProperty location;
        private final StringProperty totalRooms;      // Renamed from hostelCapacity
        private final StringProperty availableRooms;  // Renamed from hostelAvailable
        private final StringProperty hostelType;
        private final StringProperty contactPerson;   // New field
        private final StringProperty contactNumber;   // New field
        private final StringProperty email;           // New field
        private final StringProperty description;     // New field

        // Updated constructor to accept all the new fields
        public Hostel(String hostelId, String hostelName, String location, String totalRooms,
                      String availableRooms, String hostelType, String contactPerson,
                      String contactNumber, String email, String description) {
            this.hostelId = new SimpleStringProperty(hostelId);
            this.hostelName = new SimpleStringProperty(hostelName);
            this.location = new SimpleStringProperty(location);
            this.totalRooms = new SimpleStringProperty(totalRooms);
            this.availableRooms = new SimpleStringProperty(availableRooms);
            this.hostelType = new SimpleStringProperty(hostelType);
            this.contactPerson = new SimpleStringProperty(contactPerson);
            this.contactNumber = new SimpleStringProperty(contactNumber);
            this.email = new SimpleStringProperty(email);
            this.description = new SimpleStringProperty(description);
        }

        // --- Property Getters ---
        public StringProperty hostelIdProperty() { return hostelId; }
        public StringProperty hostelNameProperty() { return hostelName; }
        public StringProperty locationProperty() { return location; }
        public StringProperty totalRoomsProperty() { return totalRooms; }
        public StringProperty availableRoomsProperty() { return availableRooms; }
        public StringProperty hostelTypeProperty() { return hostelType; }
        public StringProperty contactPersonProperty() { return contactPerson; }
        public StringProperty contactNumberProperty() { return contactNumber; }
        public StringProperty emailProperty() { return email; }
        public StringProperty descriptionProperty() { return description; }


        // --- Regular Getters ---
        public String getHostelId() { return hostelId.get(); }
        public String getHostelName() { return hostelName.get(); }
        public String getLocation() { return location.get(); }
        public String getTotalRooms() { return totalRooms.get(); }
        public String getAvailableRooms() { return availableRooms.get(); }
        public String getHostelType() { return hostelType.get(); }
        public String getContactPerson() { return contactPerson.get(); }
        public String getContactNumber() { return contactNumber.get(); }
        public String getEmail() { return email.get(); }
        public String getDescription() { return description.get(); }
    }

    // --- Room Model Class ---
    static class Room {
        private final StringProperty roomId;
        private final StringProperty roomType;
        private final StringProperty capacity;
        private final StringProperty currentOccupants;
        private final StringProperty facility;
        private final StringProperty roomStatus;
        private final StringProperty monthlyRent;

        public Room(String roomId, String roomType, String capacity, String currentOccupants,
                    String facility, String roomStatus, String monthlyRent) {
            this.roomId = new SimpleStringProperty(roomId);
            this.roomType = new SimpleStringProperty(roomType);
            this.capacity = new SimpleStringProperty(capacity);
            this.currentOccupants = new SimpleStringProperty(currentOccupants);
            this.facility = new SimpleStringProperty(facility);
            this.roomStatus = new SimpleStringProperty(roomStatus);
            this.monthlyRent = new SimpleStringProperty(monthlyRent);
        }

        // Property getters
        public StringProperty roomIdProperty() { return roomId; }
        public StringProperty roomTypeProperty() { return roomType; }
        public StringProperty capacityProperty() { return capacity; }
        public StringProperty currentOccupantsProperty() { return currentOccupants; }
        public StringProperty facilityProperty() { return facility; }
        public StringProperty roomStatusProperty() { return roomStatus; }
        public StringProperty monthlyRentProperty() { return monthlyRent; }

        // Regular getters
        public String getRoomId() { return roomId.get(); }
        public String getRoomType() { return roomType.get(); }
        public String getCapacity() { return capacity.get(); }
        public String getCurrentOccupants() { return currentOccupants.get(); }
        public String getFacility() { return facility.get(); }
        public String getRoomStatus() { return roomStatus.get(); }
        public String getMonthlyRent() { return monthlyRent.get(); }
    }
}