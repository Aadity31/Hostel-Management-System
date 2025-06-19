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
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public class ManageStaff implements Initializable {

    @FXML private TextField txtuserid, txtusername, txtpassword;
    @FXML private Button add_btn, update_btn, delete_btn, clear_btn;
    @FXML private TableView<StaffRecord> staff_table;
    @FXML private TableColumn<StaffRecord,Integer> colUserId;
    @FXML private TableColumn<StaffRecord,String>  colUsername,colPassword;

    private Connection conn;
    private final ObservableList<StaffRecord> staffData = FXCollections.observableArrayList();

    @Override public void initialize(URL url, ResourceBundle rb) {
        conn = DB.connect();
        setupTable();
        refreshTable();
        txtusername.requestFocus();
        txtuserid.setDisable(true);
    }

    private void setupTable(){
        colUserId  .setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        staff_table.setItems(staffData);
    }

    private void refreshTable(){
        staffData.clear();
        String sql="SELECT * FROM staff";
        try(PreparedStatement p=conn.prepareStatement(sql); ResultSet r=p.executeQuery()){
            while(r.next()) staffData.add(new StaffRecord(
                    r.getInt("staff_id"), r.getString("username"), r.getString("password")));
        }catch(SQLException e){error("DB Error",e.getMessage());}
    }

    @FXML private void addStaff(){
        if(!validateInputs()) return;
        String sql="INSERT INTO staff (username,password) VALUES (?,?)";
        try(PreparedStatement p=conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            p.setString(1,txtusername.getText()); p.setString(2,txtpassword.getText());
            p.executeUpdate();
            info("New staff account created.");
            log("Added new staff account");
            try(ResultSet g=p.getGeneratedKeys()){ if(g.next()) txtuserid.setText(String.valueOf(g.getInt(1))); }
            refreshTable(); clearFields();
        }catch(SQLException e){error("DB Error",e.getMessage());}
    }

    @FXML private void updateStaff(){
        if(!validateAll()) return;
        if(confirm("Update Record","Update this staff account?")){
            String sql="UPDATE staff SET username=?, password=? WHERE staff_id=?";
            try(PreparedStatement p=conn.prepareStatement(sql)){
                p.setString(1,txtusername.getText()); p.setString(2,txtpassword.getText()); p.setInt(3,Integer.parseInt(txtuserid.getText()));
                p.executeUpdate();
                info("Staff account updated."); log("Updated staff account"); refreshTable();
            }catch(SQLException e){error("DB Error",e.getMessage());}
        }
    }

    @FXML private void deleteStaff(){
        if(!validateAll()) return;
        if(confirm("Delete Account","Delete this staff account?")){
            log("Deleted staff account");
            String sql="DELETE FROM staff WHERE staff_id=?";
            try(PreparedStatement p=conn.prepareStatement(sql)){
                p.setInt(1,Integer.parseInt(txtuserid.getText())); p.executeUpdate();
                info("Staff account removed."); refreshTable(); clearFields();
            }catch(SQLException e){error("DB Error",e.getMessage());}
        }
    }

    @FXML private void clearFields(){ txtuserid.clear(); txtusername.clear(); txtpassword.clear(); txtusername.requestFocus(); }

    @FXML private void handleTableClick(MouseEvent e){
        StaffRecord s=staff_table.getSelectionModel().getSelectedItem();
        if(s==null) return; txtuserid.setText(String.valueOf(s.getUserId())); txtusername.setText(s.getUsername()); txtpassword.setText(s.getPassword());
    }

    private boolean validateInputs(){ if(txtusername.getText().isBlank()||txtpassword.getText().isBlank()){ error("Validation","Fill all required fields"); return false;} return true; }
    private boolean validateAll(){ return validateInputs() && !txtuserid.getText().isBlank(); }

    private void log(String action){
        String sql="INSERT INTO logs (User_id,Date,Status) VALUES (?,?,?)";
        String timestamp=new SimpleDateFormat("HH:mm:ss / dd-MMM-yyyy").format(new java.util.Date());
        try(PreparedStatement p=conn.prepareStatement(sql)){
            p.setInt(1,Emp.UserId); p.setString(2,timestamp); p.setString(3,action); p.execute();
        }catch(SQLException ignored){}
    }

    private void info(String m){ Alert a=new Alert(Alert.AlertType.INFORMATION,m, ButtonType.OK); a.setHeaderText(null); a.showAndWait(); }
    private void error(String t,String m){ Alert a=new Alert(Alert.AlertType.ERROR,m,ButtonType.OK); a.setTitle(t); a.setHeaderText(null); a.showAndWait(); }
    private boolean confirm(String t,String m){ Alert a=new Alert(Alert.AlertType.CONFIRMATION,m,ButtonType.OK,ButtonType.CANCEL); a.setTitle(t); a.setHeaderText(null); Optional<ButtonType> r=a.showAndWait(); return r.isPresent()&&r.get()==ButtonType.OK; }

    public static class StaffRecord{
        private final int userId; private final String username,password;
        public StaffRecord(int id,String un,String pw){ userId=id; username=un; password=pw; }
        public int getUserId(){return userId;} public String getUsername(){return username;} public String getPassword(){return password;}
    }
}
