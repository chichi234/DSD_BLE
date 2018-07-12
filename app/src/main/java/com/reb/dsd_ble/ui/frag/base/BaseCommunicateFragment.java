package com.reb.dsd_ble.ui.frag.base;

/**
 * File description
 *
 * @author Reb
 * @version 1.0
 * @date 2018-7-7 17:00
 * @package_name com.reb.dsd_ble.ui.frag.communicate
 * @project_name DSD_BLE
 * @history At 2018-7-7 17:00 created by Reb
 */

public abstract class BaseCommunicateFragment extends BaseFragment {

    public abstract void onDeviceConnect();

    public abstract void onDeviceDisConnect();

    public abstract void onWriteSuccess(byte[] data, boolean success);

    public abstract void receive(byte[] data);


}
