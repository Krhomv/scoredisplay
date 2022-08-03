package com.krhom.scoredisplay.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class BluetoothManager implements BluetoothDeviceConnectionStateChangedListener
{
    /**
     * Singleton
     */
    private static BluetoothManager s_instance;
    public static BluetoothManager getInstance()
    {
        return s_instance;
    }

    public static void initialise(Context context)
    {
        s_instance = new BluetoothManager();
        s_instance.m_rxBleClient = RxBleClient.create(context);
    }

    @Override
    public void onBluetoothDeviceStatusChanged(String macAddress, RxBleConnection.RxBleConnectionState newState)
    {
        for (BluetoothDeviceConnectionStateChangedListener listener : m_bluetoothDeviceConnectionStateChangedListeners)
        {
            listener.onBluetoothDeviceStatusChanged(macAddress, newState);
        }
    }

    /**
     * Main class
     */
    private BluetoothAdapter m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private RxBleClient m_rxBleClient;

    private Map<String, BluetoothDeviceConnection> m_bluetoothDeviceConnections = new HashMap<>();
    private Set<BluetoothDeviceConnectionStateChangedListener> m_bluetoothDeviceConnectionStateChangedListeners = new HashSet();

    public BluetoothAdapter getBluetoothAdapter()
    {
        return m_bluetoothAdapter;
    }
    public RxBleClient getRxBleClient()
    {
        return m_rxBleClient;
    }

    public void addBluetoothDeviceStatusChangedListener(BluetoothDeviceConnectionStateChangedListener listener)
    {
        m_bluetoothDeviceConnectionStateChangedListeners.add(listener);
    }

    public void removeBluetoothDeviceStatusChangedListener(BluetoothDeviceConnectionStateChangedListener listener)
    {
        m_bluetoothDeviceConnectionStateChangedListeners.remove(listener);
    }

    public void connectToDevice(String macAddress)
    {
        BluetoothDeviceConnection connection = m_bluetoothDeviceConnections.get(macAddress);
        if (connection == null)
        {
            connection = new BluetoothDeviceConnection(macAddress);
            connection.addBluetoothDeviceStatusChangedListener(this);
            m_bluetoothDeviceConnections.put(macAddress, connection);
        }
        connection.connect();
    }

    public void disconnectFromDevice(String macAddress)
    {
        BluetoothDeviceConnection connection = m_bluetoothDeviceConnections.get(macAddress);
        if (connection != null)
        {
            connection.disconnect();
        }
    }

    public RxBleConnection.RxBleConnectionState getBluetoothDeviceConnectionState(String macAddress)
    {
        BluetoothDeviceConnection deviceConnection = m_bluetoothDeviceConnections.get(macAddress);
        return deviceConnection != null ? deviceConnection.getConnectionState() : RxBleConnection.RxBleConnectionState.DISCONNECTED;
    }
}
