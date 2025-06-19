package com.hms.views.users.admin;

import com.hms.utils.DB;
import com.hms.utils.Emp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;

import java.net.URL;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 𝗦𝘁𝘂𝗱𝗲𝗻𝘁𝘀 admin controller ‑‑ *adapted to new student table schema*.
 *
 * Table `student` now contains (camel‑case → DB snake_case):
 *   student_id     INT PK    | txtStudentId
 *   username       VARCHAR   | txtUsername (optional)
 *   first_name     VARCHAR   | txtFirstName
 *   last_name      VARCHAR   | txtLastName
 *   gender         VARCHAR   | txtGender
 *   mobile_number  VARCHAR   | txtMobile
 *   email          VARCHAR   | txtEmail
 *   study_program  VARCHAR   | txtProgram
 *   room_no        VARCHAR   | txtRoomCombo
 *   password       VARCHAR   | (not handled here)
 *
 * Columns that existed in the old schema (Address, Guardian, NIC, Emg_contact, Study_year
 * etc.) were dropped, so all related UI fields / SQL parts have been removed.
 */
public class Students implements Initializable {

    /* ────────── UI FIELDS ────────── */
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private ComboBox<String> txtGender;
    @FXML private TextField txtStudentId;
    @FXML private TextField txtUsername;
    @FXML private TextField txtMobile;
    @FXML private TextField txtEmail;
    @FXML private TextField txtProgram;
    @FXML private ComboBox<String> txtRoomCombo;

    @FXML private Button add_btn, update_btn, delete_btn, clear_btn;

    /* ────────── TABLE ────────── */
    @FXML private TableView<Student> student_table;
    @FXML private TableColumn<Student,String> firstNameCol, lastNameCol, genderCol, studentIdCol,
                                              usernameCol, mobileCol, emailCol, programCol, roomCol;

    /* ────────── DB ────────── */
    private Connection conn;
    private PreparedStatement pst;
    private ResultSet rs;
    private final ObservableList<Student> studentList = FXCollections.observableArrayList();

