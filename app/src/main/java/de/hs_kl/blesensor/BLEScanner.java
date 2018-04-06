package de.hs_kl.blesensor;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;

public class BLEScanner
{
    private BluetoothLeScanner bleScanner;

    public BLEScanner(BluetoothLeScanner bleScanner)
    {
        this.bleScanner = bleScanner;
    }

    public void scanForSensors(ScanCallback callback)
    {
        scanForSensorsForXMillis(callback, 10000);
    }

    public void scanForSensorsForXMillis(final ScanCallback callback, int millis)
    {
        this.bleScanner.startScan(buildScanFilters(), buildScanSettings(), callback);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                BLEScanner.this.bleScanner.stopScan(callback);
            }
        }, millis);
    }

    public void scanForSensorData(ScanCallback callback, List<String> deviceAddresses)
    {
        List<ScanFilter> filters = buildScanFiltersForSpecificDevices(deviceAddresses);
        ScanSettings settings = buildScanSettings();
        this.bleScanner.startScan(filters, settings, callback);
    }

    private List<ScanFilter> buildScanFilters()
    {
        List<ScanFilter> scanFilters = new ArrayList<>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setManufacturerData(Constants.MANUFACTURER_ID, new byte[]{});
        scanFilters.add(builder.build());
        return scanFilters;
    }

    private List<ScanFilter> buildScanFiltersForSpecificDevices(List<String> deviceAddresses)
    {
        List<ScanFilter> scanFilters = new ArrayList<>();
        for (String deviceAddress: deviceAddresses)
        {
            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setManufacturerData(Constants.MANUFACTURER_ID, new byte[]{});
            builder.setDeviceAddress(deviceAddress);
            scanFilters.add(builder.build());
        }
        return scanFilters;
    }

    private ScanSettings buildScanSettings()
    {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setReportDelay(0);
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
            builder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
            builder.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            builder.setLegacy(false);
            builder.setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED);
        }
        return builder.build();
    }

    public void stopScanning()
    {
        stopScanning(null);
    }

    public void stopScanning(ScanCallback callback)
    {
        this.bleScanner.stopScan(callback);
    }
}
