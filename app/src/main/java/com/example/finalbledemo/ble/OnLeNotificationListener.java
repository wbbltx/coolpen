package com.example.finalbledemo.ble;

/**
 * @anthor wubinbin
 * @time 2017/4/24 17:20
 */

public abstract class OnLeNotificationListener extends LeListener {

    /**
     * 历史笔迹信息
     */
    public abstract void onReadHistroyInfo();

    /**
     * 检测到有历史信息
     */
    public abstract void onHistroyInfoDetected();

    /**
     * 历史信息删除完成
     */
    public abstract void onHistroyInfoDeleted();



}
