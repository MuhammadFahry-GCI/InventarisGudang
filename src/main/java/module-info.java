module com.inventaris {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    opens com.inventaris to javafx.fxml;
    opens com.inventaris.view to javafx.fxml;
    opens com.inventaris.model to javafx.base;

    exports com.inventaris;
    exports com.inventaris.view;
    exports com.inventaris.model;
}