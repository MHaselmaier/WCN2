package de.hs_kl.wcn2_sensors;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BLEScanner
{
    private static BluetoothLeScanner bleScanner;
    private static Set<ScanResultListener> scanResultListeners = new HashSet<>();

    private static ScanCallback scanCallback = new ScanCallback()
    {
        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            super.onBatchScanResults(results);

            for (ScanResult result: results)
            {
                dispatchScanResult(result);
            }
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            super.onScanResult(callbackType, result);

            dispatchScanResult(result);
        }

        private void dispatchScanResult(ScanResult result)
        {
            for (ScanResultListener listener: BLEScanner.scanResultListeners)
            {
                if (0 == listener.getScanFilter().size())
                {
                    listener.onScanResult(new SensorData(result));
                    continue;
                }
                for (ScanFilter scanFilter: listener.getScanFilter())
                {
                    if (scanFilter.matches(result))
                    {
                        listener.onScanResult(new SensorData(result));
                        break;
                    }
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode)
        {
            super.onScanFailed(errorCode);
            switch(errorCode)
            {
                case SCAN_FAILED_ALREADY_STARTED:
                    Log.e(BLEScanner.class.getSimpleName(),
                            "Failed to start scanning: already scanning!");
                    break;
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    Log.e(BLEScanner.class.getSimpleName(),
                            "Failed to start scanning: app cannot be registered!");
                    break;
                case SCAN_FAILED_FEATURE_UNSUPPORTED:
                    Log.e(BLEScanner.class.getSimpleName(),
                            "Failed to start scanning: power optimized scan not supported!");
                    break;
                case SCAN_FAILED_INTERNAL_ERROR:
                    Log.e(BLEScanner.class.getSimpleName(),
                            "Failed to start scanning: internal error!");
                    break;
                default:
                    Log.e(BLEScanner.class.getSimpleName(),
                            "Failed to start scanning!");
                    break;
            }
        }
    };

    public static void setBluetoothLeScanner(BluetoothLeScanner bleScanner)
    {
        stopScan();
        BLEScanner.bleScanner = bleScanner;
        if (0 < BLEScanner.scanResultListeners.size())
        {
            startScan();
        }
    }

    public static void registerScanResultListener(ScanResultListener scanResultlistener)
    {
        BLEScanner.scanResultListeners.add(scanResultlistener);

        if (1 == BLEScanner.scanResultListeners.size())
        {
            startScan();
        }
    }

    public static void unregisterScanResultListener(ScanResultListener scanResultListener)
    {
        BLEScanner.scanResultListeners.remove(scanResultListener);

        if (0 == BLEScanner.scanResultListeners.size())
        {
            stopScan();
        }
    }

    private static void startScan()
    {
        if (null == BLEScanner.bleScanner) return;

        BLEScanner.bleScanner.startScan(getScanFilters(), getScanSettings(), BLEScanner.scanCallback);
    }

    private static void stopScan()
    {
        if (null == BLEScanner.bleScanner) return;

        try
        {
            BLEScanner.bleScanner.stopScan(BLEScanner.scanCallback);
        }
        catch(IllegalStateException e) {}
    }

    private static List<ScanFilter> getScanFilters()
    {
        List<ScanFilter> scanFilters = new ArrayList<>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setManufacturerData(SensorConstants.MANUFACTURER_ID, new byte[]{});
        scanFilters.add(builder.build());
        return scanFilters;
    }

    private static ScanSettings getScanSettings()
    {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setReportDelay(SensorConstants.BLESCANNER_REPORT_DELAY);
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
            builder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
            builder.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            builder.setLegacy(false);
            builder.setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED);
        }
        return builder.build();
    }
}
