package de.hs_kl.wcn2.ble_scanner;

import android.bluetooth.le.ScanFilter;

import java.util.List;

public interface ScanResultListener
{
    List<ScanFilter> getScanFilter();

    void onScanResult(SensorData result);
}