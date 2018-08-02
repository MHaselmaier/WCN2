package de.hs_kl.wcn2.fragments.search_sensor;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.widget.CompoundButton;
import android.widget.TextView;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.ble_scanner.SensorData;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

public class SensorTrackedChangeListener implements CompoundButton.OnCheckedChangeListener
{
    private Context context;
    private ScanResultAdapter adapter;

    public SensorTrackedChangeListener(Context context, ScanResultAdapter adapter)
    {
        this.context = context;
        this.adapter = adapter;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        ConstraintLayout parent = (ConstraintLayout)buttonView.getParent();
        TextView sensorMACAddressView = parent.findViewById(R.id.sensor_mac_address);
        SensorData sensorData = this.adapter.getItem(sensorMACAddressView.getText().toString());

        if (isChecked)
        {
            TrackedSensorsStorage.trackSensor(this.context, sensorData);
            buttonView.setText(R.string.sensor_tracked);
        }
        else
        {
            TrackedSensorsStorage.untrackSensor(this.context, sensorData);
            buttonView.setText(R.string.sensor_not_tracked);
        }
    }
}
