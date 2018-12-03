package de.hs_kl.wcn2.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hs_kl.wcn2.ble_scanner.SensorData;

public class TrackedSensorsStorage
{
    private static TrackedSensorsStorage instance;

    private SharedPreferences sensorIDs;
    private SharedPreferences mnemonics;
    private Map<String, SensorData> cachedData;

    private TrackedSensorsStorage(Context context)
    {
        this.sensorIDs = context.getSharedPreferences(Constants.TRACKED_SENSORS_ID,
                Context.MODE_PRIVATE);
        this.mnemonics = context.getSharedPreferences(Constants.TRACKED_SENSORS_MNEMONIC,
                Context.MODE_PRIVATE);

        List<Map.Entry<String, ?>> savedIDs = new ArrayList<>(
                this.sensorIDs.getAll().entrySet());
        List<Map.Entry<String, ?>> savedMnemonics = new ArrayList<>(
                this.mnemonics.getAll().entrySet());

        this.cachedData = new HashMap<>(savedIDs.size());
        for (int i = 0; savedIDs.size() > i; ++i)
        {
            String macAddress = savedIDs.get(i).getKey();
            byte sensorID = ((Integer)savedIDs.get(i).getValue()).byteValue();
            String mnemonic = (String)savedMnemonics.get(i).getValue();
            SensorData sensorData = new SensorData(sensorID, mnemonic, macAddress);
            this.cachedData.put(macAddress, sensorData);
        }
    }

    public List<SensorData> getTrackedSensors()
    {
        return new ArrayList<>(this.cachedData.values());
    }

    public String getMnemonic(String macAddress)
    {
        SensorData sensorData = this.cachedData.get(macAddress);
        return (null == sensorData ? "null" : sensorData.getMnemonic());
    }

    public void trackSensor(SensorData sensorData)
    {
        this.cachedData.put(sensorData.getMacAddress(), sensorData);

        SharedPreferences.Editor editor = this.sensorIDs.edit();
        editor.putInt(sensorData.getMacAddress(), sensorData.getSensorID());
        editor.apply();

        editor = this.mnemonics.edit();
        editor.putString(sensorData.getMacAddress(), sensorData.getMnemonic());
        editor.apply();
    }

    public void untrackSensor(SensorData sensorData)
    {
        this.cachedData.remove(sensorData.getMacAddress());

        SharedPreferences.Editor editor = this.sensorIDs.edit();
        editor.remove(sensorData.getMacAddress());
        editor.apply();

        editor = this.mnemonics.edit();
        editor.remove(sensorData.getMacAddress());
        editor.apply();
    }

    public boolean isTracked(SensorData sensorData)
    {
        return this.cachedData.containsKey(sensorData.getMacAddress());
    }

    public static TrackedSensorsStorage getInstance(Context context)
    {
        if (null == TrackedSensorsStorage.instance)
        {
            TrackedSensorsStorage.instance = new TrackedSensorsStorage(context);
        }

        return TrackedSensorsStorage.instance;
    }
}
