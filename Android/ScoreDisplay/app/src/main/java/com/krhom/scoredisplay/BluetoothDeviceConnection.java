package com.krhom.scoredisplay;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BluetoothDeviceConnection extends Thread
{
    static final UUID bluetoothUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Set<BluetoothDeviceStatusChangedListener> m_bluetoothDeviceStatusChangedListeners = new HashSet();
    private BluetoothDevice m_device;
    private BluetoothSocket m_socket;
    private InputStream m_inputStream;
    private OutputStream m_outputStream;

    BluetoothDeviceConnection(BluetoothDevice bluetoothDevice)
    {
        m_device = bluetoothDevice;
    }

    public void addBluetoothDeviceStatusChangedListener(BluetoothDeviceStatusChangedListener listener)
    {
        m_bluetoothDeviceStatusChangedListeners.add(listener);
    }

    public void removeBluetoothDeviceStatusChangedListener(BluetoothDeviceStatusChangedListener listener)
    {
        m_bluetoothDeviceStatusChangedListeners.remove(listener);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        try
        {
            setNewStatus(BluetoothDeviceStatus.CONNECTING);

            BluetoothClass bluetoothClass = m_device.getBluetoothClass();
            m_socket = m_device.createInsecureRfcommSocketToServiceRecord(bluetoothUUID);
            m_socket.connect();
            m_inputStream = m_socket.getInputStream();
            m_outputStream = m_socket.getOutputStream();

            setNewStatus(BluetoothDeviceStatus.CONNECTED);
        }
        catch (IOException e)
        {
            setNewStatus(BluetoothDeviceStatus.FAILED);
        }
    }

    private void setNewStatus(BluetoothDeviceStatus newStatus)
    {
        for (BluetoothDeviceStatusChangedListener listener : m_bluetoothDeviceStatusChangedListeners)
        {
            listener.onBluetoothDeviceStatusChanged(m_device, newStatus);
        }
    }
}
