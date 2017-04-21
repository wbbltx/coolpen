package com.example.finalbledemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.finalbledemo.ble.BleManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ControllerActivity extends AppCompatActivity {

    private String address;
    private String name;
    private static final String TAG = ControllerActivity.class.getName();
    private Handler mhandler;
    private TextView textView;
    private boolean is_Receive_No_Key_Write_Success_State;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        BleManager.getDefault().init(this);
//        BleManager.getDefault().conne
        textView = (TextView) findViewById(R.id.textView);

//        registerReceiver(mReceiver, makeFilter());
        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        name = intent.getStringExtra("name");
        mhandler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        BleManager.getDefault()
//                .setRetryConnectEnable(false)//设置尝试重新连接
//                .setRetryConnectCount(3)//重试连接次数
//                .setConnectTimeOut(3000)//连接超时，单位毫秒
//                .setServiceDiscoverTimeOut(5000)//发现服务超时，单位毫秒
//                .connect(address, false, gattCallback);
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            long currentTimeMillis = System.currentTimeMillis();
            Log.i(TAG, "连接状态变化时调用"+status+"-------"+newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                    intentAction = ACTION_GATT_CONNECTED;
//                    mConnectionState = STATE_CONNECTED;
//                    broadcastUpdate(intentAction);
                Log.i(TAG, "已经连接  接收到已连接回调  并将结果返回去"+currentTimeMillis);
                BleManager.getDefault().setIsConnected(true);
                mhandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_BONDING) {
                            Log.i(TAG, "连接成功，去发现服务");
                            BleManager.getDefault().discoverService(address,false,gattCallback);
//                            gatt.discoverServices();
//                            BleManager.getDefault().checkServiceDiscover(ControllerActivity.this,address,false,gattCallback);
                        }
                    }
                }, 600);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                if (status == 133){
//                    未知原因造成连接断开，执行重连
                    BleManager.getDefault()//设置尝试重新连接
                            .connect(address, false, gattCallback);
                }else {
                    BleManager.getDefault().setIsConnected(false);
                    Log.i(TAG, "连接断开,并已经setIsConnected(false)---" + System.currentTimeMillis());
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i(TAG, "有没有发现服务?");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BleManager.getDefault().setIsServiceDiscovered(true);
//                BleManager.getDefault().readCharacteristic();
                Log.i(TAG, "onServicesDiscovered received: 发现服务，接着去使能通知" + status);
                BleManager.getDefault().enableCharacteristicNotification();
            } else {
                BleManager.getDefault().setIsServiceDiscovered(false);
                Log.i(TAG, "onServicesDiscovered received: 未发现服务" + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onCharacteristicRead : Characteristic读取回调" + characteristic.getUuid());
//
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(TAG, "onCharacteristicWrite : Characteristic写入回调" + characteristic.getService().getUuid());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            final String bluMessage = bytesToHexString(characteristic.getValue());
            Log.i(TAG, "onCharacteristicChanged : Notification 是否使能的状态改变---" + bluMessage);
            if (bluMessage != null && !"".equals(bluMessage)) {
                if (bluMessage.startsWith("0f0f")) {
                    if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.NOT_KEY_STATE.getMsg())) {

                        String local_key = BytesUtils.getBleKey();
                        String message = BluUUIDUtils.BluInstruct.NOT_KEY_WRITE.getUuid()+ local_key;
                        SharedPreUtils.setString(ControllerActivity.this, BluCommonUtils.SAVE_WRITE_PEN_KEY, local_key);
                        final byte[] connKey = BytesUtils.HexString2Bytes(message);
                        BleManager.getDefault().writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(),connKey);
                        Log.i(TAG, "笔内没有保存key信息，将key写入"+local_key+"-----"+message);

                        mhandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                if (!is_Receive_No_Key_Write_Success_State)
                                BleManager.getDefault().writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(),connKey);
                                Log.i(TAG, "3s之后再发一次");
                            }
                        },3000);

                    }else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.HAVE_KEY_STATE.getMsg())) {
                        Log.i(TAG, "笔内保存了key信息");
                    }else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.NOT_KEY_WRITE_SUCCEED_STATE.getMsg())) {

                        is_Receive_No_Key_Write_Success_State = true;
                        String cacheKeyMessage = SharedPreUtils.getString(ControllerActivity.this, BluCommonUtils.SAVE_WRITE_PEN_KEY);
                        String message = BluUUIDUtils.BluInstruct.HAVE_KEY_WRITE.getUuid()+cacheKeyMessage;
                        final byte[] connKey = BytesUtils.HexString2Bytes(message);
                        if (cacheKeyMessage != null && !"".equals(cacheKeyMessage)) {
                            BleManager.getDefault().writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(),connKey);
                        }
                        Log.i(TAG, "笔内没有保存key信息，写入成功,再写一次");
                        mhandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                if (!is_Receive_No_Key_Write_Success_State)
                                BleManager.getDefault().writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(),connKey);
                                Log.i(TAG, "笔内没有保存key信息，写入成功,再写一次,3s之后再发一次");
                            }
                        },3000);

                    }else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.NOT_KEY_WRITE_FAILURE_STATE.getMsg())) {
                        Log.i(TAG, "笔内没有保存key信息，写入失败");
                    }else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.HAVE_KEY_WRITE_SUCCEED_STATE.getMsg())) {
                        Log.i(TAG, "笔内保存了key信息，写入成功,最终成功");
                    }else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.HAVE_KEY_WRITE_FAILURE_STATE.getMsg())) {
                        Log.i(TAG, "笔内保存了key信息，写入失败");
                    }else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.NOT_STORAGE_INFO.getMsg())) {
                        //无存储信息，打开书写通道
                    }
                }
            }
            String msg = BluUUIDUtils.BluInstruct.OPEN_WRITE_CHANNEL.getUuid();
            final byte[] connKey = BytesUtils.HexString2Bytes(msg);
            BleManager.getDefault().writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(), connKey);
            mhandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BleManager.getDefault().writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(), connKey);
                }
            },3000);
            Log.i(TAG, "通道已打开");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }
    };

    public String bytesToHexString(byte[] data) {
        if (data == null)
            return null;

        final StringBuilder stringBuilder = new StringBuilder(data.length);
        for (byte byteChar : data)
            stringBuilder.append(String.format("%02x", byteChar));
        return stringBuilder.toString();
    }

    public void sendmessage(View view) {
//        String local_key = BytesUtils.getBleKey();
//        BleManager.getDefault().setRetryConnectEnable(false)//设置尝试重新连接
//                .setRetryConnectCount(3)//重试连接次数
//                .setConnectTimeOut(3000)//连接超时，单位毫秒
//                .setServiceDiscoverTimeOut(5000)//发现服务超时，单位毫秒
//                .connect(this, address, false, gattCallback);

        String message = BluUUIDUtils.BluInstruct.OBTAIN_KEY_STATE.getUuid();
        byte[] connKey = BytesUtils.HexString2Bytes(message);
        BleManager.getDefault().writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(), connKey);
    }

    private IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        return filter;
    }

//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.i(TAG, "收到的广播---"+intent.getAction());
//        }
//    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(mReceiver);
    }

    public void close(View view) {
        BleManager.getDefault().close();
    }

    public void disconnect(View view) {
        BleManager.getDefault().disconnect();
    }

    public void connect(View view) {
        Log.i(TAG, "重复连接");
        BleManager.getDefault()//设置尝试重新连接
                .connect( address, false, gattCallback);
    }

    public void noretry(View view) {
        Log.i(TAG, "不重复连接");
        BleManager.getDefault()//设置尝试重新连接
                .connect( address, false, gattCallback);
    }
}
