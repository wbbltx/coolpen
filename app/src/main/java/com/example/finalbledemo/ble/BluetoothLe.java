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
import android.widget.Toast;

import com.example.finalbledemo.BytesUtils;
import com.example.finalbledemo.SystemTransformUtils;
import com.example.finalbledemo.Utils;

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
    private boolean is_Receive_Have_Key_Write_Success_State;
    private boolean is_Receive_Key_State;

    private Context applicationContext;

    private OnReadRssiListener onReadRssiListener;
    private OnLeNotificationListener onLeNotificationListener;
    private OnKeyListener onKeyGeneratedListener;

    /**
     * 打开存储通道
     */
    public static final int OPEN_STORAGE_CHANNEL = 0;
    /**
     * 打开书写通道
     */
    public static final int OPEN_WRITE_CHANNEL = 1;
    /**
     * 打开存储通道
     */
    public static final int READ_STORAGE_INFO = 2;
    /**
     * 清空存储信息
     */
    public static final int EMPTY_STORAGE_DATA = 3;
    /**
     * 获取电量
     */
    public static final int OBTAIN_ELECTRICITY = 4;
    private String cacheKeyMessage;


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
    public void init(Activity context) {
        applicationContext = context.getApplicationContext();
        if (bleManager == null) {
            bleManager = new BleManager(applicationContext, mHandler);
        }
    }

    /**
     * 开启蓝牙 内部检测是否支持BLE
     */
    public boolean openBle() {
        boolean isOpen = false;
        if (bleManager.isBluetoothSupported() && bleManager.isBleSupported() && !bleManager.isBluetoothOpen()) {
            isOpen = bleManager.enableBluetooth();
        }
        return isOpen;
    }

    /**
     * 关闭蓝牙
     */
    public void closeBle() {
        bleManager.disableBluetooth();
    }

    /**
     * @return true if bluetooth is open
     */
    public boolean isBluetoothOpen() {
        return bleManager.isBluetoothOpen();
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
        if (isBluetoothOpen()) {
            bleManager.scanLeDevice(serviceUUID, scanPeriod, mLeScanCallback);
        } else {
            Toast.makeText(activity, "请开启蓝牙", Toast.LENGTH_SHORT).show();
        }
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

//    public BluetoothLe setReadRssiInterval(int millisecond) {
//        bleManager.setReadRssiIntervalMillisecond(millisecond);
//        return this;
//    }

    /**
     * 连接远程设备
     *
     * @param remote      远程设备，支持mac地址和bluetoothdevice
     * @param autoConnect 是否自动连接 一般为否
     */
    public void connectBleDevice(Object remote, boolean autoConnect) {

        bleManager.connect(remote, autoConnect, gattCallback);
    }

    public void disconnectBleDevice() {
        closeResource();
        bleManager.disconnect();
    }

    private void closeResource() {
        is_Receive_Key_State = false;
        is_Receive_No_Key_Write_Success_State = false;
        is_Receive_Have_Key_Write_Success_State = false;
        onReadRssiListener = null;
        onLeNotificationListener = null;
    }

    public void close() {
        closeResource();
        bleManager.close();
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

    //    允许重连功能开启之后 重连参数有所改变，再次连接以后有可能导致133错误，需要复位
    public void resetRetryConfig() {
        bleManager.resetRetryConfig();
    }

    //    写入命令 默认向UUID_CHAR_WRITE中写命令
    private void writeCharacteristic(byte[] bytes) {
        bleManager.writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(), bytes);
    }

    //    发送命令
    private void sendBleInstruct(String instructInfo, boolean isKey) {
        String message = "";
        if (isKey) {
            String local_key = BytesUtils.getBleKey();
            Log.i(TAG, "保存key到本地--------" + local_key);
//            SharedPreUtils.setString(applicationContext, BluCommonUtils.SAVE_WRITE_PEN_KEY, local_key);
            if (onKeyGeneratedListener != null) {
                onKeyGeneratedListener.onSuccess(local_key);
            }
            message = instructInfo + local_key;
        } else {
            message = instructInfo;
        }
        byte[] connKey = BytesUtils.HexString2Bytes(message);
        writeCharacteristic(connKey);
    }

    public void sendBleInstruct(int flag) {
        switch (flag) {
//            打开书写通道
            case OPEN_WRITE_CHANNEL:
                sendBleInstruct(BluUUIDUtils.BluInstruct.OPEN_WRITE_CHANNEL.getUuid(), false);
                break;
//            打开存储通道
            case OPEN_STORAGE_CHANNEL:
                sendBleInstruct(BluUUIDUtils.BluInstruct.OPEN_STORAGE_CHANNEL.getUuid(), false);
                break;
//            读取通道信息
            case READ_STORAGE_INFO:
                sendBleInstruct(BluUUIDUtils.BluInstruct.READ_STORAGE_INFO.getUuid(), false);
                break;
//            清空通道信息
            case EMPTY_STORAGE_DATA:
                sendBleInstruct(BluUUIDUtils.BluInstruct.EMPTY_STORAGE_DATA.getUuid(), false);
                break;
//            获取电量信息
            case OBTAIN_ELECTRICITY:
                sendBleInstruct(BluUUIDUtils.BluInstruct.OBTAIN_ELECTRICITY.getUuid(), false);
                break;
        }
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            long currentTimeMillis = System.currentTimeMillis();
            Log.i(TAG, "连接状态变化时调用" + status + "-------" + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "已经连接  接收到已连接回调  并将结果返回去");
                bleManager.setIsConnected(true);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_BONDING) {
                            Log.i(TAG, "连接成功，去发现服务");
                            gatt.discoverServices();
                        }
                    }
                }, 600);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                bleManager.setIsConnected(false);
                if (status == 133 ||  status ==19) {
//                    未知原因 或超时造成连接断开，执行重连
                    bleManager.resetRetryConfig();
                    String address = gatt.getDevice().getAddress();
                    bleManager.connect(address, false, gattCallback);
                    Log.i(TAG, "133 19导致连接断开,重连");
                } else {
                    Log.i(TAG, "连接断开");
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
                Log.i(TAG, "onServicesDiscovered received: 发送命令获取key状态 看是否收到响应");

                sendBleInstruct(BluUUIDUtils.BluInstruct.OBTAIN_KEY_STATE.getUuid(), false);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (!is_Receive_Key_State) {
                            Log.i(TAG, "onServicesDiscovered received:没有收到响应 600后 再次发送命令获取key状态");
                            sendBleInstruct(BluUUIDUtils.BluInstruct.OBTAIN_KEY_STATE.getUuid(), false);
                        }
                    }
                }, 600);

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
            Log.i(TAG, "onCharacteristicWrite 收到书写回调---" + status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            final String bluMessage = bytesToHexString(characteristic.getValue());
            Log.i(TAG, "onCharacteristicChanged---" + bluMessage);
            if (bluMessage != null && !"".equals(bluMessage)) {
                if (bluMessage.startsWith("0f0f")) {
                    if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.NOT_KEY_STATE.getMsg())) {
                        is_Receive_Key_State = true;
                        Log.i(TAG, "收到返回  无key状态 ---" + bluMessage);
                        sendBleInstruct(BluUUIDUtils.BluInstruct.NOT_KEY_WRITE.getUuid(), true);
                        Log.i(TAG, "将key写入 ");
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!is_Receive_No_Key_Write_Success_State) {
                                    is_Receive_No_Key_Write_Success_State = false;
                                    sendBleInstruct(BluUUIDUtils.BluInstruct.NOT_KEY_WRITE.getUuid(), true);
                                    Log.i(TAG, "NOT_KEY_STATE 1s之后再将key写入一次");
                                }
                            }
                        }, 600);
                    } else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.HAVE_KEY_STATE.getMsg())) {
                        is_Receive_Key_State = true;
                        Log.i(TAG, "笔内已经保存了key信息");
//                        final String cacheKeyMessage = SharedPreUtils.getString(applicationContext, BluCommonUtils.SAVE_WRITE_PEN_KEY);
                        if (onKeyGeneratedListener != null) {
                            onKeyGeneratedListener.onGetKeyEmpty();
                        }
                        if (!cacheKeyMessage.isEmpty()) {
                            Log.i(TAG, "笔内已经保存了key信息，不为空，发送 进行比较---" + cacheKeyMessage);
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    sendBleInstruct(BluUUIDUtils.BluInstruct.HAVE_KEY_WRITE.getUuid() + cacheKeyMessage, false);
                                }
                            }, 600);
                        } else {
                            throw new NullPointerException("cacheKeyMessage 为空");
//                            Toast.makeText(applicationContext, "请将蓝牙笔设置为配对状态，再尝试连接", Toast.LENGTH_LONG).show();
                        }
                    } else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.NOT_KEY_WRITE_SUCCEED_STATE.getMsg())) {

                        is_Receive_No_Key_Write_Success_State = true;
//                        String cacheKeyMessage = SharedPreUtils.getString(applicationContext, BluCommonUtils.SAVE_WRITE_PEN_KEY);
                        if (onKeyGeneratedListener != null) {
                            onKeyGeneratedListener.onGetKeyEmpty();
                        }

                        if (cacheKeyMessage != null && !"".equals(cacheKeyMessage)) {
                            sendBleInstruct(BluUUIDUtils.BluInstruct.HAVE_KEY_WRITE.getUuid() + cacheKeyMessage, false);
                            Log.i(TAG, "笔内没有保存key信息，写入成功, 之后再写一次");
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!is_Receive_Have_Key_Write_Success_State) {
                                        sendBleInstruct(BluUUIDUtils.BluInstruct.HAVE_KEY_WRITE.getUuid() + cacheKeyMessage, false);
                                        Log.i(TAG, "无key成功,2s之后再发一次");
                                    }
                                }
                            }, 600);
                        }

                    } else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.NOT_KEY_WRITE_FAILURE_STATE.getMsg())) {
//                        执行断开连接
                        is_Receive_No_Key_Write_Success_State = true;
                        disconnectBleDevice();
                        Log.i(TAG, "笔内没有保存key信息，写入失败");

                    } else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.HAVE_KEY_WRITE_SUCCEED_STATE.getMsg())) {
                        is_Receive_Have_Key_Write_Success_State = true;
                        Log.i(TAG, "笔内保存了key信息，写入成功,最终成功 查询有没有存储信息");

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendBleInstruct(BluUUIDUtils.BluInstruct.QUERY_STORAGE_INFO.getUuid(), false);
                            }
                        }, 500);

                    } else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.HAVE_KEY_WRITE_FAILURE_STATE.getMsg())) {
                        //执行断开连接
                        is_Receive_Have_Key_Write_Success_State = true;
                        disconnectBleDevice();
                        Log.i(TAG, "笔内保存了key信息，写入失败");

                    } else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.NOT_STORAGE_INFO.getMsg())) {
                        //无存储信息，打开书写通道
                        Log.i(TAG, "没有存储信息,打开书写通道");
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendBleInstruct(BluUUIDUtils.BluInstruct.OPEN_WRITE_CHANNEL.getUuid(), false);
                            }
                        }, 500);

                    } else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.HAVE_STORAGE_INFO.getMsg())) {

                        Log.i(TAG, "有存储信息");

                        if (onLeNotificationListener != null) {
                            onLeNotificationListener.onHistroyInfoDetected();
                        }

                    } else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.STORAGE_DATA_READ_END.getMsg())) {
                        Log.i(TAG, "存储信息读取完毕");
                        if (onLeNotificationListener != null) {
                            onLeNotificationListener.onHistroyInfoReadCompleted(bluMessage);
                        }
                    } else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.STORAGE_DATA_EMPTY_END.getMsg())) {
                        Log.i(TAG, "存储信息清空完毕");
                        if (onLeNotificationListener != null) {
                            onLeNotificationListener.onHistroyInfoDeleted();
                        }
                    } else if (bluMessage.startsWith(BluUUIDUtils.BluInstructReplyMsg.ELECTRICITY_INFO.getMsg())) {
                        Log.i(TAG, "电量信息");
                        String electrice = bluMessage.substring(bluMessage.length() - 2, bluMessage.length());
                        final int parseInt = SystemTransformUtils.getInstance().hexToInt(electrice);
                        if (onLeNotificationListener != null) {
                            onLeNotificationListener.onElectricityDetected(parseInt);
                        }
                    }
                } else {
//                    不是0f0f开头的 是笔迹信息
                    if (onLeNotificationListener != null) {
                        onLeNotificationListener.onWrite(bluMessage);
                    }
                }
            }
