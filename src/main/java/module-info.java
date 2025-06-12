module com.hms {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    // Third-party UI libraries
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    // SQL dependencies
    requires java.sql;
    requires mysql.connector.j;
    requires itextpdf;
    requires org.apache.poi.ooxml;
    requires java.desktop;


    // Package exports & FXML access
    opens com.hms to javafx.fxml;
    exports com.hms;

    exports com.hms.views.auth;
    opens com.hms.views.auth to javafx.fxml;

    exports com.hms.views;
    opens com.hms.views to javafx.fxml;

    exports com.hms.views.users.admin to javafx.fxml;
    opens com.hms.views.users.admin;

    exports com.hms.views.users.staff to javafx.fxml;
    opens com.hms.views.users.staff;

    exports com.hms.views.users.student to javafx.fxml;
    opens com.hms.views.users.student;

}
