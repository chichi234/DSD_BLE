package com.reb.dsd_ble.ui.frag.communicate;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ble.profile.BleCore;
import com.reb.dsd_ble.ui.frag.base.BaseCommunicateFragment;
import com.reb.dsd_ble.ui.frag.base.BaseFragment;
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

public class RelayFragment extends BaseCommunicateFragment {

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

//    private ToggleButton mRelay1, mRelay2, mRelay3, mRelay4;
    private RadioGroup mRelay1, mRelay2, mRelay3, mRelay4;
    private RadioButton mRelayOpen1, mRelayOpen2, mRelayOpen3, mRelayOpen4,
        mRelayClose1, mRelayClose2,mRelayClose3,mRelayClose4;
    private Button mAllOn, mAllOff;

    private boolean mIsAllControl = false;
    private boolean mIsAllOn = false;
    private int mAllIndex = -1;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEND_DATA:
                    byte[] data = (byte[]) msg.obj;
                    BleCore.getInstances().sendData(data);
                    break;
                case MSG_SEND_ALL:
                    if (mIsAllControl) {
                        if (++mAllIndex >= RELAY_ON.length) {
                            // 结束
                            controlRelayEnable(true);
                            reset();
                        } else {
                            BleCore.getInstances().sendData(mIsAllOn ? RELAY_ON[mAllIndex] : RELAY_OFF[mAllIndex]);
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
            changeToggle(mRelay1, false);
            changeToggle(mRelay2, false);
            changeToggle(mRelay3, false);
            changeToggle(mRelay4, false);
            mRelay1.setOnCheckedChangeListener(mRelayChangeListener);
            mRelay2.setOnCheckedChangeListener(mRelayChangeListener);
            mRelay3.setOnCheckedChangeListener(mRelayChangeListener);
            mRelay4.setOnCheckedChangeListener(mRelayChangeListener);
            mRelayOpen1 = mRelay1.findViewById(R.id.relay1_open);
            mRelayOpen2 = mRelay2.findViewById(R.id.relay2_open);
            mRelayOpen3 = mRelay3.findViewById(R.id.relay3_open);
            mRelayOpen4 = mRelay4.findViewById(R.id.relay4_open);
            mRelayClose1 = mRelay1.findViewById(R.id.relay1_close);
            mRelayClose2 = mRelay2.findViewById(R.id.relay2_close);
            mRelayClose3 = mRelay3.findViewById(R.id.relay3_close);
            mRelayClose4 = mRelay4.findViewById(R.id.relay4_close);

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

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            controlRelayEnable(BleCore.getInstances().isConnected());
        }
    }

    public void controlRelayEnable(boolean enable) {
        if (mRelay1 != null) {
            mRelay1.setEnabled(enable);
            mRelay2.setEnabled(enable);
            mRelay3.setEnabled(enable);
            mRelay4.setEnabled(enable);

            mRelayOpen1.setEnabled(enable);
            mRelayOpen2.setEnabled(enable);
            mRelayOpen3.setEnabled(enable);
            mRelayOpen4.setEnabled(enable);

            mRelayClose1.setEnabled(enable);
            mRelayClose2.setEnabled(enable);
            mRelayClose3.setEnabled(enable);
            mRelayClose4.setEnabled(enable);

            mAllOn.setEnabled(enable);
            mAllOff.setEnabled(enable);
        }
    }

    @Override
    public void onDeviceConnect() {
        controlRelayEnable(true);
    }

    @Override
    public void onDeviceDisConnect() {
        reset();
        controlRelayEnable(false);
    }

