package de.hs_kl.wcn2_alarm.alarms;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2_sensors.SensorData;

public class WCN2CompoundAlarm extends WCN2Alarm
{
    private List<WCN2Alarm> alarms;

    public WCN2CompoundAlarm(String name, List<WCN2Alarm> alarms)
    {
        super(name, null, WCN2CompoundAlarm.getCombinedSensorData(alarms));
        this.alarms = alarms;
    }

    private static List<SensorData> getCombinedSensorData(List<WCN2Alarm> alarms)
    {
        List<SensorData> combinedSensorData = new ArrayList<>();
        for (WCN2Alarm alarm: alarms)
            for (SensorData sensorData: alarm.sensors)
                if (!combinedSensorData.contains(sensorData))
                    combinedSensorData.add(sensorData);
        return combinedSensorData;
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
        if (!super.equals(o)) return false;
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
