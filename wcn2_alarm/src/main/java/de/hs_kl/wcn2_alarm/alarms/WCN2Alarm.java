package de.hs_kl.wcn2_alarm.alarms;

import android.bluetooth.le.ScanFilter;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2_sensors.ScanResultListener;
import de.hs_kl.wcn2_sensors.SensorData;
import de.hs_kl.wcn2_sensors.WCN2Scanner;

public abstract class WCN2Alarm implements ScanResultListener
{
    @Override
    public List<ScanFilter> getScanFilter()
    {
        List<ScanFilter> scanFilters = new ArrayList<>();
        for (SensorData sensorData: this.sensors)
        {
            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setDeviceAddress(sensorData.getMacAddress());
            scanFilters.add(builder.build());
        }

        if (0 == scanFilters.size())
        {
            // If no sensors are tracked, no filters will be set.
            // This results in showing all found sensors.
            // Therefor add a dummy filter, so no sensor will be accepted:
            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setDeviceAddress("00:00:00:00:00:00");
            scanFilters.add(builder.build());
        }
        return scanFilters;
    }

    @Override
    public void onScanResult(SensorData result)
    {
        for (int i = 0; this.sensors.size() > i; ++i)
        {
            int index = this.sensors.indexOf(result);
            if (-1 != index)
            {
                this.sensors.set(index, result);
                return;
            }
        }
    }

    public enum Operator
    {
        GREATER, EQUAL, LESS
    }

    protected List<SensorData> sensors;
    protected String name;
    protected boolean activated;
    protected Operator operator;

    public WCN2Alarm(String name, Operator operator, List<SensorData> sensors)
    {
        this.name = name;
        this.operator = operator;
        this.sensors = sensors;
    }

    public String getName()
    {
        return this.name;
    }

    public void setActivated(boolean activated)
    {
        if (this.activated == activated) return;

        this.activated = activated;
        if (this.activated)
        {
            WCN2Scanner.registerScanResultListener(this);
        }
        else
        {
            WCN2Scanner.unregisterScanResultListener(this);
        }
    }

    public boolean isActivated()
    {
        return this.activated;
    }

    public Operator getOperator()
    {
        return this.operator;
    }

    public void setOperator(Operator operator)
    {
        this.operator = operator;
    }

    public List<SensorData> getSensorData()
    {
        return this.sensors;
    }

    public abstract boolean isTriggered();
    public abstract float getThreshold();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WCN2Alarm)) return false;

        WCN2Alarm alarm = (WCN2Alarm) o;

        if (sensors != null ? !sensors.equals(alarm.sensors) : alarm.sensors != null) return false;
        if (name != null ? !name.equals(alarm.name) : alarm.name != null) return false;
        return operator == alarm.operator;
    }

    @Override
    public int hashCode() {
        int result = sensors != null ? sensors.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (operator != null ? operator.hashCode() : 0);
        return result;
    }

    public static WCN2Alarm createAlarm(String name, int type, Operator operator,
                                        float value, List<SensorData> sensorData)
    {
        switch (type)
        {
        case 0:
            return new WCN2TemperatureAlarm(name, operator, value, sensorData);
        case 1:
            return new WCN2HumidityAlarm(name, operator, value, sensorData);
        case 2:
            return new WCN2PresenceAlarm(name, operator, value, sensorData);
        }

        return null;
    }
}
