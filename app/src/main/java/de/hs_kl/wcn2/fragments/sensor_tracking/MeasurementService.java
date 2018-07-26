package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2.OverviewActivity;
import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.ble_scanner.BLEScanner;
import de.hs_kl.wcn2.ble_scanner.ScanResultListener;
import de.hs_kl.wcn2.ble_scanner.SensorData;
import de.hs_kl.wcn2.util.Constants;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

public class MeasurementService extends Service implements ScanResultListener
{
    public static final String ACTION_START = "de.hs_kl.wcn2.fragments.sensor_tracking.MeasurementService.START";
    public static final String ACTION_STOP = "de.hs_kl.wcn2.fragments.sensor_tracking.MeasurementService.STOP";

    private static Dataset dataset = new Dataset();;
    public static String action = "";
    public static long startTime = Long.MIN_VALUE;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (!OverviewActivity.isVisible)
            {
                try
                {
                    int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    int locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
                    if (BluetoothAdapter.STATE_OFF == bluetoothState || Settings.Secure.LOCATION_MODE_OFF == locationMode)
                    {
                        BLEScanner.setBluetoothLeScanner(null);
                        intent = new Intent(MeasurementService.this, OverviewActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
                catch (Exception e) {}
            }
        }
    };

    @Override
    public void onCreate()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        registerReceiver(this.broadcastReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        switch (intent.getAction())
        {
        case MeasurementService.ACTION_START:
            BLEScanner.registerScanResultListener(this);
            MeasurementService.dataset.clear();
            MeasurementService.dataset.setMeasurementHeader(intent.getStringExtra(Constants.MEASUREMENT_HEADER));
            MeasurementService.action = "";
            MeasurementService.startTime = System.currentTimeMillis();

            startForeground(Constants.NOTIFICATION_ID, createNotification(intent.getStringExtra(Constants.MEASUREMENT_HEADER)));

            break;
        case MeasurementService.ACTION_STOP:
            MeasurementService.dataset.writeToFile(this);
            BLEScanner.unregisterScanResultListener(this);
            MeasurementService.action = "";
            MeasurementService.startTime = Long.MIN_VALUE;
            break;
        }

        return Service.START_STICKY;
    }

    private Notification createNotification(String measurementHeader)
    {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationManager nm = (NotificationManager)getSystemService(Activity.NOTIFICATION_SERVICE);
            if(nm != null)
            {
                NotificationChannel nChannel = nm.getNotificationChannel(Constants.NOTIFICATION_CHANNEL_ID);
                if (nChannel == null)
                {
                    nChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, getString(R.string.notification_title),  NotificationManager.IMPORTANCE_HIGH);
                    nChannel.setDescription(getString(R.string.notification_description));
                    nm.createNotificationChannel(nChannel);
                }
            }
            builder = new Notification.Builder(this, Constants.NOTIFICATION_CHANNEL_ID);
        }
        else
        {
            builder = new Notification.Builder(this);
        }

        Intent intent = new Intent(this, OverviewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        builder.setOngoing(true)
               .setSmallIcon(R.drawable.ic_measurement)
               .setContentTitle(getString(R.string.notification_title))
               .setContentText(measurementHeader)
               .setContentIntent(pendingIntent);
        return builder.build();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(this.broadcastReceiver);
        MeasurementService.dataset.writeToFile(this);
        MeasurementService.action = "";
        MeasurementService.startTime = Long.MIN_VALUE;
        BLEScanner.unregisterScanResultListener(this);
        stopSelf();
    }

    @Override
    public List<ScanFilter> getScanFilter()
    {
        List<ScanFilter> scanFilters = new ArrayList<>();
        for (SensorData sensorData: TrackedSensorsStorage.getTrackedSensors(this))
        {
            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setDeviceAddress(sensorData.getMacAddress());
            scanFilters.add(builder.build());
        }
        return scanFilters;
    }

    @Override
    public void onScanResult(SensorData result)
    {
        DatasetEntry entry = new DatasetEntry(result.getSensorID(), result.getMacAddress(),
                result.getTemperature(), result.getRelativeHumidity(), MeasurementService.action,
                result.getTimestamp() - MeasurementService.startTime);
        MeasurementService.dataset.add(entry);
    }
}
