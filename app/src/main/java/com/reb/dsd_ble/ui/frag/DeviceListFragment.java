package com.reb.dsd_ble.ui.frag;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ListView;

import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ble.profile.utility.BleConfiguration;
import com.reb.dsd_ble.ble.scanner.ExtendedBluetoothDevice;
import com.reb.dsd_ble.ble.scanner.ScannerServiceParser;
import com.reb.dsd_ble.ui.adapter.DeviceAdapter;
import com.reb.dsd_ble.util.DebugLog;

/**
 * Created by Administrator on 2018/1/7 0007.
 */

public class DeviceListFragment extends BaseFragment {
    private final static long SCAN_DURATION = 8000;

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler = new Handler();
    private boolean mIsScanning = false;

    private ListView mListView;
    private Button mScanButton;
    private DeviceAdapter mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final BluetoothManager manager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager != null) {
            mBluetoothAdapter = manager.getAdapter();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.frag_devices, null);
            mListView = mRootView.findViewById(R.id.devices);
            mScanButton = mRootView.findViewById(R.id.scan);
            mScanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIsScanning) {
                        stopScan();
                    } else {
                        startScan();
                    }
                }
            });
            mAdapter = new DeviceAdapter(getActivity());
            mListView.setAdapter(mAdapter);
            startScan();
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
        if (confirmPermission()){
            boolean scannerRet = mBluetoothAdapter.startLeScan(mLEScanCallback);
            DebugLog.i("scan ret:" + scannerRet);
            mIsScanning = true;
            mScanButton.setText(R.string.scanner_action_stop_scanning);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mIsScanning) {
                        stopScan();
                    }
                }
            }, SCAN_DURATION);
        }
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
                DebugLog.i(device.toString());
                if (BleConfiguration.SERVICE_UUID_OF_SCAN_FILTER1 != null) {
                    // 扫描过滤
                    if (ScannerServiceParser.decodeDeviceAdvData(scanRecord, BleConfiguration.SERVICE_UUID_OF_SCAN_FILTER1)) {
                        addOrUpdateScannedDevice(device, ScannerServiceParser.decodeDeviceName(scanRecord), rssi);
                    }
                } else {
                    addOrUpdateScannedDevice(device, ScannerServiceParser.decodeDeviceName(scanRecord), rssi);
                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScan();
        }
    }

    private void addOrUpdateScannedDevice(final BluetoothDevice device, final String name, final int rssi) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.addOrUpdateDevice(new ExtendedBluetoothDevice(device, name, rssi, "ONLY BLE", false));
                }
            });
        }
    }

    private boolean confirmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String s = "";
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                return false;
            }
            DebugLog.i("permission:" + s);
        }
        return true;
    }

//    private void updateScannedDevice(final BluetoothDevice device, final int rssi) {
//        if (getActivity() != null) {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mAdapter.updateDeviceRssi(device,rssi);
//                }
//            });
//        }
//    }
}
