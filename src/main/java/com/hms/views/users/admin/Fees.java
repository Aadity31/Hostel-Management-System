package com.hms.views.users.admin;

import com.hms.utils.DB;
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
import com.hms.utils.Emp;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Fees implements Initializable {

    @FXML private TextField txtstuid;
    @FXML private TextField txtname;
    @FXML private DatePicker add_date;
    @FXML private ComboBox<String> txtmonth;
    @FXML private TextField txtinvoice;
    @FXML private TextField txtamount;
    @FXML private TextField txt_search;

    @FXML private Button add_btn;
    @FXML private Button update_btn;
    @FXML private Button delete_btn;
    @FXML private Button clear_btn;
    @FXML private Button btnpdf;
    @FXML private Button btnexcel;
    @FXML private Button btnreset;

    @FXML private TableView<FeesRecord> fees_table;
    @FXML private TableColumn<FeesRecord, String> colStudentId;
    @FXML private TableColumn<FeesRecord, String> colName;
    @FXML private TableColumn<FeesRecord, String> colDate;
    @FXML private TableColumn<FeesRecord, String> colMonth;
    @FXML private TableColumn<FeesRecord, String> colInvoice;
    @FXML private TableColumn<FeesRecord, String> colAmount;

    private Connection conn;
    private ResultSet rs;
    private PreparedStatement pst;

    private ObservableList<FeesRecord> feesData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize database connection
        conn = DB.connect();

        // Initialize month combo box
        txtmonth.setItems(FXCollections.observableArrayList(
                "Select", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        ));
        txtmonth.getSelectionModel().selectFirst();

        // Initialize table columns
        setupTableColumns();

        // Load initial data
        updateTable();

        // Set focus to name field
        txtname.requestFocus();
    }

    private void setupTableColumns() {
        colStudentId.setCellValueFactory(cellData -> cellData.getValue().studentIdProperty());
        colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        colMonth.setCellValueFactory(cellData -> cellData.getValue().monthProperty());
        colInvoice.setCellValueFactory(cellData -> cellData.getValue().invoiceProperty());
        colAmount.setCellValueFactory(cellData -> cellData.getValue().amountProperty());

        fees_table.setItems(feesData);
    }

    @FXML
    private void handleAddRecord() {
        if (validateFields()) {
            try {
                String sql = "INSERT INTO fees (Student_id, Name, Date, Month, Invoice_no, Amount) VALUES (?, ?, ?, ?, ?, ?)";
                pst = conn.prepareStatement(sql);
                pst.setString(1, txtstuid.getText());
                pst.setString(2, txtname.getText());

                // Format date
                LocalDate selectedDate = add_date.getValue();
                String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("MM.dd.yyyy"));
                pst.setString(3, formattedDate);

                pst.setString(4, txtmonth.getValue());
                pst.setString(5, txtinvoice.getText());
                pst.setString(6, txtamount.getText());

                pst.execute();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data saved successfully");

                // Log the action
                logAction("New Payments Added");

                updateTable();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            } finally {
                closeResources();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "One or more required fields are empty");
        }
    }

    @FXML
    private void handleUpdate() {
        if (validateFields()) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Update Record");
            confirmAlert.setHeaderText("Are you sure you want to update?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    String sql = "UPDATE fees SET Student_id=?, Name=?, Date=?, Month=?, Invoice_no=?, Amount=? WHERE Invoice_no=?";
                    pst = conn.prepareStatement(sql);
                    pst.setString(1, txtstuid.getText());
                    pst.setString(2, txtname.getText());

                    LocalDate selectedDate = add_date.getValue();
                    String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("MM.dd.yyyy"));
                    pst.setString(3, formattedDate);

                    pst.setString(4, txtmonth.getValue());
                    pst.setString(5, txtinvoice.getText());
                    pst.setString(6, txtamount.getText());
                    pst.setString(7, txtinvoice.getText()); // WHERE clause

                    pst.execute();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Record updated successfully");

                    logAction("Payments Details Updated");
                    updateTable();

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
                } finally {
                    closeResources();
                }
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "One or more required fields are empty");
        }
    }

    @FXML
    private void handleDelete() {
        if (validateFields()) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Record");
            confirmAlert.setHeaderText("Are you sure you want to delete this record?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    String sql = "DELETE FROM fees WHERE Invoice_no=?";
                    pst = conn.prepareStatement(sql);
                    pst.setString(1, txtinvoice.getText());
                    pst.execute();

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Record deleted successfully");

                    logAction("Payments Details Removed");
                    updateTable();

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
                } finally {
                    closeResources();
                }
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "One or more required fields are empty");
        }
    }

    @FXML
    private void handleClear() {
        txtstuid.clear();
        txtname.clear();
        add_date.setValue(null);
        txtinvoice.clear();
        txtamount.clear();
        txtmonth.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleTableClick(MouseEvent event) {
        FeesRecord selectedItem = fees_table.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            txtstuid.setText(selectedItem.getStudentId());
            txtname.setText(selectedItem.getName());

            // Parse and set date
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd.yyyy");
                LocalDate date = LocalDate.parse(selectedItem.getDate(), formatter);
                add_date.setValue(date);
            } catch (Exception e) {
                System.err.println("Error parsing date: " + e.getMessage());
            }

            txtmonth.setValue(selectedItem.getMonth());
            txtinvoice.setText(selectedItem.getInvoice());
            txtamount.setText(selectedItem.getAmount());
        }
    }

    @FXML
    private void handleSearch(KeyEvent event) {
        String searchText = txt_search.getText();
        if (searchText.isEmpty()) {
            updateTable();
        } else {
            searchByStudentId(searchText);
        }
    }

    @FXML
    private void handleReset() {
        updateTable();
        txt_search.clear();
    }

    @FXML
    private void handleExportPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export as PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(fees_table.getScene().getWindow());
        if (file != null) {
            exportToPdf(file.getAbsolutePath());
            logAction("Export payment details as PDF");
        }
    }

    @FXML
    private void handleExportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        File file = fileChooser.showSaveDialog(fees_table.getScene().getWindow());
        if (file != null) {
            exportToExcel(file.getAbsolutePath());
            logAction("Export payment details to Excel");
        }
    }

    private void updateTable() {
        feesData.clear();
        try {
            String sql = "SELECT * FROM fees";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

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
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        } finally {
            closeResources();
        }
    }

    private void searchByStudentId(String studentId) {
        feesData.clear();
        try {
            String sql = "SELECT * FROM fees WHERE Student_id LIKE ?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + studentId + "%");
            rs = pst.executeQuery();

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
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        } finally {
            closeResources();
        }
    }

    private boolean validateFields() {
        return !txtstuid.getText().isEmpty() &&
                !txtname.getText().isEmpty() &&
                add_date.getValue() != null &&
                !txtmonth.getValue().equals("Select") &&
                !txtinvoice.getText().isEmpty() &&
                !txtamount.getText().isEmpty();
    }

    private void exportToPdf(String filePath) {
        try {
            com.itextpdf.text.Font blueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12,
                    com.itextpdf.text.Font.NORMAL, new BaseColor(25, 27, 82));
            com.itextpdf.text.Document doc = new com.itextpdf.text.Document(PageSize.A4.rotate());

            Calendar cal = new GregorianCalendar();
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            PdfWriter.getInstance(doc, new FileOutputStream(filePath));
            doc.open();

            doc.add(new Paragraph(new Chunk("Student Payment Details Report\n", blueFont)));
            doc.add(new Paragraph(year + "." + (month + 1) + "." + day));

            PdfPTable table = new PdfPTable(6);
            table.setHorizontalAlignment(ALIGN_CENTER);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Add header
            PdfPCell headerCell = new PdfPCell(new Paragraph("Student Payments"));
            headerCell.setColspan(6);
            headerCell.setHorizontalAlignment(ALIGN_CENTER);
            headerCell.setBackgroundColor(new BaseColor(0, 166, 80));
            headerCell.setPadding(10.0f);
            table.addCell(headerCell);

            // Add column headers
            String[] headers = {"Student_id", "Name", "Date", "Month", "Invoice_no", "Amount"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(header));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setPadding(5.0f);
                table.addCell(cell);
            }

            // Add data
            for (FeesRecord record : feesData) {
                table.addCell(record.getStudentId());
                table.addCell(record.getName());
                table.addCell(record.getDate());
                table.addCell(record.getMonth());
                table.addCell(record.getInvoice());
                table.addCell(record.getAmount());
            }

            doc.add(table);
            doc.close();

            showAlert(Alert.AlertType.INFORMATION, "Success", "PDF exported successfully");

        } catch (FileNotFoundException | DocumentException e) {
            showAlert(Alert.AlertType.ERROR, "Export Error", e.getMessage());
        }
    }

    private void exportToExcel(String filePath) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Student Payment Details Report");

            // Set column widths
            for (int i = 0; i < 6; i++) {
                sheet.setColumnWidth(i, 4000);
            }

            // Create header row
            XSSFRow headerRow = sheet.createRow(0);
            String[] headers = {"Student_id", "Name", "Date", "Month", "Invoice_no", "Amount"};
            for (int i = 0; i < headers.length; i++) {
                XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Add data rows
            int rowNum = 1;
            for (FeesRecord record : feesData) {
                XSSFRow row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getStudentId());
                row.createCell(1).setCellValue(record.getName());
                row.createCell(2).setCellValue(record.getDate());
                row.createCell(3).setCellValue(record.getMonth());
                row.createCell(4).setCellValue(record.getInvoice());
                row.createCell(5).setCellValue(record.getAmount());
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Excel file exported successfully");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export Error", e.getMessage());
        }
    }

    private void logAction(String action) {
        try {
            Date currentDate = GregorianCalendar.getInstance().getTime();
            DateFormat df = DateFormat.getDateInstance();
            String dateString = df.format(currentDate);

            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String timeString = sdf.format(d);

            String logEntry = timeString + " / " + dateString;
            int userId = Emp.UserId;

            String sql = "INSERT INTO logs (User_id, Date, Status) VALUES (?, ?, ?)";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, userId);
            pst.setString(2, logEntry);
            pst.setString(3, action);
            pst.execute();

        } catch (SQLException e) {
            System.err.println("Error logging action: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
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

    // Inner class for table data
    public static class FeesRecord {
        private final SimpleStringProperty studentId;
        private final SimpleStringProperty name;
        private final SimpleStringProperty date;
        private final SimpleStringProperty month;
        private final SimpleStringProperty invoice;
        private final SimpleStringProperty amount;

        public FeesRecord(String studentId, String name, String date, String month, String invoice, String amount) {
            this.studentId = new SimpleStringProperty(studentId);
            this.name = new SimpleStringProperty(name);
            this.date = new SimpleStringProperty(date);
            this.month = new SimpleStringProperty(month);
            this.invoice = new SimpleStringProperty(invoice);
            this.amount = new SimpleStringProperty(amount);
        }

        // Property getters
        public SimpleStringProperty studentIdProperty() { return studentId; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty monthProperty() { return month; }
        public SimpleStringProperty invoiceProperty() { return invoice; }
        public SimpleStringProperty amountProperty() { return amount; }

        // Value getters
        public String getStudentId() { return studentId.get(); }
        public String getName() { return name.get(); }
        public String getDate() { return date.get(); }
        public String getMonth() { return month.get(); }
        public String getInvoice() { return invoice.get(); }
        public String getAmount() { return amount.get(); }
    }
}