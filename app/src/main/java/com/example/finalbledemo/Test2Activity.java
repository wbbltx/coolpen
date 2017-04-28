package com.example.finalbledemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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
    private ProgressBar progressBar;
    private TextView textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textview = (TextView) findViewById(R.id.textView2);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        name = intent.getStringExtra("name");

        /**
         * 设置通知的监听
         */
        BluetoothLe.getDefault().setOnLeNotificationListener(new OnLeNotificationListener() {
            @Override
            public void onWrite(String info) {
                textview.setText("在到的笔迹信息" + info);
                Log.i(TAG, "在到的笔迹信息" + info);
            }

            @Override
            public void onReadHistroyInfo(String info) {
                textview.setText("接收到的历史笔迹信息"+info);
                Log.i(TAG, "在Test2Activity中接收到的历史笔迹信息" + info);
//                存储数据读取完毕，开启书写通道
                BluetoothLe.getDefault().sendBleInstruct(BluetoothLe.OPEN_WRITE_CHANNEL);
            }

            @Override
            public void onHistroyInfoDetected() {
                Log.i(TAG, "检测到历史数据，打开对话框");
                textview.setText("检测到历史数据，打开对话框");
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showDialog();
                    }
                });
            }

            @Override
            public void onHistroyInfoDeleted() {
                textview.setText("存储数据删除完毕,打开书写通道");
                Log.i(TAG, "存储数据删除完毕,打开书写通道");
                progressBar.setVisibility(View.GONE);
                BluetoothLe.getDefault().sendBleInstruct(BluetoothLe.OPEN_WRITE_CHANNEL);
            }
        });

        /**
         * 对key的监听
         */
        BluetoothLe.getDefault().setOnKeyListener(new OnKeyListener() {
            @Override
            public void onKeyGenerated(String key) {
                SharedPreUtils.setString(Test2Activity.this, BluCommonUtils.SAVE_WRITE_PEN_KEY, key);
                Log.i(TAG, "key生成成功 保存到本地---" + key);
            }

            @Override
            public void onSetLocalKey() {
                String cacheKeyMessage = SharedPreUtils.getString(Test2Activity.this, BluCommonUtils.SAVE_WRITE_PEN_KEY);
                BluetoothLe.getDefault().setKey(cacheKeyMessage);
                Log.i(TAG, "key从本地取出 设置进去 以便进行比较---" + cacheKeyMessage);
            }
        });

        /**
         * 对连接状态的监听
         */
        BluetoothLe.getDefault().setOnConnectListener(new OnConnectListener() {
            @Override
            public void onConnected() {
                Toast.makeText(Test2Activity.this, "连接成功", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onDisconnected() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Test2Activity.this, "连接断开", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Test2Activity.this, "请将蓝牙笔设置为配对状态", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void isConnecting() {
                Log.i(TAG, "正在连接 请等待...");
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        BluetoothLe.getDefault().setOnElectricityRequestListener(new OnElectricityRequestListener() {
            @Override
            public void onElectricityDetected(String electricity) {
                Log.i(TAG, "电量信息是" + electricity);
                textview.setText("电量信息是："+electricity);
            }
        });
    }

    public void showDialog() {
        new AlertDialog.Builder(this)
                .setTitle("有历史数据")
                .setPositiveButton("读取", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "打开存储通道");
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
                        progressBar.setVisibility(View.VISIBLE);
                        Log.i(TAG, "删除历史信息");
                    }
                })
                .create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BluetoothLe.getDefault().isBluetoothOpen()) {
            BluetoothLe.getDefault().resetRetryConfig();
            BluetoothLe.getDefault()
                    .connectBleDevice(address);
        } else {
            BluetoothLe.getDefault().enableBluetooth();
        }
    }

    public void connect(View view) {
        if (BluetoothLe.getDefault().isBluetoothOpen()) {
            BluetoothLe.getDefault().resetRetryConfig();
            BluetoothLe.getDefault()
                    .connectBleDevice(address);
        } else {
            BluetoothLe.getDefault().enableBluetooth();
        }
    }

    public void disconnect(View view) {
        Log.i(TAG, "点击了断开连接按钮");
        BluetoothLe.getDefault().disconnectBleDevice();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFinishing();
    }

    public void reconnect(View view) {
        BluetoothLe.getDefault()
                .setRetryConnectEnable(true)//设置尝试重新连接
                .setRetryConnectCount(1)//重试连接次数
                .setConnectTimeOut(5000)//连接超时，单位毫秒
                .connectBleDevice(address);
    }


    public void getElectricit(View view) {
        textview.setText("点击了获取电量");
        boolean connected = BluetoothLe.getDefault().getConnected();
        Log.i(TAG, "获取连接信息" + connected);
        if (connected) {
            BluetoothLe.getDefault().sendBleInstruct(BluetoothLe.OBTAIN_ELECTRICITY);
        }
    }


    public void antilost(View view) {
        Log.i(TAG, "开启了防丢功能");
        textview.setText("开启了防丢功能");
        BluetoothLe.getDefault().enableAntiLost(5000, new OnReadRssiListener() {
            @Override
            public void onSuccess(int rssi) {
                textview.setText("信号强度是 " + rssi);
                Log.i(TAG, "检测到信号强度:" + rssi);
            }
        });
    }

    public void closeantilost(View view) {
        BluetoothLe.getDefault().disableAntiLost();
    }

    public void clearkey(View view) {
        SharedPreUtils.setString(Test2Activity.this, BluCommonUtils.SAVE_WRITE_PEN_KEY, "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothLe.getDefault().close();
    }
}
