package com.hms.views.users.student;

import com.hms.utils.DB;
import com.hms.views.auth.Session;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TableCell;
import javafx.util.Duration;

import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class Rooms implements Initializable {

    /* ─────────────── UI ─────────────── */
    @FXML private Label txtavailable, txtfilled, selectedHostelLabel;

    /* Hostel table */
    @FXML private TableView<Hostel> hostel_table;
    @FXML private TableColumn<Hostel,String> hostelIdColumn, hostelNameColumn, hostelLocationColumn,
            totalRoomsColumn, availableRoomsColumn, hostelTypeColumn,
            contactPersonColumn, contactNumberColumn, emailColumn, descriptionColumn;

    /* Room table */
    @FXML private TableView<Room> room_table;
    @FXML private TableColumn<Room,String> roomIdColumn, roomTypeColumn, capacityColumn,
            currentOccupantsColumn, facilityColumn, roomStatusColumn, monthlyRentColumn;
    @FXML private TableColumn<Room,Void> applyActionColumn;

    /* DB */
    private Connection conn;
    private Timeline timeline;

    /* ───────────── INIT ───────────── */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DB.connect();

        setupHostelTableColumns();
        setupRoomTableColumns();
        loadHostels();

        hostel_table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedHostelLabel.setText("Selected Hostel: " + newSel.getHostelName());
                loadRoomsForHostel(newSel.getHostelId());
                updateStatistics();
            }
        });

        selectedHostelLabel.setText("Select a hostel to view rooms");
        room_table.setItems(FXCollections.observableArrayList());

        startClock();
    }

    /* ───────── Table setups ───────── */
    private void setupHostelTableColumns() {
        hostelIdColumn      .setCellValueFactory(c -> c.getValue().hostelIdProperty());
        hostelNameColumn    .setCellValueFactory(c -> c.getValue().hostelNameProperty());
        hostelLocationColumn.setCellValueFactory(c -> c.getValue().locationProperty());
        totalRoomsColumn    .setCellValueFactory(c -> c.getValue().totalRoomsProperty());
        availableRoomsColumn.setCellValueFactory(c -> c.getValue().availableRoomsProperty());
        hostelTypeColumn    .setCellValueFactory(c -> c.getValue().hostelTypeProperty());
        contactPersonColumn .setCellValueFactory(c -> c.getValue().contactPersonProperty());
        contactNumberColumn .setCellValueFactory(c -> c.getValue().contactNumberProperty());
        emailColumn         .setCellValueFactory(c -> c.getValue().emailProperty());
        descriptionColumn   .setCellValueFactory(c -> c.getValue().descriptionProperty());
    }

    private void setupRoomTableColumns() {
        roomIdColumn          .setCellValueFactory(c -> c.getValue().roomIdProperty());
        roomTypeColumn        .setCellValueFactory(c -> c.getValue().roomTypeProperty());
        capacityColumn        .setCellValueFactory(c -> c.getValue().capacityProperty());
        currentOccupantsColumn.setCellValueFactory(c -> c.getValue().currentOccupantsProperty());
        facilityColumn        .setCellValueFactory(c -> c.getValue().facilityProperty());
        roomStatusColumn      .setCellValueFactory(c -> c.getValue().roomStatusProperty());
        monthlyRentColumn     .setCellValueFactory(c -> c.getValue().monthlyRentProperty());

        /* Apply‑button column */
        applyActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Apply");

            {
                btn.getStyleClass().add("apply-button");
                btn.setOnAction(e -> {
                    Room   room   = getTableView().getItems().get(getIndex());
                    Hostel hostel = hostel_table.getSelectionModel().getSelectedItem();
                    if (hostel != null) handleRoomApplication(room, hostel);
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

                    btn.setDisable(!canApply);
                    btn.setText(
                            canApply ? "Apply"
                                     : ("Full".equalsIgnoreCase(room.getRoomStatus()) ? "Room Full"
                                                                                       : "Not Available"));
                    btn.setStyle(canApply
                            ? "-fx-background-color:#4CAF50;-fx-text-fill:white;"
                            : "-fx-background-color:#f44336;-fx-text-fill:white;");
                    setGraphic(btn);
                }
            }
        });
    }

    /* ───────── Load hostels ───────── */
    private void loadHostels() {
        ObservableList<Hostel> list = FXCollections.observableArrayList();
        String sql = """
                SELECT h.Hostel_ID, h.Hostel_Name, h.Location, h.Hostel_Type,
                       h.Contact_Person, h.Contact_Number, h.Email, h.Description,
                       COALESCE(rc.total_rooms,0)     AS Total_Rooms,
                       COALESCE(rc.available_rooms,0) AS Available_Rooms
                FROM hostels h
                LEFT JOIN (
                    SELECT Hostel_ID,
                           COUNT(Room_id)               AS total_rooms,
                           SUM(CASE WHEN Capacity > COALESCE(Current_Occupants,0) THEN 1 ELSE 0 END)
                                                          AS available_rooms
                    FROM rooms
                    GROUP BY Hostel_ID
                ) rc ON h.Hostel_ID = rc.Hostel_ID
                ORDER BY h.Hostel_Name
                """;

        try (PreparedStatement p = conn.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {

            while (r.next()) {
                list.add(new Hostel(
                        r.getString("Hostel_ID"),
                        r.getString("Hostel_Name"),
                        r.getString("Location"),
                        r.getString("Total_Rooms"),
                        r.getString("Available_Rooms"),
                        r.getString("Hostel_Type"),
                        r.getString("Contact_Person"),
                        r.getString("Contact_Number"),
                        r.getString("Email"),
                        r.getString("Description")
                ));
            }
            hostel_table.setItems(list);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Error loading hostels:\n" + e.getMessage());
        }
    }

    /* ───────── Load rooms ───────── */
    private void loadRoomsForHostel(String hostelId) {
        ObservableList<Room> list = FXCollections.observableArrayList();
        String sql = """
                SELECT Room_id, Room_type, Capacity,
                       COALESCE(Current_Occupants,0) AS Current_Occupants,
                       Facility, Monthly_Rent,
                       CASE
                           WHEN COALESCE(Current_Occupants,0) >= Capacity THEN 'Full'
                           WHEN Room_status='Available' AND COALESCE(Current_Occupants,0) < Capacity THEN 'Available'
                           ELSE 'Not Available'
                       END AS Room_status
                FROM rooms
                WHERE Hostel_ID = ?
                ORDER BY Room_id
                """;

        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, hostelId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    list.add(new Room(
                            r.getString("Room_id"),
                            r.getString("Room_type"),
                            r.getString("Capacity"),
                            r.getString("Current_Occupants"),
                            r.getString("Facility"),
                            r.getString("Room_status"),
                            r.getString("Monthly_Rent")
                    ));
                }
            }
            room_table.setItems(list);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Error loading rooms:\n" + e.getMessage());
        }
    }

    /* ───────── Statistics ───────── */
    private void updateStatistics() {
        Hostel h = hostel_table.getSelectionModel().getSelectedItem();
        if (h == null) return;

        String availSql  = "SELECT COUNT(*) FROM rooms WHERE Hostel_ID=? AND Capacity > COALESCE(Current_Occupants,0) AND Room_status='Available'";
        String filledSql = "SELECT COUNT(*) FROM rooms WHERE Hostel_ID=? AND Capacity <= COALESCE(Current_Occupants,0)";

        try (PreparedStatement pa = conn.prepareStatement(availSql);
             PreparedStatement pf = conn.prepareStatement(filledSql)) {

            pa.setString(1, h.getHostelId());
            pf.setString(1, h.getHostelId());

            int available = 0, filled = 0;

            try (ResultSet ra = pa.executeQuery()) {
                if (ra.next()) available = ra.getInt(1);
            }
            try (ResultSet rf = pf.executeQuery()) {
                if (rf.next()) filled = rf.getInt(1);
            }

            txtavailable.setText(String.valueOf(available));
            txtfilled.setText(String.valueOf(filled));

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Error updating stats:\n" + e.getMessage());
        }
    }

    /* ───────── Room application helpers ───────── */
    private boolean canApplyForRoom(Room r) {
        try {
            return Integer.parseInt(r.getCurrentOccupants()) < Integer.parseInt(r.getCapacity())
                    && "Available".equalsIgnoreCase(r.getRoomStatus());
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private void handleRoomApplication(Room room, Hostel hostel) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Apply for Room?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(String.format(
                "Apply for %s (Room %s)", hostel.getHostelName(), room.getRoomId()));

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            final int studentId = Session.getLoggedInStudentId();  // Make sure this method works
            
            
            if (studentId <= 0) {
                showAlert(Alert.AlertType.ERROR, "Login Required",
                          "Invalid or missing student ID. Please log in again.");
                return;   // stop – don’t insert anything
            }
            
            // 1. Check if already applied for this room with pending status
            String checkSql = """
                SELECT COUNT(*) FROM hostel_requests
                WHERE student_id = ? AND hostel_id = ? AND room_id = ? AND status = 'Pending'
            """;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, studentId);
                checkStmt.setInt(2, Integer.parseInt(hostel.getHostelId()));
                checkStmt.setInt(3, Integer.parseInt(room.getRoomId()));

                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showAlert(Alert.AlertType.WARNING, "Already Applied",
                            "You have already applied for this room and it's still pending.");
                    return;
                }
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "DB Error",
                        "Could not verify existing application:\n" + ex.getMessage());
                return;
            }

            String insertSql = """
            	    INSERT INTO hostel_requests
            	           (student_id, hostel_id, room_id,
            	            room_type_preference, facility_preference,
            	            request_date, status)
            	    VALUES (?, ?, ?, ?, ?, CURRENT_DATE, 'Pending')
            	""";

            	try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            	    ps.setInt   (1, studentId);
            	    ps.setInt   (2, Integer.parseInt(hostel.getHostelId()));
            	    ps.setInt   (3, Integer.parseInt(room.getRoomId()));
            	    ps.setString(4, room.getRoomType());   // << new
            	    ps.setString(5, room.getFacility());   // << new
            	    ps.executeUpdate();

            	    showAlert(Alert.AlertType.INFORMATION,
            	              "Application Submitted",
            	              "Your request is now Pending.");
            	} catch (SQLException ex) {
            	    showAlert(Alert.AlertType.ERROR,
            	              "DB Error", ex.getMessage());
            	}
        }

    }


    /* ───────── Clock (optional) ───────── */
    private void startClock() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /* ───────── Utility ───────── */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    /* ───────── Model classes ───────── */
    public static class Hostel {
        private final StringProperty hostelId, hostelName, location,
                totalRooms, availableRooms, hostelType,
                contactPerson, contactNumber, email, description;

        public Hostel(String id, String name, String loc, String total,
                      String avail, String type, String person,
                      String number, String email, String desc) {
            this.hostelId       = new SimpleStringProperty(id);
            this.hostelName     = new SimpleStringProperty(name);
            this.location       = new SimpleStringProperty(loc);
            this.totalRooms     = new SimpleStringProperty(total);
            this.availableRooms = new SimpleStringProperty(avail);
            this.hostelType     = new SimpleStringProperty(type);
            this.contactPerson  = new SimpleStringProperty(person);
            this.contactNumber  = new SimpleStringProperty(number);
            this.email          = new SimpleStringProperty(email);
            this.description    = new SimpleStringProperty(desc);
        }

        /* properties */
        public StringProperty hostelIdProperty()       { return hostelId; }
        public StringProperty hostelNameProperty()     { return hostelName; }
        public StringProperty locationProperty()       { return location; }
        public StringProperty totalRoomsProperty()     { return totalRooms; }
        public StringProperty availableRoomsProperty() { return availableRooms; }
        public StringProperty hostelTypeProperty()     { return hostelType; }
        public StringProperty contactPersonProperty()  { return contactPerson; }
        public StringProperty contactNumberProperty()  { return contactNumber; }
        public StringProperty emailProperty()          { return email; }
        public StringProperty descriptionProperty()    { return description; }

        /* getters */
        public String getHostelId()   { return hostelId.get(); }
        public String getHostelName() { return hostelName.get(); }
    }

    public static class Room {
        private final StringProperty roomId, roomType, capacity, currentOccupants,
                facility, roomStatus, monthlyRent;

        public Room(String id, String type, String cap, String occ,
                    String fac, String status, String rent) {
            this.roomId           = new SimpleStringProperty(id);
            this.roomType         = new SimpleStringProperty(type);
            this.capacity         = new SimpleStringProperty(cap);
            this.currentOccupants = new SimpleStringProperty(occ);
            this.facility         = new SimpleStringProperty(fac);
            this.roomStatus       = new SimpleStringProperty(status);
            this.monthlyRent      = new SimpleStringProperty(rent);
        }

        /* properties */
        public StringProperty roomIdProperty()           { return roomId; }
        public StringProperty roomTypeProperty()         { return roomType; }
        public StringProperty capacityProperty()         { return capacity; }
        public StringProperty currentOccupantsProperty() { return currentOccupants; }
        public StringProperty facilityProperty()         { return facility; }
        public StringProperty roomStatusProperty()       { return roomStatus; }
        public StringProperty monthlyRentProperty()      { return monthlyRent; }

        /* getters (used in controller) */
        public String getRoomId()           { return roomId.get(); }
        public String getRoomType()         { return roomType.get(); }
        public String getCapacity()         { return capacity.get(); }
        public String getCurrentOccupants() { return currentOccupants.get(); }
        public String getFacility()         { return facility.get(); }
        public String getRoomStatus()       { return roomStatus.get(); }
        public String getMonthlyRent()      { return monthlyRent.get(); }
    }
}
