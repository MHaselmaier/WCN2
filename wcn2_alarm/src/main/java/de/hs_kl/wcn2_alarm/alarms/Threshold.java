package de.hs_kl.wcn2_alarm.alarms;

import de.hs_kl.wcn2_sensors.WCN2SensorData;

public class Threshold
{
    private final Type type;
    private final float value;
    private final Operator operator;

    public Threshold(Type type, float value, Operator operator)
    {
        this.type = type;
        this.value = value;
        this.operator = operator;
    }

    public boolean isTriggered(WCN2SensorData sensorData)
    {
        float sensorValue = getCorrespondingSensorValue(sensorData);

        switch (this.operator)
        {
        case GREATER:
            return sensorValue > this.value;
        case EQUAL:
            return 1e-5 > Math.abs(sensorValue - this.value);
        case LESS:
            return sensorValue < this.value;
        }

        return false;
    }

    private float getCorrespondingSensorValue(WCN2SensorData sensorData)
    {
        switch (this.type)
        {
        case TEMPERATURE:
            return sensorData.getTemperature();
        case HUMIDITY:
            return sensorData.getRelativeHumidity();
        case ABSENCE:
            return (System.currentTimeMillis() - sensorData.getTimestamp()) / 1000f;
        }

        return Float.NaN;
    }

    public Type getType()
    {
        return this.type;
    }

    public float getValue()
    {
        return this.value;
    }

    public Operator getOperator()
    {
        return this.operator;
    }
}
