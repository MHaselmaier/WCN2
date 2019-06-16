package de.hs_kl.wcn2_alarm.alarms;

import android.bluetooth.le.ScanFilter;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2_sensors.WCN2Scanner;
import de.hs_kl.wcn2_sensors.WCN2SensorData;
import de.hs_kl.wcn2_sensors.WCN2SensorDataListener;

public class WCN2Alarm implements WCN2SensorDataListener
{
    @Override
    public List<ScanFilter> getScanFilter()
    {
        List<ScanFilter> scanFilters = new ArrayList<>();
        for (WCN2SensorData sensorData: this.sensors)
        {
            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setDeviceAddress(sensorData.getMacAddress());
            scanFilters.add(builder.build());
        }
        return scanFilters;
    }

    @Override
    public void onScanResult(WCN2SensorData result)
    {
        int index = this.sensors.indexOf(result);
        if (-1 != index)
        {
            this.sensors.set(index, result);
        }
    }

    private List<WCN2SensorData> sensors;
    private String name;
    private Uri sound;
    private List<Threshold> thresholds;
    private boolean activated;

    public WCN2Alarm(String name, Uri sound, List<Threshold> thresholds, List<WCN2SensorData> sensors)
    {
        this.name = name;
        this.sound = sound;
        this.thresholds = thresholds;
        this.sensors = sensors;
    }

    public String getName()
    {
        return this.name;
    }

    public Uri getSound()
    {
        return this.sound;
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

    public List<WCN2SensorData> getSensorData()
    {
        return this.sensors;
    }

    public boolean isTriggered()
    {
        if (!this.activated) return false;

        for (WCN2SensorData sensorData: this.sensors)
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
        if (sound != null ? !sound.equals(alarm.sound) : alarm.sound != null) return false;
        return thresholds != null ? thresholds.equals(alarm.thresholds) : alarm.thresholds == null;
    }

    @Override
    public int hashCode() {
        int result = sensors != null ? sensors.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (sound != null ? sound.hashCode() : 0);
        result = 31 * result + (thresholds != null ? thresholds.hashCode() : 0);
        result = 31 * result + (activated ? 1 : 0);
        return result;
    }
}
