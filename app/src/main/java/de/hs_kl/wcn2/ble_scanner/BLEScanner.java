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
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

public class BLEScanner
{
    private static BLEScanner instance;

    private BluetoothLeScanner bleScanner;
    private Set<ScanResultListener> scanResultListeners = new HashSet<>();
    private TrackedSensorsStorage trackedSensors;

    private BLEScanner(Context context)
    {
        this.trackedSensors = TrackedSensorsStorage.getInstance(context);
    }

    private ScanCallback scanCallback = new ScanCallback()
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
            for (ScanResultListener listener: BLEScanner.this.scanResultListeners)
            {
                String mnemonic = BLEScanner.this.trackedSensors.getMnemonic(result.getDevice()
                        .getAddress());
                if (0 == listener.getScanFilter().size())
                {
                    listener.onScanResult(new SensorData(result, mnemonic));
                    continue;
                }
                for (ScanFilter scanFilter: listener.getScanFilter())
                {
                    if (scanFilter.matches(result))
                    {
                        listener.onScanResult(new SensorData(result, mnemonic));
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

    public void setBluetoothLeScanner(BluetoothLeScanner bleScanner)
    {
        stopScan();
        this.bleScanner = bleScanner;
        if (0 < this.scanResultListeners.size())
        {
            startScan();
        }
    }

    public void registerScanResultListener(ScanResultListener scanResultlistener)
    {
        this.scanResultListeners.add(scanResultlistener);

        if (1 == this.scanResultListeners.size())
        {
            startScan();
        }
    }

    public void unregisterScanResultListener(ScanResultListener scanResultListener)
    {
        this.scanResultListeners.remove(scanResultListener);

        if (0 == this.scanResultListeners.size())
        {
            stopScan();
        }
    }

    private void startScan()
    {
        if (null == this.bleScanner) return;

        this.bleScanner.startScan(getScanFilters(), getScanSettings(), this.scanCallback);
    }

    private void stopScan()
    {
        if (null == this.bleScanner) return;

        try
        {
            this.bleScanner.stopScan(this.scanCallback);
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

    public static BLEScanner getInstance(Context context)
    {
        if (null == BLEScanner.instance)
        {
            BLEScanner.instance = new BLEScanner(context);
        }

        return BLEScanner.instance;
    }
}
