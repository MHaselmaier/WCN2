package de.hs_kl.blesensor;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrackedSensorsStorage
{
    private final static String TRACKED_SENSORS_PREFERENCES = "tracked_sensors";

    private SharedPreferences sharedPreferences;

    public TrackedSensorsStorage(Context context)
    {
        this.sharedPreferences = context.getSharedPreferences(TrackedSensorsStorage.TRACKED_SENSORS_PREFERENCES,
                Context.MODE_PRIVATE);
    }

    public List<SensorData> getTrackedSensors()
    {
        List<SensorData> trackedSensors = new ArrayList<>();
        for (Map.Entry<String, ?> entry: this.sharedPreferences.getAll().entrySet())
        {
            String macAddress = entry.getKey();
            String deviceName = (String)entry.getValue();
            trackedSensors.add(new SensorData(deviceName, macAddress));
        }
        return trackedSensors;
    }

    public void trackSensor(SensorData sensorData)
    {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString(sensorData.getMacAddress(), sensorData.getDeviceName());
        editor.commit();
    }

    public void untrackSensor(SensorData sensorData)
    {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.remove(sensorData.getMacAddress());
        editor.commit();
    }

    public boolean isTracked(SensorData sensorData)
    {
        return this.sharedPreferences.contains(sensorData.getMacAddress());
    }
}
