package com.hms.views.users.admin;

import com.hms.utils.DB;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class Requests implements Initializable {

    /* ────────────  FXML  ──────────── */
    @FXML private TextField  txt_search;      // search by student_id or status
    @FXML private TextArea   txt_remarks;     // remarks for approve / reject
    @FXML private Button     btnApprove;
    @FXML private Button     btnReject;
    @FXML private Button     btnReset;

    @FXML private TableView<HostelRequest> tbl_requests;
    @FXML private TableColumn<HostelRequest, String> colRequestId;
    @FXML private TableColumn<HostelRequest, String> colStudentId;
    @FXML private TableColumn<HostelRequest, String> colHostelId;
    @FXML private TableColumn<HostelRequest, String> colRoomType;
    @FXML private TableColumn<HostelRequest, String> colFacilityPref;
    @FXML private TableColumn<HostelRequest, String> colReqDate;
    @FXML private TableColumn<HostelRequest, String> colStatus;
    @FXML private TableColumn<HostelRequest, String> colRemarks;

    /* ───────── DB & helpers ───────── */
    private Connection         conn;
    private final ObservableList<HostelRequest> reqList = FXCollections.observableArrayList();
    private Timeline           clock;         // optional real‑time clock

    /* ───────── initialize() ───────── */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DB.connect();
        setupColumns();
        populateTable();
        wireButtonDisable();
        // startClock(); // uncomment if you actually show time somewhere
    }

    private void setupColumns() {
        colRequestId   .setCellValueFactory(c -> c.getValue().requestIdProperty());
        colStudentId   .setCellValueFactory(c -> c.getValue().studentIdProperty());
        colHostelId    .setCellValueFactory(c -> c.getValue().hostelIdProperty());
        colRoomType    .setCellValueFactory(c -> c.getValue().roomTypeProperty());
        colFacilityPref.setCellValueFactory(c -> c.getValue().facilityPrefProperty());
        colReqDate     .setCellValueFactory(c -> c.getValue().requestDateProperty());
        colStatus      .setCellValueFactory(c -> c.getValue().statusProperty());
        colRemarks     .setCellValueFactory(c -> c.getValue().remarksProperty());
        tbl_requests.setItems(reqList);
        tbl_requests.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void wireButtonDisable() {
        tbl_requests.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean disable = n == null;
            btnApprove.setDisable(disable);
            btnReject .setDisable(disable);
        });
        // initial state
        btnApprove.setDisable(true);
        btnReject .setDisable(true);
    }

    /* ───────── live clock (optional) ───────── */
    private void startClock() {
        clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            // if you have a Label for time, setText here
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
    private void stopClock() {
        if (clock != null) clock.stop();
    }

    /* ───────── table population ───────── */
    private void populateTable() {
        reqList.clear();
        String sql = "SELECT * FROM hostel_requests ORDER BY request_date DESC";
        try (PreparedStatement p = conn.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {

            while (r.next()) {
                reqList.add(new HostelRequest(
                        r.getString("request_id"),
                        r.getString("student_id"),
                        r.getString("hostel_id"),
                        r.getString("room_type_preference"),
                        r.getString("facility_preference"),
                        r.getString("request_date"),
                        r.getString("status"),
                        r.getString("remarks")
                ));
            }
        } catch (SQLException ex) {
            showAlert("DB Error", ex.getMessage());
        }
    }

    /* ───────── search field ───────── */
    @FXML
    private void onSearchKeyReleased(KeyEvent evt) {
        String key = txt_search.getText().trim();
        if (key.isEmpty()) { populateTable(); return; }

        reqList.clear();
        String sql = """
                     SELECT * FROM hostel_requests
                     WHERE CAST(student_id AS TEXT) LIKE ?
                        OR LOWER(status) LIKE LOWER(?)
                     ORDER BY request_date DESC
                     """;
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, "%" + key + "%");
            p.setString(2, "%" + key + "%");
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    reqList.add(new HostelRequest(
                            rs.getString("request_id"),
                            rs.getString("student_id"),
                            rs.getString("hostel_id"),
                            rs.getString("room_type_preference"),
                            rs.getString("facility_preference"),
                            rs.getString("request_date"),
                            rs.getString("status"),
                            rs.getString("remarks")
                    ));
                }
            }
        } catch (SQLException ex) {
            showAlert("DB Error", ex.getMessage());
        }
    }

    /* ───────── approve / reject ───────── */
    @FXML
    private void onApproveClicked() { updateStatus("Approved"); }
    @FXML
    private void onRejectClicked()  { updateStatus("Rejected"); }

    private void updateStatus(String newStatus) {

        HostelRequest sel = tbl_requests.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("No Selection", "Please select a request first."); return; }

        String remarks = txt_remarks.getText().trim();
        if (remarks.isEmpty()) { showAlert("Missing Remarks", "Please enter remarks before proceeding."); return; }

        try {
            conn.setAutoCommit(false);     // ── begin TX ──

            /* 1 ─ update hostel_requests */
            String updReq = "UPDATE hostel_requests SET status=?, remarks=? WHERE request_id=?";
            try (PreparedStatement p = conn.prepareStatement(updReq)) {
                p.setString(1, newStatus);
                p.setString(2, remarks);
                p.setString(3, sel.getRequestId());
                p.executeUpdate();
            }

            /* 2 ─ extra work only on approval */
            if ("Approved".equals(newStatus)) {
                String roomId = allocateRoomForRequest(sel);
                if (roomId == null) {
                    conn.rollback();
                    showAlert("No Rooms", "No available room matching the preference.");
                    return;
                }
                insertAllotment(sel.getStudentId(), sel.getHostelId(), roomId);
                updateStudentRoom(sel.getStudentId(), roomId);
            }

            conn.commit();                 // ── end TX ──
            showAlert("Success", "Request " + newStatus.toLowerCase() + "!");
            populateTable();
            txt_remarks.clear();
            tbl_requests.getSelectionModel().clearSelection();

        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            showAlert("DB Error", ex.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /* ───────── room allocation helpers ───────── */

    /** Find a vacant room, mark it occupied/part‑occupied, return its room_id; null if none. */
    private String allocateRoomForRequest(HostelRequest req) throws SQLException {

        String find = """
            SELECT room_id, capacity, current_occupants
            FROM rooms
            WHERE hostel_id = ?
              AND room_type = ?
              AND room_status = 'Available'
            LIMIT 1
        """;
        try (PreparedStatement pFind = conn.prepareStatement(find)) {
            pFind.setString(1, req.getHostelId());
            pFind.setString(2, req.getRoomType());
            try (ResultSet r = pFind.executeQuery()) {
                if (!r.next()) return null;           // none free

                String roomId   = r.getString("room_id");
                int capacity    = r.getInt("capacity");
                int occupants   = r.getInt("current_occupants") + 1; // will be 1

                String status   = (occupants == capacity) ? "Occupied" : "Partially Occupied";
                String updRoom  = "UPDATE rooms SET current_occupants=?, room_status=? WHERE room_id=?";
                try (PreparedStatement pUpd = conn.prepareStatement(updRoom)) {
                    pUpd.setInt   (1, occupants);
                    pUpd.setString(2, status);
                    pUpd.setString(3, roomId);
                    pUpd.executeUpdate();
                }
                return roomId;
            }
        }	
    }

    /** Insert a row into room_allotments. */
    private void insertAllotment(String studentId, String hostelId, String roomId) throws SQLException {
        String ins = """
            INSERT INTO room_allotments
                  (student_id, room_id, hostel_id, allotment_date)
            VALUES (?, ?, ?, CURRENT_DATE)
        """;
        try (PreparedStatement p = conn.prepareStatement(ins)) {
            p.setString(1, studentId);
            p.setString(2, roomId);
            p.setString(3, hostelId);
            p.executeUpdate();
        }
    }
    
    private void updateStudentRoom(String studentId, String roomId) throws SQLException {
        String sql = "UPDATE student SET room_no = ? WHERE student_id = ?";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, roomId);
            p.setString(2, studentId);
            p.executeUpdate();
        }
    }
    /* ───────── reset button ───────── */
    @FXML
    private void onResetClicked() {
        txt_search.clear();
        txt_remarks.clear();
        tbl_requests.getSelectionModel().clearSelection();
        populateTable();
    }

    /* ───────── helpers ───────── */
    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    /* ───────── cleanup on view close ───────── */
    public void cleanup() {
        stopClock();
        try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
    }

    /* ───────── POJO for TableView ───────── */
    public static class HostelRequest {
        private final SimpleStringProperty requestId;
        private final SimpleStringProperty studentId;
        private final SimpleStringProperty hostelId;
        private final SimpleStringProperty roomType;
        private final SimpleStringProperty facilityPref;
        private final SimpleStringProperty requestDate;
        private final SimpleStringProperty status;
        private final SimpleStringProperty remarks;

        public HostelRequest(String requestId, String studentId, String hostelId,
                             String roomType, String facilityPref,
                             String requestDate, String status, String remarks) {
            this.requestId    = new SimpleStringProperty(requestId);
            this.studentId    = new SimpleStringProperty(studentId);
            this.hostelId     = new SimpleStringProperty(hostelId);
            this.roomType     = new SimpleStringProperty(roomType);
            this.facilityPref = new SimpleStringProperty(facilityPref);
            this.requestDate  = new SimpleStringProperty(requestDate);
            this.status       = new SimpleStringProperty(status);
            this.remarks      = new SimpleStringProperty(remarks);
        }

        /* properties for TableView */
        public SimpleStringProperty requestIdProperty()    { return requestId; }
        public SimpleStringProperty studentIdProperty()    { return studentId; }
        public SimpleStringProperty hostelIdProperty()     { return hostelId; }
        public SimpleStringProperty roomTypeProperty()     { return roomType; }
        public SimpleStringProperty facilityPrefProperty() { return facilityPref; }
        public SimpleStringProperty requestDateProperty()  { return requestDate; }
        public SimpleStringProperty statusProperty()       { return status; }
        public SimpleStringProperty remarksProperty()      { return remarks; }

        /* getters (full set, in case you need them elsewhere) */
        public String getRequestId()   { return requestId.get(); }
        public String getStudentId()   { return studentId.get(); }
        public String getHostelId()    { return hostelId.get(); }
        public String getRoomType()    { return roomType.get(); }
        public String getFacilityPref(){ return facilityPref.get(); }
        public String getRequestDate() { return requestDate.get(); }
        public String getStatus()      { return status.get(); }
        public String getRemarks()     { return remarks.get(); }
    }
}
