package com.reb.dsd_ble.ui.act;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ble.profile.BleCore;
import com.reb.dsd_ble.ble.profile.BleManagerCallbacks;
import com.reb.dsd_ble.ui.frag.base.BaseCommunicateFragment;
import com.reb.dsd_ble.ui.frag.base.BaseFragment;
import com.reb.dsd_ble.ui.frag.communicate.BeaconFragment;
import com.reb.dsd_ble.ui.frag.communicate.BeaconStartFragment;
import com.reb.dsd_ble.ui.frag.communicate.RelayFragment;
import com.reb.dsd_ble.ui.frag.communicate.SendRecFragment;
import com.reb.dsd_ble.util.DebugLog;

import java.lang.ref.WeakReference;
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

public class ConnectActivity extends BaseFragmentActivity implements BleManagerCallbacks, BeaconStartFragment.AouthListener, BeaconFragment.SetDSDStateListener {
    private static final int MSG_READ_RSSI = 0x10001;
    private static final int MSG_READ_RSSI_RESULT = 0x10002;
    private static final int MSG_DEVICE_CONNECTED = 0x10003;
    private static final int MSG_DEVICE_DISCONNECTED = 0x10004;
    private static final int MSG_LINK_LOSS = 0x10005;
    private static final int MSG_WRITE_SUCCESS = 0x10006;
    private static final int MSG_RECEIVE_DATA = 0x10007;
    private static final int MSG_CONNECT_TIME_OUT = 0x10008;
    private static final int MSG_HIDE_ALERT = 0x10009;

    private RelayFragment mRelayFragment;
    private SendRecFragment mSendRecFragment;
    private BeaconFragment mBeaconFragment;
    private BeaconStartFragment mBeaconStartFragment;

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
    private boolean mIsFinish = false;

    private WakeHandler<ConnectActivity> mHandler;

    @SuppressLint("HandlerLeak")
    private class WakeHandler<T> extends Handler {
        private WeakReference<T> weakReference;

        WakeHandler(T act) {
            weakReference = new WeakReference<T>(act);
        }

        @Override
        public void handleMessage(Message msg) {
            if (weakReference.get() == null || mIsFinish) {
                return;
            }
            switch (msg.what) {
                case MSG_READ_RSSI:
                    if (!mBleCore.readRssi()) {
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
                    if (mCurrentFrag != null) {
                        ((BaseCommunicateFragment) mCurrentFrag).onDeviceConnect();
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
                    if (mCurrentFrag != null) {
                        ((BaseCommunicateFragment) mCurrentFrag).onDeviceDisConnect();
                        if (mCurrentFrag == mBeaconFragment) {
                            changeFragment(mBeaconStartFragment);
                        }
                    }
                    break;
                case MSG_LINK_LOSS:
                    removeMessages(MSG_READ_RSSI);
                    mDeviceInfoView.setText(mDeviceName + "(-- dBm)");
                    String macAddress = (String) msg.obj;
                    if (!mDeviceAddress.equals(macAddress)) {
                        connect();
                    }
                    mConnectBtn.setText(R.string.conn);
                    showAlert(R.string.connect_failed, false);
                    if (mCurrentFrag != null) {
                        ((BaseCommunicateFragment) mCurrentFrag).onDeviceDisConnect();
                        if (mCurrentFrag == mBeaconFragment) {
                            changeFragment(mBeaconStartFragment);
                        }
                    }
                    break;
                case MSG_WRITE_SUCCESS:
                    Object[] obj = (Object[]) msg.obj;
                    byte[] writeData = (byte[]) obj[0];
                    boolean success = (boolean) obj[1];
                    if (mCurrentFrag != null) {
                        ((BaseCommunicateFragment) mCurrentFrag).onWriteSuccess(writeData, success);
                    }
                    break;
                case MSG_RECEIVE_DATA:
                    if (mCurrentFrag != null) {
                        byte[] data = (byte[]) msg.obj;
                        ((BaseCommunicateFragment) mCurrentFrag).receive(data);
                    }
                    break;
                case MSG_CONNECT_TIME_OUT:
                    mBleCore.disconnect();
                    onDeviceDisconnected();
                    mConnectBtn.setEnabled(true);
                    mConnectBtn.setText(R.string.conn);
                    showAlert(R.string.connect_failed, false);
                    sendEmptyMessageDelayed(MSG_HIDE_ALERT, 3000);
                    break;
                case MSG_HIDE_ALERT:
                    mAlertLayout.setVisibility(View.GONE);
                    break;
            }
        }
    }

    ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new WakeHandler<>(this);
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
            mBeaconFragment = new BeaconFragment();
            mBeaconFragment.setDsdStateListener(this);
            mBeaconStartFragment = new BeaconStartFragment();
            mBeaconStartFragment.setAouthListener(this);
        } else {
            FragmentManager fm = getFragmentManager();
            mRelayFragment = (RelayFragment) fm.findFragmentByTag(RelayFragment.class.getSimpleName());
            mSendRecFragment = (SendRecFragment) fm.findFragmentByTag(SendRecFragment.class.getSimpleName());
            String mCurrentTag = savedInstanceState.getString("mCurrentFragTag", null);
            if (RelayFragment.class.getSimpleName().equals(mCurrentTag)) {
                mCurrentFrag = mRelayFragment;
            } else if (SendRecFragment.class.getSimpleName().equals(mCurrentTag)) {
                mCurrentFrag = mSendRecFragment;
            } else if (BeaconFragment.class.getSimpleName().equals(mCurrentTag)) {
                mCurrentFrag = mBeaconFragment;
            }
        }
        changeFragment(mSendRecFragment);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsFinish = true;
        BleCore.getInstances().disconnect();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void setData() {
        mBleCore.setGattCallbacks(this);
        mDeviceInfoView.setText(mDeviceName + "(" + mRssi + "dBm)");
        connect();
    }

