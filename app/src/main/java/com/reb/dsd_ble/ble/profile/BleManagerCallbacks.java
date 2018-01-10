/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 * 
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package com.reb.dsd_ble.ble.profile;

import android.bluetooth.BluetoothGattCallback;


public interface BleManagerCallbacks {

	/**
	 * Called when the device has been connected. This does not mean that the application may start communication. A service discovery will be handled automatically after this call. Service discovery
	 * may ends up with calling {@link #onServicesDiscovered()} or {@link #onDeviceNotSupported()} if required services have not been found.
	 */
	public void onDeviceConnected();

	/**
	 * Called when the device has disconnected (when the callback returned {@link BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)} with state DISCONNECTED.
	 */
	public void onDeviceDisconnected();

	/**
	 * 
	 */
	public void onServicesDiscovered();

	public void onNotifyEnable();
	
	/**
	 * disconnection not initiated by the user.
	 */
	public void onLinklossOccur();



	/**
	 * Called when service discovery has finished but the main services were not found on the device. This may occur when connecting to bonded device that does not support required services.
	 */
	public void onDeviceNotSupported();
	
	public void onWriteSuccess(byte[] data);

	void onRecive(byte[] data);
	
}
