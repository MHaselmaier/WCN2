package de.hs_kl.wcn2_alarm.alarms;

import android.content.Context;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hs_kl.wcn2_sensors.SensorData;

public class WCN2CompoundAlarm extends WCN2Alarm
{
    private List<WCN2Alarm> alarms;

    public WCN2CompoundAlarm(Context context, String name, List<WCN2Alarm> alarms)
    {
        super(context, name, null, WCN2CompoundAlarm.getCombinedSensorData(alarms));
        this.alarms = alarms;
    }

    private static Collection<SensorData> getCombinedSensorData(List<WCN2Alarm> alarms)
    {
        Map<String, SensorData> combinedSensorData = new HashMap<>();
        for (WCN2Alarm alarm: alarms)
        {
            for (SensorData sensorData: alarm.sensors)
                combinedSensorData.put(sensorData.getMacAddress(), sensorData);
        }
        return combinedSensorData.values();
    }

    public List<WCN2Alarm> getAlarms()
    {
        return this.alarms;
    }

    @Override
    public boolean isTriggered()
    {
        for (WCN2Alarm alarm: this.alarms)
        {
            if (alarm.isTriggered())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public float getThreshold()
    {
        return Float.NaN;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;

        WCN2CompoundAlarm that = (WCN2CompoundAlarm) o;

        return null != this.alarms ? this.alarms.equals(that.alarms) : null == that.alarms;
    }

    @Override
    public int hashCode()
    {
        return null != this.alarms ? this.alarms.hashCode() : 0;
    }
}
