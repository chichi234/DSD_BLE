package com.reb.dsd_ble.ui.frag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ui.adapter.DeviceAdapter;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.frag_relay, null);
        }
        return mRootView;
    }

}