    /* ────────── Validation ────────── */
    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"+
            "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");

    @Override public void initialize(URL url, ResourceBundle rb) {
        conn = DB.connect();

        txtGender.setItems(FXCollections.observableArrayList("Select", "Male", "Female"));
        txtGender.setValue("Select");
        fillRoomCombo();
        setupTable();
        updateTable();
    }

    /* ────────── Setup TableView ────────── */
    private void setupTable() {
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        mobileCol.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        programCol.setCellValueFactory(new PropertyValueFactory<>("program"));
        roomCol.setCellValueFactory(new PropertyValueFactory<>("roomNo"));
        student_table.setItems(studentList);
    }

    /* ────────── Populate available rooms ────────── */
    private void fillRoomCombo() {
        try (PreparedStatement p = conn.prepareStatement(
                "SELECT Room_id FROM rooms WHERE Room_status='Available'");
             ResultSet r = p.executeQuery()) {

            ObservableList<String> rooms = FXCollections.observableArrayList();
            rooms.add("Select");
            while (r.next()) rooms.add(r.getString(1));
            txtRoomCombo.setItems(rooms);
            txtRoomCombo.setValue("Select");
        } catch (SQLException e) { showAlert("Error", e.getMessage(), Alert.AlertType.ERROR); }
    }

    /* ────────── Refresh table data ────────── */
    private void updateTable() {
        String sql = "SELECT * FROM student";
        try (PreparedStatement p = conn.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            studentList.clear();
            while (r.next()) {
                studentList.add(new Student(
                        r.getString("first_name"),
                        r.getString("last_name"),
                        r.getString("gender"),
                        r.getString("student_id"),
                        r.getString("username"),
                        r.getString("mobile_number"),
                        r.getString("email"),
                        r.getString("study_program"),
                        r.getString("room_no")
                ));
            }
        } catch (SQLException e) { showAlert("Error", e.getMessage(), Alert.AlertType.ERROR); }
    }

    /* ────────── CRUD ────────── */

    @FXML private void add(ActionEvent e) {
        if (!validateFields()) return;
        String sql = "INSERT INTO student (first_name,last_name,gender,student_id,username,mobile_number,email,study_program,room_no,password) "+
                     "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, txtFirstName.getText());
            p.setString(2, txtLastName.getText());
            p.setString(3, txtGender.getValue());
            p.setString(4, txtStudentId.getText());
            p.setString(5, txtUsername.getText());
            p.setString(6, txtMobile.getText());
            p.setString(7, txtEmail.getText());
            p.setString(8, txtProgram.getText());
            p.setString(9, txtRoomCombo.getValue());
            p.setString(10, "default"); // or ask user – we don't handle passwords here
            p.executeUpdate();
            showAlert("Success", "Student added", Alert.AlertType.INFORMATION);
            log("Added new student");
            updateTable();
        } catch (SQLException ex) { showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR); }
    }

    @FXML private void update(ActionEvent e) {
        if (!validateFields()) return;
        String sql = "UPDATE student SET first_name=?,last_name=?,gender=?,username=?,mobile_number=?,email=?,study_program=?,room_no=? WHERE student_id=?";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, txtFirstName.getText());
            p.setString(2, txtLastName.getText());
            p.setString(3, txtGender.getValue());
            p.setString(4, txtUsername.getText());
            p.setString(5, txtMobile.getText());
            p.setString(6, txtEmail.getText());
            p.setString(7, txtProgram.getText());
            p.setString(8, txtRoomCombo.getValue());
            p.setString(9, txtStudentId.getText());
            p.executeUpdate();
            showAlert("Success", "Record updated", Alert.AlertType.INFORMATION);
            log("Updated student details");
            updateTable();
        } catch (SQLException ex) { showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR); }
    }

    @FXML private void delete(ActionEvent e) {
        if (txtStudentId.getText().isEmpty()) { showAlert("Error", "Student ID required", Alert.AlertType.ERROR); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this student?", ButtonType.OK, ButtonType.CANCEL);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try (PreparedStatement p = conn.prepareStatement("DELETE FROM student WHERE student_id=?")) {
            p.setString(1, txtStudentId.getText());
            p.executeUpdate();
            showAlert("Success", "Record deleted", Alert.AlertType.INFORMATION);
            log("Deleted student");
            updateTable();
        } catch (SQLException ex) { showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR); }
    }

    @FXML private void clear(ActionEvent e) {
        txtFirstName.clear(); txtLastName.clear(); txtStudentId.clear(); txtUsername.clear();
        txtMobile.clear(); txtEmail.clear(); txtProgram.clear();
        txtGender.setValue("Select"); txtRoomCombo.setValue("Select");
    }

    /* ────────── Table click ────────── */
    @FXML private void tableClicked(MouseEvent e) {
        Student s = student_table.getSelectionModel().getSelectedItem();
        if (s == null) return;
        txtFirstName.setText(s.getFirstName());
        txtLastName.setText(s.getLastName());
        txtGender.setValue(s.getGender());
        txtStudentId.setText(s.getStudentId());
        txtUsername.setText(s.getUsername());
        txtMobile.setText(s.getMobile());
        txtEmail.setText(s.getEmail());
        txtProgram.setText(s.getProgram());
        txtRoomCombo.setValue(s.getRoomNo());
    }

    /* ────────── Validation helpers ────────── */
    private boolean validateFields() {
        if (txtFirstName.getText().isEmpty() || txtLastName.getText().isEmpty() ||
            txtStudentId.getText().isEmpty() || txtUsername.getText().isEmpty() ||
            txtMobile.getText().isEmpty() || txtEmail.getText().isEmpty() ||
            txtProgram.getText().isEmpty() || txtGender.getValue().equals("Select") ||
            txtRoomCombo.getValue().equals("Select")) {
            showAlert("Error", "Please fill all required fields", Alert.AlertType.ERROR);
            return false;
        }
        if (txtMobile.getText().length() != 10) {
            showAlert("Error", "Mobile number must be 10 digits", Alert.AlertType.ERROR);
            return false;
        }
        Matcher m = EMAIL_REGEX.matcher(txtEmail.getText());
        if (!m.matches()) {
            showAlert("Error", "Invalid email", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    /* ────────── Log helper ────────── */
    private void log(String activity) {
        String sql = "INSERT INTO logs (User_id, Date, Status) VALUES (?,?,?)";
        String timestamp = new SimpleDateFormat("HH:mm:ss / dd-MMM-yyyy").format(new Date());
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, Emp.UserId);
            p.setString(2, timestamp);
            p.setString(3, activity);
            p.execute();
        } catch (SQLException ignored) {}
    }

    /* ────────── Alert helper ────────── */
    private void showAlert(String title, String msg, Alert.AlertType t) {
        Alert a = new Alert(t);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    /* ────────── Inner model ────────── */
    public static class Student {
        private final String firstName, lastName, gender, studentId, username,
                             mobile, email, program, roomNo;
        public Student(String fn,String ln,String g,String id,String un,String mob,String em,String prog,String room){
            firstName=fn; lastName=ln; gender=g; studentId=id; username=un;
            mobile=mob; email=em; program=prog; roomNo=room;
        }
        public String getFirstName(){return firstName;}
        public String getLastName(){return lastName;}
        public String getGender(){return gender;}
        public String getStudentId(){return studentId;}
        public String getUsername(){return username;}
        public String getMobile(){return mobile;}
        public String getEmail(){return email;}
        public String getProgram(){return program;}
        public String getRoomNo(){return roomNo;}
    }
}
