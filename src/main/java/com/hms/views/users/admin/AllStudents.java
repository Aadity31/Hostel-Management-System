package com.hms.views.users.admin;

import com.hms.utils.DB;
import com.hms.utils.Emp;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import static com.itextpdf.text.Element.ALIGN_CENTER;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
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

public class AllStudents implements Initializable {

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> addressColumn;
    @FXML private TableColumn<Student, String> genderColumn;
    @FXML private TableColumn<Student, String> guardianColumn;
    @FXML private TableColumn<Student, String> studentIdColumn;
    @FXML private TableColumn<Student, String> studyYearColumn;
    @FXML private TableColumn<Student, String> nicColumn;
    @FXML private TableColumn<Student, String> contactColumn;
    @FXML private TableColumn<Student, String> emailColumn;
    @FXML private TableColumn<Student, String> emergencyContactColumn;
    @FXML private TableColumn<Student, String> programmeColumn;
    @FXML private TableColumn<Student, String> roomColumn;
    @FXML private TextField searchTextField;
    @FXML private Button resetButton;
    @FXML private Button pdfButton;
    @FXML private Button excelButton;

    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;
    private ObservableList<Student> studentList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conn = DB.connect();
        setupTableColumns();
        updateTable();
        setupSearchListener();
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
        emergencyContactColumn.setCellValueFactory(new PropertyValueFactory<>("emergencyContact"));
        programmeColumn.setCellValueFactory(new PropertyValueFactory<>("programme"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("room"));
    }

