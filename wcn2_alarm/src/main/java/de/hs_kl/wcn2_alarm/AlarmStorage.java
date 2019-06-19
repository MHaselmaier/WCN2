package de.hs_kl.wcn2_alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hs_kl.wcn2_alarm.alarms.Operator;
import de.hs_kl.wcn2_alarm.alarms.Threshold;
import de.hs_kl.wcn2_alarm.alarms.Type;
import de.hs_kl.wcn2_alarm.alarms.WCN2Alarm;
import de.hs_kl.wcn2_sensors.WCN2Scanner;
import de.hs_kl.wcn2_sensors.WCN2SensorData;

public class AlarmStorage
{
    private static AlarmStorage instance;

    private SharedPreferences alarms;
    private SharedPreferences sensorData;
    private List<WCN2Alarm> cachedData = new ArrayList<>();

    private AlarmStorage(Context context)
    {
        this.alarms = context.getSharedPreferences("alarms", Context.MODE_PRIVATE);
        this.sensorData = context.getSharedPreferences("sensorData", Context.MODE_PRIVATE);

        Set<String> names = this.alarms.getStringSet("names", new HashSet<>());
        for (String n: names)
            this.cachedData.add(null);
        for (String name: names)
        {
            int position = this.alarms.getInt(name + ":position", -1);
            Uri sound = Uri.parse(this.alarms.getString(name + ":sound", ""));
            List<Threshold> thresholds = loadThresholds(name);
            List<WCN2SensorData> sensorData = loadSensorData(name);

            WCN2Alarm alarm = new WCN2Alarm(name, sound, thresholds, sensorData);
            alarm.setActivated(this.alarms.getBoolean(name + ":activated", false));
            this.cachedData.set(position, alarm);
        }
    }

    private List<Threshold> loadThresholds(String name)
    {
        List<Threshold> thresholds = new ArrayList<>();

        int amtThresholds = this.alarms.getInt(name + ":amtThresholds", 0);
        for (int i = 0; amtThresholds > i; ++i)
        {
            int typeOrdinal = this.alarms.getInt(name + ":threshold" + i + ":type", -1);
            Type type = null;
            if (0 <= typeOrdinal)
                type = Type.values()[typeOrdinal];
            float value = this.alarms.getFloat(name + ":threshold" + i + ":value", Float.NaN);
            int operatorOrdinal = this.alarms.getInt(name + ":threshold" + i + ":operator", -1);
            Operator operator = null;
            if (0 <= operatorOrdinal)
                operator = Operator.values()[operatorOrdinal];

            thresholds.add(new de.hs_kl.wcn2_alarm.alarms.Threshold(type, value, operator));
        }

        return thresholds;
    }

    private List<WCN2SensorData> loadSensorData(String name)
    {
        List<WCN2SensorData> sensorData = new ArrayList<>();
        Set<String> addresses = this.sensorData.getStringSet(name + ":macAddresses", new HashSet<>());
        for (String address: addresses)
        {
            byte id = (byte)this.sensorData.getInt(name + ":" + address, 0);
            sensorData.add(new WCN2SensorData(id, "null", address));
        }
        return sensorData;
    }

    public WCN2Alarm[] getAlarms()
    {
        return this.cachedData.toArray(new WCN2Alarm[0]);
    }

    public synchronized void saveAlarm(WCN2Alarm alarm)
    {
        int position = this.alarms.getInt(alarm.getName() + ":position", -1);
        if (-1 != position)
        {
            deleteAlarm(alarm);
            this.cachedData.add(position, alarm);
            moveAlarmsDown(position);
        }
        else
        {
            position = this.cachedData.size();
            this.cachedData.add(alarm);
        }

        SharedPreferences.Editor editor = this.alarms.edit();

        String name = alarm.getName();
        Set<String> names = this.alarms.getStringSet("names", new HashSet<>());
        names.add(name);
        editor.putStringSet("names", names);

        editor.putInt(name + ":position", position);
        if (null != alarm.getSound())
            editor.putString(name + ":sound", alarm.getSound().toString());
        editor.putBoolean(name + ":activated", alarm.isActivated());
        saveThresholds(editor, alarm);
        saveSensorData(alarm);

        editor.apply();
    }

