package de.hs_kl.blesensor;

import android.content.Context;
import android.widget.CompoundButton;

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
