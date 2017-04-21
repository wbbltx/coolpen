package com.example.finalbledemo.ble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.finalbledemo.BluUUIDUtils;
import com.example.finalbledemo.ControllerActivity;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

/**
 * Created by Administrator on 2017/4/17.
 */

public class BleManager {

    private static final String TAG = ControllerActivity.class.getName();
    private BluetoothGatt gatt;
    private BluetoothGattService service;
    private boolean mRetryConnectEnable = false;
    private int mRetryConnectCount = 1;
    private int connectTimeoutMillis = 5000;
    private int serviceTimeoutMillis;
    //    private int scanPeriod = 10000;
    private boolean isScanning;
    //    private UUID[] serviceUUID = {BluUUIDUtils.BtSmartUuid.UUID_SERVICE.getUuid()};
    private BluetoothAdapter defaultAdapter;
    private Context context;
    private boolean isConnected = false;
    private boolean isServiceDiscovered;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private UUID characteristicUUID = BluUUIDUtils.BtSmartUuid.UUID_CHAR_WRITE.getUuid();

    //    不需要了
//    private static class SingletonHolder {
//        private static final BleManager INSTANCE = new BleManager();
//    }

    //    不需要了
//    public BleManager() {
//    }

    BleManager(Context context,Handler handler) {
        this.context = context;
        this.mHandler = handler;
    }

    //    不需要了
//    public static BleManager getDefault() {
//        return SingletonHolder.INSTANCE;
//    }

    //    不需要了
//    public void init(Activity context) {
//        this.context = context;
//        defaultAdapter = BluetoothAdapter.getDefaultAdapter();
//    }

    //判断是否支持ble
    public boolean isBleSupported() {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, "不支持BLE", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //判断蓝牙是否开启
    public boolean isBluetoothOpen() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }

    //intent开启蓝牙
    private boolean enableIntentBluetooth(Activity activity) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "不支持蓝牙", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "false. your device does not support bluetooth. ");
            return false;
        }

        isBleSupported();

        if (bluetoothAdapter.isEnabled()) {
            Toast.makeText(context, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
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

        if (bluetoothAdapter == null) {
            Toast.makeText(context, "不支持蓝牙", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "false. your device does not support bluetooth. ");
            return false;
        }

        isBleSupported();

        if (bluetoothAdapter.isEnabled()) {
            Toast.makeText(context, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "your device has been turn on bluetooth.");
            return false;
        }
        boolean enable = bluetoothAdapter.enable();
        if (enable)
            Toast.makeText(context, "蓝牙开启", Toast.LENGTH_SHORT).show();
        Toast.makeText(context, "蓝牙未开启", Toast.LENGTH_SHORT).show();

        return enable;
    }

    //    关闭蓝牙
    boolean disableBluetooth() {
        synchronized (BleManager.class) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
                Toast.makeText(context, "蓝牙关闭", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                Toast.makeText(context, "蓝牙已经关闭", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "false. your device has been turn off Bluetooth.");
                return false;
            }
        }
    }

    //        BluetoothLeScanner
    //扫描
    public void scanLeDevice(UUID[] serviceUUID, int scanPeriod, final BluetoothAdapter.LeScanCallback mLeScanCallback) {
        stopScanLeDevice(mLeScanCallback);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
    public void stopScanLeDevice(BluetoothAdapter.LeScanCallback mLeScanCallback) {
        if (isScanning == true) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.stopLeScan(mLeScanCallback);
            isScanning = false;
        }
    }

//       设置扫描时长
//    public BleManager setScanPeriod(int millisecond) {
//        this.scanPeriod = millisecond;
//        return this;
//    }

//     根据服务UUID进行过滤扫描
//    public BleManager setScanWithServiceUUID(UUID[] serviceUUID) {
//        this.serviceUUID = serviceUUID;
//        return this;
//    }

    //连接
    @Deprecated
    private void connect1(Context context, String address, boolean autoConnect, BluetoothGattCallback bluetoothGattCallback) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.i(TAG, "通过---" + address + "---获取设备");
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
        Log.i(TAG, "开始连接+++" + remoteDevice.getAddress());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "获取gatt实例 4个参数");
            gatt = remoteDevice.connectGatt(context, autoConnect, bluetoothGattCallback, TRANSPORT_LE);
        } else {
            Log.i(TAG, "获取gatt实例 3个参数");
            gatt = remoteDevice.connectGatt(context, autoConnect, bluetoothGattCallback);
        }
//        gatt = remoteDevice.connectGatt(context, autoConnect, bluetoothGattCallback);
        checkConnected(address, autoConnect, bluetoothGattCallback);
        Log.i(TAG, "正在连接");
    }

    void setIsConnected(boolean isConnected) {
        Log.i(TAG, "接收返回的连接消息-----" + isConnected);
        this.isConnected = isConnected;
    }

    void setIsServiceDiscovered(boolean isServiceDiscovered) {
        this.isServiceDiscovered = isServiceDiscovered;
    }

    /**
     * @param autoConnect           是否自动连接
     * @param bluetoothGattCallback 回调 如果连接成功，一定要在回调的方法中setIsConnected（true）同理 如果连接断开，则要在回调中设置setIsConnected（false）
     */
    public void connect(Object remote, boolean autoConnect, BluetoothGattCallback bluetoothGattCallback) {
        //     防止重复调用连接方法
        if (isConnected) {
            Log.i(TAG, "已经连接 直接返回");
            return;
        }

        Log.i(TAG, "连接之前判断 如果gatt不为空，则先close");
        close();
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice remoteDevice = getBluetoothDevice(remote);

        Log.i(TAG, "开始连接+++");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            gatt = remoteDevice.connectGatt(context, autoConnect, bluetoothGattCallback, TRANSPORT_LE);
        } else {
            gatt = remoteDevice.connectGatt(context, autoConnect, bluetoothGattCallback);
        }
