package com.reb.dsd_ble.ui.act;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ble.profile.BleCore;
import com.reb.dsd_ble.ble.profile.BleManagerCallbacks;
import com.reb.dsd_ble.ui.frag.BaseFragment;
import com.reb.dsd_ble.ui.frag.RelayFragment;
import com.reb.dsd_ble.ui.frag.SendRecFragment;
import com.reb.dsd_ble.util.DebugLog;

import java.util.Arrays;

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

public class ConnectActivity extends BaseFragmentActivity implements BleManagerCallbacks{
    private static final int MSG_READ_RSSI = 0x10001;
    private static final int MSG_READ_RSSI_RESULT = 0x10002;
    private static final int MSG_DEVICE_CONNECTED = 0x10003;
    private static final int MSG_DEVICE_DISCONNECTED = 0x10004;
    private static final int MSG_LINK_LOSS = 0x10005;
    private static final int MSG_WRITE_SUCCESS = 0x10006;
    private static final int MSG_RECEIVE_DATA = 0x10007;

    private RelayFragment mRelayFragment;
    private SendRecFragment mSendRecFragment;

    private ViewGroup mAlertLayout;
    private TextView mAlertText;
    private ProgressBar mWaitView;
    private TextView mDeviceInfoView;
    private TextView mConnectBtn;
    private RadioGroup mTabGroup;

