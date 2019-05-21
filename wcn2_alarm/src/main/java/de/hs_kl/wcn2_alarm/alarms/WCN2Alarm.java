package de.hs_kl.wcn2_alarm.alarms;

import android.bluetooth.le.ScanFilter;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2_sensors.ScanResultListener;
import de.hs_kl.wcn2_sensors.SensorData;
import de.hs_kl.wcn2_sensors.WCN2Scanner;

public class WCN2Alarm implements ScanResultListener
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

    private List<SensorData> sensors;
    private String name;
    private List<Threshold> thresholds;
    private boolean activated;

    public WCN2Alarm(String name, List<Threshold> thresholds, List<SensorData> sensors)
    {
        this.name = name;
        this.thresholds = thresholds;
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

    public List<SensorData> getSensorData()
    {
        return this.sensors;
    }

    public boolean isTriggered()
    {
        for (SensorData sensorData: this.sensors)
        {
            for (Threshold threshold: this.thresholds)
            {
                if (threshold.isTriggered(sensorData))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Threshold> getThresholds()
    {
        return this.thresholds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WCN2Alarm alarm = (WCN2Alarm) o;

        if (activated != alarm.activated) return false;
        if (sensors != null ? !sensors.equals(alarm.sensors) : alarm.sensors != null) return false;
        if (name != null ? !name.equals(alarm.name) : alarm.name != null) return false;
        return thresholds != null ? thresholds.equals(alarm.thresholds) : alarm.thresholds == null;
    }

    @Override
    public int hashCode() {
        int result = sensors != null ? sensors.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (thresholds != null ? thresholds.hashCode() : 0);
        result = 31 * result + (activated ? 1 : 0);
        return result;
    }
}
