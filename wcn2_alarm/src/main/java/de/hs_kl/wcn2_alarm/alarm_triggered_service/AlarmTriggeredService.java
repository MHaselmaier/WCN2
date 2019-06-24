package de.hs_kl.wcn2_alarm.alarm_triggered_service;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;

import de.hs_kl.wcn2_alarm.AlarmStorage;
import de.hs_kl.wcn2_alarm.OverviewActivity;
import de.hs_kl.wcn2_alarm.alarms.WCN2Alarm;
import de.hs_kl.wcn2_sensors.WCN2Scanner;

public class AlarmTriggeredService extends Service
{
    private BroadcastReceiver btAdapterChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            BluetoothManager btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            if (null != btManager)
            {
                BluetoothAdapter btAdapter = btManager.getAdapter();
                if (null != btAdapter && btAdapter.isEnabled()) return;
            }

            startActivityToEnableBluetoothAndLocationService();
        }
    };

    private BroadcastReceiver locationModeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            if (null != lm && lm.isLocationEnabled())
                return;

            startActivityToEnableBluetoothAndLocationService();
        }
    };

    private void startActivityToEnableBluetoothAndLocationService()
    {
        if (OverviewActivity.isVisible) return;

        Intent intent = new Intent(this, OverviewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private Handler handler = new Handler();

    @Override
    public void onCreate()
    {
        registerReceiver(this.btAdapterChangeReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(this.locationModeChangeReceiver,
                new IntentFilter((LocationManager.MODE_CHANGED_ACTION)));

        setupWCN2Scanner();

        AlarmNotifications.createServiceNotificationChannel(this);
    }

    private void setupWCN2Scanner()
    {
        BluetoothManager btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        if (null == btManager)
        {
            startActivityToEnableBluetoothAndLocationService();
            return;
        }

        BluetoothAdapter btAdapter = btManager.getAdapter();
        if (null == btAdapter || !btAdapter.isEnabled())
        {
            startActivityToEnableBluetoothAndLocationService();
            return;
        }

        WCN2Scanner.setBluetoothLeScanner(btAdapter.getBluetoothLeScanner());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        startForeground(1, AlarmNotifications.createServiceNotification(this));

        this.handler.removeCallbacksAndMessages(null);
        this.handler.post(this::checkAlarms);

        return Service.START_STICKY;
    }

    private void checkAlarms()
    {
        boolean noActivatedAlarms = true;
        for (WCN2Alarm alarm: AlarmStorage.getInstance(getApplicationContext()).getAlarms())
        {
            if (alarm.isActivated())
            {
                noActivatedAlarms = false;
            }
            if (alarm.isTriggered())
            {
                showNotificationForAlarm(alarm);
            }
        }

        if (noActivatedAlarms)
        {
            stopSelf();
        }
        else
        {
            this.handler.postDelayed(this::checkAlarms, 1000);
        }
    }

    public void showNotificationForAlarm(WCN2Alarm alarm)
    {
        NotificationManager manager = (NotificationManager)getSystemService(
                Activity.NOTIFICATION_SERVICE);
        if (null == manager) return;

        if (!OverviewActivity.isVisible)
            manager.notify(alarm.hashCode(), AlarmNotifications.createAlarmNotification(this, alarm));
        else
            manager.cancel(alarm.hashCode());
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onDestroy()
    {
        this.handler.removeCallbacksAndMessages(null);
        unregisterReceiver(this.btAdapterChangeReceiver);
        unregisterReceiver(this.locationModeChangeReceiver);
        stopSelf();
    }
}
