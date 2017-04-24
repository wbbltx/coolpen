package com.example.finalbledemo;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.finalbledemo.ble.BluetoothLe;
import com.example.finalbledemo.ble.OnLeNotificationListener;

public class Test2Activity extends AppCompatActivity {

    private String address;
    private String name;
    private static final String TAG = BluetoothLe.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        name = intent.getStringExtra("name");
        Log.i(TAG, "跳转传值"+address+"-------"+name);

        BluetoothLe.getDefault().setOnLeNotificationListener(new OnLeNotificationListener() {
            @Override
            public void onWrite(String info) {
                Log.i(TAG, "在Test2Activity中接收到的笔迹"+info);
            }

            @Override
            public void onHistroyInfoReadCompleted(String info) {
                Log.i(TAG, "在Test2Activity中接收到的历史笔迹信息"+info);
            }

            @Override
            public void onHistroyInfoDetected() {
                Log.i(TAG, "检测到历史数据");

            }

            @Override
            public void onHistroyInfoDeleted() {
                Log.i(TAG, "历史数据删除完毕");
            }

            @Override
            public void onElectricityDetected(int electricity) {
                Log.i(TAG, "检测到电量"+electricity);
            }
        });
    }

    public void connect(View view) {

        BluetoothLe.getDefault()
//                .setRetryConnectEnable(false)
                .connectBleDevice(address, false);
    }

    public void disconnect(View view) {
        BluetoothLe.getDefault().disconnectBleDevice();
    }

    public void reconnect(View view) {
        BluetoothLe.getDefault()
                .setRetryConnectEnable(true)//设置尝试重新连接
                .setRetryConnectCount(3)//重试连接次数
                .setConnectTimeOut(3000)//连接超时，单位毫秒
                .connectBleDevice(address, false);
    }


    public void getElectricit(View view) {
        BluetoothLe.getDefault().sendBleInstruct(BluUUIDUtils.BluInstruct.OBTAIN_ELECTRICITY.getUuid(),false);
    }
}
