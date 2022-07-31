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

    private int resourceLayout;
    private Context mContext;

    public BluetoothDeviceArrayAdapter(Context context, int resource, List<BluetoothDevice> items)
    {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @SuppressLint("MissingPermission")
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

        View v = convertView;

        if (v == null)
        {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        BluetoothDevice p = getItem(position);

        if (p != null)
        {
            TextView btDeviceName = (TextView) v.findViewById(R.id.btDeviceName);
            TextView btDeviceConnected = (TextView) v.findViewById(R.id.btDeviceConnected);
            TextView btDeviceAddress = (TextView) v.findViewById(R.id.btDeviceAddress);

            if (btDeviceName != null)
            {
                btDeviceName.setText(p.getName());
            }

            if (btDeviceConnected != null)
            {
                btDeviceConnected.setText("CONNECTED?");
            }

            if (btDeviceAddress != null)
            {
                btDeviceAddress.setText(p.getAddress());
            }
        }

        return v;
    }

}