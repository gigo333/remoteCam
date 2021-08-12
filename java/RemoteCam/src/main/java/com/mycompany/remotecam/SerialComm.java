/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package com.mycompany.remotecam;

import com.fazecast.jSerialComm.*;

/**
 *
 * @author user
 */
public class SerialComm implements SerialPortDataListener {
    RecvThread recvThread;
    
     public SerialComm(RecvThread recvThread){
         this.recvThread=recvThread;
         SerialPort[] serialPorts = SerialPort.getCommPorts();
         if(serialPorts.length>0){
            SerialPort serialPort =serialPorts[serialPorts.length-1];
            serialPort.setBaudRate(115200);
            serialPort.openPort();
            sendNextionCommnad(serialPort, "h0.val=1");
            sendNextionCommnad(serialPort, "n0.val=1");
            sendNextionCommnad(serialPort, "page 1");
            serialPort.addDataListener(this);
         }
     }
     
     @Override
     public void serialEvent(SerialPortEvent spe){
         if(spe.getEventType()==1){
            SerialPort port = spe.getSerialPort();
            byte[] buffer =new byte[4];
            int receivedLen=0;
            do{
                receivedLen+=port.readBytes(buffer, 4-receivedLen, receivedLen);
            } while(receivedLen<4); 
             String s=new String(buffer);
             if(s.contains("H0")){
                 int val=(int)buffer[2];
                 System.out.println("Zoom: x"+val);
                 recvThread.sendCommand(val*100, false);
             } else if(s.contains("B00")){
                 System.out.println("Torch!");
                 recvThread.sendCommand(-1, true);
             }else if(s.contains("B01")){
                 sendNextionCommnad(port, "page 0");
                 System.out.println("Quitting!");
                 //port.closePort();
                 System.exit(0);
             }
           
         }
         
         
     }
     
     @Override
     public int getListeningEvents(){
         return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
     }
     
     private void sendNextionCommnad(SerialPort serialPort, String command){
         byte[] buffer = command.getBytes();
         byte[] send={(byte)0Xff,(byte)0Xff,(byte)0Xff };
         serialPort.writeBytes(buffer, buffer.length);
         serialPort.writeBytes(send, send.length);
     }
     
}
