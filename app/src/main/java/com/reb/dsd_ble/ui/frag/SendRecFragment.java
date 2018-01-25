package com.reb.dsd_ble.ui.frag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ble.profile.BleCore;
import com.reb.dsd_ble.ble.profile.utility.BleConfiguration;
import com.reb.dsd_ble.constant.ShareString;
import com.reb.dsd_ble.util.HexStringConver;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2018/1/11 0011.
 */

public class SendRecFragment extends BaseFragment {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.sss ");

    private TextView mRevView;
    private ToggleButton mRevToggle;
    private EditText mSendView;
    private ToggleButton mSendToggle;
    private Button mSendBtn;
    private EditText mAutoIntervalView;
    private ToggleButton mAutoToggle;
    private Button mClearBtn;

    private Handler mHandler = new Handler();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.frag_send_rev, null);
            findViews();
            initListener();
            mAutoIntervalView.setText(BleConfiguration.AUTO_INTERVAL + "");
        }
        return mRootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences share = getActivity().getSharedPreferences(ShareString.FILE_NAME, Context.MODE_PRIVATE);
        try {
            long delay = Long.parseLong(mAutoIntervalView.getText().toString());
            BleConfiguration.AUTO_INTERVAL = delay;
            share.edit().putLong(ShareString.SAVE_AUTO_INTERVAL, delay).apply();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void findViews() {
        mRevView = mRootView.findViewById(R.id.read);
        mRevToggle = mRootView.findViewById(R.id.toggle_rx);
        mSendView = mRootView.findViewById(R.id.sendContent);
        mSendToggle = mRootView.findViewById(R.id.toggle_tx);
        mSendBtn = mRootView.findViewById(R.id.send);
        mAutoIntervalView = mRootView.findViewById(R.id.repeat_interval);
        mAutoToggle = mRootView.findViewById(R.id.toggle_auto_send);
        mClearBtn = mRootView.findViewById(R.id.clear);
    }

    private void initListener() {
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
        mClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRevView.setText("");
                mSendView.setText("");
            }
        });
        mAutoToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mHandler.post(autoSendRunnable);
                } else {
                    mHandler.removeCallbacks(autoSendRunnable);
                }
            }
        });
    }

    private Runnable autoSendRunnable = new Runnable() {
        @Override
        public void run() {
            send();
            if (mAutoToggle.isChecked()) {
                long delay = 0;
                try {
                    delay = Long.parseLong(mAutoIntervalView.getText().toString());
                    mHandler.postDelayed(autoSendRunnable, delay);
                } catch (NumberFormatException e) {
                    mAutoToggle.setChecked(false);
                    appendViewContent(mRevView, getString(R.string.auto_send_interval_error));
                }
            }
        }
    };

    private void send() {
        String content = mSendView.getText().toString();
        if (TextUtils.isEmpty(content)) {
            appendViewContent(mRevView, getString(R.string.uncorrect_data));
        } else {
            sendData(content);
        }
    }

    private void sendData(String content) {
        byte[] data;
        if (mSendToggle.isChecked()) {
            String rule = HexStringConver.getRuledHexString(content);
            if (!TextUtils.isEmpty(rule)) {
                data = HexStringConver.convertHexString2Bytes(content);
            } else {
                appendViewContent(mRevView, getString(R.string.uncorrect_data));
                return;
            }
        } else {
            data = content.getBytes();
        }
        if (BleCore.getInstances().isConnected()) {
            BleCore.getInstances().sendData(data);
        } else {
            appendViewContent(mRevView, getString(R.string.device_disConn));
        }
    }

    private void appendViewContent(final TextView v, final String appending) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String content = v.getText().toString();
                StringBuilder sb = new StringBuilder(content);
                sb.append("\n" + getCurrentTime() + appending);
                v.setText(sb.toString());
            }
        });
    }

    private String getCurrentTime() {
        return sdf.format(new Date());
    }


    public void writeSuccess(byte[] data, boolean success) {

    }

    public void receive(byte[] data) {
        if (mRevToggle.isChecked()) {
            appendViewContent(mRevView, HexStringConver.bytes2HexStr(data));
        } else {
            appendViewContent(mRevView, new String(data));
        }
    }

    public void connected() {

    }

    public void disconnected() {
    }
}