//        gatt = remoteDevice.connectGatt(context, autoConnect, bluetoothGattCallback);
        checkConnected(remote, autoConnect, bluetoothGattCallback);
//        Log.i(TAG, "正在连接");
    }

    private BluetoothDevice getBluetoothDevice(Object remote) {
        BluetoothDevice remoteDevice;
        if (remote instanceof String) {
            remoteDevice = defaultAdapter.getRemoteDevice((String) remote);
            Log.i(TAG, "参数是mac地址" + remoteDevice.getAddress());
        } else if (remote instanceof BluetoothDevice) {
            Log.i(TAG, "参数是蓝牙设备");
            remoteDevice = (BluetoothDevice) remote;
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
    private void checkConnected(final Object address, final boolean autoConnect, final BluetoothGattCallback bluetoothGattCallback) {
        Log.i(TAG, mRetryConnectCount + "---" + mRetryConnectEnable + "---" + connectTimeoutMillis);
        if (mRetryConnectEnable && mRetryConnectCount > 0 && connectTimeoutMillis > 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "决定是否进入重连---" + isConnected);
                    if (isConnected == false) {
                        Log.i(TAG, mRetryConnectCount + "---进入重连---" + isConnected + "---" + connectTimeoutMillis + "以后在执行");
                        connect(address, autoConnect, bluetoothGattCallback);
                        mRetryConnectCount = mRetryConnectCount - 1;
                    }
                }
            }, connectTimeoutMillis);
        }
    }

    //使能CharacteristicNotification
    void enableCharacteristicNotification() {
        service = gatt.getService(BluUUIDUtils.BtSmartUuid.UUID_SERVICE.getUuid());
        if (service == null) {
            Log.i(TAG, "使能通知---service为空");
            throw new NullPointerException("servicr 不能为空");
        }
        Log.i(TAG, "通知被使能---service不为空");
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
            gatt.writeDescriptor(descriptor);
        }
    }

    //写入信息
    void writeCharacteristic(UUID characteristicUUID, byte[] bytes) {
        if (service != null && gatt != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
            characteristic.setValue(bytes);
//            Log.i(TAG, "正在写characteristic" + characteristic.getUuid());
            gatt.writeCharacteristic(characteristic);
        }
    }

    public void writeCharacteristic(byte[] bytes) {
        if (service != null && gatt != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
            characteristic.setValue(bytes);
            Log.i(TAG, "正在写characteristic" + characteristic.getUuid());
            gatt.writeCharacteristic(characteristic);
        }
    }

    public void setCharacteristicUUID(UUID characteristicUUID) {
        this.characteristicUUID = characteristicUUID;
    }

    private static final UUID SERVICE = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS_UUID = UUID.fromString("00002A04-0000-1000-8000-00805f9b34fb");

    //读取characterristics   这个方法暂时没有用
    public void readCharacteristic() {
        if (gatt == null) {
            return;
        }
        Log.i(TAG, "首先获取另外的服务");
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

        Log.i(TAG, "读取characteristics是否为真" + gatt.readCharacteristic(preCharacteristic));
        return gatt.readCharacteristic(preCharacteristic);
    }

    //    设置是否允许重连
    void setRetryConnectEnable(boolean retryConnectEnable) {
        Log.i(TAG, "设置是否允许重连" + retryConnectEnable);
        mRetryConnectEnable = retryConnectEnable;
    }

    //    设置连接次数
    void setRetryConnectCount(int count) {
        mRetryConnectCount = count;
        Log.i(TAG, "设置连接次数" + count);
    }

    //    设置连接超时
    void setConnectTimeOut(int millisecond) {
        this.connectTimeoutMillis = millisecond;
        Log.i(TAG, "设置连接超时" + millisecond);
    }

    //    发现服务超时
    void setServiceDiscoverTimeOut(int millisecond) {
        this.serviceTimeoutMillis = millisecond;
    }

    /**
     * 检查服务是否发现 调用该方法并且生效需要设置setRetryConnectEnable为true（默认为false），
     * setRetryConnectCount大于0（默认为1），
     * setConnectTimeOut大于0（默认10000）
     * 同时必须要在onServicesDiscovered回调中根据返回的状态设置setIsServiceDiscovered
     */
    public void checkServiceDiscover(final Context context, final Object address, final boolean autoConnect, final BluetoothGattCallback bluetoothGattCallback) {
        if (mRetryConnectEnable && mRetryConnectCount > 0 && serviceTimeoutMillis > 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isServiceDiscovered) {
                        connect(address, autoConnect, bluetoothGattCallback);
                        mRetryConnectCount -= 1;
                    }
                }
            }, serviceTimeoutMillis);
        }
    }

    /**
     *
     */
    public boolean discoverService(final Object address, final boolean autoConnect, final BluetoothGattCallback bluetoothGattCallback) {
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

    public void close() {
        if (gatt != null) {
            Log.i(TAG, "gatt不为空，执行关闭置空gatt");
            isConnected = false;
            isServiceDiscovered = false;
            gatt.close();
            gatt = null;
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public void disconnect() {
        if (gatt != null) {
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

}
