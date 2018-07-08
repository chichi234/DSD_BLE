package com.reb.dsd_ble.ui.frag.communicate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ble.profile.BleCore;
import com.reb.dsd_ble.ui.frag.base.BaseCommunicateFragment;

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

public class BeaconStartFragment extends BaseCommunicateFragment implements View.OnClickListener{

    private Button mStartBtn;
    private AouthListener mAouthListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.frag_beacon_start, null);
            mStartBtn = mRootView.findViewById(R.id.start);
            mStartBtn.setOnClickListener(this);
        }
        return mRootView;
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
        if ("$OK,Opened$".equals(resp)) {
            if (mAouthListener != null) {
                mAouthListener.onAouthSuccess();
            }
        }
    }

    @Override
    public void onClick(View v) {
        BleCore.getInstances().sendData("#OpenDSDAtEngine#".getBytes());
        mStartBtn.setEnabled(false);
        // TODO 删除
        if (mAouthListener != null) {
            mAouthListener.onAouthSuccess();
        }
    }

    public void setAouthListener(AouthListener aouthListener) {
        this.mAouthListener = aouthListener;
    }

    public interface AouthListener {
        void onAouthSuccess();
    }
}
