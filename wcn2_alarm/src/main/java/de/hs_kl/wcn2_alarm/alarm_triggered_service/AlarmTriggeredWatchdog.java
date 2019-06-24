package de.hs_kl.wcn2_alarm.alarm_triggered_service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import de.hs_kl.wcn2_alarm.AlarmStorage;

public class AlarmTriggeredWatchdog extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (null == action ||
            (!intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) &&
             !intent.getAction().equals(Intent.ACTION_LOCKED_BOOT_COMPLETED) &&
             !intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON") &&
             !intent.getAction().equals("com.htc.intent.action.QUICKBOOT_POWERON")))
            return;

        context = context.getApplicationContext();

        if (0 == AlarmStorage.getInstance(context).getAlarms().length)
            return;

        enable(context);
    }

    public static void enable(Context context)
    {
        context = context.getApplicationContext();

        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (null == alarmMgr)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            context.startForegroundService(createStartServiceIntent(context));
        }
        else
        {
            context.startService(createStartServiceIntent(context));
        }

        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                PendingIntent.getService(context, 0,
                        createStartServiceIntent(context), 0));
    }

    public static void disable(Context context)
    {
        context = context.getApplicationContext();

        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (null == alarmMgr)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            alarmMgr.cancel(PendingIntent.getForegroundService(context, 0,
                    createStartServiceIntent(context), 0));
        }
        else
        {
            alarmMgr.cancel(PendingIntent.getService(context, 0,
                    createStartServiceIntent(context), 0));
        }
    }

    private static Intent createStartServiceIntent(Context context)
    {
        return new Intent(context, AlarmTriggeredService.class);
    }
}