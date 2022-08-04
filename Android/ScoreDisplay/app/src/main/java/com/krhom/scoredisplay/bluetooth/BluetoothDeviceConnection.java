package com.krhom.scoredisplay.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import com.jakewharton.rx.ReplayingShare;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.helpers.ValueInterpreter;

import java.util.HashSet;
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
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");

    private Context m_context;
    private RxBleClient m_rxBleClient;
    private RxBleDevice m_bleDevice;
    private Observable<RxBleConnection> m_connectionObservable;
    private final CompositeDisposable m_compositeDisposable = new CompositeDisposable();
    private PublishSubject<Boolean> m_disconnectTriggerSubject = PublishSubject.create();
    private Set<BluetoothDeviceConnectionStateChangedListener> m_bluetoothDeviceConnectionStateChangedListeners = new HashSet();

    public void addBluetoothDeviceStatusChangedListener(BluetoothDeviceConnectionStateChangedListener listener)
    {
        m_bluetoothDeviceConnectionStateChangedListeners.add(listener);
    }

    public void removeBluetoothDeviceStatusChangedListener(BluetoothDeviceConnectionStateChangedListener listener)
    {
        m_bluetoothDeviceConnectionStateChangedListeners.remove(listener);
    }

    BluetoothDeviceConnection(Context context, String macAddress)
    {
        m_context = context;
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
                .flatMapSingle(RxBleConnection::discoverServices)
                .flatMapSingle(rxBleDeviceServices ->
                {
                    Single<BluetoothGattCharacteristic> characteristic = rxBleDeviceServices.getCharacteristic(CHARACTERISTIC_UUID);
                    return characteristic;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        characteristic -> {
                            onConnected();
                        },
                        this::onConnectionFailure,
                        this::onConnectionFinished
                );

        m_compositeDisposable.add(connectionDisposable);
    }

    public void disconnect() {
        m_disconnectTriggerSubject.onNext(true);
    }

    public RxBleConnection.RxBleConnectionState getConnectionState()
    {
        return m_bleDevice.getConnectionState();
    }

    public void send(String message)
    {
        final Disposable sendDisposable = m_connectionObservable
                .firstOrError()
                .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(CHARACTERISTIC_UUID, message.getBytes()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        bytes -> onWriteSuccess(message),
                        this::onWriteFailure
                );

        m_compositeDisposable.add(sendDisposable);
    }

    private void onConnected()
    {
        // Setup the receive notification
        final Disposable notificationDisposable = m_connectionObservable
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(CHARACTERISTIC_UUID))
                .doOnNext(notificationObservable -> ((Activity)m_context).runOnUiThread(this::notificationHasBeenSetUp))
                .flatMap(notificationObservable -> notificationObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure);

        m_compositeDisposable.add(notificationDisposable);
    }

    private void onConnectionFailure(Throwable throwable)
    {
    }

    private void onConnectionFinished()
    {
    }

    private void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState)
    {
        for (BluetoothDeviceConnectionStateChangedListener listener : m_bluetoothDeviceConnectionStateChangedListeners)
        {
            listener.onBluetoothDeviceStatusChanged(m_bleDevice.getMacAddress(), newState);
        }
    }

    private void onWriteSuccess(String message) {
        Log.d("BT",  "Successfully sent message: " + message);
    }

    private void onWriteFailure(Throwable throwable) {
        Log.d("BT", "Write error: " + throwable);
    }

    private void onNotificationReceived(byte[] bytes) {
        Log.d("BT", "Change: " + ValueInterpreter.getStringValue(bytes, 0));
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        Log.d("BT", "Notifications error: " + throwable);
    }

    private void notificationHasBeenSetUp() {
        Log.d("BT", "Notifications has been set up");
    }

}
