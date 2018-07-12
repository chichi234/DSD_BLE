package com.reb.dsd_ble.ui.frag.communicate;

import android.annotation.SuppressLint;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ble.profile.BleCore;
import com.reb.dsd_ble.ui.frag.base.BaseCommunicateFragment;
import com.reb.dsd_ble.util.DebugLog;

/**
 * File description
 *
 * @author Reb
 * @version 1.0
 * @date 2018-7-7 17:24
 * @package_name com.reb.dsd_ble.ui.frag.communicate
 * @project_name DSD_BLE
 * @history At 2018-7-7 17:24 created by Reb
 */

public class BeaconStartFragment extends BaseCommunicateFragment implements View.OnClickListener {
    private static final int MSG_SEND_CMD_FAILED = 0x10001;
    private Button mStartBtn;
    private AouthListener mAouthListener;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEND_CMD_FAILED:
                    Toast.makeText(getActivity(), "Set Failed, please retry later", Toast.LENGTH_LONG).show();
                    mStartBtn.setEnabled(true);
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.frag_beacon_start, null);
            mStartBtn = mRootView.findViewById(R.id.start);
            mStartBtn.setOnClickListener(this);
            mStartBtn.setEnabled(BleCore.getInstances().isConnected());
        }
        return mRootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(MSG_SEND_CMD_FAILED);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            mStartBtn.setEnabled(BleCore.getInstances().isConnected());
        } else {
            mHandler.removeMessages(MSG_SEND_CMD_FAILED);
        }
    }

    @Override
    public void onDeviceConnect() {
        mStartBtn.setEnabled(true);
    }

    @Override
    public void onDeviceDisConnect() {
        mStartBtn.setEnabled(false);
    }

    @Override
    public void onWriteSuccess(byte[] data, boolean success) {
        if (!success) {
            mStartBtn.setEnabled(true);
        }
    }

    @Override
    public void receive(byte[] data) {
        String resp = new String(data);
        DebugLog.i("receive:" + resp);
        if ("$OK,Opened$".equals(resp)) {
            mHandler.removeMessages(MSG_SEND_CMD_FAILED);
            if (mAouthListener != null) {
                mAouthListener.onAouthSuccess();
            } else {
                mStartBtn.setEnabled(true);
            }
        }
    }

    @Override
    public void onClick(View v) {
        BleCore.getInstances().sendData("#OpenDSDAtEngine#".getBytes());
        mHandler.removeMessages(MSG_SEND_CMD_FAILED);
        mHandler.sendEmptyMessageDelayed(MSG_SEND_CMD_FAILED, 3000);
        mStartBtn.setEnabled(false);
    }

    public void setAouthListener(AouthListener aouthListener) {
        this.mAouthListener = aouthListener;
    }

    public interface AouthListener {
        void onAouthSuccess();
    }
}
