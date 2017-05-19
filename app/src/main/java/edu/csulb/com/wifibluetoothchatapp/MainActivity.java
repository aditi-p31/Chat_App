package edu.csulb.com.wifibluetoothchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button bluetooth,wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        bluetooth = (Button)findViewById(R.id.bt);
        wifi = (Button)findViewById(R.id.Wifi);


        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ChatScreen.class);
                startActivity(i);
            }
        });


        wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, WiFiDirectActivity.class);
                startActivity(i);
            }
        });

    }
}