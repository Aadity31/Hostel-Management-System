// REWRITE entire AllStudents.java with fixes
package com.hms.views.users.admin;

import com.hms.utils.DB;
import com.hms.utils.Emp;
import com.itextpdf.text.*;
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
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

/**
 * AllStudents view — fixed compile errors & matches new schema.
 */
public class AllStudents implements Initializable {

    /* ────────────── UI ────────────── */
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student,String> firstNameCol,lastNameCol,usernameCol,genderCol,studentIdCol,
                                              mobileCol,emailCol,programCol,roomCol;
    @FXML private TextField searchTextField;
    @FXML private Button resetButton,pdfButton,excelButton;

    /* ────────────── DB ────────────── */
    private Connection conn;
    private final ObservableList<Student> studentList = FXCollections.observableArrayList();

    @Override public void initialize(URL url, ResourceBundle rb) {
        conn = DB.connect();
        setupColumns();
        reloadTable();
        searchTextField.textProperty().addListener((obs,ov,nv)->{
            if(nv.trim().isEmpty()) reloadTable(); else searchById(nv.trim());
        });
    }

    /* ── table cols ── */
    private void setupColumns(){
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol .setCellValueFactory(new PropertyValueFactory<>("lastName"));
        usernameCol .setCellValueFactory(new PropertyValueFactory<>("username"));
        genderCol   .setCellValueFactory(new PropertyValueFactory<>("gender"));
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        mobileCol   .setCellValueFactory(new PropertyValueFactory<>("mobile"));
        emailCol    .setCellValueFactory(new PropertyValueFactory<>("email"));
        programCol  .setCellValueFactory(new PropertyValueFactory<>("program"));
        roomCol     .setCellValueFactory(new PropertyValueFactory<>("room"));
    }

