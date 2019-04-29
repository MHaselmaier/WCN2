package de.hs_kl.wcn2_alarm.alarms;

import java.util.List;

import de.hs_kl.wcn2_sensors.SensorData;

public class WCN2HumidityAlarm extends WCN2Alarm
{
    private float humidityThreshold;

    public WCN2HumidityAlarm(String name, Operator operator,
                                float humidityThreshold, List<SensorData> sensors)
    {
        super(name, operator, sensors);

        this.humidityThreshold = humidityThreshold;
    }

    @Override
    public boolean isTriggered()
    {
        for (SensorData sensor: super.sensors)
        {
            switch (this.operator)
            {
            case GREATER:
                if (this.humidityThreshold < sensor.getRelativeHumidity()) return true;
                break;
            case EQUAL:
                if (this.humidityThreshold == sensor.getRelativeHumidity()) return true;
                break;
            case LESS:
                if (this.humidityThreshold > sensor.getRelativeHumidity()) return true;
                break;
            }
        }
        return false;
    }

    @Override
    public float getThreshold()
    {
        return this.humidityThreshold;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!super.equals(o)) return false;
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;

        WCN2HumidityAlarm that = (WCN2HumidityAlarm) o;

        return Float.compare(that.humidityThreshold, this.humidityThreshold) == 0;
    }

    @Override
    public int hashCode()
    {
        return (+0.0f != this.humidityThreshold ? Float.floatToIntBits(this.humidityThreshold) : 0);
    }
}