    private void connect() {
        mConnectBtn.setText(R.string.connecting);
        mConnectBtn.setEnabled(false);
//        if (mCurrentFrag == mRelayFragment) {
//            mRelayFragment.controlRelayEnable(false);
//        }
        showAlert(R.string.connecting_alert, true);
        mBleCore.connect(getApplicationContext(), mDeviceAddress);
        mHandler.sendEmptyMessageDelayed(MSG_CONNECT_TIME_OUT, 6 * 1000);
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
                mExitTargetFragment = null;
                DebugLog.i("checkId:" + checkedId + "," + R.id.beacon + ",====" + (mCurrentFrag == mBeaconFragment));
                BaseCommunicateFragment target = null;
                switch (checkedId) {
                    case R.id.relay:
                        target = mRelayFragment;
                        break;
                    case R.id.log:
                        target = mSendRecFragment;
                        break;
                    case R.id.beacon:
                        target = mBeaconStartFragment;
                        break;
                }
                if (mCurrentFrag != null && mCurrentFrag == mBeaconFragment) {
                    if (checkedId != R.id.beacon) {
                        showAlertDialog(target);
                    }
                } else {
                    changeFragment(target);
                }
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
        mHandler.removeMessages(MSG_HIDE_ALERT);
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
        mHandler.removeMessages(MSG_CONNECT_TIME_OUT);
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
    public void onWriteSuccess(byte[] data, boolean success) {
//        DebugLog.i("write success:" + Arrays.toString(data) + ",success:" + success);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_WRITE_SUCCESS, new Object[]{data, success}));
    }

    @Override
    public void onRecive(byte[] data) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_RECEIVE_DATA, data));
    }

    @Override
    public void onReadRssi(int rssi) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_READ_RSSI_RESULT, rssi));
    }

    @Override
    public void onAouthSuccess() {
        changeFragment(mBeaconFragment);
    }

    @Override
    public void onExitSetState() {
        if (mExitTargetFragment != null) {
            changeFragment(mExitTargetFragment);
            mExitTargetFragment = null;
        } else {
            changeFragment(mBeaconStartFragment);
        }
    }

    @Override
    public void onExitSetNoResponse() {
        if (mExitTargetFragment != null) {
            RadioButton radioButton = findViewById(R.id.beacon);
            radioButton.setChecked(true);
            mExitTargetFragment = null;
        }
    }

    private void showAlertDialog(final BaseCommunicateFragment targetFragment) {
        new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setCancelable(true)
                .setMessage(R.string.exit_beacon_alert)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RadioButton radioButton = findViewById(R.id.beacon);
                        radioButton.setChecked(true);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        RadioButton radioButton = findViewById(R.id.beacon);
                        radioButton.setChecked(true);
                    }
                })
                .setNeutralButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mExitTargetFragment = targetFragment;
                        mBeaconFragment.sendFinish();
                        changeFragment(mExitTargetFragment);
                    }
                })
                .create().show();
    }

    private BaseCommunicateFragment mExitTargetFragment;
}
