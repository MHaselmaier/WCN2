package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2.OverviewActivity;
import de.hs_kl.wcn2_sensors.WCN2Scanner;
import de.hs_kl.wcn2.util.Constants;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;
import de.hs_kl.wcn2.util.WCN2Notifications;
import de.hs_kl.wcn2_sensors.WCN2SensorData;
import de.hs_kl.wcn2_sensors.WCN2SensorDataListener;

public class MeasurementService extends Service implements WCN2SensorDataListener
{
    public static final String ACTION_START = "de.hs_kl.wcn2.fragments.sensor_tracking.MeasurementService.START";
    public static final String ACTION_STOP = "de.hs_kl.wcn2.fragments.sensor_tracking.MeasurementService.STOP";

    public static String action = "";
    public static long startTime = Long.MIN_VALUE;

    private Measurement measurement;
    private PowerManager.WakeLock wakeLock;

    private  TrackedSensorsStorage trackedSensorsStorage;
    private List<WCN2SensorData> trackedSensors = new ArrayList<>();
    private Handler handler = new Handler();
    private Runnable checkSensorDataContinuity = () ->
    {
        Context context = getBaseContext();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        for (int i = this.trackedSensors.size() - 1; 0 <= i; --i)
        {
            WCN2SensorData sensorData = this.trackedSensors.get(i);
          if (!TrackedSensorsStorage.getInstance(context).isTracked(sensorData))
          {
              this.trackedSensors.remove(sensorData);
              notificationManager.cancel(sensorData.getSensorID() << 1);
              notificationManager.cancel(sensorData.getSensorID() << 1 + 1);
              continue;
          }

          if (sensorData.isTimedOut())
          {
              Notification notification = WCN2Notifications.buildSensorDataNotification(context,
                      sensorData.getSensorID(), sensorData.getMnemonic());
              notificationManager.notify(sensorData.getSensorID() << 1, notification);
          }
          else
          {
              notificationManager.cancel(sensorData.getSensorID() << 1);
          }

          if (sensorData.isBatteryLow())
          {
              Notification notification = WCN2Notifications.buildSensorBatteryLowNotification(context,
                      sensorData.getSensorID(), sensorData.getMnemonic());
              notificationManager.notify(sensorData.getSensorID() << 1 + 1, notification);
          }
          else
          {
              notificationManager.cancel(sensorData.getSensorID() << 1 + 1);
          }
        }

        this.handler.postDelayed(MeasurementService.this.checkSensorDataContinuity, 1000);
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (OverviewActivity.isVisible) return;

            try
            {
                int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                int locationMode = Settings.Secure.getInt(getContentResolver(),
                        Settings.Secure.LOCATION_MODE);
                if (BluetoothAdapter.STATE_OFF == bluetoothState ||
                        Settings.Secure.LOCATION_MODE_OFF == locationMode)
                {
                    WCN2Scanner.setBluetoothLeScanner(null);
                    intent = new Intent(MeasurementService.this, OverviewActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
            catch (Exception e) {}
        }
    };

    @Override
    public void onCreate()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        registerReceiver(this.broadcastReceiver, filter);

        this.trackedSensorsStorage = TrackedSensorsStorage.getInstance(getBaseContext());

        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        if (null != powerManager)
        {
            this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    MeasurementService.class.getSimpleName());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (null != intent.getAction())
        {
            switch (intent.getAction())
            {
            case MeasurementService.ACTION_START:
                onActionStart(intent);
                break;
            case MeasurementService.ACTION_STOP:
                onActionStop();
                break;
            }
        }

        return Service.START_STICKY;
    }

    private void onActionStart(Intent intent)
    {
        WCN2Scanner.registerSensorDataListener(this);
        String filename = intent.getStringExtra(Constants.MEASUREMENT_FILENAME);
        String header = intent.getStringExtra(Constants.MEASUREMENT_HEADER);
        int averageRate = intent.getIntExtra(Constants.MEASUREMENT_RATE, 1);
        this.measurement = new Measurement(getBaseContext(), header, filename, averageRate);
        MeasurementService.action = "";
        MeasurementService.startTime = System.currentTimeMillis();

        Notification notification = WCN2Notifications.buildMeasurementNotification(this, header);
        startForeground(Constants.MEASUREMENT_ID, notification);

        if (!this.wakeLock.isHeld())
        {
            this.wakeLock.acquire();
        }

        this.trackedSensors = TrackedSensorsStorage.getInstance(getBaseContext()).getTrackedSensors();
        this.handler.removeCallbacks(this.checkSensorDataContinuity);
        this.handler.postDelayed(this.checkSensorDataContinuity, 1000);
    }

    private void onActionStop()
    {
        this.measurement.finish();
        WCN2Scanner.unregisterSensorDataListener(this);
        MeasurementService.action = "";
        MeasurementService.startTime = Long.MIN_VALUE;

        this.wakeLock.release();

        this.handler.removeCallbacks(this.checkSensorDataContinuity);

        cancelAllSensorNotifications();
    }

    private void cancelAllSensorNotifications()
    {
        Context context = getBaseContext();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        for (WCN2SensorData sensorData: this.trackedSensors)
        {
            notificationManager.cancel(sensorData.getSensorID() << 1);
            notificationManager.cancel(sensorData.getSensorID() << 1 + 1);
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onDestroy()
    {
        onActionStop();

        unregisterReceiver(this.broadcastReceiver);
        stopSelf();
    }

    @Override
    public List<ScanFilter> getScanFilter()
    {
        TrackedSensorsStorage trackedSensors = TrackedSensorsStorage.getInstance(getBaseContext());
        List<ScanFilter> scanFilters = new ArrayList<>();
        for (WCN2SensorData sensorData: trackedSensors.getTrackedSensors())
        {
            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setDeviceAddress(sensorData.getMacAddress());
            scanFilters.add(builder.build());
        }
        return scanFilters;
    }

    @Override
    public void onScanResult(WCN2SensorData result)
    {
        result.setMnemonic(this.trackedSensorsStorage.getMnemonic(result.getMacAddress()));
        this.measurement.addData(result, MeasurementService.action);

        updateLastTrackedSensorData(result);
    }

    private void updateLastTrackedSensorData(WCN2SensorData result)
    {
        for (int i = 0; this.trackedSensors.size() > i; ++i)
        {
            if (this.trackedSensors.get(i).getMacAddress().equals(result.getMacAddress()))
            {
                this.trackedSensors.set(i, result);
                return;
            }
        }
        this.trackedSensors.add(result);
    }
}
