module com.mycompany.remotecam {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.remotecam to javafx.fxml;
    exports com.mycompany.remotecam;
    requires com.fazecast.jSerialComm;
}
