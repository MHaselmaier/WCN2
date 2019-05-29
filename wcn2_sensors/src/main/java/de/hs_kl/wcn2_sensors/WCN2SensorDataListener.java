package de.hs_kl.wcn2_sensors;

import android.bluetooth.le.ScanFilter;

import java.util.List;

public interface WCN2SensorDataListener
{
    List<ScanFilter> getScanFilter();

    void onScanResult(WCN2SensorData result);
}