    @Override
    public void onWriteSuccess(byte[] data, boolean success) {

        if (mIsAllControl && success) {
            byte[][] dataArray = mIsAllOn ? RELAY_ON : RELAY_OFF;
            int index = findArrayIndex(dataArray, data);
            DebugLog.i("mIsAllControl:" + mIsAllControl + ",index:" + index);
            switch (index) {
                case 0:
                    changeToggle(mRelay1, mIsAllOn);
                    break;
                case 1:
                    changeToggle(mRelay2, mIsAllOn);
                    break;
                case 2:
                    changeToggle(mRelay3, mIsAllOn);
                    break;
                case 3:
                    changeToggle(mRelay4, mIsAllOn);
                    break;
            }
            mHandler.sendEmptyMessageDelayed(MSG_SEND_ALL, 100);
        }
        // 失败还原
        if (!success) {
            boolean isOn = true;
            int index = findArrayIndex(RELAY_ON, data);
            if (index == -1) {
                isOn = false;
                index = findArrayIndex(RELAY_OFF, data);
            }
            switch (index) {
                case 0:
                    changeToggle(mRelay1, !isOn);
                    break;
                case 1:
                    changeToggle(mRelay2, !isOn);
                    break;
                case 2:
                    changeToggle(mRelay3, !isOn);
                    break;
                case 3:
                    changeToggle(mRelay4, !isOn);
                    break;
            }
        }
    }

    @Override
    public void receive(byte[] data) {

    }

    private void changeToggle(RadioGroup relay, boolean checked) {
        relay.setOnCheckedChangeListener(null);
        int checkedId = R.id.relay1_open;
        switch (relay.getId()) {
            case R.id.relay1_toggle:
                checkedId = checked ? R.id.relay1_open : R.id.relay1_close;
                break;
            case R.id.relay2_toggle:
                checkedId = checked ? R.id.relay2_open : R.id.relay2_close;
                break;
            case R.id.relay3_toggle:
                checkedId = checked ? R.id.relay3_open : R.id.relay3_close;
                break;
            case R.id.relay4_toggle:
                checkedId = checked ? R.id.relay4_open : R.id.relay4_close;
                break;
        }
        relay.check(checkedId);
//        relay.setChecked(checked);
        relay.setOnCheckedChangeListener(mRelayChangeListener);
    }

    private int findArrayIndex(byte[][] dataArray, byte[] data) {
        for (int i = 0; i < dataArray.length; i++) {
            if (Arrays.equals(dataArray[i], data)) {
                return i;
            }
        }
        return -1;
    }

    private void reset() {
        mIsAllControl = false;
        mIsAllOn = false;
        mAllIndex = -1;
        mHandler.removeCallbacksAndMessages(null);
    }

//    private CompoundButton.OnCheckedChangeListener mRelayChangeListener = new CompoundButton.OnCheckedChangeListener() {
//        @Override
//        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//            int index = -1;
//            switch (buttonView.getId()) {
//                case R.id.relay1_toggle:
//                    index = 0;
//                    break;
//                case R.id.relay2_toggle:
//                    index = 1;
//                    break;
//                case R.id.relay3_toggle:
//                    index = 2;
//                    break;
//                case R.id.relay4_toggle:
//                    index = 3;
//                    break;
//            }
//            if (index != -1) {
//                mHandler.sendMessage(mHandler.obtainMessage(MSG_SEND_DATA, isChecked ? RELAY_ON[index] : RELAY_OFF[index]));
//            }
//        }
//    };

    private RadioGroup.OnCheckedChangeListener mRelayChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int index = -1;
            boolean isChecked = true;
            switch (checkedId) {
                case R.id.relay1_open:
                    index = 0;
                    break;
                case R.id.relay2_open:
                    index = 1;
                    break;
                case R.id.relay3_open:
                    index = 2;
                    break;
                case R.id.relay4_open:
                    index = 3;
                    break;
                case R.id.relay1_close:
                    index = 0;
                    isChecked = false;
                    break;
                case R.id.relay2_close:
                    index = 1;
                    isChecked = false;
                    break;
                case R.id.relay3_close:
                    index = 2;
                    isChecked = false;
                    break;
                case R.id.relay4_close:
                    isChecked = false;
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
            mAllIndex = -1;
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
