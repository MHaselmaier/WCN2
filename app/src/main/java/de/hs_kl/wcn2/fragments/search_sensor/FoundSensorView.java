package de.hs_kl.wcn2.fragments.search_sensor;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import de.hs_kl.wcn2.R;

class FoundSensorView
{
    public View root;
    public TextView macAddress;
    public TextView sensorID;
    public TextView lastSeen;
    public TextView mnemonic;
    public ImageButton mnemonicEdit;
    public Switch trackSwitch;
    public ImageView batteryLevel;
    public ImageView signalStrength;

    public FoundSensorView(View view)
    {
        this.root = view;
        this.macAddress = view.findViewById(R.id.sensor_mac_address);
        this.sensorID = view.findViewById(R.id.sensor_id);
        this.lastSeen = view.findViewById(R.id.last_seen);
        this.mnemonic = view.findViewById(R.id.mnemonic);
        this.mnemonicEdit = view.findViewById(R.id.mnemonic_edit);
        this.trackSwitch = view.findViewById(R.id.sensor_tracked);
        this.batteryLevel = view.findViewById(R.id.battery_level);
        this.signalStrength = view.findViewById(R.id.signal_strength);
    }
}