    private String mDeviceName;
    private String mDeviceAddress;
    private int mRssi;
    private BleCore mBleCore;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_READ_RSSI :
                    if (!mBleCore.readRssi()){
                        mDeviceInfoView.setText(mDeviceName + "(-- dBm)");
                        removeMessages(MSG_READ_RSSI);
                        sendEmptyMessageDelayed(MSG_READ_RSSI, 1000);
                    }
                    break;
                case MSG_READ_RSSI_RESULT:
                    mRssi = (int) msg.obj;
                    mDeviceInfoView.setText(mDeviceName + "(" + mRssi + "dBm)");
                    removeMessages(MSG_READ_RSSI);
                    sendEmptyMessageDelayed(MSG_READ_RSSI, 1000);
                    break;
                case MSG_DEVICE_CONNECTED:
                    mAlertLayout.setVisibility(View.GONE);
                    if (mCurrentFrag == mRelayFragment) {
                        mRelayFragment.controlRelayEnable(true);
                    } else if(mCurrentFrag == mSendRecFragment) {
                        mSendRecFragment.connected();
                    }
                    mConnectBtn.setText(R.string.disconn);
                    mConnectBtn.setEnabled(true);
                    removeMessages(MSG_READ_RSSI);
                    sendEmptyMessage(MSG_READ_RSSI);
                    break;
                case MSG_DEVICE_DISCONNECTED:
                    removeMessages(MSG_READ_RSSI);
                    mDeviceInfoView.setText(mDeviceName + "(-- dBm)");
                    mConnectBtn.setText(R.string.conn);
                    mRelayFragment.onDisconnected();
                    if (mCurrentFrag == mRelayFragment) {
                        mRelayFragment.controlRelayEnable(false);
                    } else if(mCurrentFrag == mSendRecFragment) {
                        mSendRecFragment.disconnected();
                    }
                    break;
                case MSG_LINK_LOSS:
                    String macAddress = (String) msg.obj;
                    if (!mDeviceAddress.equals(macAddress)) {
                        showAlert(R.string.connecting_alert, true);
                        mBleCore.connect(getApplicationContext(), mDeviceAddress);
                    }
                    break;
                case MSG_WRITE_SUCCESS:
                    byte[] writeData = (byte[]) msg.obj;
                    if (mCurrentFrag == mSendRecFragment) {
                        mSendRecFragment.writeSuccess(writeData);
                    } else if (mCurrentFrag == mRelayFragment) {
                        mRelayFragment.onWriteSuccess(writeData);
                    }
                    break;
                case MSG_RECEIVE_DATA:
                    if (mCurrentFrag == mSendRecFragment) {
                        byte[] data = (byte[]) msg.obj;
                        mSendRecFragment.receive(data);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        initData();
        initView();
        initFragment(savedInstanceState);
        setData();
    }

    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mRelayFragment = new RelayFragment();
            mSendRecFragment = new SendRecFragment();
        } else {
            FragmentManager fm = getFragmentManager();
            mRelayFragment = (RelayFragment) fm.findFragmentByTag(RelayFragment.class.getSimpleName());
            mSendRecFragment = (SendRecFragment) fm.findFragmentByTag(SendRecFragment.class.getSimpleName());
            String mCurrentTag = savedInstanceState.getString("mCurrentFragTag", null);
            if (RelayFragment.class.getSimpleName().equals(mCurrentTag)) {
                mCurrentFrag = mRelayFragment;
            } else if (SendRecFragment.class.getSimpleName().equals(mCurrentTag)) {
                mCurrentFrag = mSendRecFragment;
            }
        }
        changeFragment(mRelayFragment);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        BleCore.getInstances().disconnect();
    }

    private void setData() {
        mBleCore.setGattCallbacks(this);
        mDeviceInfoView.setText(mDeviceName + "(" + mRssi + "dBm)");
        connect();
    }

    private void connect() {
        mConnectBtn.setText(R.string.connecting);
        mConnectBtn.setEnabled(false);
        if (mCurrentFrag == mRelayFragment) {
            mRelayFragment.controlRelayEnable(false);
        }
        showAlert(R.string.connecting_alert, true);
        mBleCore.connect(getApplicationContext(), mDeviceAddress);
    }

    private void initView() {
        mAlertLayout = findViewById(R.id.alert_layout);
        mAlertText = findViewById(R.id.alert_text);
        mWaitView = findViewById(R.id.alert_progress);
        mDeviceInfoView = findViewById(R.id.act_connect_device_info);
        mConnectBtn = findViewById(R.id.connect_btn);
        mTabGroup = findViewById(R.id.data_send_tab);
        mTabGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                BaseFragment target = null;
                switch (checkedId) {
                    case R.id.relay:
                        target = mRelayFragment;
                        break;
                    case R.id.log:
                        target = mSendRecFragment;
                        break;
                }
                changeFragment(target);
            }
        });
        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugLog.i("connect btn .connected ? " + BleCore.getInstances().isConnected());
                if (BleCore.getInstances().isConnected()) {
                    mBleCore.disconnect();
                } else {
                    connect();
                }
            }
        });
    }

    private void initData() {
        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra("name");
        mDeviceAddress = intent.getStringExtra("address");
        mRssi = intent.getIntExtra("rssi", -100);
        if (TextUtils.isEmpty(mDeviceName)) {
            mDeviceName = getString(R.string.unknown);
        }

        mBleCore = BleCore.getInstances();
        mBleCore.setGattCallbacks(this);
    }

    private void showAlert(int stringResId, boolean showBar) {
        mAlertLayout.setVisibility(View.VISIBLE);
        mAlertText.setText(stringResId);
        if (showBar) {
            mWaitView.setVisibility(View.VISIBLE);
        } else {
            mWaitView.setVisibility(View.GONE);
        }
    }

    public void close(View view) {
        finish();
    }

    @Override
    public void onDeviceConnected() {
        mHandler.sendEmptyMessage(MSG_DEVICE_CONNECTED);
    }

    @Override
    public void onDeviceDisconnected() {
        mHandler.sendEmptyMessage(MSG_DEVICE_DISCONNECTED);
    }

    @Override
    public void onServicesDiscovered() {

    }

    @Override
    public void onNotifyEnable() {

    }

    @Override
    public void onLinklossOccur(String macAddress) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_LINK_LOSS, macAddress));
    }

    @Override
    public void onDeviceNotSupported() {

    }

    @Override
    public void onWriteSuccess(byte[] data) {
        DebugLog.i("write success:" + Arrays.toString(data));
        mHandler.sendMessage(mHandler.obtainMessage(MSG_WRITE_SUCCESS, data));
    }

    @Override
    public void onRecive(byte[] data) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_RECEIVE_DATA, data));
    }

    @Override
    public void onReadRssi(int rssi) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_READ_RSSI_RESULT, rssi));
    }
}
