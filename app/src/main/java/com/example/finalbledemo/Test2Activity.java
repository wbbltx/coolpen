package com.example.finalbledemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.finalbledemo.ble.BluetoothLe;
import com.example.finalbledemo.ble.OnConnectListener;
import com.example.finalbledemo.ble.OnElectricityRequestListener;
import com.example.finalbledemo.ble.OnKeyListener;
import com.example.finalbledemo.ble.OnLeNotificationListener;
import com.example.finalbledemo.ble.OnReadRssiListener;

public class Test2Activity extends AppCompatActivity {

    private String address;
    private String name;
    private static final String TAG = BluetoothLe.class.getName();
    Handler mhandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        name = intent.getStringExtra("name");
        Log.i(TAG, "跳转传值" + address + "-------" + name);

        BluetoothLe.getDefault().setOnLeNotificationListener(new OnLeNotificationListener() {
            @Override
            public void onWrite(String info) {
                Log.i(TAG, "在Test2Activity中接收到的笔迹" + info);
            }

            @Override
            public void onHistroyInfoReadCompleted(String info) {
                Log.i(TAG, "在Test2Activity中接收到的历史笔迹信息" + info);
                BluetoothLe.getDefault().sendBleInstruct(BluetoothLe.OPEN_WRITE_CHANNEL);
            }

            @Override
            public void onHistroyInfoDetected() {
                Log.i(TAG, "检测到历史数据");
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showDialog();
                    }
                });
            }

            @Override
            public void onHistroyInfoDeleted() {
                Log.i(TAG, "历史数据删除完毕,打开书写通道");
                BluetoothLe.getDefault().sendBleInstruct(BluetoothLe.OPEN_WRITE_CHANNEL);
            }
        });

        BluetoothLe.getDefault().setOnKeyListener(new OnKeyListener() {
            @Override
            public void onSuccess(String key) {
                SharedPreUtils.setString(Test2Activity.this, BluCommonUtils.SAVE_WRITE_PEN_KEY, key);
                Log.i(TAG, "key生成成功 保存到本地---"+key);
            }
            @Override
            public void onGetKeyEmpty() {
                String cacheKeyMessage = SharedPreUtils.getString(Test2Activity.this, BluCommonUtils.SAVE_WRITE_PEN_KEY);
                BluetoothLe.getDefault().setKey(cacheKeyMessage);
                Log.i(TAG, "key从本地取出 设置进去---"+cacheKeyMessage);
            }
        });

        BluetoothLe.getDefault().setOnConnectListener(new OnConnectListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(Test2Activity.this,"1111111111连接成功",Toast.LENGTH_SHORT).show();
                Log.i(TAG, "1111111111连接成功");
            }

            @Override
            public void onFailed() {
                Toast.makeText(Test2Activity.this,"1111111111连接断开",Toast.LENGTH_SHORT).show();
                Log.i(TAG, "1111111111连接断开");
            }
        });

        BluetoothLe.getDefault().setOnElectricityRequestListener(new OnElectricityRequestListener() {
            @Override
            public void onElectricityDetected(String electricity) {
                Log.i(TAG, "电量信息"+electricity);
            }
        });
    }


    public void showDialog() {
        new AlertDialog.Builder(this)
                .setTitle("有历史数据")
                .setPositiveButton("读取", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "打开存储通道" );
                        BluetoothLe.getDefault().sendBleInstruct(BluetoothLe.OPEN_STORAGE_CHANNEL);
                        mhandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "读取历史信息");
                                BluetoothLe.getDefault().sendBleInstruct(BluetoothLe.READ_STORAGE_INFO);
                            }
                        }, 600);
                    }
                })
                .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BluetoothLe.getDefault().sendBleInstruct(BluetoothLe.EMPTY_STORAGE_DATA);
                        Log.i(TAG, "删除历史信息");
                    }
                })
                .create().show();
    }

    public void connect(View view) {
        if (BluetoothLe.getDefault().isBluetoothOpen()){
            BluetoothLe.getDefault().resetRetryConfig();
            BluetoothLe.getDefault()
//                .setRetryConnectEnable(false)
                    .connectBleDevice(address, false);
        }else {
            BluetoothLe.getDefault().openBle();
        }
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
        BluetoothLe.getDefault().sendBleInstruct(BluetoothLe.OBTAIN_ELECTRICITY);
    }


    public void antilost(View view) {
        Log.i(TAG, "点击了防丢按钮");
        BluetoothLe.getDefault().enableAntiLost(5000, new OnReadRssiListener() {
            @Override
            public void onSuccess(int rssi) {
                Log.i(TAG, "检测到信号强度"+rssi);
            }
        });
    }

    public void closeantilost(View view) {
        BluetoothLe.getDefault().disableAntiLost();
    }
}
