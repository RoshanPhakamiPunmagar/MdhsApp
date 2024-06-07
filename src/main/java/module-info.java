module mdhsapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.base;
    opens mdhsapp to javafx.fxml;
    exports mdhsapp;
}
