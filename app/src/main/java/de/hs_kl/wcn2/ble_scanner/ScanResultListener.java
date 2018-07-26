package de.hs_kl.wcn2.ble_scanner;

import android.bluetooth.le.ScanFilter;

import java.util.List;

public interface ScanResultListener
{
    public List<ScanFilter> getScanFilter();

    public void onScanResult(SensorData result);
}