//            String msg = BluUUIDUtils.BluInstruct.OPEN_WRITE_CHANNEL.getUuid();
//            final byte[] connKey = BytesUtils.HexString2Bytes(msg);
//            bleManager.writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(), connKey);
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    bleManager.writeCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid(), connKey);
//                }
//            }, 3000);
//            Log.i(TAG, "通道已打开");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, final int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.i(TAG, "是否被触发？"+status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                if (rssi < -110) {
                    onReadRssiListener.onSuccess(rssi, Utils.getDistance(rssi));
//                }
//                    }
//                });
            }
        }
    };

    public void enableAntiLost(int millisecond, OnReadRssiListener onReadRssiListener) {
        Log.i(TAG, "设置了时间 和 监听");
        this.onReadRssiListener = onReadRssiListener;
        bleManager.readRssi(millisecond);
    }

    public void disableAntiLost() {
        bleManager.cancelReadRssiTimerTask();
    }

    public String bytesToHexString(byte[] data) {
        if (data == null)
            return null;
        final StringBuilder stringBuilder = new StringBuilder(data.length);
        for (byte byteChar : data)
            stringBuilder.append(String.format("%02x", byteChar));
        return stringBuilder.toString();
    }

    public void setOnLeNotificationListener(OnLeNotificationListener onLeNotificationListener) {
        this.onLeNotificationListener = onLeNotificationListener;
    }

    public void setOnKeyListener(OnKeyListener onKeyGeneratedListener) {
        Log.i(TAG, "key生成的监听");
        this.onKeyGeneratedListener = onKeyGeneratedListener;
    }

    public void setKey(String key) {
        Log.i(TAG, "将sp中的key设置进来");
        this.cacheKeyMessage = key;
    }

}
