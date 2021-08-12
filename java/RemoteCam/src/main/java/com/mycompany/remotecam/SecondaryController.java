package com.mycompany.remotecam;

import java.net.InetAddress;
import java.net.UnknownHostException;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class SecondaryController {

    @FXML
    private ImageView imageView;
    @FXML
    private Text batteryLabel;

    public void initialize(){
        //imageView.setScaleX(-1);
        try {
            String address = InetAddress.getLocalHost().toString().split("/")[1];
            System.out.println(address);
            RecvThread recvThread = new RecvThread(address, 10000, imageView, batteryLabel);
            SerialComm serialComm = new SerialComm(recvThread);
            recvThread.start();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }
}