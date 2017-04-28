package com.example.finalbledemo;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.finalbledemo.ble.BleManager;
import com.example.finalbledemo.ble.BluetoothLe;
import com.example.finalbledemo.ble.OnBleScanListener;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    private ListView listView;
    private BleListAdapter bleListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        BluetoothLe.getDefault().init(this);

        listView = (ListView) findViewById(R.id.listView);
        bleListAdapter = new BleListAdapter(this);
        listView.setAdapter(bleListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                BluetoothLe.getDefault().stopScan();

                BluetoothDevice device = (BluetoothDevice) bleListAdapter.getItem(i);
//                BluetoothDevice device = (BluetoothDevice) mLeDeviceListAdapter.getItem(i);  //这两行的效果是一样的
                String address = device.getAddress();
                String name = device.getName();
                Intent intent = new Intent(TestActivity.this,Test2Activity.class);
                intent.putExtra("address",address);
                intent.putExtra("name",name);
                startActivity(intent);
            }
        });

    }

    public void enableBle(View view) {
        boolean b = BluetoothLe.getDefault().enableBluetooth();
        Log.i("BluetoothLe", "" + b);
    }

    public void closeBle(View view) {
        BluetoothLe.getDefault().disableBle();
    }

    public void scan(View view) {
        Log.i("controll", "点击扫描");
        bleListAdapter.clear();
        BluetoothLe.getDefault()
//                .setScanPeriod(SEET_MILLI_SECOND_TIME)//设置扫描时长，单位毫秒，默认10秒
//                .setScanByServiceUUID(null)
                .startScan(new OnBleScanListener() {
                    @Override
                    public void onScanResult(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
                        Log.i("controll", bluetoothDevice.getAddress());
                        bleListAdapter.addDevice(bluetoothDevice);
                        bleListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onScanCompleted() {

                    }
                });

    }

    public void stopscan(View view) {
        BluetoothLe.getDefault().stopScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("controll", "处理权限0" + ActivityCompat.shouldShowRequestPermissionRationale(TestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION));
        if (grantResults.length > 0) {
            Log.i("controll", "未授权权限的长度---" + grantResults.length);
            Log.i("controll", "处理权限1" + ActivityCompat.shouldShowRequestPermissionRationale(TestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION));
            List<String> deniedPermissions = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                int grantResult = grantResults[i];
                String permission = permissions[i];
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permission);
                    Log.i("controll", "未授权的权限长度加入集合中");
                }
            }
            if (deniedPermissions.size() != 0) {
                Log.i("controll", "有权限未授权---" + deniedPermissions.size());
                ActivityCompat.requestPermissions(this, deniedPermissions.toArray(new String[deniedPermissions.size()]), 1);

                if (!ActivityCompat.shouldShowRequestPermissionRationale(TestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Log.i("controll", "处理权限2" + ActivityCompat.shouldShowRequestPermissionRationale(TestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION));
                    finish();
                }
                if (ActivityCompat.shouldShowRequestPermissionRationale(TestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Log.i("controll", "处理权限3" + ActivityCompat.shouldShowRequestPermissionRationale(TestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION));
                    finish();
                }
            } else {
                Log.i("controll", "处理权限4" + ActivityCompat.shouldShowRequestPermissionRationale(TestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION));
                Log.i("controll", "权限全部授权");
            }
        }
    }

    public void connect(View view) {
        Intent intent = new Intent(TestActivity.this,Test2Activity.class);
        intent.putExtra("address","07:01:03:D1:00:C4");
        intent.putExtra("name","07:01:03:D1:00:C4");
        startActivity(intent);
    }
}
