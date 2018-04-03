package de.hs_kl.blesensor;

import android.widget.CompoundButton;

public class SensorTrackedChangeListener implements CompoundButton.OnCheckedChangeListener
{
    private TrackedSensorsStorage trackedSensorsStorage;
    private SensorData sensorData;

    public SensorTrackedChangeListener(TrackedSensorsStorage trackedSensorsStorage,
                                       SensorData sensorData)
    {
        this.trackedSensorsStorage = trackedSensorsStorage;
        this.sensorData = sensorData;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (isChecked)
        {
            this.trackedSensorsStorage.trackSensor(sensorData);
            buttonView.setText(R.string.sensor_tracked);
        }
        else
        {
            this.trackedSensorsStorage.untrackSensor(sensorData);
            buttonView.setText(R.string.sensor_not_tracked);
        }
    }
}
