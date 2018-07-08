package com.reb.dsd_ble.ui.frag.communicate;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
    private static final int SEND_TIMEOUT = 3000;
    private static final int MSG_SEND_CMD_FAILED = 0x10001;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEND_CMD_FAILED:
                    Toast.makeText(getActivity(),"Set Failed, please retry later",Toast.LENGTH_LONG).show();
                    setViewEnable(true);
                    break;
            }
        }
    };

    private Spinner mTransmitPower;
    private Button mTransmitPowerBtn;
    private Spinner mAdInterval;
    private Button mAdIntervalBtn;
    private EditText mBeaconName;
    private Button mBeaconNameBtn;
    private EditText mEddystone;
    private Button mEddystoneEnableBtn;
    private Button mEddystoneDisnableBtn;
    private EditText mUUID;
    private EditText mMajor;
    private EditText mMinor;
    private Button miBeaconEnableBtn;
    private Button miBeaconDisableBtn;
    private Button mFinishBtn;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.frag_beacon, null);
            mTransmitPower = mRootView.findViewById(R.id.transmit_power);
            mTransmitPowerBtn = mRootView.findViewById(R.id.transmit_power_set);
            mAdInterval= mRootView.findViewById(R.id.ad_interval);
            mAdIntervalBtn = mRootView.findViewById(R.id.ad_interval_set);
            mBeaconName = mRootView.findViewById(R.id.beacon_name);
            mBeaconNameBtn = mRootView.findViewById(R.id.beacon_name_set);
            mEddystone = mRootView.findViewById(R.id.eddystone);
            mEddystoneEnableBtn = mRootView.findViewById(R.id.enable_eddystone);
            mEddystoneDisnableBtn = mRootView.findViewById(R.id.disable_eddystone);
            mUUID = mRootView.findViewById(R.id.uuid);
            mMajor = mRootView.findViewById(R.id.major);
            mMinor = mRootView.findViewById(R.id.minor);
            miBeaconEnableBtn = mRootView.findViewById(R.id.enable_major);
            miBeaconDisableBtn = mRootView.findViewById(R.id.disable_major);
            mFinishBtn = mRootView.findViewById(R.id.finish);

            mBeaconName.setFilters(new InputFilter.LengthFilter[]{new InputFilter.LengthFilter(25)});

            mTransmitPowerBtn.setOnClickListener(this);
            mAdIntervalBtn.setOnClickListener(this);
            mBeaconNameBtn.setOnClickListener(this);
            mEddystoneEnableBtn.setOnClickListener(this);
            mEddystoneDisnableBtn.setOnClickListener(this);
            miBeaconEnableBtn.setOnClickListener(this);
            miBeaconDisableBtn.setOnClickListener(this);
            mFinishBtn.setOnClickListener(this);
        }
        return mRootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDeviceConnect() {

    }

    @Override
    public void onDeviceDisConnect() {
        setViewEnable(false);
        // TODO 切换到Start
    }

    @Override
    public void onWriteSuccess(byte[] data, boolean success) {
        if (!success && mHandler.hasMessages(MSG_SEND_CMD_FAILED)) {
            mHandler.removeMessages(MSG_SEND_CMD_FAILED);
            mHandler.sendEmptyMessage(MSG_SEND_CMD_FAILED);
        }
    }

    @Override
    public void receive(byte[] data) {
        if ("OK".equals(new String(data))) {
            setViewEnable(true);
            mHandler.removeMessages(MSG_SEND_CMD_FAILED);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.transmit_power_set:
                setTransmitPower();
                break;
            case R.id.ad_interval_set:
                setAdInterval();
                break;
            case R.id.beacon_name_set:
                setBeaconName();
                break;
            case R.id.enable_eddystone:
                enableEddystone();
                break;
            case R.id.disable_eddystone:
                disableEddystone();
                break;
        }
    }

    private void enableEddystone() {
        String net = mEddystone.getText().toString();
        int length =2;
        String command = "AT+BADVDATA=" + "0,0201060303AAFF" + length;
        sendData(command);
    }

    private void disableEddystone() {
        String command = "AT+BADVDATA=0,00,0";
        sendData(command);
    }

    private void setBeaconName() {
        String beaconName = mBeaconName.getText().toString().trim();
        if (beaconName.isEmpty()) {
            Toast.makeText(getActivity(), "Beacon Name must be 0~25 characters", Toast.LENGTH_LONG).show();
        } else {
            String command = "AT+NAME=" + beaconName;
            sendData(command);
        }
    }

    private void setAdInterval() {
        String interval = (String) mAdInterval.getSelectedItem();
        String command = "AT+ADVIN=" + interval;
        sendData(command);
    }

    private void setTransmitPower() {
        int position = mTransmitPower.getSelectedItemPosition();
        String command = "AT+TXPOWER=" + Integer.toHexString(position).toUpperCase();
        sendData(command);
    }

    private void sendData(String command) {
        command = command + "\\r\\n";
        setViewEnable(false);
        mHandler.removeMessages(MSG_SEND_CMD_FAILED);
        mHandler.sendEmptyMessageDelayed(MSG_SEND_CMD_FAILED, SEND_TIMEOUT);
        BleCore.getInstances().sendData(command.getBytes());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // TODO 此时可以切换吗
        if (hidden) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void setViewEnable(boolean enable) {
        mTransmitPowerBtn.setEnabled(enable);
        mAdIntervalBtn.setEnabled(enable);
        mBeaconNameBtn.setEnabled(enable);
    }

}
