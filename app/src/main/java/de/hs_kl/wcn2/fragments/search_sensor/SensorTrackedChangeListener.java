package de.hs_kl.wcn2.fragments.search_sensor;

import android.content.Context;
import android.widget.CompoundButton;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.ble_scanner.SensorData;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

public class SensorTrackedChangeListener implements CompoundButton.OnCheckedChangeListener
{
    private Context context;
    private SensorData sensorData;

    public SensorTrackedChangeListener(Context context, SensorData sensorData)
    {
        this.context = context;
        this.sensorData = sensorData;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (isChecked)
        {
            TrackedSensorsStorage.getInstance(this.context).trackSensor(this.sensorData);
            buttonView.setText(R.string.sensor_tracked);
        }
        else
        {
            TrackedSensorsStorage.getInstance(this.context).untrackSensor(this.sensorData);
            buttonView.setText(R.string.sensor_not_tracked);
        }
    }
}
