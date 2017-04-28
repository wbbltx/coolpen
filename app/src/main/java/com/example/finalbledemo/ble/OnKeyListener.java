package com.example.finalbledemo.ble;

/**
 * @anthor wubinbin
 * @time 2017/4/25 16:14
 */

public abstract class OnKeyListener extends LeListener {

    /**
     * 保存生成的key
     * @param key
     */
    public abstract void onKeyGenerated(String key);

    public abstract void onSetLocalKey();
}
