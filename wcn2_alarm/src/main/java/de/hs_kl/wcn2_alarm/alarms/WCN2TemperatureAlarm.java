package de.hs_kl.wcn2_alarm.alarms;

import java.util.List;

import de.hs_kl.wcn2_sensors.SensorData;

public class WCN2TemperatureAlarm extends WCN2Alarm
{
    private float temperatureThreshold;

    public WCN2TemperatureAlarm(String name, Operator operator,
                                float temperatureThreshold, List<SensorData> sensors)
    {
        super(name, operator, sensors);

        this.temperatureThreshold = temperatureThreshold;
    }

    @Override
    public boolean isTriggered()
    {
        for (SensorData sensor: super.sensors)
        {
            switch (this.operator)
            {
            case GREATER:
                if (this.temperatureThreshold < sensor.getTemperature()) return true;
                break;
            case EQUAL:
                if (this.temperatureThreshold == sensor.getTemperature()) return true;
                break;
            case LESS:
                if (this.temperatureThreshold > sensor.getTemperature()) return true;
                break;
            }
        }
        return false;
    }

    @Override
    public float getThreshold()
    {
        return this.temperatureThreshold;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!super.equals(o)) return false;
        if (this == o) return true;
        if (null == o|| getClass() != o.getClass()) return false;

        WCN2TemperatureAlarm that = (WCN2TemperatureAlarm) o;

        return Float.compare(that.temperatureThreshold, this.temperatureThreshold) == 0;
    }

    @Override
    public int hashCode()
    {
        return (+0.0f != this.temperatureThreshold ?
                Float.floatToIntBits(this.temperatureThreshold) : 0);
    }
}
