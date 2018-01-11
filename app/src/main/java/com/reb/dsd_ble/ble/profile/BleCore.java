package com.reb.dsd_ble.ble.profile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import com.reb.dsd_ble.R;
import com.reb.dsd_ble.ble.profile.utility.BleConfiguration;
import com.reb.dsd_ble.constant.ShareString;
import com.reb.dsd_ble.util.DebugLog;
import com.reb.dsd_ble.util.HexStringConver;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BleCore {
    private static final String TAG = "BleManager";

//	private static final long WRITE_PERIOD = 1000L;

//    private static UUID SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
//    private static UUID CHARACTER_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
//    private static UUID notify_UUID;
//    private static UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    private final static String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
    private final static String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
    //	private final static String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
    private final static String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
//	private final static String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";

    private BleManagerCallbacks mCallbacks;
    private BluetoothGatt mBluetoothGatt;

    private boolean mUserDisConnect;
    private boolean mIsConnected;

    private String address;
    private static BleCore mInstances;

    public static BleCore getInstances() {
        if (mInstances == null) {
            synchronized (BleCore.class) {
                if (mInstances == null){
                    mInstances = new BleCore();
                }
            }
        }
        return mInstances;
    }

    public BleCore() {
//        String head = ctx.getString(R.string.uuidHead);
//        String end = ctx.getString(R.string.uuidEnd);
//        SharedPreferences mShare = ctx.getSharedPreferences(ShareString.SHARE_NAME, Context.MODE_PRIVATE);
//        String uuidStr = mShare.getString(ShareString.SAVE_SERVICE_UUID, ctx.getString(R.string.UUIDservice));
//        SERVICE_UUID = UUID.fromString(head + uuidStr + end);
//        String chaUUIDStr = mShare.getString(ShareString.SAVE_CHARACT_UUID, ctx.getString(R.string.UUIDwrite));
//        CHARACTER_UUID = UUID.fromString(head + chaUUIDStr + end);
//        String notifyUUIDStr = mShare.getString(ShareString.SAVE_NOTIFY_UUID, ctx.getString(R.string.UUIDnotify));
//        notify_UUID = UUID.fromString(head + notifyUUIDStr + end);
    }


    public boolean connect(Context context, BluetoothDevice device) {
        Log.i(TAG, "connect--" + device.getAddress());
        if (isConnected()) {
            if (device.getAddress().equals(address)) {
                Log.d(TAG, "是同一个设备，忽略掉");
                if (mIsConnected) {
                    mCallbacks.onDeviceConnected();
                }
                return true;
            } else {
                Log.d(TAG, "不同设备，先断开");
                disConnect(false);
                return true;
            }
        }
        address = device.getAddress();
        if (mBluetoothGatt == null) {
            mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
            return true;
        } else {
            return mBluetoothGatt.connect();
        }
    }

    public boolean connect(Context context, String address) {
        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            final BluetoothAdapter adapter = bluetoothManager.getAdapter();
            if (adapter != null) {
                BluetoothDevice device = adapter.getRemoteDevice(address);
                return connect(context, device);
            }
        }
        return false;
    }

    ;

    public void disconnect() {
        disConnect(true);

    }

    private void disConnect(boolean userDisconnect) {
        mUserDisConnect = userDisconnect;
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

    public void setGattCallbacks(final BleManagerCallbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public void closeBluetoothGatt() {
        BleConfiguration.mDataSend.resetAllState();
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        mIsConnected = false;
    }


    /**
     * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving indication, etc
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Device connected");
                    mBluetoothGatt.discoverServices();
                    //This will send callback to RSCActivity when device get connected
                    mCallbacks.onDeviceConnected();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Device disconnected");
                    if (mUserDisConnect) {
                        mCallbacks.onDeviceDisconnected();
                    } else {
                        mCallbacks.onLinklossOccur(address);
                        mUserDisConnect = false;
                    }
                    closeBluetoothGatt();
                }
            } else {
                onError(ERROR_CONNECTION_STATE_CHANGE, status);
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                final List<BluetoothGattService> services = gatt.getServices();
                BluetoothGattCharacteristic mCharacteristic = null;
                for (BluetoothGattService service : services) {
                    if (service.getUuid().equals(BleConfiguration.SERVICE_BLE_SERVICE2)) {
                        mCharacteristic = service.getCharacteristic(BleConfiguration.WRITE_LONG_DATA_CHARACTERISTIC2);
                        Log.i(TAG, "service is found" + "------" + mCharacteristic);
                    }
                }
                if (mCharacteristic == null) {
                    mCallbacks.onDeviceNotSupported();
                    gatt.disconnect();
                } else {
                    mCallbacks.onServicesDiscovered();

                    // We have discovered services, let's start notifications and indications, one by one: battery, csc measurement
                    enableNotification(gatt);
                }
            } else {
                onError(ERROR_DISCOVERY_SERVICE, status);
            }
        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            Log.e(TAG, "onCharacteristicRead---->");
        }

        @Override
        public void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            Log.e(TAG, "onDescriptorWrite---->" + Arrays.toString(descriptor.getValue()));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (descriptor.getUuid().equals(BleConfiguration.NOTIFY_DESCRIPTOR)) {
                    byte[] value = descriptor.getValue();
                    if (value[0] == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE[0]) {
                        mCallbacks.onNotifyEnable();
                        mIsConnected = true;
                    }
                }
            } else {
                onError(ERROR_WRITE_DESCRIPTOR, status);
            }
        }

        ;

        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged---->");
            byte[] value = characteristic.getValue();
//			String valueArr = Util.bytes2HexStr(value);
//			Log.e(TAG, "reciver:" + valueArr);
//			DataPraser.praser(mCallbacks, value);

            mCallbacks.onRecive(value);
        }

        ;

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicWrite---->" + characteristic.getUuid().equals(BleConfiguration.WRITE_LONG_DATA_CHARACTERISTIC2) + "******" + (mCallbacks != null) + "*****" + characteristic.getUuid().equals(BleConfiguration.WRITE_LONG_DATA_CHARACTERISTIC2));
            if (characteristic.getUuid().equals(BleConfiguration.WRITE_LONG_DATA_CHARACTERISTIC2)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    byte[] value = characteristic.getValue();
                    DebugLog.i("write:" + HexStringConver.bytes2HexStr(value));
                    BleConfiguration.mDataSend.updateCmdState(DataSend.STATE_DATA_SEND_IDLE);
                    if (BleConfiguration.mDataSend.isLastChunk()) {
                        BleConfiguration.mDataSend.sendNextCmd(false);
                    }
                } else {
                    BleConfiguration.mDataSend.updateCmdState(DataSend.STATE_DATA_SEND_IDLE);
                    if (BleConfiguration.mDataSend.isLastChunk()) {
                        BleConfiguration.mDataSend.sendNextCmd(true);
                    } else {
                        // 重新发送该条消息(中断分包发送，发送下一条消息)
                        BleConfiguration.mDataSend.updateCmdState(DataSend.STATE_DATA_SEND_ERROR);
                    }
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            DebugLog.i("rssi:" + rssi);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mCallbacks.onReadRssi(rssi);
            } else {
                mBluetoothGatt.readRemoteRssi();
            }
        }
    };

    /**
     * Enabling notification on Characteristic
     */
    private void enableNotification(final BluetoothGatt gatt) {
        BluetoothGattCharacteristic notifyCha = mBluetoothGatt.getService(BleConfiguration.SERVICE_BLE_SERVICE2).getCharacteristic(BleConfiguration.NOTIFY_LONG_DATA_CHARACTERISTIC1);
        gatt.setCharacteristicNotification(notifyCha, true);
        final BluetoothGattDescriptor descriptor = notifyCha.getDescriptor(BleConfiguration.NOTIFY_DESCRIPTOR);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    protected void onError(String errorWriteDescriptor, int status) {
        disConnect(false);
    }

    public boolean readRssi() {
        if (mIsConnected && mBluetoothGatt != null) {
            return mBluetoothGatt.readRemoteRssi();
        }
        return false;
    }

    public boolean sendData(byte[] cmd) {
        if (mIsConnected) {
            BleConfiguration.mDataSend.sendData(mBluetoothGatt, cmd);
            return true;
        }
        return false;
    }

//
//    public boolean write(byte[] data) {
//        if (mCharacteristic != null) {
//            mCharacteristic.setValue(data);
//            if (mBluetoothGatt.writeCharacteristic(mCharacteristic)) {
//                mLastWriteData = data;
//                return true;
//            }
//        }
//        return false;
//    }

}
