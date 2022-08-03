package com.krhom.scoredisplay.bluetooth;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class BluetoothDeviceConnection
{
    private RxBleClient m_rxBleClient;
    private RxBleDevice m_bleDevice;
    private Disposable m_connectionDisposable;
    private Disposable m_stateDisposable;
    private Set<BluetoothDeviceConnectionStateChangedListener> m_bluetoothDeviceConnectionStateChangedListeners = new HashSet();

    public void addBluetoothDeviceStatusChangedListener(BluetoothDeviceConnectionStateChangedListener listener)
    {
        m_bluetoothDeviceConnectionStateChangedListeners.add(listener);
    }

    public void removeBluetoothDeviceStatusChangedListener(BluetoothDeviceConnectionStateChangedListener listener)
    {
        m_bluetoothDeviceConnectionStateChangedListeners.remove(listener);
    }

    BluetoothDeviceConnection(String macAddress)
    {
        m_rxBleClient = BluetoothManager.getInstance().getRxBleClient();
        m_bleDevice = m_rxBleClient.getBleDevice(macAddress);
        // How to listen for connection state changes
        m_stateDisposable = m_bleDevice.observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionStateChange);
    }

    public void connect()
    {
        m_connectionDisposable = m_bleDevice.establishConnection(true)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionReceived, this::onConnectionFailure);
    }

    public void disconnect() {

        if (m_connectionDisposable != null) {
            m_connectionDisposable.dispose();
        }
    }

    public RxBleConnection.RxBleConnectionState getConnectionState()
    {
        return m_bleDevice.getConnectionState();
    }

    public boolean isConnected() {
        return m_bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private void onConnectionFailure(Throwable throwable)
    {
    }

    @SuppressWarnings("unused")
    private void onConnectionReceived(RxBleConnection connection)
    {
    }

    private void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState)
    {
        for (BluetoothDeviceConnectionStateChangedListener listener : m_bluetoothDeviceConnectionStateChangedListeners)
        {
            listener.onBluetoothDeviceStatusChanged(m_bleDevice.getMacAddress(), newState);
        }
    }

}
