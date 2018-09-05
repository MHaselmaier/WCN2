package de.hs_kl.wcn2.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hs_kl.wcn2.ble_scanner.SensorData;
import de.hs_kl.wcn2.fragments.search_sensor.SensorTrackedChangeListener;

public class TrackedSensorsStorage
{
    private static Map<String, SensorData> trackedSensors;

    public static void init(Context context)
    {
        if (null != TrackedSensorsStorage.trackedSensors) return;

        SharedPreferences trackedSensors = context.getSharedPreferences(Constants.TRACKED_SENSORS_ID,
                Context.MODE_PRIVATE);
        List<Map.Entry<String, ?>> savedIDs = new ArrayList<Map.Entry<String, ?>>(trackedSensors
                .getAll().entrySet());
        SharedPreferences mnemonics = context.getSharedPreferences(Constants.TRACKED_SENSORS_MNEMONIC,
                Context.MODE_PRIVATE);
        List<Map.Entry<String, ?>> savedMnemonics = new ArrayList<Map.Entry<String, ?>>(mnemonics
                .getAll().entrySet());

        TrackedSensorsStorage.trackedSensors = new HashMap<>(savedIDs.size());
        for (int i = 0; savedIDs.size() > i; ++i)
        {
            String macAddress = savedIDs.get(i).getKey();
            byte sensorID = ((Integer)savedIDs.get(i).getValue()).byteValue();
            String mnemonic = (String)savedMnemonics.get(i).getValue();
            SensorData sensorData = new SensorData(sensorID, mnemonic, macAddress);
            TrackedSensorsStorage.trackedSensors.put(macAddress, sensorData);
        }
    }

    public static List<SensorData> getTrackedSensors()
    {
        return new ArrayList<>(TrackedSensorsStorage.trackedSensors.values());
    }

    public static String getMnemonic(String macAddress)
    {
        SensorData sensorData = TrackedSensorsStorage.trackedSensors.get(macAddress);
        return (null == sensorData ? "null" : sensorData.getMnemonic());
    }

    public static void trackSensor(Context context, SensorData sensorData)
    {
        TrackedSensorsStorage.trackedSensors.put(sensorData.getMacAddress(), sensorData);

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TRACKED_SENSORS_ID,
                                                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(sensorData.getMacAddress(), sensorData.getSensorID());
        editor.apply();

        sharedPreferences = context.getSharedPreferences(Constants.TRACKED_SENSORS_MNEMONIC,
                                                        Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(sensorData.getMacAddress(), sensorData.getMnemonic());
        editor.apply();
    }

    public static void untrackSensor(Context context, SensorData sensorData)
    {
        TrackedSensorsStorage.trackedSensors.remove(sensorData.getMacAddress());

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TRACKED_SENSORS_ID,
                                                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(sensorData.getMacAddress());
        editor.apply();

        sharedPreferences = context.getSharedPreferences(Constants.TRACKED_SENSORS_MNEMONIC,
                Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.remove(sensorData.getMacAddress());
        editor.apply();
    }

    public static boolean isTracked(SensorData sensorData)
    {
        return TrackedSensorsStorage.trackedSensors.containsKey(sensorData.getMacAddress());
    }
}
