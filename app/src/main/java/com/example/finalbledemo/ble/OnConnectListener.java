package com.example.finalbledemo.ble;

/**
 * Created by Administrator on 2017/4/25 0025.
 */

public abstract class OnConnectListener extends LeListener {

    public abstract void onSuccess();

    public abstract void onFailed();
}
