package com.reb.dsd_ble.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.reb.dsd_ble.ble.scanner.ExtendedBluetoothDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/7 0007.
 */

public class DeviceAdapter extends BaseAdapter {

    private List<ExtendedBluetoothDevice> devices = new ArrayList<>();

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }

    public void clearDevices() {
        devices.clear();
        notifyDataSetChanged();
    }
}
