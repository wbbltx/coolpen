package com.example.finalbledemo.ble;

/**
 * @anthor wubinbin
 * @time 2017/4/25 11:19
 */

public abstract class OnReadRssiListener extends LeListener {
    public abstract void onSuccess(int rssi, int cm);
}
