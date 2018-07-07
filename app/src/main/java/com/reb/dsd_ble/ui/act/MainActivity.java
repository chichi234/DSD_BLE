package com.reb.dsd_ble.ui.act;

import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ui.frag.AboutFragment;
import com.reb.dsd_ble.ui.frag.base.BaseFragment;
import com.reb.dsd_ble.ble.scanner.DeviceListFragment;
import com.reb.dsd_ble.ui.frag.SettingsFragment;

public class MainActivity extends BaseFragmentActivity {
    private static final int REQUEST_ENABLE_BT = 1;

    private RadioGroup mTabGroup;

    private DeviceListFragment mDeviceListFrag;
    private SettingsFragment mSettingsFrag;
    private AboutFragment mAboutFrag;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showBLEDialog();
        setContentView(R.layout.activity_main);
        initView();
        initFragment(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (!isBLEEnabled()) {
                Toast.makeText(this, R.string.bluetooth_disabled, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mDeviceListFrag = new DeviceListFragment();
            mSettingsFrag = new SettingsFragment();
            mAboutFrag = new AboutFragment();
        } else {
            FragmentManager fm = getFragmentManager();
            mDeviceListFrag = (DeviceListFragment) fm.findFragmentByTag(DeviceListFragment.class.getSimpleName());
            mSettingsFrag = (SettingsFragment) fm.findFragmentByTag(SettingsFragment.class.getSimpleName());
            mAboutFrag = (AboutFragment) fm.findFragmentByTag(AboutFragment.class.getSimpleName());
            String mCurrentTag = savedInstanceState.getString("mCurrentFragTag", null);
            if (DeviceListFragment.class.getSimpleName().equals(mCurrentTag)) {
                mCurrentFrag = mDeviceListFrag;
            } else if (SettingsFragment.class.getSimpleName().equals(mCurrentTag)) {
                mCurrentFrag = mSettingsFrag;
            } else if (AboutFragment.class.getSimpleName().equals(mCurrentTag)) {
                mCurrentFrag = mAboutFrag;
            }
        }
        changeFragment(mDeviceListFrag);
    }

    private void initView() {
        mTabGroup = findViewById(R.id.main_tab);
        mTabGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                BaseFragment target = null;
                switch (id) {
                    case R.id.devices:
                        target = mDeviceListFrag;
                        break;
                    case R.id.settings:
                        target = mSettingsFrag;
                        break;
                    case R.id.about:
                        target = mAboutFrag;
                        break;
                }
                changeFragment(target);
            }
        });
    }

    public boolean isBLEEnabled() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter != null && (adapter.getState() == BluetoothAdapter.STATE_ON || adapter.getState() == BluetoothAdapter.STATE_TURNING_ON) ;
    }


    public void showBLEDialog() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }
}
