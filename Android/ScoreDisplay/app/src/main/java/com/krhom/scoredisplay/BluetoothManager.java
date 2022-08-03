package com.krhom.scoredisplay;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class BluetoothManager implements BluetoothDeviceStatusChangedListener
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

    @Override
    public void onBluetoothDeviceStatusChanged(BluetoothDevice bluetoothDevice, BluetoothDeviceStatus newStatus)
    {
        BluetoothDeviceState deviceState = m_bluetoothDeviceStates.get(bluetoothDevice);
        if (deviceState != null)
        {
            deviceState.status = newStatus;
        }

        for (BluetoothDeviceStatusChangedListener listener : m_bluetoothDeviceStatusChangedListeners)
        {
            listener.onBluetoothDeviceStatusChanged(bluetoothDevice, newStatus);
        }
    }


    /**
     * Connection State
     */
    public class BluetoothDeviceState
    {
        public BluetoothDeviceConnection connection;
        public BluetoothDeviceStatus status;
    }

    /**
     * Main class
     */
    private BluetoothAdapter m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice m_bluetoothDevice;

    private Map<BluetoothDevice, BluetoothDeviceState> m_bluetoothDeviceStates = new HashMap<>();
    private Set<BluetoothDeviceStatusChangedListener> m_bluetoothDeviceStatusChangedListeners = new HashSet();

    public BluetoothAdapter getBluetoothAdapter()
    {
        return m_bluetoothAdapter;
    }

    public void addBluetoothDeviceStatusChangedListener(BluetoothDeviceStatusChangedListener listener)
    {
        m_bluetoothDeviceStatusChangedListeners.add(listener);
    }

    public void removeBluetoothDeviceStatusChangedListener(BluetoothDeviceStatusChangedListener listener)
    {
        m_bluetoothDeviceStatusChangedListeners.remove(listener);
    }

    public void startConnectionToDevice(BluetoothDevice bluetoothDevice)
    {
        BluetoothDeviceState deviceState = m_bluetoothDeviceStates.get(bluetoothDevice);
        if (deviceState == null)
        {
            BluetoothDeviceState newState = new BluetoothDeviceState();
            newState.status = BluetoothDeviceStatus.DISCONNECTED;
            newState.connection = new BluetoothDeviceConnection(bluetoothDevice);
            newState.connection.addBluetoothDeviceStatusChangedListener(this);

            m_bluetoothDeviceStates.put(bluetoothDevice, newState);

            newState.connection.start();
        }
    }

    public BluetoothDeviceStatus getBluetootDeviceStatus(BluetoothDevice bluetoothDevice)
    {
        BluetoothDeviceState deviceState = m_bluetoothDeviceStates.get(bluetoothDevice);
        return deviceState != null ? deviceState.status : BluetoothDeviceStatus.DISCONNECTED;
    }
}
