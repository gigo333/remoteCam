/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.remotecam;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

/**
 *
 * @author user
 */
public class RecvThread extends Thread {
    private final String address;
    private final int port;
    private boolean connected;
    private int height;
    private int width;
    private boolean running;
    private Socket so;
    private boolean torchEnabled;
    private int zoom;
    private int batteryStatus;
    
    ImageView imageView;
    Text batteryLabel;

    public RecvThread(String address, int port, ImageView imageView, Text batteryLabel){
        this.address=address;
        this.port=port;
        this.imageView=imageView;
        this.batteryLabel=batteryLabel;
        connected=false;
        running=false;
        so = null;
        torchEnabled=false;
        zoom=100;
        batteryStatus=0;
        this.setDaemon(true);
    }

    @Override
    public void run(){
        ServerSocket listener=null;
        DataInputStream dIn =null;
        try {
            InetSocketAddress SocketAddressendPoint=new InetSocketAddress(address, port);  
            listener = new ServerSocket();
            listener.setReuseAddress(true);
            listener.bind(SocketAddressendPoint);
            running=true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Started!");
        while (running) {
            if (!connected) {
                try {
                    
                    so = listener.accept();
                    so.setSoTimeout(1000);
                    System.out.println("Connected!");
                    byte[] sendHeight = ByteBuffer.allocate(4).putInt(height).array();
                    byte[] sendWidth = ByteBuffer.allocate(4).putInt(width).array();
                    dIn= new DataInputStream(so.getInputStream());
                    height=dIn.readInt();
                    width=dIn.readInt();
                    System.out.println(height+" "+width);
                    int val=torchEnabled ? 1 : 0;
                    byte[] buffer = ByteBuffer.allocate(4).putInt(zoom*2+val).array();
                    so.getOutputStream().write(buffer);
                    connected=true;
                }  catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if(connected){
                
                try {
                    //Log.v(TAG, String.valueOf(toSend.length));
                    int battStatus=dIn.readInt();
                    if(batteryStatus!=battStatus){
                        batteryStatus=battStatus;
                        String text = "Battery: "+battStatus/2+"%";
                        if(battStatus%2==1){
                            text+="\tCharging";
                        }
                        batteryLabel.setText(text);
                    }
                    int recv_len=dIn.readInt();
                    byte[] buffer = new byte[recv_len]; 
                    int received_len = 0;
                    int old_received_len = 0;
                    byte[] chunk = null;

                    while(recv_len != received_len) {
                        chunk = new byte[recv_len - received_len];
                        old_received_len = received_len;
                        received_len += so.getInputStream().read(chunk);
                        System.arraycopy(chunk, 0, buffer, old_received_len, chunk.length);
                        //System.out.print("RECV LEN: ");
                        //System.out.println(received_len);
                    }
                    Image img = new Image(new ByteArrayInputStream(buffer));
                    imageView.setImage(img);
                } catch (IOException e) {
                    e.printStackTrace();
                    connected=false;
                    System.out.println("Disconnected!");
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
        System.out.println("Stopped!");
    }
    
    public void sendCommand(int zoom, boolean torch){
        if(connected){
            boolean modified=false;
            if(zoom!=-1 && (zoom)!=this.zoom){
                this.zoom=zoom;
                modified=true;
            }
            if(torch){
                torchEnabled=!torchEnabled;
                modified=true;
            }
            if(modified){
                try {
                    int val=torchEnabled ? 1 : 0;
                    byte[] buffer = ByteBuffer.allocate(4).putInt(val+zoom*2).array();
                    so.getOutputStream().write(buffer);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
}
