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
                    WCN2Alarm alarm = WCN2Alarm.createAlarm(context, name + ":" + n, type, operator,
                            value, sensorData);
                    allAlarms.add(alarm);
                }
                this.cachedData.set(position, new WCN2CompoundAlarm(context, name, allAlarms));
            }
            else
            {
                Operator operator = Operator.values()[this.alarms.getInt(name + ":operator", -1)];
                float value = this.alarms.getFloat(name + ":value", Float.NaN);
                List<SensorData> sensorData = loadSensorData(name);
                WCN2Alarm alarm = WCN2Alarm.createAlarm(context, name, type, operator,
                        value, sensorData);
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

    public void saveAlarm(WCN2Alarm alarm)
    {
        if (this.cachedData.contains(alarm)) return;

        this.cachedData.add(alarm);

        SharedPreferences.Editor editor = this.alarms.edit();

        String name = alarm.getName();
        Set<String> names = this.alarms.getStringSet("names", new HashSet<>());
        names.add(name);
        editor.putStringSet("names", names);
        editor.putInt(name + ":position", this.cachedData.size() - 1);

        if (alarm instanceof WCN2CompoundAlarm)
        {
            Set<String> containedNames = this.alarms.getStringSet(name + ":names", new HashSet<>());
            for (String containedName: containedNames)
            {
                editor.remove(name + ":" + containedName + ":type");
                editor.remove(name + ":" + containedName + ":operator");
                editor.remove(name + ":" + containedName + ":value");
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

    public void deleteAlarm(WCN2Alarm alarm)
    {
        if (!this.cachedData.remove(alarm)) return;

        SharedPreferences.Editor editor = this.alarms.edit();
        editor.remove(alarm.getName() + ":type");
        editor.remove(alarm.getName() + ":position");
        editor.remove(alarm.getName() + ":operator");
        editor.remove(alarm.getName() + ":value");

        for (String name: this.alarms.getStringSet(alarm.getName() + ":names", new HashSet<>()))
        {
            editor.remove(alarm.getName() + ":" + name + ":type");
            editor.remove(alarm.getName() + ":" + name + ":operator");
            editor.remove(alarm.getName() + ":" + name + ":value");
            deleteSensorData(alarm.getName() + ":" + name);
        }

        deleteSensorData(alarm.getName());

        Set<String> names = this.alarms.getStringSet("names", new HashSet<>());
        if (names.remove(alarm.getName()))
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
