package com.hms.views.users.student;


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

import java.awt.Toolkit;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;

public class Fees implements Initializable {

    @FXML private TextField txtstuid;
    @FXML private TextField txtname;
    @FXML private ComboBox<String> txtmonth;
    @FXML private TextField txtinvoice;
    @FXML private DatePicker add_date;
    @FXML private TextField txtamount;
    @FXML private Button add_btn;
    @FXML private Button clear_btn;
    @FXML private TextField txt_search;
    @FXML private Button btnreset;
    @FXML private Button btnpdf;
    @FXML private Button btnexcel;
    @FXML private TableView<FeesRecord> fees_table;
    @FXML private TableColumn<FeesRecord, String> col_student_id;
    @FXML private TableColumn<FeesRecord, String> col_name;
    @FXML private TableColumn<FeesRecord, String> col_date;
    @FXML private TableColumn<FeesRecord, String> col_month;
    @FXML private TableColumn<FeesRecord, String> col_invoice_no;
    @FXML private TableColumn<FeesRecord, String> col_amount;

    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;
    private ObservableList<FeesRecord> feesData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DB.connect();

        // Initialize ComboBox
        txtmonth.setValue("Select");

        // Initialize month combo box
        txtmonth.setItems(FXCollections.observableArrayList(
                "Select", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        ));
        txtmonth.getSelectionModel().selectFirst();

