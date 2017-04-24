package com.example.finalbledemo.ble;

import android.bluetooth.BluetoothDevice;

import java.util.List;




public abstract class OnBleScanListener extends LeListener {
    public abstract void onScanResult(BluetoothDevice bluetoothDevice, int rssi,byte[] scanRecord);

    public abstract void onScanCompleted();


}