    /* ── load all ── */
    private void reloadTable(){
        studentList.clear();
        String sql="SELECT first_name,last_name,username,gender,student_id,mobile_number,email,study_program,room_no FROM student";
        try(PreparedStatement p=conn.prepareStatement(sql); ResultSet r=p.executeQuery()){
            while(r.next()) studentList.add(new Student(
                    r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),
                    r.getString(6),r.getString(7),r.getString(8),r.getString(9)));
            studentTable.setItems(studentList);
        }catch(SQLException e){alert("DB Error",e.getMessage());}
    }

    /* ── search by id ── */
    private void searchById(String id){
        studentList.clear();
        String sql="SELECT first_name,last_name,username,gender,student_id,mobile_number,email,study_program,room_no FROM student WHERE student_id=?";
        try(PreparedStatement p=conn.prepareStatement(sql)){
            p.setString(1,id); ResultSet r=p.executeQuery();
            while(r.next()) studentList.add(new Student(
                    r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),
                    r.getString(6),r.getString(7),r.getString(8),r.getString(9)));
            studentTable.setItems(studentList);
        }catch(SQLException e){alert("DB Error",e.getMessage());}
    }

    /* ── reset ── */
    @FXML private void handleReset(ActionEvent e){ searchTextField.clear(); reloadTable(); }

    /* ── export PDF ── */
    @FXML private void handleExportPdf(ActionEvent e){
        File file=choose("Export as PDF","PDF Files","*.pdf"); if(file==null) return;
        try{exportPdf(file.getAbsolutePath()); log("Exported students PDF"); alert("Success","PDF exported."); }
        catch(Exception ex){alert("Export Error",ex.getMessage());}
    }
    private void exportPdf(String path) throws FileNotFoundException, DocumentException{
        Document doc=new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(doc,new FileOutputStream(path)); doc.open();
        doc.add(new Paragraph("Student List",FontFactory.getFont(FontFactory.HELVETICA_BOLD,14)));
        doc.add(new Paragraph(new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new java.util.Date())));
        PdfPTable t=new PdfPTable(9); t.setWidthPercentage(100); String[] h={"First","Last","Username","Gender","ID","Mobile","Email","Program","Room"};
        for(String s:h){ PdfPCell c=new PdfPCell(new Phrase(s)); c.setBackgroundColor(BaseColor.LIGHT_GRAY); t.addCell(c);}        
        for(Student s:studentTable.getItems()){
            t.addCell(s.getFirstName()); t.addCell(s.getLastName()); t.addCell(s.getUsername());
            t.addCell(s.getGender()); t.addCell(s.getStudentId()); t.addCell(s.getMobile());
            t.addCell(s.getEmail()); t.addCell(s.getProgram()); t.addCell(s.getRoom());
        }
        doc.add(t); doc.close();
    }

    /* ── export Excel ── */
    @FXML private void handleExportExcel(ActionEvent e){
        File file=choose("Export Excel","Excel Files","*.xlsx"); if(file==null) return;
        try{exportExcel(file.getAbsolutePath()); log("Exported students Excel"); alert("Success","Excel exported."); }
        catch(Exception ex){alert("Export Error",ex.getMessage());}
    }
    private void exportExcel(String path) throws IOException{
        XSSFWorkbook wb=new XSSFWorkbook(); XSSFSheet sh=wb.createSheet("Students");
        String[] h={"First Name","Last Name","Username","Gender","Student ID","Mobile","Email","Program","Room"};
        XSSFRow hr=sh.createRow(0); for(int i=0;i<h.length;i++) hr.createCell(i).setCellValue(h[i]);
        int r=1; for(Student s:studentTable.getItems()){
            XSSFRow row=sh.createRow(r++);
            row.createCell(0).setCellValue(s.getFirstName());
            row.createCell(1).setCellValue(s.getLastName());
            row.createCell(2).setCellValue(s.getUsername());
            row.createCell(3).setCellValue(s.getGender());
            row.createCell(4).setCellValue(s.getStudentId());
            row.createCell(5).setCellValue(s.getMobile());
            row.createCell(6).setCellValue(s.getEmail());
            row.createCell(7).setCellValue(s.getProgram());
            row.createCell(8).setCellValue(s.getRoom());
        }
        try(FileOutputStream out=new FileOutputStream(path)){wb.write(out);} wb.close();
    }

    /* ── helpers ── */
    private File choose(String t,String d,String ext){ FileChooser fc=new FileChooser(); fc.setTitle(t); fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(d,ext)); return fc.showSaveDialog((Stage)studentTable.getScene().getWindow()); }
    private void alert(String title,String msg){ Alert a=new Alert(Alert.AlertType.INFORMATION); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait(); }
    private void log(String act){ String sql="INSERT INTO logs (User_id,Date,Status) VALUES (?,?,?)"; try(PreparedStatement p=conn.prepareStatement(sql)){ p.setInt(1,Emp.UserId); p.setString(2,new SimpleDateFormat("HH:mm:ss / dd-MMM-yyyy").format(new java.util.Date())); p.setString(3,act); p.execute(); } catch(SQLException ignored){} }

    /* ── model ── */
    public static class Student{
        private final SimpleStringProperty firstName,lastName,username,gender,studentId,mobile,email,program,room;
        public Student(String f,String l,String u,String g,String id,String m,String em,String pr,String r){ firstName=new SimpleStringProperty(f); lastName=new SimpleStringProperty(l); username=new SimpleStringProperty(u); gender=new SimpleStringProperty(g); studentId=new SimpleStringProperty(id); mobile=new SimpleStringProperty(m); email=new SimpleStringProperty(em); program=new SimpleStringProperty(pr); room=new SimpleStringProperty(r);}        
        public String getFirstName(){return firstName.get();}
        public String getLastName(){return lastName.get();}
        public String getUsername(){return username.get();}
        public String getGender(){return gender.get();}
        public String getStudentId(){return studentId.get();}
        public String getMobile(){return mobile.get();}
        public String getEmail(){return email.get();}
        public String getProgram(){return program.get();}
        public String getRoom(){return room.get();}
    }
}
