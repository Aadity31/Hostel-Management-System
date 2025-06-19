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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Activity‑log viewer aligned with the current `logs` schema:
 *   log_id     INT PK AUTO_INCREMENT
 *   User_name  VARCHAR
 *   Date       VARCHAR / DATETIME
 *   Status     VARCHAR
 */
public class Activity implements Initializable {

    /* ────────── UI ────────── */
    @FXML private TextField txt_search;
    @FXML private Button btnreset;
    @FXML private TableView<LogEntry> tbl_3;
    @FXML private TableColumn<LogEntry,String> colLogId,colUserName,colDate,colStatus;

    /* ────────── DB ────────── */
    private Connection conn;
    private ObservableList<LogEntry> logsList = FXCollections.observableArrayList();
    private Timeline timeline;

    @Override public void initialize(URL url, ResourceBundle rb) {
        conn = DB.connect();
        setupColumns();
        startClock();
        loadLogs();
    }

    /* ── Table columns ── */
    private void setupColumns(){
        colLogId   .setCellValueFactory(new PropertyValueFactory<>("logId"));
        colUserName.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colDate    .setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatus  .setCellValueFactory(new PropertyValueFactory<>("status"));
        tbl_3.setItems(logsList);
    }

    /* ── clock util (optional) ── */
    private void startClock(){ timeline = new Timeline(new KeyFrame(Duration.seconds(1))); timeline.setCycleCount(Animation.INDEFINITE); timeline.play(); }
    public  void stopClock(){ if(timeline!=null) timeline.stop(); }

    /* ── Load all logs ── */
    private void loadLogs(){
        logsList.clear();
        String sql="SELECT log_id,User_name,Date,Status FROM logs";
        try(PreparedStatement p=conn.prepareStatement(sql); ResultSet r=p.executeQuery()){
            while(r.next()) logsList.add(new LogEntry(
                r.getString("log_id"), r.getString("User_name"), r.getString("Date"), r.getString("Status")));
        }catch(SQLException e){ showError("DB Error",e.getMessage()); }
    }

    /* ── search by user name ── */
    @FXML private void onSearchKeyReleased(KeyEvent e){
        String q=txt_search.getText().trim();
        if(q.isEmpty()){ loadLogs(); return; }
        logsList.clear();
        String sql="SELECT log_id,User_name,Date,Status FROM logs WHERE User_name LIKE ?";
        try(PreparedStatement p=conn.prepareStatement(sql)){
            p.setString(1,"%"+q+"%"); ResultSet r=p.executeQuery();
            while(r.next()) logsList.add(new LogEntry(
                r.getString("log_id"), r.getString("User_name"), r.getString("Date"), r.getString("Status")));
        }catch(SQLException ex){ showError("DB Error",ex.getMessage()); }
    }

    /* ── reset button ── */
    @FXML private void onResetClicked(MouseEvent e){ txt_search.clear(); loadLogs(); }

    /* ── helpers ── */
    private void showError(String t,String m){ Alert a=new Alert(Alert.AlertType.ERROR,m, ButtonType.OK); a.setTitle(t); a.setHeaderText(null); a.showAndWait(); }

    /* ── model ── */
    public static class LogEntry{
        private final SimpleStringProperty logId,userName,date,status;
        public LogEntry(String id,String un,String dt,String st){ logId=new SimpleStringProperty(id); userName=new SimpleStringProperty(un); date=new SimpleStringProperty(dt); status=new SimpleStringProperty(st);}        
        public String getLogId(){return logId.get();}
        public String getUserName(){return userName.get();}
        public String getDate(){return date.get();}
        public String getStatus(){return status.get();}
    }
}
