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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Students implements Initializable {

    @FXML private TextField txtname;
    @FXML private TextField txtaddress;
    @FXML private ComboBox<String> txtgencom;
    @FXML private TextField txtguardian;
    @FXML private TextField txtstuid;
    @FXML private TextField txtstuyear;
    @FXML private TextField txtnic;
    @FXML private TextField txtcontact;
    @FXML private TextField txtemail;
    @FXML private TextField txtemgcontact;
    @FXML private TextField txtprogramme;
    @FXML private ComboBox<String> txtroomcom;

    @FXML private Button add_btn;
    @FXML private Button update_btn;
    @FXML private Button delete_btn;
    @FXML private Button clear_btn;

    @FXML private TableView<Student> student_table;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> addressColumn;
    @FXML private TableColumn<Student, String> genderColumn;
    @FXML private TableColumn<Student, String> guardianColumn;
    @FXML private TableColumn<Student, String> studentIdColumn;
    @FXML private TableColumn<Student, String> studyYearColumn;
    @FXML private TableColumn<Student, String> nicColumn;
    @FXML private TableColumn<Student, String> contactColumn;
    @FXML private TableColumn<Student, String> emailColumn;
    @FXML private TableColumn<Student, String> emgContactColumn;
    @FXML private TableColumn<Student, String> programmeColumn;
    @FXML private TableColumn<Student, String> roomColumn;

    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;
    private ObservableList<Student> studentList = FXCollections.observableArrayList();

    private static final String EMAIL_PATTERN
            = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        conn = DB.connect();
        txtname.requestFocus();
        fillCombobox();
        setupTableColumns();

        txtgencom.setItems(FXCollections.observableArrayList("Select", "Male", "Female"));
        txtgencom.setValue("Select");

        updateTable();
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        guardianColumn.setCellValueFactory(new PropertyValueFactory<>("guardian"));
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studyYearColumn.setCellValueFactory(new PropertyValueFactory<>("studyYear"));
        nicColumn.setCellValueFactory(new PropertyValueFactory<>("nic"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        emgContactColumn.setCellValueFactory(new PropertyValueFactory<>("emgContact"));
        programmeColumn.setCellValueFactory(new PropertyValueFactory<>("programme"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNo"));

        student_table.setItems(studentList);
    }

    private void fillCombobox() {
        try {
            String sql = "select * from rooms where Room_status='Available'";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            ObservableList<String> roomList = FXCollections.observableArrayList();
            roomList.add("Select");

            while (rs.next()) {
                String roomno = rs.getString("Room_id");
                roomList.add(roomno);
            }

            txtroomcom.setItems(roomList);
            txtroomcom.setValue("Select");

        } catch (SQLException e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
                showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void updateTable() {
        try {
            String sql = "select * from student";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            studentList.clear();

            while (rs.next()) {
                Student student = new Student(
                        rs.getString("Name"),
                        rs.getString("Address"),
                        rs.getString("Gender"),
                        rs.getString("Guardian"),
                        rs.getString("Student_id"),
                        rs.getString("Study_year"),
                        rs.getString("NIC"),
                        rs.getString("Contact_no"),
                        rs.getString("Email"),
                        rs.getString("Emg_contact"),
                        rs.getString("Programme"),
                        rs.getString("Room_no")
                );
                studentList.add(student);
            }

        } catch (SQLException e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
                showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void clear_btnActionPerformed(ActionEvent event) {
        txtname.setText("");
        txtaddress.setText("");
        txtcontact.setText("");
        txtemail.setText("");
        txtemgcontact.setText("");
        txtguardian.setText("");
        txtnic.setText("");
        txtprogramme.setText("");
        txtstuid.setText("");
        txtstuyear.setText("");
        txtgencom.setValue("Select");
        txtroomcom.setValue("Select");
    }

    @FXML
    private void delete_btnActionPerformed(ActionEvent event) {
        if (validateFields()) {
            if (validateContactNumbers()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Delete");
                confirmAlert.setHeaderText("Are you sure you want to delete record?");

                if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                    logActivity("Student Details Deleted");

                    try {
                        String sql = "delete from student where Student_id=?";
                        pst = conn.prepareStatement(sql);
                        pst.setString(1, txtstuid.getText());
                        pst.execute();

                        showAlert("Success", "Record Deleted", Alert.AlertType.INFORMATION);
                        updateTable();

                    } catch (SQLException e) {
                        showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
                    } finally {
                        try {
                            if (rs != null) rs.close();
                            if (pst != null) pst.close();
                        } catch (SQLException e) {
                            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
                        }
                    }
                }
            } else {
                showAlert("Error", "Enter valid contact number", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Error", "One or more required fields are empty", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void update_btnActionPerformed(ActionEvent event) {
        if (validateFields()) {
            if (validateContactNumbers()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Update Record");
                confirmAlert.setHeaderText("Are you sure you want to update?");

                if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                    try {
                        String sql = "update student set Name=?, Address=?, Gender=?, Guardian=?, Student_id=?, Study_year=?, NIC=?, Contact_no=?, Email=?, Emg_contact=?, Programme=?, Room_no=? where Student_id=?";

                        pst = conn.prepareStatement(sql);
                        pst.setString(1, txtname.getText());
                        pst.setString(2, txtaddress.getText());
                        pst.setString(3, txtgencom.getValue());
                        pst.setString(4, txtguardian.getText());
                        pst.setString(5, txtstuid.getText());
                        pst.setString(6, txtstuyear.getText());
                        pst.setString(7, txtnic.getText());
                        pst.setString(8, txtcontact.getText());
                        pst.setString(9, txtemail.getText());
                        pst.setString(10, txtemgcontact.getText());
                        pst.setString(11, txtprogramme.getText());
                        pst.setString(12, txtroomcom.getValue());
                        pst.setString(13, txtstuid.getText());

                        pst.execute();
                        showAlert("Success", "Record Updated", Alert.AlertType.INFORMATION);

                        logActivity("Student Details Updated");
                        updateTable();

                    } catch (SQLException e) {
                        showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
                    } finally {
                        try {
                            if (rs != null) rs.close();
                            if (pst != null) pst.close();
                        } catch (SQLException e) {
                            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
                        }
                    }
                }
            } else {
                showAlert("Error", "Enter valid contact number", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Error", "One or more required fields are empty", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void add_btnActionPerformed(ActionEvent event) {
        if (validateFields()) {
            if (validateContactNumbers()) {
                try {
                    String sql = "insert into student (Name,Address,Gender,Guardian,Student_id,Study_year,NIC,Contact_no,Email,Emg_contact,Programme,Room_no) values (?,?,?,?,?,?,?,?,?,?,?,?)";

                    pst = conn.prepareStatement(sql);
                    pst.setString(1, txtname.getText());
                    pst.setString(2, txtaddress.getText());
                    pst.setString(3, txtgencom.getValue());
                    pst.setString(4, txtguardian.getText());
                    pst.setString(5, txtstuid.getText());
                    pst.setString(6, txtstuyear.getText());
                    pst.setString(7, txtnic.getText());
                    pst.setString(8, txtcontact.getText());
                    pst.setString(9, txtemail.getText());
                    pst.setString(10, txtemgcontact.getText());
                    pst.setString(11, txtprogramme.getText());
                    pst.setString(12, txtroomcom.getValue());

                    pst.execute();
                    showAlert("Success", "Data is saved successfully", Alert.AlertType.INFORMATION);

                    logActivity("New Student Added");
                    updateTable();

                } catch (SQLException e) {
                    showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
                } finally {
                    try {
                        if (rs != null) rs.close();
                        if (pst != null) pst.close();
                    } catch (SQLException e) {
                        showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            } else {
                showAlert("Error", "Enter valid contact number", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Error", "One or more required fields are empty", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void student_tableMouseClicked(MouseEvent event) {
        Student selectedStudent = student_table.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            txtname.setText(selectedStudent.getName());
            txtaddress.setText(selectedStudent.getAddress());
            txtgencom.setValue(selectedStudent.getGender());
            txtguardian.setText(selectedStudent.getGuardian());
            txtstuid.setText(selectedStudent.getStudentId());
            txtstuyear.setText(selectedStudent.getStudyYear());
            txtnic.setText(selectedStudent.getNic());
            txtcontact.setText(selectedStudent.getContact());
            txtemail.setText(selectedStudent.getEmail());
            txtemgcontact.setText(selectedStudent.getEmgContact());
            txtprogramme.setText(selectedStudent.getProgramme());
            txtroomcom.setValue(selectedStudent.getRoomNo());
        }
    }

    @FXML
    private void txtcontactKeyReleased(KeyEvent event) {
        try {
            if (!txtcontact.getText().isEmpty()) {
                Integer.parseInt(txtcontact.getText());
            }
        } catch (NumberFormatException e) {
            txtcontact.setText("");
            showAlert("Error", "You can add numbers only", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void txtemgcontactKeyReleased(KeyEvent event) {
        try {
            if (!txtemgcontact.getText().isEmpty()) {
                Integer.parseInt(txtemgcontact.getText());
            }
        } catch (NumberFormatException e) {
            txtemgcontact.setText("");
            showAlert("Error", "You can add numbers only", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void txtemailFocusLost(ActionEvent event) {
        String email = txtemail.getText();
        if (!email.isEmpty()) {
            Matcher matcher = pattern.matcher(email);
            if (!matcher.matches()) {
                txtemail.setText("");
                showAlert("Error", "Enter valid email", Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validateFields() {
        return !(txtname.getText().isEmpty() || txtaddress.getText().isEmpty() ||
                txtcontact.getText().isEmpty() || txtguardian.getText().isEmpty() ||
                txtemgcontact.getText().isEmpty() || txtnic.getText().isEmpty() ||
                txtprogramme.getText().isEmpty() || txtstuid.getText().isEmpty() ||
                txtstuyear.getText().isEmpty() || txtemail.getText().isEmpty() ||
                txtgencom.getValue().equals("Select") || txtroomcom.getValue().equals("Select"));
    }

    private boolean validateContactNumbers() {
        return txtcontact.getText().length() == 10 && txtemgcontact.getText().length() == 10;
    }

    private void logActivity(String activity) {
        Date currentDate = GregorianCalendar.getInstance().getTime();
        DateFormat df = DateFormat.getDateInstance();
        String dateString = df.format(currentDate);

        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String timeString = sdf.format(d);

        try {
            String reg = "insert into logs (User_id, Date, Status) values (?, ?, ?)";
            pst = conn.prepareStatement(reg);
            pst.setInt(1, Emp.UserId);
            pst.setString(2, timeString + " / " + dateString);
            pst.setString(3, activity);
            pst.execute();
        } catch (SQLException e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Student model class
    public static class Student {
        private String name;
        private String address;
        private String gender;
        private String guardian;
        private String studentId;
        private String studyYear;
        private String nic;
        private String contact;
        private String email;
        private String emgContact;
        private String programme;
        private String roomNo;

        public Student(String name, String address, String gender, String guardian, String studentId,
                       String studyYear, String nic, String contact, String email, String emgContact,
                       String programme, String roomNo) {
            this.name = name;
            this.address = address;
            this.gender = gender;
            this.guardian = guardian;
            this.studentId = studentId;
            this.studyYear = studyYear;
            this.nic = nic;
            this.contact = contact;
            this.email = email;
            this.emgContact = emgContact;
            this.programme = programme;
            this.roomNo = roomNo;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getGuardian() { return guardian; }
        public void setGuardian(String guardian) { this.guardian = guardian; }

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }

        public String getStudyYear() { return studyYear; }
        public void setStudyYear(String studyYear) { this.studyYear = studyYear; }

        public String getNic() { return nic; }
        public void setNic(String nic) { this.nic = nic; }

        public String getContact() { return contact; }
        public void setContact(String contact) { this.contact = contact; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getEmgContact() { return emgContact; }
        public void setEmgContact(String emgContact) { this.emgContact = emgContact; }

        public String getProgramme() { return programme; }
        public void setProgramme(String programme) { this.programme = programme; }

        public String getRoomNo() { return roomNo; }
        public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    }
}