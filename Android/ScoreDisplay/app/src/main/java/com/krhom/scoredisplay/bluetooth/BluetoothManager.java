package com.krhom.scoredisplay.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;



public class BluetoothManager implements BluetoothDeviceConnection.StatusChangedListener
{
    public static final UUID SERVICE = UUID.fromString("00000000-6f1e-40f5-909b-8e50c592dca9");
    public static final UUID TEAM1_SCORE_CHARACTERISTIC = UUID.fromString("10000000-6f1e-40f5-909b-8e50c592dca9");
    public static final UUID TEAM1_COLOR_CHARACTERISTIC = UUID.fromString("10000001-6f1e-40f5-909b-8e50c592dca9");
    public static final UUID TEAM1_BRIGHTNESS_CHARACTERISTIC = UUID.fromString("10000002-6f1e-40f5-909b-8e50c592dca9");
    public static final UUID TEAM2_SCORE_CHARACTERISTIC = UUID.fromString("20000000-6f1e-40f5-909b-8e50c592dca9");
    public static final UUID TEAM2_COLOR_CHARACTERISTIC = UUID.fromString("20000001-6f1e-40f5-909b-8e50c592dca9");
    public static final UUID TEAM2_BRIGHTNESS_CHARACTERISTIC = UUID.fromString("20000002-6f1e-40f5-909b-8e50c592dca9");

    public enum Status
    {
        DISCONNECTED,
        SCANNING,
        CONNECTING,
        CONNECTED,
        ERROR,
    }

