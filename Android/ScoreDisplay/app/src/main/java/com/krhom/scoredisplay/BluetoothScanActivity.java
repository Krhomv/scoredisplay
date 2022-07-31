package com.krhom.scoredisplay;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressLint("MissingPermission")
public class BluetoothScanActivity extends AppCompatActivity
{

    private ListView m_bluetoothDeviceListView;
    private ArrayList<String> m_bluetoothDeviceNames = new ArrayList<>();
    private BluetoothManager m_bluetoothManager;
    private BluetoothDeviceArrayAdapter m_bluetoothDeviceArrayAdapter;
    private List<BluetoothDevice> m_bluetoothDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.connect_bluetooth));
        }

        m_bluetoothDeviceListView = (ListView) findViewById(R.id.bluetoothDeviceList);
        m_bluetoothManager = BluetoothManager.getInstance();
        BluetoothAdapter bluetoothAdapter = m_bluetoothManager.getBluetoothAdapter();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);

        if (bluetoothAdapter.isEnabled())
        {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            m_bluetoothDeviceList.addAll(pairedDevices);

            m_bluetoothDeviceArrayAdapter = new BluetoothDeviceArrayAdapter(this, R.layout.bluetooth_device_view, m_bluetoothDeviceList);
            m_bluetoothDeviceListView.setAdapter(m_bluetoothDeviceArrayAdapter);

            boolean started = bluetoothAdapter.startDiscovery();
            if (started)
            {
                Toast.makeText(this, "Scan Discovery Started", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Scan Discovery Didn't Start", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                m_bluetoothDeviceList.add(device);
                m_bluetoothDeviceArrayAdapter.notifyDataSetChanged();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                Toast.makeText(getApplicationContext(), "Scan Started", Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                Toast.makeText(getApplicationContext(), "Scan Stopped", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        BluetoothAdapter bluetoothAdapter = m_bluetoothManager.getBluetoothAdapter();
        bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(receiver);
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
}