package com.reb.dsd_ble.ui.frag;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reb.dsd_ble.R;

/**
 * Created by Administrator on 2018/1/7 0007.
 */

public class SettingsFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.frag_settings, null);
        }
        return mRootView;
    }
}
