package com.example.finalbledemo.permission;

import java.util.List;

/**
 * @anthor wubinbin
 * @time 2017/4/21 9:45
 */

public interface PermissionListener {

    void onGranted();

    void onDenied(List<String> permissions);
}