        // Initialize table columns
        col_student_id.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        col_date.setCellValueFactory(new PropertyValueFactory<>("date"));
        col_month.setCellValueFactory(new PropertyValueFactory<>("month"));
        col_invoice_no.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));
        col_amount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        fees_table.setItems(feesData);

        updateTable();
        txtname.requestFocus();
    }

    @FXML
    private void addRecord() {
        if (validateFields()) {
            try {
                String sql = "INSERT INTO fees (Student_id, Name, Date, Month, Invoice_no, Amount) VALUES (?, ?, ?, ?, ?, ?)";
                pst = conn.prepareStatement(sql);
                pst.setString(1, txtstuid.getText());
                pst.setString(2, txtname.getText());

                String date = add_date.getValue().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
                pst.setString(3, date);
                pst.setString(4, txtmonth.getValue());
                pst.setString(5, txtinvoice.getText());
                pst.setString(6, txtamount.getText());

                pst.execute();

                Toolkit.getDefaultToolkit().beep();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data is saved successfully");

                logActivity("New Payments Added");
                updateTable();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            } finally {
                closeResources();
            }
        } else {
            Toolkit.getDefaultToolkit().beep();
            showAlert(Alert.AlertType.ERROR, "Error", "One or more required fields are empty");
        }
    }

    @FXML
    private void clearFields() {
        txtstuid.clear();
        txtname.clear();
        add_date.setValue(null);
        txtinvoice.clear();
        txtamount.clear();
        txtmonth.setValue("Select");
    }

    @FXML
    private void searchByMonth() {
        String tmp = Emp.UserName;

        try {
            String sql = "SELECT * FROM fees WHERE Student_id=? AND Month=?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, tmp);
            pst.setString(2, txt_search.getText());
            rs = pst.executeQuery();

            loadTableData(rs);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        } finally {
            closeResources();
        }
    }

    @FXML
    private void resetTable() {
        updateTable();
        txt_search.clear();
    }

    @FXML
    private void exportToPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export as PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        Stage stage = (Stage) btnpdf.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                com.itextpdf.text.Font blueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12,
                        com.itextpdf.text.Font.NORMAL, new BaseColor(25, 27, 82));
                com.itextpdf.text.Document doc = new com.itextpdf.text.Document(PageSize.A4.rotate());

                Calendar cal = new GregorianCalendar();
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                PdfWriter.getInstance(doc, new FileOutputStream(file.getAbsolutePath()));
                doc.open();

                doc.add(new Paragraph(new Chunk("Student Payment Details Report\n", blueFont)));
                doc.add(new Paragraph(year + "." + (month + 1) + "." + day));

                PdfPTable table = new PdfPTable(6);
                table.setHorizontalAlignment(ALIGN_CENTER);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                // Header
                PdfPCell headerCell = new PdfPCell(new Paragraph("Student Payments"));
                headerCell.setColspan(6);
                headerCell.setHorizontalAlignment(ALIGN_CENTER);
                headerCell.setBackgroundColor(new BaseColor(0, 166, 80));
                headerCell.setPadding(10.0f);
                table.addCell(headerCell);

                // Column headers
                String[] headers = {"Student_id", "Name", "Date", "Month", "Invoice_no", "Amount"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Paragraph(header));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setPadding(5.0f);
                    table.addCell(cell);
                }

                // Data rows
                for (FeesRecord record : feesData) {
                    table.addCell(record.getStudentId());
                    table.addCell(record.getName());
                    table.addCell(record.getDate());
                    table.addCell(record.getMonth());
                    table.addCell(record.getInvoiceNo());
                    table.addCell(record.getAmount());
                }

                doc.add(table);
                doc.close();

                Toolkit.getDefaultToolkit().beep();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Export Successfully");

                logActivity("Export payment details as PDF");

            } catch (FileNotFoundException | DocumentException e) {
                showAlert(Alert.AlertType.ERROR, "Export Error", e.getMessage());
            }
        }
    }

    @FXML
    private void exportToExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        Stage stage = (Stage) btnexcel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            FileOutputStream excelFOU = null;
            BufferedOutputStream excelBOU = null;
            XSSFWorkbook excelWorkbook = null;

            try {
                excelWorkbook = new XSSFWorkbook();
                XSSFSheet excelSheet = excelWorkbook.createSheet("Student Payment Details Report");

                // Set column widths
                excelSheet.setColumnWidth(0, 8000);
                excelSheet.setColumnWidth(1, 4000);
                excelSheet.setColumnWidth(2, 4000);
                excelSheet.setColumnWidth(3, 4000);
                excelSheet.setColumnWidth(4, 4000);
                excelSheet.setColumnWidth(5, 4000);

                // Create header row
                XSSFRow headerRow = excelSheet.createRow(0);
                String[] headers = {"Student_id", "Name", "Date", "Month", "Invoice_no", "Amount"};

                for (int i = 0; i < headers.length; i++) {
                    XSSFCell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }

                // Add data rows
                int rowIndex = 1;
                for (FeesRecord record : feesData) {
                    XSSFRow row = excelSheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(record.getStudentId());
                    row.createCell(1).setCellValue(record.getName());
                    row.createCell(2).setCellValue(record.getDate());
                    row.createCell(3).setCellValue(record.getMonth());
                    row.createCell(4).setCellValue(record.getInvoiceNo());
                    row.createCell(5).setCellValue(record.getAmount());
                }

                excelFOU = new FileOutputStream(file.getAbsolutePath());
                excelBOU = new BufferedOutputStream(excelFOU);
                excelWorkbook.write(excelBOU);

                Toolkit.getDefaultToolkit().beep();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Export Successfully");

                logActivity("Export payment details to Excel");

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Export Error", e.getMessage());
            } finally {
                try {
                    if (excelBOU != null) excelBOU.close();
                    if (excelFOU != null) excelFOU.close();
                    if (excelWorkbook != null) excelWorkbook.close();
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        }
    }

    private void updateTable() {
        String tmp = Emp.UserName;

        try {
            String sql = "SELECT * FROM fees WHERE Student_id=?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, tmp);
            rs = pst.executeQuery();

            loadTableData(rs);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        } finally {
            closeResources();
        }
    }

    private void loadTableData(ResultSet rs) throws SQLException {
        feesData.clear();

        while (rs.next()) {
            FeesRecord record = new FeesRecord(
                    rs.getString("Student_id"),
                    rs.getString("Name"),
                    rs.getString("Date"),
                    rs.getString("Month"),
                    rs.getString("Invoice_no"),
                    rs.getString("Amount")
            );
            feesData.add(record);
        }
    }

    private boolean validateFields() {
        return !(txtstuid.getText().isEmpty() || txtname.getText().isEmpty() ||
                add_date.getValue() == null || txtmonth.getValue().equals("Select") ||
                txtinvoice.getText().isEmpty() || txtamount.getText().isEmpty());
    }

    private void logActivity(String activity) {
        try {
            Date currentDate = GregorianCalendar.getInstance().getTime();
            DateFormat df = DateFormat.getDateInstance();
            String dateString = df.format(currentDate);

            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String timeString = sdf.format(d);

            String value0 = timeString;
            String value1 = dateString;
            int value = Emp.UserId;

            String sql = "INSERT INTO logs (User_id, Date, Status) VALUES (?, ?, ?)";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, value);
            pst.setString(2, value0 + " / " + value1);
            pst.setString(3, activity);
            pst.execute();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Log Error", e.getMessage());
        } finally {
            closeResources();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
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
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    // Inner class for table data
    public static class FeesRecord {
        private final SimpleStringProperty studentId;
        private final SimpleStringProperty name;
        private final SimpleStringProperty date;
        private final SimpleStringProperty month;
        private final SimpleStringProperty invoiceNo;
        private final SimpleStringProperty amount;

        public FeesRecord(String studentId, String name, String date, String month, String invoiceNo, String amount) {
            this.studentId = new SimpleStringProperty(studentId);
            this.name = new SimpleStringProperty(name);
            this.date = new SimpleStringProperty(date);
            this.month = new SimpleStringProperty(month);
            this.invoiceNo = new SimpleStringProperty(invoiceNo);
            this.amount = new SimpleStringProperty(amount);
        }

        // Getters
        public String getStudentId() { return studentId.get(); }
        public String getName() { return name.get(); }
        public String getDate() { return date.get(); }
        public String getMonth() { return month.get(); }
        public String getInvoiceNo() { return invoiceNo.get(); }
        public String getAmount() { return amount.get(); }

        // Property getters for TableView
        public SimpleStringProperty studentIdProperty() { return studentId; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty monthProperty() { return month; }
        public SimpleStringProperty invoiceNoProperty() { return invoiceNo; }
        public SimpleStringProperty amountProperty() { return amount; }
    }
}