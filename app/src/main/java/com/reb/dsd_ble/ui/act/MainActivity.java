package com.reb.dsd_ble.ui.act;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.RadioGroup;

import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ui.frag.AboutFragment;
import com.reb.dsd_ble.ui.frag.BaseFragment;
import com.reb.dsd_ble.ui.frag.DeviceListFragment;
import com.reb.dsd_ble.ui.frag.SettingsFragment;

public class MainActivity extends Activity {

    private RadioGroup mTabGroup;

    private DeviceListFragment mDeviceListFrag;
    private SettingsFragment mSettingsFrag;
    private AboutFragment mAboutFrag;
    private BaseFragment mCurrentFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initFragment(savedInstanceState);
    }




    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mDeviceListFrag = new DeviceListFragment();
            mSettingsFrag = new SettingsFragment();
            mAboutFrag = new AboutFragment();
            mTabGroup.check(R.id.devices);
        } else {
            FragmentManager fm = getFragmentManager();
            mDeviceListFrag = (DeviceListFragment) fm.findFragmentByTag(DeviceListFragment.class.getSimpleName());
            mSettingsFrag = (SettingsFragment) fm.findFragmentByTag(SettingsFragment.class.getSimpleName());
            mAboutFrag = (AboutFragment) fm.findFragmentByTag(AboutFragment.class.getSimpleName());
        }
    }

    private void initView() {
        mTabGroup = findViewById(R.id.main_tab);
        mTabGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                BaseFragment target = null;
                switch (radioGroup.getId()) {
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

    private void changeFragment(BaseFragment target) {
        if (target != null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            if (mCurrentFrag != null) {
                fragmentTransaction.hide(mCurrentFrag);
            }
            if (target.isAdded()) {
                fragmentTransaction.show(target);
            } else {
                fragmentTransaction.add(R.id.main_container, target, target.getClass().getSimpleName());
            }
            mCurrentFrag = target;
            fragmentTransaction.commitAllowingStateLoss();
        }
    }


}
