package com.reb.dsd_ble.ui.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ble.scanner.ExtendedBluetoothDevice;
import com.reb.dsd_ble.ui.act.ConnectActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2018/1/7 0007.
 */

public class DeviceAdapter extends BaseAdapter {

    private List<ExtendedBluetoothDevice> devices = new ArrayList<>();
    private final ExtendedBluetoothDevice.AddressComparator comparator = new ExtendedBluetoothDevice.AddressComparator();
    private Context mContext;

    public DeviceAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int i) {
        return devices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_device, null);
            vh = new ViewHolder();
            vh.mNameView = convertView.findViewById(R.id.item_device_name);
            vh.mAddressView = convertView.findViewById(R.id.item_device_address);
            vh.mTypeView = convertView.findViewById(R.id.item_device_type);
            vh.mConnectBtn = convertView.findViewById(R.id.item_device_connect);
            vh.mRssiView = convertView.findViewById(R.id.item_device_rssi);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        final ExtendedBluetoothDevice device = devices.get(i);
        vh.mAddressView.setText(device.device.getAddress());
        vh.mTypeView.setText(device.type);
        vh.mRssiView.setText(mContext.getString(R.string.rssi, device.rssi));
        if (TextUtils.isEmpty(device.name)) {
            vh.mNameView.setText(R.string.unknown);
        } else {
            vh.mNameView.setText(device.name);
        }
        vh.mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ConnectActivity.class);
                intent.putExtra("address", device.device.getAddress());
                intent.getIntExtra("rssi", device.rssi);
                mContext.startActivity(intent);
            }
        });
        return convertView;
    }

    private class ViewHolder{
        TextView mAddressView;
        TextView mNameView;
        TextView mTypeView;
        TextView mConnectBtn;
        TextView mRssiView;
    }

    public void clearDevices() {
        devices.clear();
        notifyDataSetChanged();
    }

//    public void updateDeviceRssi(BluetoothDevice device, int rssi) {
//		comparator.address = device.getAddress();
//		final int indexInBonded = devices.indexOf(comparator);
//		if (indexInBonded >= 0) {
//			ExtendedBluetoothDevice previousDevice = devices.get(indexInBonded);
//			previousDevice.rssi = rssi;
//			notifyDataSetChanged();
//		}
//    }

    public void addOrUpdateDevice(ExtendedBluetoothDevice device) {
		final int indexInNotBonded = devices.indexOf(device);
		if (indexInNotBonded >= 0) {
		    // update
			ExtendedBluetoothDevice previousDevice = devices.get(indexInNotBonded);
			previousDevice.rssi = device.rssi;
		} else {
		    // add
            devices.add(device);
        }
        notifyDataSetChanged();
    }
}
