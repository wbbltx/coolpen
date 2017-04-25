package com.example.finalbledemo.ble;

/**
 * @anthor wubinbin
 * @time 2017/4/24 17:20
 */

public abstract class OnLeNotificationListener extends LeListener {

    /**
     * 笔迹信息
     * @param info
     */
    public abstract void onWrite(String info);

    /**
     * 历史信息
     * @param info
     */
    public abstract void onHistroyInfoReadCompleted(String info);

    /**
     * 检测到有历史信息
     */
    public abstract void onHistroyInfoDetected();

    /**
     * 历史信息删除完成
     */
    public abstract void onHistroyInfoDeleted();

    /**
     * 电量信息
     * @param electricity
     */
    public abstract void onElectricityDetected(int electricity);

}