    private void saveThresholds(SharedPreferences.Editor editor, WCN2Alarm alarm)
    {
        List<Threshold> thresholds = alarm.getThresholds();
        int amtThresholds = this.alarms.getInt(alarm.getName() + ":amtThresholds", 0);

        for (int i = thresholds.size(); amtThresholds > i; ++i)
        {
            editor.remove(alarm.getName() + ":threshold" + i + ":type");
            editor.remove(alarm.getName() + ":threshold" + i + ":value");
            editor.remove(alarm.getName() + ":threshold" + i + ":operator");
        }

        editor.putInt(alarm.getName() + ":amtThresholds", thresholds.size());
        for (int i = 0; thresholds.size() > i; ++i)
        {
            editor.putInt(alarm.getName() + ":threshold" + i + ":type",
                    thresholds.get(i).getType().ordinal());
            editor.putFloat(alarm.getName() + ":threshold" + i + ":value",
                    thresholds.get(i).getValue());
            editor.putInt(alarm.getName() + ":threshold" + i + ":operator",
                    thresholds.get(i).getOperator().ordinal());
        }
    }

    private void saveSensorData(WCN2Alarm alarm)
    {
        deleteSensorData(alarm.getName());

        SharedPreferences.Editor editor = this.sensorData.edit();

        String name = alarm.getName();
        Set<String> addresses = new HashSet<>();
        for (WCN2SensorData sensorData: alarm.getSensorData())
        {
            addresses.add(sensorData.getMacAddress());
            editor.putInt(name + ":" + sensorData.getMacAddress(), sensorData.getSensorID());
        }
        editor.putStringSet(name + ":macAddresses", addresses);

        editor.apply();
    }

    public synchronized void deleteAlarm(WCN2Alarm alarm)
    {
        deleteAlarm(alarm.getName());
    }

    public synchronized void deleteAlarm(String alarm)
    {
        int position = this.alarms.getInt(alarm + ":position", -1);
        if (-1 != position)
        {
            WCN2Scanner.unregisterSensorDataListener(this.cachedData.remove(position));
            moveAlarmsUp(position);
        }

        SharedPreferences.Editor editor = this.alarms.edit();
        editor.remove(alarm + ":position");
        editor.remove(alarm + ":sound");
        editor.remove(alarm + ":activated");
        deleteThresholds(editor, alarm);
        deleteSensorData(alarm);

        Set<String> names = this.alarms.getStringSet("names", new HashSet<>());
        if (names.remove(alarm))
        {
            editor.putStringSet("names", names);
        }

        editor.apply();
    }

    private void deleteThresholds(SharedPreferences.Editor editor, String name)
    {
        int amtThresholds = this.alarms.getInt(name + ":amtThresholds", 0);
        for (int i = 0; amtThresholds > i; ++i)
        {
            editor.remove(name + ":threshold" + i + ":type");
            editor.remove(name + ":threshold" + i + ":value");
            editor.remove(name + ":threshold" + i + ":operator");
        }
        editor.remove(name + ":amtThresholds");
    }

    private void deleteSensorData(String name)
    {
        SharedPreferences.Editor editor = this.sensorData.edit();

        Set<String> addresses = this.sensorData.getStringSet(name + ":macAddresses", new HashSet<>());
        for (String address : addresses)
        {
            editor.remove(name + ":" + address);
        }
        editor.remove(name + ":macAddresses");

        editor.apply();
    }

    private void moveAlarmsUp(int position)
    {
        SharedPreferences.Editor editor = this.alarms.edit();
        for (WCN2Alarm alarm: this.cachedData)
        {
            int currentPosition = this.alarms.getInt(alarm.getName() + ":position", -1);
            if (-1 == currentPosition || currentPosition <= position)
                continue;

            editor.putInt(alarm.getName() + ":position", currentPosition - 1);
        }
        editor.apply();
    }

    private void moveAlarmsDown(int position)
    {
        SharedPreferences.Editor editor = this.alarms.edit();
        for (WCN2Alarm alarm: this.cachedData)
        {
            int currentPosition = this.alarms.getInt(alarm.getName() + ":position", -1);
            if (-1 == currentPosition || currentPosition < position)
                continue;

            editor.putInt(alarm.getName() + ":position", currentPosition + 1);
        }
        editor.apply();
    }

    public boolean isSaved(String name)
    {
        return this.alarms.getStringSet("names", new HashSet<>()).contains(name);
    }

    public static AlarmStorage getInstance(Context context)
    {
        if (null == AlarmStorage.instance)
        {
            AlarmStorage.instance = new AlarmStorage(context);
        }

        return AlarmStorage.instance;
    }
}
