package com.example.finalbledemo.ble;

/**
 * @anthor wubinbin
 * @time 2017/4/24 17:20
 */

public abstract class OnLeNotificationListener extends LeListener {

    public abstract void onWrite(String info);

    public abstract void onHistroyInfoReadCompleted(String info);

    public abstract void onHistroyInfoDetected();

    public abstract void onHistroyInfoDeleted();

    public abstract void onElectricityDetected(int electricity);


}
