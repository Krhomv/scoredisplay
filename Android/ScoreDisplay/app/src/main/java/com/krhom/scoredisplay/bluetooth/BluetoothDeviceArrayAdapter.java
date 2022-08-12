package com.krhom.scoredisplay.bluetooth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.krhom.scoredisplay.R;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanResult;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BluetoothDeviceArrayAdapter extends ArrayAdapter<RxBleDevice>
{

    private int m_resourceLayout;
    private Context m_context;
    List<RxBleDevice> m_items;
    private static final Comparator<RxBleDevice> SORTING_COMPARATOR = (lhs, rhs) ->
            lhs.getMacAddress().compareTo(rhs.getMacAddress());

    public BluetoothDeviceArrayAdapter(Context context, int resource, List<RxBleDevice> items)
    {
        super(context, resource, items);
        this.m_resourceLayout = resource;
        this.m_context = context;
        this.m_items = items;
        Collections.sort(m_items, SORTING_COMPARATOR);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;

        if (v == null)
        {
            LayoutInflater vi;
            vi = LayoutInflater.from(m_context);
            v = vi.inflate(m_resourceLayout, null);
        }

        RxBleDevice bleDevice = getItem(position);

        if (bleDevice != null)
        {
            TextView btDeviceName = (TextView) v.findViewById(R.id.btDeviceName);
            TextView btDeviceConnected = (TextView) v.findViewById(R.id.btDeviceConnected);
            TextView btDeviceAddress = (TextView) v.findViewById(R.id.btDeviceAddress);

            if (btDeviceName != null)
            {
                btDeviceName.setText(bleDevice.getName());
            }

            if (btDeviceConnected != null)
            {
                BluetoothManager bluetoothManager = BluetoothManager.getInstance();
                RxBleConnection.RxBleConnectionState connectionState = bluetoothManager.getBluetoothDeviceConnectionState(bleDevice.getMacAddress());
                switch (connectionState)
                {
                    case CONNECTED:
                        btDeviceConnected.setText(m_context.getString(R.string.bluetooth_device_connected));
                        btDeviceConnected.setTextColor(m_context.getColor(R.color.bluetoothDeviceConnected));
                        break;
                    case CONNECTING:
                        btDeviceConnected.setText(m_context.getString(R.string.bluetooth_device_connecting));
                        btDeviceConnected.setTextColor(m_context.getColor(R.color.bluetoothDeviceConnecting));
                        break;
                    case DISCONNECTED:
                        btDeviceConnected.setText(m_context.getString(R.string.bluetooth_device_disconnected));
                        btDeviceConnected.setTextColor(m_context.getColor(R.color.bluetoothDeviceDisconnected));
                        break;
                    case DISCONNECTING:
                        btDeviceConnected.setText(m_context.getString(R.string.bluetooth_device_connection_failed));
                        btDeviceConnected.setTextColor(m_context.getColor(R.color.bluetoothDeviceConnectionFailed));
                        break;
                }
            }

            if (btDeviceAddress != null)
            {
                btDeviceAddress.setText(bleDevice.getMacAddress());
            }
        }

        return v;
    }

    void addScanResult(ScanResult bleScanResult)
    {
        RxBleDevice bleDevice = bleScanResult.getBleDevice();
        String macAddress = bleDevice.getMacAddress();
        if (!m_items.stream().filter(x -> x.getMacAddress().equals(macAddress)).findFirst().isPresent())
        {
            m_items.add(bleScanResult.getBleDevice());
            Collections.sort(m_items, SORTING_COMPARATOR);
            notifyDataSetChanged();
        }
    }

    void clearScanResults()
    {
        m_items.clear();
        notifyDataSetChanged();
    }

}