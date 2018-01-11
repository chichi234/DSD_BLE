package com.reb.dsd_ble.ui.act;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ui.frag.BaseFragment;
import com.reb.dsd_ble.ui.frag.RelayFragment;

/**
 * File description
 *
 * @author Reb
 * @version 1.0
 * @date 2018-1-11 16:21
 * @package_name com.reb.dsd_ble.ui.act
 * @project_name DSD_BLE
 * @history At 2018-1-11 16:21 created by Reb
 */

public class ConnectActivity extends Activity{

    private RelayFragment mRelayFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

    }

    public void close(View view) {
        finish();
    }
}