    private void setupSearchListener() {
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                updateTable();
            } else {
                searchStudent(newValue);
            }
        });
    }

    private void updateTable() {
        studentList.clear();
        try {
            String sql = "SELECT CONCAT(first_name, ' ', last_name) AS name, address, gender, guardian, student_id, " +
                         "study_year, nic, contact_no, email, emg_contact, programme, room_no FROM student";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            while (rs.next()) {
                Student student = new Student(
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("gender"),
                        rs.getString("guardian"),
                        rs.getString("student_id"),
                        rs.getString("study_year"),
                        rs.getString("nic"),
                        rs.getString("contact_no"),
                        rs.getString("email"),
                        rs.getString("emg_contact"),
                        rs.getString("programme"),
                        rs.getString("room_no")
                );
                studentList.add(student);
            }
            studentTable.setItems(studentList);
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading student data: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
                showAlert("Error", "Error closing database resources: " + e.getMessage());
            }
        }
    }


    private void searchStudent(String studentId) {
        studentList.clear();
        try {
            String sql = "SELECT * FROM student WHERE student_id = ?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, studentId);
            rs = pst.executeQuery();

            while (rs.next()) {
                Student student = new Student(
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("gender"),
                        rs.getString("guardian"),
                        rs.getString("student_id"),
                        rs.getString("study_year"),
                        rs.getString("nic"),
                        rs.getString("contact_no"),
                        rs.getString("email"),
                        rs.getString("emg_contact"),
                        rs.getString("programme"),
                        rs.getString("room_no")
                );
                studentList.add(student);
            }
            studentTable.setItems(studentList);
        } catch (SQLException e) {
            showAlert("Database Error", "Error searching student: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
                showAlert("Error", "Error closing database resources: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleReset(ActionEvent event) {
        searchTextField.clear();
        updateTable();
    }

    @FXML
    private void handleExportPdf(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export as PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        Stage stage = (Stage) pdfButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                exportToPdf(file.getAbsolutePath());
                showAlert("Success", "PDF exported successfully!");
                logActivity("Export student details as PDF");
            } catch (Exception e) {
                showAlert("Export Error", "Error exporting to PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportExcel(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );

        Stage stage = (Stage) excelButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                exportToExcel(file.getAbsolutePath());
                showAlert("Success", "Excel file exported successfully!");
                logActivity("Export student details to Excel");
            } catch (Exception e) {
                showAlert("Export Error", "Error exporting to Excel: " + e.getMessage());
            }
        }
    }

    private void exportToPdf(String filePath) throws DocumentException, FileNotFoundException {
        com.itextpdf.text.Font blueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12,
                com.itextpdf.text.Font.NORMAL, new BaseColor(25, 27, 82));
        com.itextpdf.text.Document doc = new com.itextpdf.text.Document(PageSize.A4.rotate());

        Calendar cal = new GregorianCalendar();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        doc.open();

        doc.add(new Paragraph(new Chunk("Student Details Report\n", blueFont)));
        doc.add(new Paragraph(year + "." + (month + 1) + "." + day));

        PdfPTable table = new PdfPTable(12);
        table.setHorizontalAlignment(ALIGN_CENTER);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Header
        PdfPCell headerCell = new PdfPCell(new Paragraph("Student Details"));
        headerCell.setColspan(12);
        headerCell.setHorizontalAlignment(ALIGN_CENTER);
        headerCell.setBackgroundColor(new BaseColor(0, 166, 80));
        headerCell.setPadding(10.0f);
        table.addCell(headerCell);

        // Column headers
        String[] headers = {"Name", "Address", "Gender", "Guardian", "Student_id",
                "Study_year", "NIC", "Contact_no", "Email", "Emg_contact", "Programme", "Room_no"};

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setPadding(5.0f);
            table.addCell(cell);
        }

        // Data rows
        for (Student student : studentTable.getItems()) {
            table.addCell(student.getName());
            table.addCell(student.getAddress());
            table.addCell(student.getGender());
            table.addCell(student.getGuardian());
            table.addCell(student.getStudentId());
            table.addCell(student.getStudyYear());
            table.addCell(student.getNic());
            table.addCell(student.getContact());
            table.addCell(student.getEmail());
            table.addCell(student.getEmergencyContact());
            table.addCell(student.getProgramme());
            table.addCell(student.getRoom());
        }

        doc.add(table);
        doc.close();
    }

    private void exportToExcel(String filePath) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Student Details Report");

        // Set column widths
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 10000);
        sheet.setColumnWidth(3, 10000);
        sheet.setColumnWidth(4, 4000);
        sheet.setColumnWidth(5, 4000);
        sheet.setColumnWidth(6, 4000);
        sheet.setColumnWidth(7, 8000);
        sheet.setColumnWidth(8, 4000);
        sheet.setColumnWidth(9, 4000);
        sheet.setColumnWidth(10, 4000);
        sheet.setColumnWidth(11, 4000);

        // Create header row
        XSSFRow headerRow = sheet.createRow(0);
        String[] headers = {"Name", "Address", "Gender", "Guardian", "Student ID",
                "Study Year", "NIC/Passport", "Contact No", "Email", "Emg Contact",
                "Study Programme", "Room No"};

        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Create data rows
        int rowNum = 1;
        for (Student student : studentTable.getItems()) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getName());
            row.createCell(1).setCellValue(student.getAddress());
            row.createCell(2).setCellValue(student.getGender());
            row.createCell(3).setCellValue(student.getGuardian());
            row.createCell(4).setCellValue(student.getStudentId());
            row.createCell(5).setCellValue(student.getStudyYear());
            row.createCell(6).setCellValue(student.getNic());
            row.createCell(7).setCellValue(student.getContact());
            row.createCell(8).setCellValue(student.getEmail());
            row.createCell(9).setCellValue(student.getEmergencyContact());
            row.createCell(10).setCellValue(student.getProgramme());
            row.createCell(11).setCellValue(student.getRoom());
        }

        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
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
            showAlert("Logging Error", "Error logging activity: " + e.getMessage());
        } finally {
            try {
                if (pst != null) pst.close();
            } catch (SQLException e) {
                showAlert("Error", "Error closing database resources: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Student model class
    public static class Student {
        private final SimpleStringProperty name;
        private final SimpleStringProperty address;
        private final SimpleStringProperty gender;
        private final SimpleStringProperty guardian;
        private final SimpleStringProperty studentId;
        private final SimpleStringProperty studyYear;
        private final SimpleStringProperty nic;
        private final SimpleStringProperty contact;
        private final SimpleStringProperty email;
        private final SimpleStringProperty emergencyContact;
        private final SimpleStringProperty programme;
        private final SimpleStringProperty room;

        public Student(String name, String address, String gender, String guardian,
                       String studentId, String studyYear, String nic, String contact,
                       String email, String emergencyContact, String programme, String room) {
            this.name = new SimpleStringProperty(name);
            this.address = new SimpleStringProperty(address);
            this.gender = new SimpleStringProperty(gender);
            this.guardian = new SimpleStringProperty(guardian);
            this.studentId = new SimpleStringProperty(studentId);
            this.studyYear = new SimpleStringProperty(studyYear);
            this.nic = new SimpleStringProperty(nic);
            this.contact = new SimpleStringProperty(contact);
            this.email = new SimpleStringProperty(email);
            this.emergencyContact = new SimpleStringProperty(emergencyContact);
            this.programme = new SimpleStringProperty(programme);
            this.room = new SimpleStringProperty(room);
        }

        // Getters
        public String getName() { return name.get(); }
        public String getAddress() { return address.get(); }
        public String getGender() { return gender.get(); }
        public String getGuardian() { return guardian.get(); }
        public String getStudentId() { return studentId.get(); }
        public String getStudyYear() { return studyYear.get(); }
        public String getNic() { return nic.get(); }
        public String getContact() { return contact.get(); }
        public String getEmail() { return email.get(); }
        public String getEmergencyContact() { return emergencyContact.get(); }
        public String getProgramme() { return programme.get(); }
        public String getRoom() { return room.get(); }

        // Property getters for TableView
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty addressProperty() { return address; }
        public SimpleStringProperty genderProperty() { return gender; }
        public SimpleStringProperty guardianProperty() { return guardian; }
        public SimpleStringProperty studentIdProperty() { return studentId; }
        public SimpleStringProperty studyYearProperty() { return studyYear; }
        public SimpleStringProperty nicProperty() { return nic; }
        public SimpleStringProperty contactProperty() { return contact; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty emergencyContactProperty() { return emergencyContact; }
        public SimpleStringProperty programmeProperty() { return programme; }
        public SimpleStringProperty roomProperty() { return room; }
    }
}