package de.hs_kl.blesensor;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;

import java.util.List;

public interface ScanResultListener
{
    public List<ScanFilter> getScanFilter();

    public void onScanResult(ScanResult result);
}