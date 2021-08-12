package com.mycompany.remotecam;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.WindowEvent;

public class PrimaryController {
    @FXML Label ipLabel;
    
    public void initialize(){
        try {
            String ipAddress = InetAddress.getLocalHost().toString();
            ipLabel.setText(ipLabel.getText() + " " + ipAddress.split("/")[1]);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }
    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
        
    }
    
}
