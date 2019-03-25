package de.hs_kl.wcn2_alarm;

import android.content.Context;

import java.util.List;

import de.hs_kl.wcn2_sensors.SensorData;

public class WCN2TemperatureAlarm extends WCN2Alarm
{
    private float temperatureThreshold;

    public WCN2TemperatureAlarm(Context context, String name, List<SensorData> sensors,
                                float temperatureThreshold)
    {
        super(context, name, sensors);

        this.temperatureThreshold = temperatureThreshold;
    }

    @Override
    public boolean isTriggered()
    {
        for (SensorData sensor: super.sensors)
        {
            if (this.temperatureThreshold < sensor.getTemperature())
            {
                return true;
            }
        }
        return false;
    }
}
