package com.example.finalbledemo.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

/**
 * Created by Administrator on 2017/4/17.
 */

public class BleManager {

    private Context context;

    private static final String TAG = BluetoothLe.class.getName();
    private static final int DEFAULT_COUNT = 1;
    private static final int DEFAULT_TIMEOUT = 800;

    private boolean mRetryConnectEnable = false;
    private boolean isServiceDiscovered;
    private boolean isConnected = false;
    private boolean isScanning;

    private int connectTimeoutMillis = DEFAULT_TIMEOUT;
    private int mRetryConnectCount = DEFAULT_COUNT;
    private int serviceTimeoutMillis;

    private BluetoothGatt gatt;
    private BluetoothGattService service;
    private Handler mHandler;
    private Timer mTimer;
    private TimerTask mTimerTask;

    BleManager(Context context, Handler handler) {
        this.context = context;
        this.mHandler = handler;
    }

    //判断是否支持ble
    boolean isBleSupported() {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e(TAG, "BLE is not supported. ");
            return false;
        }
        return true;
    }

    //判断蓝牙是否开启
    public boolean isBluetoothOpen() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "bluetooth is already on. ");
            return true;
        }
        return false;
    }

    //是否支持蓝牙
    boolean isBluetoothSupported() {
        if (null == BluetoothAdapter.getDefaultAdapter()) {
//            Toast.makeText(context, "不支持蓝牙", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "bluetooth is not support . ");
            return false;
        }
        return true;
    }

    //intent开启蓝牙
    boolean enableIntentBluetooth(Activity activity) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
