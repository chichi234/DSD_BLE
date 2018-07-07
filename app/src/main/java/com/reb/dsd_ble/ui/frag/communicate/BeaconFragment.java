package com.reb.dsd_ble.ui.frag.communicate;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ble.profile.BleCore;
import com.reb.dsd_ble.ui.frag.base.BaseCommunicateFragment;

/**
 * File description
 *
 * @author Reb
 * @version 1.0
 * @date 2018-7-7 14:46
 * @package_name com.reb.dsd_ble.ui.frag
 * @project_name DSD_BLE
 * @history At 2018-7-7 14:46 created by Reb
 */

public class BeaconFragment extends BaseCommunicateFragment implements View.OnClickListener{

    private Handler mHandler = new Handler();

    private Spinner mTransmitPower;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.frag_beacon, null);
            mTransmitPower = mRootView.findViewById(R.id.transmit_power);
        }
        return mRootView;
    }

    @Override
    public void onDeviceConnect() {

    }

    @Override
    public void onDeviceDisConnect() {

    }

    @Override
    public void onWriteSuccess(byte[] data, boolean success) {

    }

    @Override
    public void receive(byte[] data) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.transmit_power_set:
                setTransmitPower();
                break;
        }
    }

    private void setTransmitPower() {
        int position = mTransmitPower.getSelectedItemPosition();
        String command = "AT+TXPOWER=" + Integer.toHexString(position).toUpperCase();
        sendData(command);
    }

    private void sendData(String command) {
        command = command + "\\r\\n";

        BleCore.getInstances().sendData(command.getBytes());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // TODO 此时可以切换吗
    }


}
