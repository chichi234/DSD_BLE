package com.reb.dsd_ble.ble.profile.utility;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.SharedPreferences;

import com.reb.dsd_ble.ble.profile.DataSend;
import com.reb.dsd_ble.constant.ShareString;
import com.reb.dsd_ble.ui.DsdApplication;

import java.util.UUID;

/**
 * TODO(BLE): this is an util used to configure some unChangeable deviceInfo through the whole application , which 
 * is depending on the level of OEM customizability requirement . It asks developers to definite it 
 * according to different aim-devices at the beginning of this project .
 */
public class BleConfiguration {
	//Used to configure the time of scanning
	public static final int SCAN_PERIOD = 15000;

	/**
	 *@category part one:scan part.
	 *
	 *configure some scan parameters here.
	 */
	// this unique UUID used to filter bluetooth devices when scanning so that we can get the aim-devices list.
	//It is provided by deviceDevelopers.
//	public static final UUID SERVICE_UUID_OF_SCAN_FILTER2 = UUID.fromString("0000faa0-0000-1000-8000-00805f9b34fb");
	public static UUID SERVICE_UUID_OF_SCAN_FILTER1= null;//UUID.fromString("0000fea5-0000-1000-8000-00805f9b34fb");

	
	/**
	 *@category part two:some function parameters
	 * part two:some function parameters,like:auto-connect etc.
	 */
	public static final boolean IS_AUTO_CONNECT=false;
	/**
	 * ??????????????
	 */
	public static final int MAX_CONNECTED_DEVICES=3;
	/**
	 *@category part three:communicate part.
	 * configure device relating UUID here.
	 */
	//SERVICE 1 for normal data
//	public static final UUID SERVICE_BLE_SERVICE1= UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb1");
	public static UUID SERVICE_BLE_SERVICE2= null;//UUID.fromString("0000faa0-0000-1000-8000-00805f9b34fb");
//	public static final UUID NOTIFY_SHORT_DATA_CHARACTERISTIC1= UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb2");
//	public static final UUID NOTIFY_SHORT_DATA_CHARACTERISTIC2= UUID.fromString("0000fad1-0000-1000-8000-00805f9b34fb");
//	public static final UUID WRITE_SHORT_DATA_CHARACTERISTIC2= UUID.fromString("0000faa1-0000-1000-8000-00805f9b34fb");
//	public static final int WRITE_SHORT_DATA_CHARACTERISTIC_WRITE_TYPE= BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
	public static UUID NOTIFY_LONG_DATA_CHARACTERISTIC1= null;//UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb3");
//	public static final UUID NOTIFY_LONG_DATA_CHARACTERISTIC2= UUID.fromString("0000fad2-0000-1000-8000-00805f9b35fb");
	public static UUID WRITE_LONG_DATA_CHARACTERISTIC2 = null;//UUID.fromString("0000faa2-0000-1000-8000-00805f9b36fb");
	public static final int WRITE_LONG_DATA_CHARACTERISTIC_WRITE_TYPE= BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;

	public static final UUID NOTIFY_DESCRIPTOR= UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

//	/**
//	 *  create some BleProperties utilities here for particular characteristic of device's specific service.
//	 *  As for device's service,one utility corresponds to one notify characteristic
//	 */
//	public static BleProperties bp1=new BleProperties(SERVICE_BLE_SERVICE2, NOTIFY_SHORT_DATA_CHARACTERISTIC2,
//			NOTIFY_DESCRIPTOR,CharacteristicType.CHARACTERISTIC_TYPE_SHORT);
//
//	public static BleProperties bp2=new BleProperties(SERVICE_BLE_SERVICE2,NOTIFY_LONG_DATA_CHARACTERISTIC2,
//			NOTIFY_DESCRIPTOR,CharacteristicType.CHARACTERISTIC_TYPE_LONG);
//	public static BleProperties bp3=new BleProperties(SERVICE_BLE_SERVICE2,WRITE_SHORT_DATA_CHARACTERISTIC2,
//			null,CharacteristicType.CHARACTERISTIC_TYPE_SHORT);
//	public static BleProperties bp4=new BleProperties(SERVICE_BLE_SERVICE2,WRITE_LONG_DATA_CHARACTERISTIC2,
//			null,CharacteristicType.CHARACTERISTIC_TYPE_LONG);
//	public static BleProperties bp5=new BleProperties(SERVICE_BLE_SERVICE1,NOTIFY_SHORT_DATA_CHARACTERISTIC1,
//			NOTIFY_DESCRIPTOR,CharacteristicType.CHARACTERISTIC_TYPE_LONG);
//	public static BleProperties bp6=new BleProperties(SERVICE_BLE_SERVICE1,NOTIFY_SHORT_DATA_CHARACTERISTIC1,
//			NOTIFY_DESCRIPTOR,CharacteristicType.CHARACTERISTIC_TYPE_LONG);
//
//
//	public static final BleProperties[] bps=new BleProperties[]{bp1,bp2,bp3,bp4,bp5,bp6};
	/**
	 * create some DataSend utilities here for sendData-services of this device.
	 * As for this device,one utility corresponds to one sendData-service.
	 */
	public static DataSend mDataSend= new DataSend(SERVICE_BLE_SERVICE2,
//			WRITE_SHORT_DATA_CHARACTERISTIC2,
//			WRITE_SHORT_DATA_CHARACTERISTIC_WRITE_TYPE,
			WRITE_LONG_DATA_CHARACTERISTIC2,
			WRITE_LONG_DATA_CHARACTERISTIC_WRITE_TYPE);

    public static void init(DsdApplication context) {
		SharedPreferences share = context.getSharedPreferences(ShareString.FILE_NAME, Context.MODE_PRIVATE);
		String scanService = share.getString(ShareString.SCAN_FILTER_SERVICE_UUID, "ffe0");
		String service = share.getString(ShareString.SAVE_SERVICE_UUID, "ffe0");
		String writeCharactor = share.getString(ShareString.SAVE_CHARACT_UUID,"ffe1");
		String notifyCharactor = share.getString(ShareString.SAVE_NOTIFY_UUID,"ffe1");
		String head = "0000";
		String end = "-0000-1000-8000-00805f9b34fb";
		SERVICE_UUID_OF_SCAN_FILTER1 = UUID.fromString(head + scanService + end);
		SERVICE_BLE_SERVICE2 = UUID.fromString(head + service + end);
		WRITE_LONG_DATA_CHARACTERISTIC2 = UUID.fromString(head + writeCharactor + end);
		SERVICE_UUID_OF_SCAN_FILTER1 = UUID.fromString(head + scanService + end);
		NOTIFY_LONG_DATA_CHARACTERISTIC1 = UUID.fromString(head + notifyCharactor + end);

    }
//	public static DataSend mDataSend1 = new DataSend(SERVICE_BLE_SERVICE1,
//			NOTIFY_SHORT_DATA_CHARACTERISTIC1,
//			WRITE_SHORT_DATA_CHARACTERISTIC_WRITE_TYPE,
//			NOTIFY_LONG_DATA_CHARACTERISTIC1,
//			WRITE_LONG_DATA_CHARACTERISTIC_WRITE_TYPE);
//	public static final DataSend[] dataSends=new DataSend[]{mDataSend2,mDataSend1};
	
}

