package de.hs_kl.blesensor.fragments.search_sensor;

import android.content.Context;
import android.widget.CompoundButton;

import de.hs_kl.blesensor.R;
import de.hs_kl.blesensor.ble_scanner.SensorData;
import de.hs_kl.blesensor.util.TrackedSensorsStorage;

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
            TrackedSensorsStorage.trackSensor(this.context, this.sensorData);
            buttonView.setText(R.string.sensor_tracked);
        }
        else
        {
            TrackedSensorsStorage.untrackSensor(this.context, this.sensorData);
            buttonView.setText(R.string.sensor_not_tracked);
        }
    }
}
