package de.hs_kl.wcn2_alarm.alarms;

import android.content.Context;

import java.util.Collection;
import java.util.List;

import de.hs_kl.wcn2_sensors.SensorData;

public class WCN2PresenceAlarm extends WCN2Alarm
{
    private long timeThreshold;

    public WCN2PresenceAlarm(Context context, String name, Operator operator,
                                long timeThreshold, List<SensorData> sensors)
    {
        super(context, name, operator, sensors);

        this.timeThreshold = timeThreshold;
    }

    @Override
    public boolean isTriggered()
    {
        for (SensorData sensor: super.sensors)
        {
            long timeout = System.currentTimeMillis() - sensor.getTimestamp();
            switch (this.operator)
            {
            case GREATER:
                if (this.timeThreshold < timeout) return true;
                break;
            case EQUAL:
                if (this.timeThreshold == timeout) return true;
                break;
            case LESS:
                if (this.timeThreshold > timeout) return true;
                break;
            }
        }
        return false;
    }

    @Override
    public float getThreshold()
    {
        return this.timeThreshold;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;

        WCN2PresenceAlarm that = (WCN2PresenceAlarm) o;

        return this.timeThreshold == that.timeThreshold;
    }

    @Override
    public int hashCode()
    {
        return (int) (this.timeThreshold ^ (this.timeThreshold >>> 32));
    }
}
