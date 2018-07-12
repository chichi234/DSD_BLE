package com.reb.dsd_ble.ble.profile;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.reb.dsd_ble.ble.profile.task.AbTaskItem;
import com.reb.dsd_ble.ble.profile.task.AbTaskListener;
import com.reb.dsd_ble.ble.profile.task.AbTaskQueue;
import com.reb.dsd_ble.ble.profile.utility.BleDataTypeUtils;
import com.reb.dsd_ble.util.DebugLog;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class DataSend {
    private final static String TAG = "DataSend";

    public static final int STATE_DATA_SEND_IDLE = 0;
    private final int STATE_DATA_SENDING = 1;
    private final int STATE_DATA_SEND_RESPONSE_ACK = 2;
    private final int STATE_DATA_SEND_RESPONSE_NACK = 3;
    private final int STATE_DATA_SEND_TIMOUT = 4;
    private final int STATE_LONG_DATA_SEND_IDLE = 5;
    private final int STATE_LONG_DATA_SENDING = 6;
    public static final int STATE_DATA_SEND_ERROR = 7;

    private static final int chunkSize = 20;
    private int totalChunk;
    private boolean isLastChunk = false;
    private boolean is_long_data = false;
    private int mServiceState = STATE_DATA_SEND_IDLE;
    private Object mSendThreadLock = new Object();
    private Object object = new Object();
    private List<AbTaskItem> mTaskItems = new LinkedList<AbTaskItem>();
    private final AbTaskQueue mAbTaskQueue = AbTaskQueue.getInstance();

    private BluetoothGatt mGatt = null;

    //    public String mDeviceAddressOfThis=null;
    public UUID mServiceUuid = null;
    //    private UUID mShortDataCharacteristic = null;
//    private int mShortDataCharacteristicWriteType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
    private UUID mLongDataCharacteristic = null;
    //    private int mLongDataCharacteristicWriteType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
    private int mLongDataCharacteristicWriteType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;

    public DataSend(UUID mServiceUuid, /*UUID mShortDataCharacteristic,
                    int mShortDataCharacteristicWriteType,*/
                    UUID mLongDataCharacteristic, int mLongDataCharacteristicWriteType) {
        super();
        this.mServiceUuid = mServiceUuid;
//		this.mShortDataCharacteristic = mShortDataCharacteristic;
//		this.mShortDataCharacteristicWriteType = mShortDataCharacteristicWriteType;
        this.mLongDataCharacteristic = mLongDataCharacteristic;
        this.mLongDataCharacteristicWriteType = mLongDataCharacteristicWriteType;
    }

    public void sendData(final BluetoothGatt gatt, final byte[] cmd) {
//        DebugLog.i("sendData cmd :"+cmd+",gatt :"+gatt);
        if (cmd == null || gatt == null || cmd.length == 0) {
            DebugLog.i("sendData cmd error");
            return;
        }

        mGatt = gatt;
        final AbTaskItem item = new AbTaskItem();
        item.setListener(new AbTaskListener() {

            @Override
            public void update() {
            }

            @Override
            public void get() {
//                DebugLog.i("BleManager send pre:" + BleDataTypeUtils.bytesToHexString(cmd));
                startSendLongThread(cmd);
            }
        });

        synchronized (object) {
            DebugLog.i("mTaskItems.size:" + mTaskItems.size());
            if (mTaskItems.size() == 0) {
                mTaskItems.add(item);
                mAbTaskQueue.execute(mTaskItems.get(0));
            } else {
                mTaskItems.add(item);
            }
        }
    }


    /**
     *
     */
    private void sendCmd2Ble(final byte[] cmd) {
//        DebugLog.i("sendCmd2Ble start");
        synchronized (mSendThreadLock) {
            BluetoothGattCharacteristic characteristic = null;
            try {
                BluetoothGatt bg = mGatt;
                if (bg == null) {
                    DebugLog.i("BluetoothGatt is null");
                    updateCmdState(STATE_DATA_SEND_IDLE);
                    return;
                }

                BluetoothGattService bgs = bg.getService(mServiceUuid);
                if (bgs == null) {
                    DebugLog.i("GattAttributes.SMALLRADAR_BLE_SERVICE is null");
                    updateCmdState(STATE_DATA_SEND_IDLE);
                    return;
                }

                mServiceState = STATE_DATA_SENDING;
                characteristic = bgs.getCharacteristic(mLongDataCharacteristic);
                if (characteristic == null) {
                    updateCmdState(STATE_DATA_SEND_IDLE);
                    DebugLog.i("GattAttributes.SMALLRADAR_BLE_SERVICE_WRITE_NOTIFY_CHARACTERISTIC is null,mLongDataCharacteristic:" + mLongDataCharacteristic);
                    return;
                }
                characteristic.setWriteType(mLongDataCharacteristicWriteType);
//                }
                characteristic.setValue(cmd);
                boolean writeResult = bg.writeCharacteristic(characteristic);
                DebugLog.i("write  result: " + writeResult + ",cmd: " + BleDataTypeUtils.bytesToHexString(cmd));
            } catch (Exception e) {
                DebugLog.i("write failed,characteristic is  not exist ");
                updateCmdState(STATE_DATA_SEND_IDLE);
            }
        }
//        DebugLog.i("sendCmd2Ble end");
    }

//    /**
//     *
//     * @Title: startSendThread
//     * @Description:
//     * @param @param cmd
//     * @return void
//     * @exception/throws description
//     */
//    private void startSendThread(final byte[] cmd) {
//        ThreadPool pool = ThreadPool.getInstance();
//        if (pool != null) {
//            sendCmd2Ble(cmd);
//        }
//    }

    /**
     * @param @param cmd
     * @return void
     * @Title: startSendLongDataThread
     * @Description: 发送长数据线程
     * @exception/throws description
     */
    private void startSendLongThread(final byte[] cmd) {

        if ((cmd.length % chunkSize) > 0) {
            totalChunk = (cmd.length / chunkSize) + 1;
        } else {
            totalChunk = (cmd.length / chunkSize);
        }
        isLastChunk = false;
        is_long_data = true;
        int ChunkNumble = 0;
        while (!isLastChunk) {
//            DebugLog.i("发送第" + ChunkNumble + "个包, state:" + mServiceState);
            switch (mServiceState) {
                case STATE_DATA_SENDING:
                    continue;
                case STATE_DATA_SEND_ERROR:
                    // 报错了，重新发送该条消息
                    updateCmdState(STATE_DATA_SEND_IDLE);
                    sendNextCmd(true);
                    return;
            }
            DebugLog.i("发送第" + ChunkNumble + "个包");
            if ((totalChunk > (ChunkNumble + 1))) {
                isLastChunk = false;
                sendCmd2Ble(BleDataTypeUtils.bytesCut(cmd,
                        ChunkNumble * chunkSize, chunkSize));
                ChunkNumble++;

            } else {
                isLastChunk = true;
                if (cmd.length % chunkSize > 0) {
                    sendCmd2Ble(BleDataTypeUtils.bytesCut(cmd, ChunkNumble
                            * chunkSize, cmd.length % chunkSize));
                } else {
                    sendCmd2Ble(BleDataTypeUtils.bytesCut(cmd, ChunkNumble
                            * chunkSize, chunkSize));
                }
                ChunkNumble++;
                DebugLog.i("发送结束");
            }
        }
//        sendNextCmd(false);
    }

    public boolean isLastChunk() {
        return isLastChunk;
    }

//    private void sendDataThread(final byte[] cmd) {
//        // TODO Auto-generated method stub
//
//        final AbTaskItem item = new AbTaskItem();
//        item.setListener(new AbTaskListener() {
//
//            @Override
//            public void update() {
//            }
//            @Override
//            public void get() {
//                if (cmd[0] == (byte) TclProtocol.SEND_LACK_DATA_APP_TO_DEV) {
//                    startSendThread(cmd);
//                } else if (cmd[0] == (byte) TclProtocol.SEND_LONG_DATA_APP_TO_DEV) {
////					startSendLongThread(cmd);
//                }
//            }
//        });
//
//        synchronized (object) {
//            if (mTaskItems.size() == 0) {
//                addTaskItems(item);
//                mAbTaskQueue.execute(mTaskItems.get(0));
//            } else {
//                mTaskItems.add(item);
//            }
//        }
//    }

    /**
     *
     */
    private void addTaskItems(AbTaskItem item) {
        // TODO Auto-generated method stub
        synchronized (object) {
            mTaskItems.add(item);
        }
    }

    /**
     *
     */
    private void removeTaskItems(int item) {
        // TODO Auto-generated method stub
        synchronized (object) {
            mTaskItems.remove(item);
        }
    }

    /**
     *
     */
    public void clearTaskItems() {
        // TODO Auto-generated method stub
        synchronized (object) {
            if (mTaskItems != null) {
                mTaskItems.clear();
            }
        }
    }

    public void updateCmdState(int newState) {
        mServiceState = newState;
    }

    public void resetAllState() {
        isLastChunk = true;
        mServiceState = STATE_DATA_SEND_IDLE;
        clearTaskItems();
    }

    /**
     *
     */
    public void sendNextCmd(boolean sendfail) {
        // TODO Auto-generated method stub
        Log.i(TAG, "BleManager sendNextCmd");
        if (mTaskItems.size() > 0) {
            if (!sendfail) {
                Log.i(TAG, "BleManager sendNextCmd,remove");
                removeTaskItems(0);
            }
            if (mTaskItems.size() > 0) {
                Log.i(TAG, "BleManager sendNextCmd,execute");
                mAbTaskQueue.execute(mTaskItems.get(0));
            }

        }
    }
}