//            Toast.makeText(context, "不支持蓝牙", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "false. your device does not support bluetooth. ");
            return false;
        }

        isBleSupported();

        if (bluetoothAdapter.isEnabled()) {
//            Toast.makeText(context, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "false. your device has been turn on bluetooth.");
            return false;
        }
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivity(intent);
        return true;
    }

    // 直接开启蓝牙，不经过提示
    boolean enableBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean enable = bluetoothAdapter.enable();
        if (enable) {
            Log.i(TAG, "bluetooth is on. ");
            return enable;
        }
        Log.i(TAG, "bluetooth is off. ");
        return false;
    }

    //    关闭蓝牙
    boolean disableBluetooth() {
        synchronized (BleManager.class) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                boolean disable = bluetoothAdapter.disable();
                Log.i(TAG, "bluetooth is off. ");
                return disable;
            } else {
                Log.i(TAG, "bluetooth has already been turned off. ");
                return true;
            }
        }
    }

    //        BluetoothLeScanner
    //扫描
    void scanLeDevice(UUID[] serviceUUID, int scanPeriod, final BluetoothAdapter.LeScanCallback mLeScanCallback) {
        stopScanLeDevice(mLeScanCallback);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        if (null == serviceUUID || 0 == serviceUUID.length) {
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            bluetoothAdapter.startLeScan(serviceUUID, mLeScanCallback);
        }
        if (scanPeriod <= 0) {
            scanPeriod = 10000;
        }
        isScanning = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isScanning) {
                    stopScanLeDevice(mLeScanCallback);
                    Log.i(TAG, "设定时间到，扫描停止");
                }
            }
        }, scanPeriod);
    }


    //停止扫描  正在扫描时才可以停止扫描
    void stopScanLeDevice(BluetoothAdapter.LeScanCallback mLeScanCallback) {
        if (isScanning == true) {
            Log.i(TAG, "stop scanning");
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.stopLeScan(mLeScanCallback);
            isScanning = false;
        }
    }

    void setIsConnected(boolean isConnected) {
//        Log.i(TAG, "接收返回的连接消息-----" + isConnected);
        this.isConnected = isConnected;
    }

    void setIsServiceDiscovered(boolean isServiceDiscovered) {
        this.isServiceDiscovered = isServiceDiscovered;
    }

    /**
     * @param autoConnect           是否自动连接
     * @param bluetoothGattCallback 回调 如果连接成功，一定要在回调的方法中setIsConnected（true）同理 如果连接断开，则要在回调中设置setIsConnected（false）
     */
    void connect(Object remote, boolean autoConnect, BluetoothGattCallback bluetoothGattCallback, OnConnectListener onConnectListener) {
        //     防止重复调用连接方法
        if (isConnected) {
            Log.i(TAG, "已经连接");
            return;
        }

//        Log.i(TAG, "连接之前判断 如果gatt不为空，则先close");
        close();
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice remoteDevice = getBluetoothDevice(remote);

        Log.i(TAG, "start connect" + System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            gatt = remoteDevice.connectGatt(context, autoConnect, bluetoothGattCallback, TRANSPORT_LE);
        } else {
            gatt = remoteDevice.connectGatt(context, autoConnect, bluetoothGattCallback);
        }
        onConnectListener.isConnecting();
        Log.i(TAG, "connecting...");
        checkConnected(remote, autoConnect, bluetoothGattCallback, onConnectListener);
    }

    private BluetoothDevice getBluetoothDevice(Object remote) {
        BluetoothDevice remoteDevice;
        if (remote instanceof String) {
//            Log.i(TAG, "参数是mac地址");
            remoteDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice((String) remote);
        } else if (remote instanceof BluetoothDevice) {
            remoteDevice = (BluetoothDevice) remote;
//            Log.i(TAG, "参数是蓝牙设备");
        } else {
            throw new IllegalArgumentException("参数必须为MAC地址或者蓝牙设备");
        }
        return remoteDevice;
    }

    /**
     * 检查重连 调用该方法需要同时设置setRetryConnectEnable为true， setRetryConnectCount大于0， setConnectTimeOut大于0
     * 同时必须要在onConnectionStateChange回调中根据返回的状态设置setIsConnected
     *
     * @param bluetoothGattCallback
     */
    private void checkConnected(final Object address, final boolean autoConnect, final BluetoothGattCallback bluetoothGattCallback, final OnConnectListener onConnectListener) {
        Log.i(TAG, mRetryConnectEnable + "------" + mRetryConnectCount + "------" + connectTimeoutMillis);
        if (mRetryConnectEnable && mRetryConnectCount > 0 && connectTimeoutMillis > 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    Log.i(TAG, "决定是否进入重连---" + isConnected);
                    if (isConnected == false) {
//                        Log.i(TAG, mRetryConnectCount + "---进入重连---" + isConnected + "---" + connectTimeoutMillis + "以后在执行");
                        connect(address, autoConnect, bluetoothGattCallback, onConnectListener);
                        mRetryConnectCount = mRetryConnectCount - 1;
                    }
                }
            }, connectTimeoutMillis);
        }
    }

    //使能CharacteristicNotification
    boolean enableCharacteristicNotification() {
        service = gatt.getService(BluUUIDUtils.BtSmartUuid.UUID_SERVICE.getUuid());
        if (service == null) {
            Log.i(TAG, "service is null");
            throw new NullPointerException("servicr cannot be null");
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(BluUUIDUtils.BtSmartUuid.UUID_CHAR_READ.getUuid());
        gatt.setCharacteristicNotification(characteristic, true);
//        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
//        getDescriptor的参数没有特定要求，也可以直接得到descriptor数组 关键是setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
//        将ble设备的通知功能开启。
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS_UUID);
        if (descriptor != null) {
            Log.i(TAG, "writeDescriptor(notification), " + CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean b = gatt.writeDescriptor(descriptor);
            Log.i(TAG, "notification enabled");
            return b;
        }
        return false;
    }

    //向特定特征 写入信息
    void writeCharacteristic(UUID characteristicUUID, byte[] bytes) {
        if (service != null && gatt != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
            characteristic.setValue(bytes);
            Log.i(TAG, "write characteristic to " + characteristic.getUuid());
            gatt.writeCharacteristic(characteristic);
        }
    }

    private static final UUID SERVICE = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS_UUID = UUID.fromString("00002A04-0000-1000-8000-00805f9b34fb");

    //读取characterristics   这个方法暂时没有用
    void readCharacteristic() {
        if (gatt == null) {
            return;
        }
//        Log.i(TAG, "首先获取另外的服务");
        readCharacteristicQueue(SERVICE, PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS_UUID);
    }

    private boolean readCharacteristicQueue(UUID service, UUID parametersUuid) {
        BluetoothGattService preService = gatt.getService(service);
        BluetoothGattCharacteristic preCharacteristic = preService.getCharacteristic(parametersUuid);

        if (preCharacteristic == null)
            return false;
        // Check characteristic property
        final int properties = preCharacteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) == 0)
            return false;

