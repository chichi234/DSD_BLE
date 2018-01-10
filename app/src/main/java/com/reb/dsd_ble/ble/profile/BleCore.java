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
import com.reb.dsd_ble.constant.ShareString;
import com.reb.dsd_ble.util.HexStringConver;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BleCore{
	private static final String TAG = "BleManager";
	
//	private static final long WRITE_PERIOD = 1000L;

	public static  UUID SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
	public static  UUID CHARACTER_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
	public static UUID notify_UUID;
	public static  UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	
	
	
	private final static String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
	private final static String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
//	private final static String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
	private final static String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
//	private final static String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";
	
	private BluetoothGattCharacteristic mCharacteristic;
	private BleManagerCallbacks mCallbacks;
	private BluetoothGatt mBluetoothGatt;
	private Context mContext;
	
//	private Boolean mWriteBusy = false;
	private byte[] mLastWriteData;
//	private int mWriteCount = 0;
	private boolean mUserDisConnect;
	private boolean mIsConnected;
	
	private String address;
	
	public BleCore(Context ctx){
		String head = ctx.getString(R.string.uuidHead);
		String end = ctx.getString(R.string.uuidEnd);
		SharedPreferences mShare = ctx.getSharedPreferences(ShareString.SHARE_NAME, Context.MODE_PRIVATE);
		String uuidStr = mShare.getString(ShareString.SAVE_SERVICE_UUID, ctx.getString(R.string.UUIDservice));
		SERVICE_UUID = UUID.fromString(head + uuidStr + end);
		String chaUUIDStr = mShare.getString(ShareString.SAVE_CHARACT_UUID, ctx.getString(R.string.UUIDwrite));
		CHARACTER_UUID = UUID.fromString(head + chaUUIDStr + end);
		String notifyUUIDStr = mShare.getString(ShareString.SAVE_NOTIFY_UUID, ctx.getString(R.string.UUIDnotify));
		notify_UUID = UUID.fromString(head + notifyUUIDStr + end);
	}
	

	public boolean connect(Context context, BluetoothDevice device) {
		Log.i(TAG, "connect--" + device.getAddress());
		this.mContext = context;
		if(isConnected()){
			if(device.getAddress().equals(address)){
				Log.d(TAG, "是同一个设备，忽略掉");
				return true;
			}else{
				Log.d(TAG, "不同设备，先断开");
				disConnect(false);
				return true;
			}
		}
		address = device.getAddress();
		if (mBluetoothGatt == null) {
			mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
			return true;
		} else {
			return mBluetoothGatt.connect();
		}
	}
	
	public boolean connect(Context context, String address) {
		if(BluetoothAdapter.checkBluetoothAddress(address)){
			final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
			final BluetoothAdapter adapter = bluetoothManager.getAdapter();
			if(adapter != null){
				BluetoothDevice device = adapter.getRemoteDevice(address);
				if(device != null){
					return connect(context, device);
				}
			}
		}
		return false;
	};

	public void disconnect() {
		disConnect(true);
		
	}
	
	private void disConnect(boolean userDisconnect){
		mUserDisConnect = userDisconnect;
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
		}
	}

	public void setGattCallbacks(final BleManagerCallbacks callbacks) {
		this.mCallbacks = callbacks;
	}
	
	public boolean isConnected(){
		return mIsConnected;
	}

	public void closeBluetoothGatt() {
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
//		writeEnd(false);
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
					if(mUserDisConnect){
						mCallbacks.onDeviceDisconnected();
					}else{
						mCallbacks.onLinklossOccur();
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
				for (BluetoothGattService service : services) {
					if (service.getUuid().equals(SERVICE_UUID)) {
						mCharacteristic = service.getCharacteristic(CHARACTER_UUID);
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
				if(descriptor.getUuid() .equals( CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID)){
					byte[] value = descriptor.getValue();
					if(value[0] == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE[0]){
						mCallbacks.onNotifyEnable();
						mIsConnected = true;
					}
				}
			} else {
				onError(ERROR_WRITE_DESCRIPTOR, status);
			}
		};

		@Override
		public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			Log.d(TAG, "onCharacteristicChanged---->");
			byte[] value = characteristic.getValue();
//			String valueArr = Util.bytes2HexStr(value);
//			Log.e(TAG, "reciver:" + valueArr);
//			DataPraser.praser(mCallbacks, value);

			mCallbacks.onRecive(value);
		};
		
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			Log.d(TAG, "onCharacteristicWrite---->" + characteristic.getUuid().equals(CHARACTER_UUID) + "******" + (mCallbacks != null) + "*****" + characteristic.getUuid().equals(CHARACTER_UUID));
			if(status == BluetoothGatt.GATT_SUCCESS){
				byte[] value = characteristic.getValue();
				String valueStr = HexStringConver.bytes2HexStr(value);
				Log.e(TAG, "write:" + valueStr);
				if(characteristic.getUuid().equals(CHARACTER_UUID)){
					if(Arrays.equals(value, mLastWriteData)){
//						writeEnd(true);
						if(mCallbacks != null){
							Log.e(TAG, "write:" + valueStr);
							mCallbacks.onWriteSuccess(mLastWriteData);
						}
					}
				}
			}
		};
	};
	
	/**
	 * Enabling notification on Characteristic
	 */
	private void enableNotification(final BluetoothGatt gatt) {
		BluetoothGattCharacteristic notifyCha = mBluetoothGatt.getService(SERVICE_UUID).getCharacteristic(notify_UUID);
		gatt.setCharacteristicNotification(notifyCha, true);
		final BluetoothGattDescriptor descriptor = notifyCha.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		gatt.writeDescriptor(descriptor);
	}

	protected void onError(String errorWriteDescriptor, int status) {
		disConnect(false);
	}
	
	
	public boolean write(byte[] data){
		if(mCharacteristic != null ){
			mCharacteristic.setValue(data);
			if (mBluetoothGatt.writeCharacteristic(mCharacteristic)){
				mLastWriteData = data;
				return true;
			}
		}
		return false;
	}
	
	//去除重发机制

//	public boolean write(final byte[] data) {
//		if(mWriteTimer == null){
//			mWriteTimer = new Timer();
//		}
//		synchronized (mWriteBusy) {
//			mWriteTimer.schedule(new TimerTask() {
//				
//				@Override
//				public void run() {
//					Log.i(TAG, "mWriteCount = " + mWriteCount);
//					if(mWriteCount < 3){
//						mWriteCount++;
//						if(mCharacteristic != null && (!mWriteBusy || data.equals(mLastWriteData)) ){
//							mCharacteristic.setValue(data);
//							if(mBluetoothGatt.writeCharacteristic(mCharacteristic)){
//								mWriteBusy = true;
//								mLastWriteData = data;
//							}
//						}
//					}else{
//						writeEnd(false);
//					}
//				}
//			}, 0, WRITE_PERIOD);
//		}
//		return false;
//	}
//	
//	private void writeEnd(boolean success){
//		Log.i(TAG, "mWriteTimer = " + mWriteTimer);
//		if(mWriteTimer != null){
//			mWriteTimer.cancel();
//			mWriteTimer = null;
//		}
//		if(mLastWriteData != null && success){
//			mCallbacks.onWriteSuccess(mLastWriteData);
//		}
//		mWriteBusy = false;
//		mLastWriteData = null;
//		mWriteCount = 0;
//	}
//	
//	
//	public Timer mWriteTimer;
	
	
}
