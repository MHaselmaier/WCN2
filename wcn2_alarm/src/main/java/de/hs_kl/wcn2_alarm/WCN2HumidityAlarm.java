package de.hs_kl.wcn2_alarm;

import android.content.Context;

import java.util.List;

import de.hs_kl.wcn2_sensors.SensorData;

public class WCN2HumidityAlarm extends WCN2Alarm
{
    private float humidityThreshold;

    public WCN2HumidityAlarm(Context context, String name, List<SensorData> sensors,
                                float humidityThreshold)
    {
        super(context, name, sensors);

        this.humidityThreshold = humidityThreshold;
    }

    @Override
    public boolean isTriggered()
    {
        for (SensorData sensor: super.sensors)
        {
            if (this.humidityThreshold < sensor.getRelativeHumidity())
            {
                return true;
            }
        }
        return false;
    }
}