//        Log.i(TAG, "读取characteristics是否为真" + gatt.readCharacteristic(preCharacteristic));
        return gatt.readCharacteristic(preCharacteristic);
    }

    //    设置是否允许重连
    void setRetryConnectEnable(boolean retryConnectEnable) {
//        Log.i(TAG, "设置是否允许重连" + retryConnectEnable);
        mRetryConnectEnable = retryConnectEnable;
    }

    //    设置连接次数
    void setRetryConnectCount(int count) {
        mRetryConnectCount = count;
//        Log.i(TAG, "设置连接次数" + count);
    }

    //    设置连接超时
    void setConnectTimeOut(int millisecond) {
        this.connectTimeoutMillis = millisecond;
//        Log.i(TAG, "设置连接超时" + millisecond);
    }

    //    复位重连设置
    void resetRetryConfig() {
        mRetryConnectEnable = false;
        mRetryConnectCount = DEFAULT_COUNT;
        this.connectTimeoutMillis = DEFAULT_TIMEOUT;
    }

    //    发现服务超时
    void setServiceDiscoverTimeOut(int millisecond) {
        this.serviceTimeoutMillis = millisecond;
    }

    void checkServiceDiscover(final Object address, final boolean autoConnect, final BluetoothGattCallback bluetoothGattCallback, final OnConnectListener onConnectListener) {
        if (mRetryConnectEnable && mRetryConnectCount > 0 && serviceTimeoutMillis > 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isServiceDiscovered) {
                        connect(address, autoConnect, bluetoothGattCallback, onConnectListener);
                        mRetryConnectCount -= 1;
                    }
                }
            }, serviceTimeoutMillis);
        }
    }

    /**
     *
     */
    boolean discoverService(final Object address, final boolean autoConnect, final BluetoothGattCallback bluetoothGattCallback) {
        if (gatt == null) {
            return false;
        }
        if (isServiceDiscovered == false) {
            boolean b = gatt.discoverServices();
//            checkServiceDiscover(context, address, autoConnect, bluetoothGattCallback);
            return b;
        }
        return false;
    }

    void close() {
        if (gatt != null) {
            cancelReadRssiTimerTask();
//            Log.i(TAG, "gatt不为空，执行关闭置空gatt");
            isConnected = false;
            isServiceDiscovered = false;
            gatt.close();
            gatt = null;
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    void disconnect() {
        if (isConnected && gatt != null) {
            cancelReadRssiTimerTask();
            isConnected = false;
            isServiceDiscovered = false;
            gatt.disconnect();
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    //    清除缓存
    boolean clearDeviceCache() {
        synchronized (BleManager.class) {
            if (gatt == null) {
                Log.e(TAG, "please connected bluetooth then clear cache.");
                return false;
            }
            try {
                Method e = BluetoothGatt.class.getMethod("refresh", new Class[0]);
                if (e != null) {
                    boolean success = ((Boolean) e.invoke(gatt, new Object[0])).booleanValue();
                    Log.i(TAG, "refresh Device Cache: " + success);
                    return success;
                }
            } catch (Exception exception) {
                Log.e(TAG, "An exception occured while refreshing device", exception);
            }
            return false;
        }
    }

    private void readRssiTimerTask(int readRssiIntervalMillisecond) {
        mTimer = null;
        mTimerTask = null;
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (gatt != null) {
                    Log.i(TAG, "读取信号");
                    gatt.readRemoteRssi();
                }
            }
        };
        mTimer.schedule(mTimerTask, 100, readRssiIntervalMillisecond);
    }

    void cancelReadRssiTimerTask() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    void readRssi(int millisecond) {
        if (isConnected) {
            Log.i(TAG, "连接 执行读取信号");
            readRssiTimerTask(millisecond);
        } else {
            Log.i(TAG, "please make sure the bluetooth device is connected");
        }
    }

    boolean getScanning() {
        return isScanning;
    }

    boolean getConnected() {
        return isConnected;
    }

}
