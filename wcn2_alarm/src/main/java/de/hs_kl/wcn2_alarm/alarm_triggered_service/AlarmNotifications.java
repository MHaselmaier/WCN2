package de.hs_kl.wcn2_alarm.alarm_triggered_service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import de.hs_kl.wcn2_alarm.OverviewActivity;
import de.hs_kl.wcn2_alarm.R;
import de.hs_kl.wcn2_alarm.alarms.WCN2Alarm;
import de.hs_kl.wcn2_sensors.WCN2SensorData;

public class AlarmNotifications
{
    private final static String SERVICE_CHANNEL_ID = "AlarmTriggerService";
    private final static String ALARM_CHANNEL_GROUP_ID = "Alarms";
    private final static String BATTERY_LOW_CHANNEL_ID = "BatterLow";

    private final static int LIGHTS_COLOR = 0xFFFF0000;
    private final static long[] VIBRATION_PATTERN = {0, 500};

    static void createServiceNotificationChannel(Context context)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        createNotificationChannel(context,
                NotificationManager.IMPORTANCE_LOW,
                AlarmNotifications.SERVICE_CHANNEL_ID,
                context.getString(R.string.alarm_trigger_service),
                context.getString(R.string.alarm_trigger_service_description),
                null,
                false,
                false);
    }

    static void createBatteryLowNotificationChannel(Context context)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        createNotificationChannel(context,
                NotificationManager.IMPORTANCE_HIGH,
                AlarmNotifications.BATTERY_LOW_CHANNEL_ID,
                context.getString(R.string.battery_low),
                context.getString(R.string.battery_low_description),
                null,
                true,
                true);
    }

    public static void createAlarmNotificationChannel(Context context, String alarm)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        createAlarmNotificationGroup(context);
        createNotificationChannel(context,
                NotificationManager.IMPORTANCE_HIGH,
                alarm,
                alarm,
                context.getString(R.string.alarm_channel_description, alarm),
                AlarmNotifications.ALARM_CHANNEL_GROUP_ID,
                true,
                true);
    }

    private static void createAlarmNotificationGroup(Context context)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager manager = (NotificationManager)context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (null == manager) return;

        manager.createNotificationChannelGroup(new NotificationChannelGroup(
                AlarmNotifications.ALARM_CHANNEL_GROUP_ID,
                context.getString(R.string.alarm_group_channel)));
    }

    private static void createNotificationChannel(Context context, int importance, String id,
                                                  String name, String description, String groupID,
                                                  boolean showLights, boolean vibrate)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager manager = (NotificationManager)context
                .getSystemService(Activity.NOTIFICATION_SERVICE);
        if (null == manager || null != manager.getNotificationChannel(id)) return;

        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        channel.setGroup(groupID);
        if (showLights)
        {
            channel.setLightColor(AlarmNotifications.LIGHTS_COLOR);
            channel.enableLights(true);
        }
        if (vibrate)
        {
            channel.setVibrationPattern(AlarmNotifications.VIBRATION_PATTERN);
            channel.enableVibration(true);
        }

        manager.createNotificationChannel(channel);
    }

    static Notification createServiceNotification(Context context)
    {
        Intent i = new Intent(context, OverviewActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, 0);

        Notification.Builder builder = createNotificationBuilder(context,
                AlarmNotifications.SERVICE_CHANNEL_ID);
        builder.setOngoing(true)
               .setOnlyAlertOnce(true)
               .setContentTitle(context.getString(R.string.alarm_trigger_service))
               .setContentText(context.getString(R.string.alarm_trigger_service_content))
               .setContentIntent(pendingIntent)
               .setSmallIcon(R.drawable.ic_alarms_monitored);
        return builder.build();
    }

    static Notification createAlarmNotification(Context context, WCN2Alarm alarm)
    {
        createAlarmNotificationChannel(context, alarm.getName());

        Intent i = new Intent(context, OverviewActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, 0);

        Notification.Builder builder = createNotificationBuilder(context, alarm.getName());
        builder.setContentTitle(context.getString(R.string.alarm_triggered, alarm.getName()))
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentText(context.getString(R.string.alarm_triggered_content, alarm.getName()))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        {
            builder.setLights(AlarmNotifications.LIGHTS_COLOR, 500, 500);
            builder.setVibrate(AlarmNotifications.VIBRATION_PATTERN);
            if (null != alarm.getSound())
            {
                builder.setSound(alarm.getSound());
            }
        }

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_INSISTENT;
        return notification;
    }

    static Notification createBatteryLowNotification(Context context, WCN2SensorData sensorData)
    {
        Intent i = new Intent(context, OverviewActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, 0);

        Notification.Builder builder = createNotificationBuilder(context,
                AlarmNotifications.BATTERY_LOW_CHANNEL_ID);
        builder.setContentTitle(context.getString(R.string.battery_low))
                .setSmallIcon(R.drawable.ic_battery_low)
                .setContentText(context.getString(R.string.battery_low_content,
                        sensorData.getSensorID()))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        {
            builder.setLights(AlarmNotifications.LIGHTS_COLOR, 500, 500);
            builder.setVibrate(AlarmNotifications.VIBRATION_PATTERN);
        }

        return builder.build();
    }

    private static Notification.Builder createNotificationBuilder(Context context, String id)
    {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        {
            builder = new Notification.Builder(context);
        }
        else
        {
            builder = new Notification.Builder(context, id);
        }
        return builder;
    }
}
