package com.example.finalbledemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.finalbledemo.ble.BleManager;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ListView listview;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private static final String TAG = MainActivity.class.getName();
    private Handler mhandler = new Handler();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                return;
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_COARSE_LOCATION);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG,"权限");
            //获取文件读写权限
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        listview = (ListView) findViewById(R.id.listview);
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        listview.setAdapter(mLeDeviceListAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

//                BleManager.getDefault().stopScanLeDevice(mLeScanCallback);

                BluetoothDevice device = mLeDeviceListAdapter.getDevice(i);
//                BluetoothDevice device = (BluetoothDevice) mLeDeviceListAdapter.getItem(i);  //这两行的效果是一样的
                String address = device.getAddress();
                String name = device.getName();
//                Intent intent = new Intent(MainActivity.this,ControllerActivity.class);
//                intent.putExtra("address",address);
//                intent.putExtra("name",name);
//                startActivity(intent);
            }
        });
    }

    public void startScan(View view) {
        mLeDeviceListAdapter.clear();
//        BleManager.getDefault()
//                .setScanPeriod(20000)
//                .setScanWithServiceUUID(null)
//                .scanLeDevice(null,2000,mLeScanCallback);
//        scanLeDevice(true);
        Log.i(TAG,"开始扫描");
//        BleManager.getDefault().scanLeDevice(uuids, mLeScanCallback);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG,"扫描中");
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }


    };

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            Log.i(TAG,"构造函数");
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = MainActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            Log.i(TAG,"新增设备");
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            Log.i(TAG,"获取position位置的item");
            return mLeDevices.get(position);
        }

        public void clear() {
            Log.i(TAG,"清空集合");
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            Log.i(TAG,"获取集合长度");
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            Log.i(TAG,"获取i位置的item");
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            Log.i(TAG,"获取i");
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Log.i(TAG,"获取每一个view");
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText("未知设备");
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}
