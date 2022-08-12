package com.krhom.scoredisplay.bluetooth;

import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.krhom.scoredisplay.R;
import com.krhom.scoredisplay.util.ScanPermission;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class BluetoothScanActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, BluetoothDeviceConnection.StatusChangedListener
{
    @BindView(R.id.bluetoothDeviceList)
    ListView m_bluetoothDeviceListView;
    private BluetoothDeviceArrayAdapter m_bluetoothDeviceArrayAdapter;
    private BluetoothManager m_bluetoothManager;
    private RxBleClient m_rxBleClient;
    private Disposable m_scanDisposable;
    private boolean m_shouldStartScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);
        ButterKnife.bind(this);
        m_bluetoothManager = BluetoothManager.getInstance();
        m_bluetoothManager.addBluetoothDeviceStatusChangedListener(this);
        m_rxBleClient = m_bluetoothManager.getRxBleClient();
        configureResultList();

        startScanning();
    }

    public void startScanning() {

        if (isScanning()) {
            m_scanDisposable.dispose();
        } else {
            if (m_rxBleClient.isScanRuntimePermissionGranted()) {
                scanBleDevices();
            } else {
                m_shouldStartScanning = true;
                ScanPermission.requestScanPermission(this, m_rxBleClient);
            }
        }
    }

    private void scanBleDevices() {
        m_scanDisposable = m_rxBleClient.scanBleDevices(
                        new ScanSettings.Builder()
                                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                                .build(),
                        new ScanFilter.Builder()
                                .setServiceUuid(new ParcelUuid(BluetoothManager.SERVICE))
                                .build()
                )
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::dispose)
                .subscribe(m_bluetoothDeviceArrayAdapter::addScanResult, this::onScanFailure);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (ScanPermission.isScanPermissionGranted(requestCode, permissions, grantResults, m_rxBleClient)
                && m_shouldStartScanning) {
            m_shouldStartScanning = false;
            scanBleDevices();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isScanning()) {
            /*
             * Stop scanning in onPause callback.
             */
            m_scanDisposable.dispose();
        }
    }

    private void configureResultList() {
        Set<RxBleDevice> connectedDevices = m_rxBleClient.getConnectedPeripherals();
        ArrayList<RxBleDevice> adapterArray = new ArrayList<>();
        adapterArray.addAll(connectedDevices);

        m_bluetoothDeviceArrayAdapter = new BluetoothDeviceArrayAdapter(this, R.layout.bluetooth_device_view, adapterArray);
        m_bluetoothDeviceListView.setAdapter(m_bluetoothDeviceArrayAdapter);

        m_bluetoothDeviceListView.setClickable(true);
        m_bluetoothDeviceListView.setOnItemClickListener(this);
    }

    private boolean isScanning() {
        return m_scanDisposable != null;
    }

    private void onScanFailure(Throwable throwable) {
        if (throwable instanceof BleScanException) {
            //ScanExceptionHandler.handleException(this, (BleScanException) throwable);
        } else {
            Log.w("ScanActivity", "Scan failed", throwable);
        }
    }

    private void dispose() {
        m_scanDisposable = null;
    }

    @Override
    public void onDestroy()
    {
        m_bluetoothManager.removeBluetoothDeviceStatusChangedListener(this);
        m_bluetoothDeviceArrayAdapter.clearScanResults();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        // Start the connection
        RxBleDevice bleDevice = (RxBleDevice) adapterView.getItemAtPosition(i);
        switch (bleDevice.getConnectionState())
        {
            case DISCONNECTED:
                m_bluetoothManager.connectToDevice(bleDevice.getMacAddress());
                break;
            case CONNECTED:
                m_bluetoothManager.disconnectFromDevice(bleDevice.getMacAddress());
                break;
        }
    }

    @Override
    public void onBluetoothDeviceConnectionStatusChanged(String macAddress, BluetoothDeviceConnection.Status newStatus)
    {
        m_bluetoothDeviceArrayAdapter.notifyDataSetChanged();
    }
}