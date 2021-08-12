package com.example.remotecam;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;
    private TextView ip1, ip2, ip3, ip4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button enableCamera = findViewById(R.id.enableCamera);
        ip1=findViewById(R.id.IP1);
        ip2=findViewById(R.id.IP2);
        ip3=findViewById(R.id.IP3);
        ip4=findViewById(R.id.IP4);

        enableCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasCameraPermission()) {
                    if(hasWriteSettingsPermission()) {
                        enableCamera();
                    } else {
                        notifyWriteSettingsPermission();
                    }
                } else {
                    requestCameraPermission();
                }
            }
        });

    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasWriteSettingsPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(this);
        } else {
            return true;
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                CAMERA_PERMISSION,
                CAMERA_REQUEST_CODE
        );
    }

    private void notifyWriteSettingsPermission(){
        Toast.makeText(this, "Write settings permission not granted", Toast.LENGTH_LONG).show();;
    }

    private void enableCamera() {
        Integer IP1, IP2, IP3, IP4;
        IP1= Integer.parseInt(ip1.getText().toString());
        IP2= Integer.parseInt(ip2.getText().toString());
        IP3= Integer.parseInt(ip3.getText().toString());
        IP4= Integer.parseInt(ip4.getText().toString());
        if(isUint8(IP1) && isUint8(IP2) && isUint8(IP3) && isUint8(IP4)) {
            String address = IP1 + "." + IP2 + "." + IP3 + "." + IP4;
            if(address!="0.0.0.0" && address!="255.255.255.255") {
                Intent intent = new Intent(this, CameraActivity.class);
                intent.putExtra("ip_address", address);
                startActivity(intent);
            } else {
                Toast.makeText(this,"IP address not allowed!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,"IP address not valid!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isUint8(Integer n){
        if(n>255 || n<0)
            return false;
        else
            return true;
    }

}