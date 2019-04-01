package de.hs_kl.wcn2_sensors;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WCN2Scanner
{
    private static boolean scanning = false;
    private static BluetoothLeScanner bleScanner;
    private static Set<ScanResultListener> scanResultListeners = new HashSet<>();

    private static long lastScanStarted = 0;

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
            for (ScanResultListener listener: WCN2Scanner.scanResultListeners)
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
                    Log.e(WCN2Scanner.class.getSimpleName(),
                            "Failed to start scanning: already scanning!");
                    break;
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    Log.e(WCN2Scanner.class.getSimpleName(),
                            "Failed to start scanning: app cannot be registered!");
                    break;
                case SCAN_FAILED_FEATURE_UNSUPPORTED:
                    Log.e(WCN2Scanner.class.getSimpleName(),
                            "Failed to start scanning: power optimized scan not supported!");
                    break;
                case SCAN_FAILED_INTERNAL_ERROR:
                    Log.e(WCN2Scanner.class.getSimpleName(),
                            "Failed to start scanning: internal error!");
                    break;
                default:
                    Log.e(WCN2Scanner.class.getSimpleName(),
                            "Failed to start scanning!");
                    break;
            }
        }
    };

    public static void setBluetoothLeScanner(BluetoothLeScanner bleScanner)
    {
        stopScan();
        WCN2Scanner.bleScanner = bleScanner;
        if (0 < WCN2Scanner.scanResultListeners.size())
        {
            startScan();
        }
    }

    public static void registerScanResultListener(ScanResultListener scanResultlistener)
    {
        WCN2Scanner.scanResultListeners.add(scanResultlistener);

        if (1 == WCN2Scanner.scanResultListeners.size())
        {
            startScan();
        }
    }

    public static void unregisterScanResultListener(ScanResultListener scanResultListener)
    {
        WCN2Scanner.scanResultListeners.remove(scanResultListener);

        if (0 == WCN2Scanner.scanResultListeners.size())
        {
            // Keep scanning if last scan was started lass then 35 seconds ago.
            // Android denies scanning if it is started too many times in 30 seconds.
            // If a new ScanResultListener registers to early, the scan could be started
            // too many times in 30 seconds and no results would be received.
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (0 == WCN2Scanner.scanResultListeners.size())
                {
                    WCN2Scanner.stopScan();
                }
            }, Math.max(35, System.currentTimeMillis() - WCN2Scanner.lastScanStarted));
        }
    }

    private static void startScan()
    {
        if (null == WCN2Scanner.bleScanner || WCN2Scanner.scanning) return;

        WCN2Scanner.scanning = true;
        WCN2Scanner.lastScanStarted = System.currentTimeMillis();
        WCN2Scanner.bleScanner.startScan(getScanFilters(), getScanSettings(), WCN2Scanner.scanCallback);
    }

    private static void stopScan()
    {
        if (null == WCN2Scanner.bleScanner || !WCN2Scanner.scanning) return;

        WCN2Scanner.scanning = false;
        try
        {
            WCN2Scanner.bleScanner.stopScan(WCN2Scanner.scanCallback);
        }
        catch(IllegalStateException e) {}
    }

    private static List<ScanFilter> getScanFilters()
    {
        List<ScanFilter> scanFilters = new ArrayList<>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setManufacturerData(Constants.MANUFACTURER_ID, new byte[]{});
        scanFilters.add(builder.build());
        return scanFilters;
    }

    private static ScanSettings getScanSettings()
    {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setReportDelay(Constants.BLESCANNER_REPORT_DELAY);
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
