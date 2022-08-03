package com.krhom.scoredisplay;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class BluetoothDeviceArrayAdapter extends ArrayAdapter<BluetoothDevice>
{

    private int m_resourceLayout;
    private Context m_context;

    public BluetoothDeviceArrayAdapter(Context context, int resource, List<BluetoothDevice> items)
    {
        super(context, resource, items);
        this.m_resourceLayout = resource;
        this.m_context = context;
    }

    @SuppressLint("MissingPermission")
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

        BluetoothDevice bluetoothDevice = getItem(position);

        if (bluetoothDevice != null)
        {
            TextView btDeviceName = (TextView) v.findViewById(R.id.btDeviceName);
            TextView btDeviceConnected = (TextView) v.findViewById(R.id.btDeviceConnected);
            TextView btDeviceAddress = (TextView) v.findViewById(R.id.btDeviceAddress);

            if (btDeviceName != null)
            {
                btDeviceName.setText(bluetoothDevice.getName());
            }

            if (btDeviceConnected != null)
            {
                BluetoothManager bluetoothManager = BluetoothManager.getInstance();
                switch (bluetoothManager.getBluetootDeviceStatus(bluetoothDevice))
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
                    case FAILED:
                        btDeviceConnected.setText(m_context.getString(R.string.bluetooth_device_connection_failed));
                        btDeviceConnected.setTextColor(m_context.getColor(R.color.bluetoothDeviceConnectionFailed));
                        break;
                }
            }

            if (btDeviceAddress != null)
            {
                btDeviceAddress.setText(bluetoothDevice.getAddress());
            }
        }

        return v;
    }

}