    public interface StatusChangedListener
    {
        void onBluetoothManagerStatusChanged(Status newStatus);
    }

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
        s_instance = new BluetoothManager(context);
    }

    /**
     * Main class
     */
    private Context m_context;
    private BluetoothAdapter m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private RxBleClient m_rxBleClient;
    private Disposable m_scanDisposable;

    private String m_preferredDeviceMacAddress;
    private Status m_currentStatus = Status.DISCONNECTED;

    private Map<String, BluetoothDeviceConnection> m_bluetoothDeviceConnections = new HashMap<>();
    private Set<BluetoothDeviceConnection.StatusChangedListener> m_bluetoothDeviceConnectionStatusChangedListeners = new HashSet();
    private Set<StatusChangedListener> m_statusChangedListeners = new HashSet<>();

    public BluetoothAdapter getBluetoothAdapter()
    {
        return m_bluetoothAdapter;
    }
    public RxBleClient getRxBleClient()
    {
        return m_rxBleClient;
    }

    BluetoothManager(Context context)
    {
        m_context = context;
        m_rxBleClient = RxBleClient.create(context);
    }

    public Status getStatus()
    {
        return m_currentStatus;
    }

    public void addBluetoothDeviceStatusChangedListener(BluetoothDeviceConnection.StatusChangedListener listener)
    {
        m_bluetoothDeviceConnectionStatusChangedListeners.add(listener);
    }

    public void removeBluetoothDeviceStatusChangedListener(BluetoothDeviceConnection.StatusChangedListener listener)
    {
        m_bluetoothDeviceConnectionStatusChangedListeners.remove(listener);
    }

    public void addStatusChangedListener(StatusChangedListener listener)
    {
        m_statusChangedListeners.add(listener);
    }

    public void removeStatusChangedListener(StatusChangedListener listener)
    {
        m_statusChangedListeners.remove(listener);
    }

    public void startScanAndConnect()
    {
        if (m_preferredDeviceMacAddress != null)
        {
            connectToDevice(m_preferredDeviceMacAddress);
        }
        else
        {
            startScanning();
        }
    }

    public void stopScanAndDisconnect()
    {
        if (m_preferredDeviceMacAddress != null)
        {
            disconnectFromDevice(m_preferredDeviceMacAddress);
        }
        m_scanDisposable.dispose();
        setStatus(Status.DISCONNECTED);
    }

    public void connectToDevice(String macAddress)
    {
        BluetoothDeviceConnection connection = m_bluetoothDeviceConnections.get(macAddress);
        if (connection == null)
        {
            connection = new BluetoothDeviceConnection(m_context, macAddress);
            connection.addStatusChangedListener(this);
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

    private void startScanning()
    {
        setStatus(Status.SCANNING);
        m_scanDisposable = m_rxBleClient.scanBleDevices(
                        new ScanSettings.Builder()
                                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                                .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
                                .build(),
                        new ScanFilter.Builder()
                                .setServiceUuid(new ParcelUuid(BluetoothManager.SERVICE))
                                .build()
                )
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onDeviceFound, this::onScanFailure);
    }

    public void sendTeam1Score(int score)
    {
        for (BluetoothDeviceConnection connection : m_bluetoothDeviceConnections.values())
        {
            if (connection.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED)
            {
                connection.writeCharacteristic(TEAM1_SCORE_CHARACTERISTIC, new byte[]{(byte) score});
            }
        }
    }

    public void sendTeam1Color(int color)
    {
        for (BluetoothDeviceConnection connection : m_bluetoothDeviceConnections.values())
        {
            if (connection.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED)
            {
                byte[] bytes = ByteBuffer.allocate(4).putInt(color).array();
                connection.writeCharacteristic(TEAM1_COLOR_CHARACTERISTIC, bytes);
            }
        }
    }

    public void sendTeam1Brightness(int brightness)
    {
        for (BluetoothDeviceConnection connection : m_bluetoothDeviceConnections.values())
        {
            if (connection.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED)
            {
                connection.writeCharacteristic(TEAM1_BRIGHTNESS_CHARACTERISTIC, new byte[]{(byte) brightness});
            }
        }
    }

    public void sendTeam2Score(int score)
    {
        for (BluetoothDeviceConnection connection : m_bluetoothDeviceConnections.values())
        {
            if (connection.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED)
            {
                connection.writeCharacteristic(TEAM2_SCORE_CHARACTERISTIC, new byte[]{(byte) score});
            }
        }
    }

    public void sendTeam2Color(int color)
    {
        for (BluetoothDeviceConnection connection : m_bluetoothDeviceConnections.values())
        {
            if (connection.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED)
            {
                byte[] bytes = ByteBuffer.allocate(4).putInt(color).array();
                connection.writeCharacteristic(TEAM2_COLOR_CHARACTERISTIC, bytes);
            }
        }
    }

    public void sendTeam2Brightness(int brightness)
    {
        for (BluetoothDeviceConnection connection : m_bluetoothDeviceConnections.values())
        {
            if (connection.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED)
            {
                connection.writeCharacteristic(TEAM2_BRIGHTNESS_CHARACTERISTIC, new byte[]{(byte) brightness});
            }
        }
    }

    private void setStatus(Status newStatus)
    {
        if (newStatus != m_currentStatus)
        {
            m_currentStatus = newStatus;
            for (StatusChangedListener listener : m_statusChangedListeners)
            {
                listener.onBluetoothManagerStatusChanged(newStatus);
            }
        }
    }


    private void onDeviceFound(ScanResult scanResult)
    {
        m_preferredDeviceMacAddress = scanResult.getBleDevice().getMacAddress();
        connectToDevice(m_preferredDeviceMacAddress);
        m_scanDisposable.dispose();
    }

    private void onScanFailure(Throwable throwable)
    {
        setStatus(Status.ERROR);
        Log.w("ScanActivity", "Scan failed", throwable);
    }


    @Override
    public void onBluetoothDeviceConnectionStatusChanged(String macAddress, BluetoothDeviceConnection.Status newStatus)
    {
        switch (newStatus)
        {
            case CONNECTING:
                if (macAddress == m_preferredDeviceMacAddress)
                {
                    setStatus(Status.CONNECTING);
                }
                break;
            case CONNECTED:
                if (macAddress == m_preferredDeviceMacAddress)
                {
                    setStatus(Status.CONNECTED);
                }
                break;
            case DISCONNECTED:
                if (macAddress == m_preferredDeviceMacAddress)
                {
                    setStatus(Status.DISCONNECTED);
                }

                break;
            case FAILED:
                if (macAddress == m_preferredDeviceMacAddress)
                {
                    setStatus(Status.ERROR);
                    m_preferredDeviceMacAddress = null;

                    startScanning();
                }
                break;
        }
    }
}
