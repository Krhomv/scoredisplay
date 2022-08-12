package com.krhom.scoredisplay.bluetooth;

import android.content.Context;
import android.util.Log;

import com.jakewharton.rx.ReplayingShare;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.helpers.ValueInterpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class BluetoothDeviceConnection
{
    private Context m_context;
    private String m_macAddress;
    private RxBleClient m_rxBleClient;
    private RxBleDevice m_bleDevice;
    private Observable<RxBleConnection> m_connectionObservable;
    private final CompositeDisposable m_compositeDisposable = new CompositeDisposable();
    private PublishSubject<Boolean> m_disconnectTriggerSubject = PublishSubject.create();
    private Set<StatusChangedListener> m_statusChangedListeners = new HashSet();

    private Status m_currentStatus = Status.DISCONNECTED;

    public enum Status
    {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        FAILED,
    }

    public interface StatusChangedListener
    {
        void onBluetoothDeviceConnectionStatusChanged(String macAddress, Status newStatus);
    }

    public void addStatusChangedListener(StatusChangedListener listener)
    {
        m_statusChangedListeners.add(listener);
    }

    public void removeStatusChangedListener(StatusChangedListener listener)
    {
        m_statusChangedListeners.remove(listener);
    }

    BluetoothDeviceConnection(Context context, String macAddress)
    {
        m_context = context;
        m_macAddress = macAddress;
        m_rxBleClient = BluetoothManager.getInstance().getRxBleClient();
        m_bleDevice = m_rxBleClient.getBleDevice(macAddress);

        // Prepare the connection observable
        m_connectionObservable = m_bleDevice.establishConnection(true)
            .takeUntil(m_disconnectTriggerSubject)
            .compose(ReplayingShare.instance());

        // How to listen for connection state changes
        Disposable connectionChangeDisposable = m_bleDevice.observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionStateChange);
        m_compositeDisposable.add(connectionChangeDisposable);
    }

    public void connect()
    {
        final Disposable connectionDisposable = m_connectionObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionReceived, this::onConnectionFailure);

        m_compositeDisposable.add(connectionDisposable);
    }

    public void disconnect()
    {
        m_disconnectTriggerSubject.onNext(true);
    }

    public void dispose()
    {
        m_compositeDisposable.dispose();
    }

    public RxBleConnection.RxBleConnectionState getConnectionState()
    {
        return m_bleDevice.getConnectionState();
    }

    public void writeCharacteristic(UUID characteristicUuid, byte[] data)
    {
        final Disposable sendDisposable = m_connectionObservable
                .firstOrError()
                .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(characteristicUuid, data))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        bytes -> onWriteSuccess(characteristicUuid, data),
                        this::onWriteFailure
                );
    }

    public void readCharacteristics(UUID[] characteristics)
    {
        final Disposable sendDisposable = m_connectionObservable
                .firstOrError()
                .flatMap(rxBleConnection ->
                {
                    List<Single<byte[]>> observables = new ArrayList<>();
                    for (UUID characteristic : characteristics)
                    {
                        observables.add(rxBleConnection.readCharacteristic(characteristic));
                    }
                    return Single.zip(observables, args ->
                    {
                        Map<UUID, byte[]> resultMap = new HashMap<>();

                        int characteristicIndex = 0;
                        for (Object o : args)
                        {
                            resultMap.put(characteristics[characteristicIndex++], (byte[])o);
                        }
                        return resultMap;
                    });
                }).subscribe(
                        resultMap ->
                        {
                            onReadSuccess(resultMap);
                        },
                        throwable ->
                        {
                            onReadFailure(throwable);
                        }
                );
    }

    private void setStatus(Status newStatus)
    {
        if (newStatus != m_currentStatus)
        {
            m_currentStatus = newStatus;
            for (StatusChangedListener listener : m_statusChangedListeners)
            {
                listener.onBluetoothDeviceConnectionStatusChanged(m_macAddress, newStatus);
            }
        }
    }

    private void onConnectionReceived(RxBleConnection connection)
    {
    }

    private void onConnectionFailure(Throwable throwable)
    {
        setStatus(Status.FAILED);
    }

    private void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState)
    {
        switch (newState)
        {
            case CONNECTING:
                setStatus(Status.CONNECTING);
                break;
            case CONNECTED:
                setStatus(Status.CONNECTED);
                break;
            case DISCONNECTED:
                setStatus(Status.DISCONNECTED);
                break;
            case DISCONNECTING:
                break;
        }

    }

    private void onWriteSuccess(UUID characteristicUuid, byte[] data)
    {
        Log.d("BT",  "Successfully wrote characteristic " + characteristicUuid + ": " + data);
    }

    private void onWriteFailure(Throwable throwable)
    {
        Log.d("BT", "Write error: " + throwable);
    }

    private void onReadSuccess(Map<UUID, byte[]> resultMap)
    {
        for (Map.Entry<UUID, byte[]> mapEntry : resultMap.entrySet())
        {
            byte[] result = mapEntry.getValue();
            Log.d("BT",  "Successfully read characteristic " + mapEntry.getKey() + ": " + Integer.toHexString(ValueInterpreter.getIntValue(result, result.length == 1 ? ValueInterpreter.FORMAT_UINT8 : ValueInterpreter.FORMAT_UINT32, 0)));
        }
    }

    private void onReadFailure(Throwable throwable)
    {
        Log.d("BT", "Read error: " + throwable);
    }

}
