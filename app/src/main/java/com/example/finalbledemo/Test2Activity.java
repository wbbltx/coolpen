package com.example.finalbledemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.finalbledemo.ble.BluetoothLe;

public class Test2Activity extends AppCompatActivity {

    private String address;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        name = intent.getStringExtra("name");
    }

    public void connect(View view) {

        BluetoothLe.getDefault()
                .setRetryConnectEnable(false)//设置尝试重新连接
                .setRetryConnectCount(3)//重试连接次数
                .setConnectTimeOut(3000)//连接超时，单位毫秒
                .connectBleDevice(address, false);
    }

    public void disconnect(View view) {

    }
}
