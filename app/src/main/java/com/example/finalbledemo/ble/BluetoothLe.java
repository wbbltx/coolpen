package com.example.finalbledemo.ble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.finalbledemo.BluCommonUtils;
import com.example.finalbledemo.BluUUIDUtils;
import com.example.finalbledemo.BytesUtils;
import com.example.finalbledemo.ControllerActivity;
import com.example.finalbledemo.SharedPreUtils;
import com.example.finalbledemo.TestActivity;

import java.util.UUID;
/**
 * @anthor wubinbin
 * @time 2017/4/20 17:50
 */
public class BluetoothLe {

    private static final String TAG = BluetoothLe.class.getName();
    private BleManager bleManager;
    private int scanPeriod = 10000;
    private UUID[] serviceUUID = {BluUUIDUtils.BtSmartUuid.UUID_SERVICE.getUuid()};
    private OnBleScanListener onBleScanListener;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean is_Receive_No_Key_Write_Success_State;
    Context context;

    private static class SingletonHolder {
        private static final BluetoothLe INSTANCE = new BluetoothLe();
    }

    private BluetoothLe() {
    }

    /**
     * 获取Ble实例
     *
     * @return 返回Ble实例
     */
    public static BluetoothLe getDefault() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        if (bleManager == null) {
            bleManager = new BleManager(context.getApplicationContext(),mHandler);
        }
    }

    /**
     * 开启蓝牙 内部检测是否支持BLE
     */
    public void openBle() {
        bleManager.enableBluetooth();
    }

    /**
     * 关闭蓝牙
     */
    public void closeBle() {
        bleManager.disableBluetooth();
    }

    /**
     * 扫描回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (onBleScanListener != null) {
                onBleScanListener.onScanResult(device, rssi, scanRecord);
            }
        }
    };

    /**
     * @param scanPeriod 设置扫描时长 不设置默认为10秒
     * @return
     */
    public BluetoothLe setScanPeriod(int scanPeriod) {
        this.scanPeriod = scanPeriod;
        return this;
    }

    /**
     * @param serviceUUID 设置扫描目标 不进行设置默认只搜索酷神笔 设置为null搜索不进行过滤 也可以根据场景需求设置需要搜索的服务
     * @return
     */
    public BluetoothLe setScanByServiceUUID(UUID[] serviceUUID) {
        this.serviceUUID = serviceUUID;
        return this;
    }

    /**
     * 开始扫描
     *
     * @param activity
     */
    public void startScan(Activity activity, OnBleScanListener onBleScanListener) {

        this.onBleScanListener = onBleScanListener;

        requestPermission(activity);

        bleManager.scanLeDevice(serviceUUID, scanPeriod, mLeScanCallback);
    }

    private void requestPermission(Activity context) {
        Log.i("controll", "执行权限申请");
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                return;
            }
            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
        bleManager.stopScanLeDevice(mLeScanCallback);
        if (onBleScanListener != null) {
            onBleScanListener.onScanCompleted();
        }
    }

    /**
     * 连接远程设备
     * @param remote 远程设备，支持mac地址和bluetoothdevice
     * @param autoConnect  是否自动连接 一般为否
     */
    public void connectBleDevice(Object remote,boolean autoConnect){

        bleManager.connect(remote,autoConnect,gattCallback);
    }

    //    设置是否允许重连 默认不允许
    public BluetoothLe setRetryConnectEnable(boolean b) {
        bleManager.setRetryConnectEnable(b);
        return this;
    }
    //    设置连接次数 重连状态为FALSE 设置无效 默认一次
    public BluetoothLe setRetryConnectCount(int i) {
        bleManager.setRetryConnectCount(i);
        return this;
    }
    //    设置连接超时 重连状态为FALSE 设置无效 默认5秒
    public BluetoothLe setConnectTimeOut(int i) {
        bleManager.setConnectTimeOut(i);
        return this;
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
                bleManager.setIsConnected(true);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_BONDING) {
                            Log.i(TAG, "连接成功，去发现服务");
//                            BleManager.getDefault().discoverService(address,false,gattCallback);
//                            gatt.discoverServices();
//                            BleManager.getDefault().checkServiceDiscover(ControllerActivity.this,address,false,gattCallback);
                        }
                    }
                }, 600);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                if (status == 133){
//                    未知原因造成连接断开，执行重连
//                    BleManager.getDefault()//设置尝试重新连接
//                            .connect(address, false, gattCallback);
                }else {
                    bleManager.setIsConnected(false);
                    Log.i(TAG, "连接断开,并已经setIsConnected(false)---" + System.currentTimeMillis());
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i(TAG, "有没有发现服务?");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                bleManager.setIsServiceDiscovered(true);
//                BleManager.getDefault().readCharacteristic();
                Log.i(TAG, "onServicesDiscovered received: 发现服务，接着去使能通知" + status);
                bleManager.enableCharacteristicNotification();
            } else {
                bleManager.setIsServiceDiscovered(false);
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
//                        SharedPreUtils.setString(ControllerActivity.this, BluCommonUtils.SAVE_WRITE_PEN_KEY, local_key);
                        final byte[] connKey = BytesUtils.HexString2Bytes(message);
                        bleManager.writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(),connKey);
                        Log.i(TAG, "笔内没有保存key信息，将key写入"+local_key+"-----"+message);

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                if (!is_Receive_No_Key_Write_Success_State)
                                bleManager.writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(),connKey);
                                Log.i(TAG, "3s之后再发一次");
                            }
                        },3000);

                    }else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.HAVE_KEY_STATE.getMsg())) {
                        Log.i(TAG, "笔内保存了key信息");
                    }else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.NOT_KEY_WRITE_SUCCEED_STATE.getMsg())) {

                        is_Receive_No_Key_Write_Success_State = true;
                        String cacheKeyMessage = SharedPreUtils.getString(context, BluCommonUtils.SAVE_WRITE_PEN_KEY);
                        String message = BluUUIDUtils.BluInstruct.HAVE_KEY_WRITE.getUuid()+cacheKeyMessage;
                        final byte[] connKey = BytesUtils.HexString2Bytes(message);
                        if (cacheKeyMessage != null && !"".equals(cacheKeyMessage)) {
                            bleManager.writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(),connKey);
                        }
                        Log.i(TAG, "笔内没有保存key信息，写入成功,再写一次");
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                if (!is_Receive_No_Key_Write_Success_State)
                                bleManager.writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(),connKey);
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
            bleManager.writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(), connKey);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bleManager.writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(), connKey);
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


}
