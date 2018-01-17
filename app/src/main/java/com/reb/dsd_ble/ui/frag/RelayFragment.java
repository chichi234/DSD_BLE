package com.reb.dsd_ble.ui.frag;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ble.profile.BleCore;
import com.reb.dsd_ble.ui.adapter.DeviceAdapter;
import com.reb.dsd_ble.util.DebugLog;

import java.util.Arrays;

/**
 * File description
 *
 * @author Reb
 * @version 1.0
 * @date 2018-1-11 18:52
 * @package_name com.reb.dsd_ble.ui.frag
 * @project_name DSD_BLE
 * @history At 2018-1-11 18:52 created by Reb
 */

public class RelayFragment extends BaseFragment {

    private static final int MSG_SEND_ALL = 0x10001;
    private static final int MSG_SEND_DATA = 0x10002;

    private static final byte[][] RELAY_ON = {
            {(byte) 0xA0, 1, 1, (byte) 0xA2},
            {(byte) 0xA0, 2, 1, (byte) 0xA3},
            {(byte) 0xA0, 3, 1, (byte) 0xA4},
            {(byte) 0xA0, 4, 1, (byte) 0xA5}
    };

    private static final byte[][] RELAY_OFF = {
            {(byte) 0xA0, 1, 0, (byte) 0xA1},
            {(byte) 0xA0, 2, 0, (byte) 0xA2},
            {(byte) 0xA0, 3, 0, (byte) 0xA3},
            {(byte) 0xA0, 4, 0, (byte) 0xA4}
    };

    private ToggleButton mRelay1,mRelay2,mRelay3,mRelay4;
    private Button mAllOn,mAllOff;

    private boolean mIsAllControl = false;
    private boolean mIsAllOn = false;
    private int mAllIndex = 0;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEND_DATA:
                    byte[] data = (byte[]) msg.obj;
                    BleCore.getInstances().sendData(data);
                    break;
                case MSG_SEND_ALL :
                    if (mIsAllControl) {
                        BleCore.getInstances().sendData(mIsAllOn ? RELAY_ON[mAllIndex] : RELAY_OFF[mAllIndex]);
                        if (++mAllIndex >= RELAY_ON.length) {
                            // 结束
                            controlRelayEnable(true);
                            reset();
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.frag_relay, null);
            mRelay1 = mRootView.findViewById(R.id.relay1_toggle);
            mRelay2 = mRootView.findViewById(R.id.relay2_toggle);
            mRelay3 = mRootView.findViewById(R.id.relay3_toggle);
            mRelay4 = mRootView.findViewById(R.id.relay4_toggle);
            mRelay1.setOnCheckedChangeListener(mRelayChangeListener);
            mRelay2.setOnCheckedChangeListener(mRelayChangeListener);
            mRelay3.setOnCheckedChangeListener(mRelayChangeListener);
            mRelay4.setOnCheckedChangeListener(mRelayChangeListener);

            mAllOn = mRootView.findViewById(R.id.relayAll_on);
            mAllOff = mRootView.findViewById(R.id.relayAll_off);
            mAllOn.setOnClickListener(clickListener);
            mAllOff.setOnClickListener(clickListener);
            if (!BleCore.getInstances().isConnected()) {
                controlRelayEnable(false);
            }
        }
        return mRootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        reset();
    }

    public void controlRelayEnable(boolean enable) {
        if (mRelay1 != null) {
            mRelay1.setEnabled(enable);
            mRelay2.setEnabled(enable);
            mRelay3.setEnabled(enable);
            mRelay4.setEnabled(enable);
            mAllOn.setEnabled(enable);
            mAllOff.setEnabled(enable);
        }
    }

    public void onDisconnected () {
        reset();
    }

    public void onWriteSuccess(byte[] data) {
        DebugLog.i("mIsAllControl:" + mIsAllControl);
        if (mIsAllControl) {
//            byte[] preSendData = mIsAllOn ? RELAY_ON[mAllIndex] : RELAY_OFF[mAllIndex];
//            DebugLog.i("preSendData:" + Arrays.toString(preSendData));
//            if (Arrays.equals(data, preSendData)) {
//                DebugLog.i("preSendData:" + Arrays.toString(preSendData));
                mHandler.sendEmptyMessageDelayed(MSG_SEND_ALL,100);
//            }
        }
    }

    private void reset() {
        mIsAllControl = false;
        mIsAllOn = false;
        mAllIndex = 0;
        mHandler.removeCallbacksAndMessages(null);
    }

    private CompoundButton.OnCheckedChangeListener mRelayChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int index = -1;
            switch (buttonView.getId()) {
                case R.id.relay1_toggle :
                    index = 0;
                    break;
                case R.id.relay2_toggle :
                    index = 1;
                    break;
                case R.id.relay3_toggle :
                    index = 2;
                    break;
                case R.id.relay4_toggle :
                    index = 3;
                    break;
            }
            if (index != -1) {
                mHandler.sendMessage(mHandler.obtainMessage(MSG_SEND_DATA, isChecked ? RELAY_ON[index] : RELAY_OFF[index]));
            }
        }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mIsAllControl = true;
            mAllIndex = 0;
            switch (v.getId()) {
                case R.id.relayAll_on:
                    mIsAllOn = true;
                    break;
                case R.id.relayAll_off:
                    mIsAllOn = false;
                    break;
            }
            controlRelayEnable(false);
            mHandler.sendEmptyMessage(MSG_SEND_ALL);
        }
    };

}
