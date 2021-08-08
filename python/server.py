import cv2
import socket
import struct
from io import BytesIO
import numpy as np
import keyboard
import time
import threading

#default port (set also in android app)
PORT=10000

so=None

def controlHandler():
    zoom=100
    torchEnabled=False
    control=-1
    while True:
        if keyboard.is_pressed('z'):
            if(zoom<1000):
                print("Zooming in!")
                zoom+=100
                control=zoom*2
                changed=True
        elif keyboard.is_pressed('x'):
            if(zoom>100):
                print("Zooming out!")
                zoom-=100
                control=zoom*2
                changed=True
                
        elif keyboard.is_pressed('c'):
            torchEnabled= not torchEnabled
            print("Torch:", torchEnabled)
            control= torchEnabled
            changed=True
            
        if(control!=-1):
            toSend=struct.pack(">i", control)
            if so!=None:
                try:
                    so.send(toSend)
                except:
                    pass
                
            control=-1
            changed=False
            time.sleep(0.5)
        else:
            time.sleep(0.05)

address=socket.gethostbyname(socket.gethostname())
print("The server IP address is", address)
sc=socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sc.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
sc.bind((address,PORT))
controlThread = threading.Thread(target=controlHandler)
controlThread.daemon=True
controlThread.start()
while(1):
    sc.listen(1)
    so, addr= sc.accept()
    so.settimeout(0.5)
    try:
        print("Connected!")
        b=so.recv(4)
        l=len(b)
        while(l!=4):
            b+=so.recv(4-l)
            l=len(b)
                
        height=struct.unpack(">i", b)[0]
        b=so.recv(4)
        l=len(b)
        while(l!=4):
            b+=so.recv(4-l)
            l=len(b)
                
        width=struct.unpack(">i", b)[0]
        print(height, width)
        toSend=struct.pack(">i", 100)
        so.send(toSend)
        while(1):
            b=so.recv(4)
            l=len(b)
            while(l!=4):
                b+=so.recv(4-l)
                l=len(b)
                
            length=struct.unpack(">i", b)[0]
            buffer=so.recv(length)
            l=len(buffer)
            while(l!=length):
                buffer+=so.recv(length-l)
                l=len(buffer)
            
            i = np.frombuffer(buffer,dtype=np.uint8)
            im = cv2.imdecode(i,cv2.IMREAD_UNCHANGED)
            im=cv2.flip(im,1)
            cv2.imshow("image", im)
            cv2.waitKey(1) 
            
    except Exception as e:
        print(e)
        print("Disonnected!")
        so.close()
        so=None

