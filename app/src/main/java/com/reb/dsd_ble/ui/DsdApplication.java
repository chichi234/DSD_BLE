package com.reb.dsd_ble.ui;

import android.app.Application;

import com.reb.dsd_ble.ble.profile.utility.BleConfiguration;

/**
 * Created by Administrator on 2018/1/11 0011.
 */

public class DsdApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BleConfiguration.init(this);
    }
}
