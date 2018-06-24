package de.hs_kl.blesensor.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hs_kl.blesensor.ble_scanner.SensorData;

public class TrackedSensorsStorage
{
    public static String getMnemonic(Context context, String macAddress)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TRACKED_SENSORS_MNEMONIC,
                Context.MODE_PRIVATE);
        return sharedPreferences.getString(macAddress, null);
    }

    public static List<SensorData> getTrackedSensors(Context context)
    {
        List<Map.Entry<String, ?>> savedIDs = new ArrayList<Map.Entry<String, ?>>(context.getSharedPreferences(Constants.TRACKED_SENSORS_ID,
                                                        Context.MODE_PRIVATE).getAll().entrySet());
        List<Map.Entry<String, ?>> savedMnemonics = new ArrayList<Map.Entry<String, ?>>(context.getSharedPreferences(Constants.TRACKED_SENSORS_MNEMONIC,
                                                        Context.MODE_PRIVATE).getAll().entrySet());
        List<SensorData> trackedSensors = new ArrayList<>(savedIDs.size());
        for (int i = 0; savedIDs.size() > i; ++i)
        {
            String macAddress = savedIDs.get(i).getKey();
            byte sensorID = ((Integer)savedIDs.get(i).getValue()).byteValue();
            String mnemonic = (String)savedMnemonics.get(i).getValue();
            trackedSensors.add(new SensorData(sensorID, mnemonic, macAddress));
        }

        return trackedSensors;
    }

    public static void trackSensor(Context context, SensorData sensorData)
    {
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

    public static boolean isTracked(Context context, SensorData sensorData)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TRACKED_SENSORS_ID,
                                                        Context.MODE_PRIVATE);
        return sharedPreferences.contains(sensorData.getMacAddress());
    }
}
