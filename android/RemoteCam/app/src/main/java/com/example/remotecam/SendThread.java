package com.example.remotecam;

import android.util.Log;

import androidx.camera.core.CameraControl;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class SendThread  extends Thread {
    private byte[] buffer;
    private String address;
    private int port;
    private boolean connected;
    private String TAG = "SOCKET";
    private int height;
    private int width;
    private boolean running;
    private Socket so;
    private CameraControl cameraControl;
    private boolean torchEnabled;
    private int zoom;
    private int batteryStatus;

    public SendThread(String address, int port, int height, int width){
        this.address=address;
        this.port=port;
        this.height=height;
        this.width=width;
        batteryStatus=0;
        connected=false;
        running=false;
        buffer = null;
        so = null;
        cameraControl=null;
        torchEnabled=false;
        zoom=100;
        this.setDaemon(true);
    }

    @Override
    public void run(){
        boolean torch=false;
        final Thread recvThread= new Thread() {
            @Override
            public void run() {
                recvControls();
            }
        };
        recvThread.setDaemon(true);
        recvThread.start();
        Log.v(TAG, "Started!");
        running=true;
        while (running) {
            while (!connected) {
                try {
                    so = new Socket(address, port);
                    byte[] sendHeight = ByteBuffer.allocate(4).putInt(height).array();
                    byte[] sendWidth = ByteBuffer.allocate(4).putInt(width).array();
                    so.getOutputStream().write(sendHeight);
                    so.getOutputStream().write(sendWidth);
                    so.getOutputStream().flush();
                    connected = true;
                    Log.v(TAG, "Connected!");
                    if(cameraControl!=null) {
                        cameraControl.setZoomRatio(zoom / 100);
                        cameraControl.enableTorch(torchEnabled);
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!connected) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(buffer!=null){
                byte toSend[]= buffer.clone();
                byte[] sendLen = ByteBuffer.allocate(4).putInt(toSend.length).array();
                byte[] sendBatteryStatus= ByteBuffer.allocate(4).putInt(batteryStatus).array();
                buffer=null;
                try {
                    //Log.v(TAG, String.valueOf(toSend.length));
                    so.getOutputStream().write(sendBatteryStatus);
                    so.getOutputStream().write(sendLen);
                    so.getOutputStream().write(toSend);
                    so.getOutputStream().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    connected=false;
                    Log.v(TAG, "Disconnected!");
                }
            }
        }
        if(connected){
            try {
                so.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.v(TAG, "Stopped!");
    }

    public void setBuffer(byte[] buffer){
        if(connected && this.buffer==null){
            this.buffer=buffer;
        }
    }

    public void setCameraControl(CameraControl cameraControl){
        this.cameraControl=cameraControl;
    }

    /*public void stopThread(){
        running=false;
        Log.v(TAG, "Stopping!");
    }*/

    private void recvControls(){
        DataInputStream dIn =null;
        while (running){
            if(connected){
                try {
                    dIn = new DataInputStream(so.getInputStream());
                    int recvd = dIn.readInt();
                    if((recvd/2)>50)
                        zoom=recvd/2;
                    if(recvd%2==0){
                        torchEnabled=false;
                    } else {
                        torchEnabled=true;
                    }
                    if(cameraControl!=null) {
                        cameraControl.setZoomRatio(zoom / 100);
                        cameraControl.enableTorch(torchEnabled);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    connected=false;
                    Log.v(TAG, "Disconnected!");
                }
            } else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void setBatteryStatus(int batteryStatus){
        if(this.batteryStatus!=batteryStatus)
            this.batteryStatus=batteryStatus;
    }
}
