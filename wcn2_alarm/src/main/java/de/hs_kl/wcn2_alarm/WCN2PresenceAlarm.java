package de.hs_kl.wcn2_alarm;

import android.content.Context;

import java.util.List;

import de.hs_kl.wcn2_sensors.SensorData;

public class WCN2PresenceAlarm extends WCN2Alarm
{
    private long timeThreshold;

    public WCN2PresenceAlarm(Context context, String name, List<SensorData> sensors,
                                long timeThreshold)
    {
        super(context, name, sensors);

        this.timeThreshold = timeThreshold;
    }

    @Override
    public boolean isTriggered()
    {
        for (SensorData sensor: super.sensors)
        {
            if (this.timeThreshold < System.currentTimeMillis() - sensor.getTimestamp())
            {
                return true;
            }
        }
        return false;
    }
}
