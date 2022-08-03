package com.krhom.scoredisplay;


import android.bluetooth.BluetoothDevice;

/**
 * Listener interface
 */
public interface BluetoothDeviceStatusChangedListener
{
    void onBluetoothDeviceStatusChanged(BluetoothDevice bluetoothDevice, BluetoothDeviceStatus newStatus);
}
