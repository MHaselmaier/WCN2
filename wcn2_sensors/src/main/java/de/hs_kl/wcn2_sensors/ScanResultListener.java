package de.hs_kl.wcn2_sensors;

import android.bluetooth.le.ScanFilter;

import java.util.List;

public interface ScanResultListener
{
    List<ScanFilter> getScanFilter();

    void onScanResult(SensorData result);
}