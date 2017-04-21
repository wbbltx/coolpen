package com.example.finalbledemo;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @anthor wubinbin
 * @time 2017/4/21 11:43
 */

public class BleListAdapter extends BaseAdapter {
    private List<BluetoothDevice> devices;
    private LayoutInflater inflater;

    public BleListAdapter(Activity context) {
        this.devices = new ArrayList<>();
        this.inflater = context.getLayoutInflater();
    }

    public void addDevice(BluetoothDevice device){
        if (!devices.contains(device)){
            devices.add(device);
        }
    }

    public void clear(){
        devices.clear();
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            convertView = inflater.inflate(R.layout.listitem_device,null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) convertView.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice bluetoothDevice = devices.get(position);
        String name = bluetoothDevice.getName();
        if (name !=null){
            viewHolder.deviceName.setText(name);
        }else
            viewHolder.deviceName.setText("未知设备");

        viewHolder.deviceAddress.setText(bluetoothDevice.getAddress());
        return convertView;
    }

    static class ViewHolder{
        TextView deviceName,deviceAddress;
    }
}
