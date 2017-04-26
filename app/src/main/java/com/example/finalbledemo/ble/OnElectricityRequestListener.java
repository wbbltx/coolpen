package com.example.finalbledemo.ble;

/**
 * @anthor wubinbin
 * @time 2017/4/26 14:26
 */

public abstract class OnElectricityRequestListener extends LeListener {

    /**
     * 电量信息
     * @param electricity
     */
    public abstract void onElectricityDetected(String electricity);
}
