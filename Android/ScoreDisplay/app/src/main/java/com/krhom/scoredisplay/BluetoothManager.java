package com.krhom.scoredisplay;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class BluetoothManager
{
    /**
     * Singleton
     */
    private static BluetoothManager s_instance;
    public static BluetoothManager getInstance()
    {
        if (s_instance == null)
        {
            s_instance = new BluetoothManager();
        }
        return s_instance;
    }

    /**
     * Main class
     */
    private BluetoothAdapter m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice m_bluetoothDevice;

    public BluetoothAdapter getBluetoothAdapter()
    {
        return m_bluetoothAdapter;
    }

    public BluetoothDevice getBluetoothDevice() { return m_bluetoothDevice; }
    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) { m_bluetoothDevice = bluetoothDevice; }
}
