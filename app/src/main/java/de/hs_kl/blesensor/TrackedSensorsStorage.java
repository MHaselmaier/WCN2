package de.hs_kl.blesensor;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrackedSensorsStorage
{
    private final static String TRACKED_SENSORS_PREFERENCES = "tracked_sensors";

    public static List<SensorData> getTrackedSensors(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TrackedSensorsStorage.TRACKED_SENSORS_PREFERENCES,
                                                         Context.MODE_PRIVATE);
        List<SensorData> trackedSensors = new ArrayList<>();
        for (Map.Entry<String, ?> entry: sharedPreferences.getAll().entrySet())
        {
            String macAddress = entry.getKey();
            String deviceName = (String)entry.getValue();
            trackedSensors.add(new SensorData(deviceName, macAddress));
        }
        return trackedSensors;
    }

    public static void trackSensor(Context context, SensorData sensorData)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TrackedSensorsStorage.TRACKED_SENSORS_PREFERENCES,
                                                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(sensorData.getMacAddress(), sensorData.getDeviceName());
        editor.commit();
    }

    public static void untrackSensor(Context context, SensorData sensorData)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TrackedSensorsStorage.TRACKED_SENSORS_PREFERENCES,
                                                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(sensorData.getMacAddress());
        editor.commit();
    }

    public static boolean isTracked(Context context, SensorData sensorData)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TrackedSensorsStorage.TRACKED_SENSORS_PREFERENCES,
                                                        Context.MODE_PRIVATE);
        return sharedPreferences.contains(sensorData.getMacAddress());
    }
}