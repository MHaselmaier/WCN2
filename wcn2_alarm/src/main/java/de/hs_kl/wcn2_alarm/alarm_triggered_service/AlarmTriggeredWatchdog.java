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
            alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000, 60000,
                    PendingIntent.getForegroundService(context, 0,
                            createStartServiceIntent(context), 0));
        }
        else
        {
            context.startService(createStartServiceIntent(context));
            alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000, 60000,
                    PendingIntent.getService(context, 0,
                            createStartServiceIntent(context), 0));
        }
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