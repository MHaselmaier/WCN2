package de.hs_kl.wcn2_alarm;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hs_kl.wcn2_alarm.alarms.WCN2Alarm;
import de.hs_kl.wcn2_alarm.alarms.WCN2CompoundAlarm;
import de.hs_kl.wcn2_alarm.alarms.WCN2HumidityAlarm;
import de.hs_kl.wcn2_alarm.alarms.WCN2PresenceAlarm;
import de.hs_kl.wcn2_alarm.alarms.WCN2TemperatureAlarm;
import de.hs_kl.wcn2_sensors.SensorData;
import de.hs_kl.wcn2_sensors.WCN2Scanner;

import static de.hs_kl.wcn2_alarm.alarms.WCN2Alarm.Operator;

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
            int type = this.alarms.getInt(name + ":type", -1);
            int position = this.alarms.getInt(name + ":position", -1);
            if (type == 3)
            {
                List<WCN2Alarm> allAlarms = new ArrayList<>();
                for (String n: this.alarms.getStringSet(name + ":names", new HashSet<>()))
                {
                    type = this.alarms.getInt(name + ":" + n + ":type", -1);
                    Operator operator = Operator.values()[this.alarms.getInt(name + ":" + n + ":operator", -1)];
                    float value = this.alarms.getFloat(name + ":" + n + ":value", Float.NaN);
                    List<SensorData> sensorData = loadSensorData(name + ":" + n);
                    WCN2Alarm alarm = WCN2Alarm.createAlarm(name + ":" + n, type, operator,
                            value, sensorData);
                    alarm.setActivated(this.alarms.getBoolean(name + ":activated", false));
                    allAlarms.add(alarm);
                }
                this.cachedData.set(position, new WCN2CompoundAlarm(name, allAlarms));
            }
            else
            {
                Operator operator = Operator.values()[this.alarms.getInt(name + ":operator", -1)];
                float value = this.alarms.getFloat(name + ":value", Float.NaN);
                List<SensorData> sensorData = loadSensorData(name);
                WCN2Alarm alarm = WCN2Alarm.createAlarm(name, type, operator, value, sensorData);
                alarm.setActivated(this.alarms.getBoolean(name + ":activated", false));
                this.cachedData.set(position, alarm);
            }
        }
    }

    private List<SensorData> loadSensorData(String name)
    {
        List<SensorData> sensorData = new ArrayList<>();
        Set<String> addresses = this.sensorData.getStringSet(name + ":macAddresses", new HashSet<>());
        for (String address: addresses)
        {
            byte id = (byte)this.sensorData.getInt(name + ":" + address, 0);
            sensorData.add(new SensorData(id, "null", address));
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

        if (alarm instanceof WCN2CompoundAlarm)
        {
            Set<String> containedNames = this.alarms.getStringSet(name + ":names", new HashSet<>());
            for (String containedName: containedNames)
            {
                editor.remove(name + ":" + containedName + ":type");
                editor.remove(name + ":" + containedName + ":operator");
                editor.remove(name + ":" + containedName + ":value");
                editor.remove(name + ":" + containedName + ":activated");
                deleteSensorData(name + ":" + containedName);
            }
            containedNames.clear();
            for (WCN2Alarm a: ((WCN2CompoundAlarm)alarm).getAlarms())
            {
                saveAlarm(editor, a, name + ":" + a.getName());
                containedNames.add(a.getName());
            }
            editor.putStringSet(name + ":names", containedNames);
        }

        saveAlarm(editor, alarm, name);

        editor.apply();
    }

    private void saveAlarm(SharedPreferences.Editor editor, WCN2Alarm alarm, String name)
    {
        int type = -1;
        if (alarm instanceof WCN2TemperatureAlarm)
            type = 0;
        if (alarm instanceof WCN2HumidityAlarm)
            type = 1;
        if (alarm instanceof WCN2PresenceAlarm)
            type = 2;
        if (alarm instanceof WCN2CompoundAlarm)
            type = 3;

        editor.putInt(name + ":type", type);
        editor.putInt(name + ":operator", alarm.getOperator().ordinal());
        editor.putFloat(name + ":value", alarm.getThreshold());
        editor.putBoolean(name + ":activated", alarm.isActivated());
        saveSensorData(alarm);
    }

    private void saveSensorData(WCN2Alarm alarm)
    {
        deleteSensorData(alarm.getName());

        SharedPreferences.Editor editor = this.sensorData.edit();

        String name = alarm.getName();
        Set<String> addresses = new HashSet<>();
        for (SensorData sensorData: alarm.getSensorData())
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
            WCN2Scanner.unregisterScanResultListener(this.cachedData.remove(position));
            moveAlarmsUp(position);
        }

        SharedPreferences.Editor editor = this.alarms.edit();
        editor.remove(alarm + ":type");
        editor.remove(alarm + ":position");
        editor.remove(alarm + ":operator");
        editor.remove(alarm + ":value");
        editor.remove(alarm + ":activated");

        for (String name: this.alarms.getStringSet(alarm + ":names", new HashSet<>()))
        {
            editor.remove(alarm + ":" + name + ":type");
            editor.remove(alarm + ":" + name + ":operator");
            editor.remove(alarm + ":" + name + ":value");
            editor.remove(alarm + ":" + name + ":activated");
            deleteSensorData(alarm + ":" + name);
        }

        deleteSensorData(alarm);

        Set<String> names = this.alarms.getStringSet("names", new HashSet<>());
        if (names.remove(alarm))
        {
            editor.putStringSet("names", names);
        }

        editor.apply();
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
