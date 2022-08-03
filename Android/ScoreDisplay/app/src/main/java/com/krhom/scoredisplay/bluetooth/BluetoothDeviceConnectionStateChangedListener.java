package com.krhom.scoredisplay.bluetooth;


import com.polidea.rxandroidble2.RxBleConnection;

/**
 * Listener interface
 */
public interface BluetoothDeviceConnectionStateChangedListener
{
    void onBluetoothDeviceStatusChanged(String macAddress, RxBleConnection.RxBleConnectionState newState);
}
