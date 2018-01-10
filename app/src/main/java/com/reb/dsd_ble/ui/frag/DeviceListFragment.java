package com.reb.dsd_ble.ui.frag;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ListView;

import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ble.scanner.ScannerServiceParser;
import com.reb.dsd_ble.ui.adapter.DeviceAdapter;

import java.util.UUID;

/**
 * Created by Administrator on 2018/1/7 0007.
 */

public class DeviceListFragment extends BaseFragment {
    private final static String TAG = "ScannerFragment";

    private final static String PARAM_UUID = "param_uuid";
    private final static long SCAN_DURATION = 8000;

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler = new Handler();
    private UUID mUuid;
    private boolean mIsScanning = false;

    private View mRootView;
    private ListView mListView;
    private Button mScanButton;
    private DeviceAdapter mAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView != null) {
            ViewParent parent = mRootView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(mRootView);
            }
        } else {
            mRootView = inflater.inflate(R.layout.frag_devices, null);
            mListView = mRootView.findViewById(R.id.devices);
            mScanButton = mRootView.findViewById(R.id.scan);
            mAdapter = new DeviceAdapter();
        }
        return mRootView;
    }

    /**
     * Scan for 5 seconds and then stop scanning when a BluetoothLE device is found then mLEScanCallback is activated This will perform regular scan for custom BLE Service UUID and then filter out.
     * using class ScannerServiceParser
     */
    private void startScan() {
        mAdapter.clearDevices();
        mScanButton.setText(R.string.scanner_action_cancel);
        boolean scannerRet = mBluetoothAdapter.startLeScan(mLEScanCallback);

        mIsScanning = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsScanning) {
                    stopScan();
                }
            }
        }, SCAN_DURATION);
    }


    /**
     * Stop scan if user tap Cancel button.
     */
    private void stopScan() {
        if (mIsScanning) {
            mScanButton.setText(R.string.scanner_action_scan);
            mBluetoothAdapter.stopLeScan(mLEScanCallback);
            mIsScanning = false;
        }
    }

    /**
     * Callback for scanned devices class {@link ScannerServiceParser} will be used to filter devices with custom BLE service UUID then the device will be added in a list.
     */
    private BluetoothAdapter.LeScanCallback mLEScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            if (device != null) {
//                updateScannedDevice(device, rssi);
//                if (mIsCustomUUID) {
//                    try {
//                        if (ScannerServiceParser.decodeDeviceAdvData(scanRecord, mUuid)) {
//                            // On some devices device.getName() is always null. We have to parse the name manually :(
//                            // This bug has been found on Sony Xperia Z1 (C6903) with Android 4.3.
//                            // https://devzone.nordicsemi.com/index.php/cannot-see-device-name-in-sony-z1
//                            addScannedDevice(device, ScannerServiceParser.decodeDeviceName(scanRecord), rssi, DEVICE_NOT_BONDED);
//                        }
//                    } catch (Exception e) {
//                        Log.e(TAG, "Invalid data in Advertisement packet " + e.toString());
//                    }
//                } else {
//                    addScannedDevice(device, ScannerServiceParser.decodeDeviceName(scanRecord), rssi, DEVICE_NOT_BONDED);
//                }
            }
        }
    };
}
