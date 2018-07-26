package de.hs_kl.wcn2.ble_scanner;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hs_kl.wcn2.util.Constants;

public class BLEScanner
{
    private static Context context;
    private static BluetoothLeScanner bleScanner;
    private static Set<ScanResultListener> scanResultListeners = new HashSet<>();

    private static ScanCallback scanCallback = new ScanCallback() {
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
                    listener.onScanResult(new SensorData(result, BLEScanner.context));
                    continue;
                }
                for (ScanFilter scanFilter: listener.getScanFilter())
                {
                    if (scanFilter.matches(result))
                    {
                        listener.onScanResult(new SensorData(result, BLEScanner.context));
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
                    Log.d(BLEScanner.class.getSimpleName(),
                            "Failed to start scanning: already scanning!");
                    break;
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    Log.d(BLEScanner.class.getSimpleName(),
                            "Failed to start scanning: app cannot be registered!");
                    break;
                case SCAN_FAILED_FEATURE_UNSUPPORTED:
                    Log.d(BLEScanner.class.getSimpleName(),
                            "Failed to start scanning: power optimized scan not supported!");
                    break;
                case SCAN_FAILED_INTERNAL_ERROR:
                    Log.d(BLEScanner.class.getSimpleName(),
                            "Failed to start scanning: internal error!");
                    break;
                default:
                    Log.d(BLEScanner.class.getSimpleName(),
                            "Failed to start scanning!");
                    break;
            }
        }
    };

    public static void setContext(Context context)
    {
        BLEScanner.context = context;
    }

    public static void setBluetoothLeScanner(BluetoothLeScanner bleScanner)
    {
        BLEScanner.stopScan();
        BLEScanner.bleScanner = bleScanner;
        if (0 < BLEScanner.scanResultListeners.size())
        {
            BLEScanner.startScan();
        }
    }

    public static void registerScanResultListener(ScanResultListener scanResultlistener)
    {
        BLEScanner.scanResultListeners.add(scanResultlistener);

        if (1 == BLEScanner.scanResultListeners.size())
        {
            BLEScanner.startScan();
        }
    }

    public static void unregisterScanResultListener(ScanResultListener scanResultListener)
    {
        BLEScanner.scanResultListeners.remove(scanResultListener);

        if (0 == BLEScanner.scanResultListeners.size())
        {
            BLEScanner.stopScan();
        }
    }

    private static void startScan()
    {
        if (null != BLEScanner.bleScanner)
        {
            BLEScanner.bleScanner.startScan(getScanFilters(), getScanSettings(), BLEScanner.scanCallback);
        }
    }

    private static void stopScan()
    {
        if (null != BLEScanner.bleScanner)
        {
            try
            {
                BLEScanner.bleScanner.stopScan(BLEScanner.scanCallback);
            }
            catch(IllegalStateException e) {}
        }
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
        builder.setReportDelay(0);
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
