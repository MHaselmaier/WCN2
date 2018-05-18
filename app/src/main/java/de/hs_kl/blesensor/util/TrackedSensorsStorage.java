package de.hs_kl.blesensor.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hs_kl.blesensor.ble_scanner.SensorData;

public class TrackedSensorsStorage
{
    public static List<SensorData> getTrackedSensors(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TRACKED_SENSORS_PREFERENCES,
                                                         Context.MODE_PRIVATE);
        List<SensorData> trackedSensors = new ArrayList<>();
        for (Map.Entry<String, ?> entry: sharedPreferences.getAll().entrySet())
        {
            String macAddress = entry.getKey();
            byte deviceID = ((Integer)entry.getValue()).byteValue();
            trackedSensors.add(new SensorData(deviceID, macAddress));
        }
        return trackedSensors;
    }

    public static void trackSensor(Context context, SensorData sensorData)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TRACKED_SENSORS_PREFERENCES,
                                                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(sensorData.getMacAddress(), sensorData.getDeviceID());
        editor.commit();
    }

    public static void untrackSensor(Context context, SensorData sensorData)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TRACKED_SENSORS_PREFERENCES,
                                                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(sensorData.getMacAddress());
        editor.commit();
    }

    public static boolean isTracked(Context context, SensorData sensorData)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TRACKED_SENSORS_PREFERENCES,
                                                        Context.MODE_PRIVATE);
        return sharedPreferences.contains(sensorData.getMacAddress());
    }
}
