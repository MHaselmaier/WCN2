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

public class AlarmNotifications
{
    private final static String SERVICE_CHANNEL_ID = "AlarmTriggerService";
    private final static String ALARM_CHANNEL_GROUP_ID = "Alarms";

    private final static int LIGHTS_COLOR = 0xFFFF0000;
    private final static long[] VIBRATION_PATTERN = {0, 500};

    static void createServiceNotificationChannel(Context context)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        createNotificationChannel(context, NotificationManager.IMPORTANCE_LOW,
            AlarmNotifications.SERVICE_CHANNEL_ID, "AlarmTriggerService",
                "This channel is used to indicate to the user, that his alarms are monitored.", null,
                false, false);
    }

    public static void createAlarmNotificationChannel(Context context, String alarm)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        createAlarmNotificationGroup(context);
        createNotificationChannel(context, NotificationManager.IMPORTANCE_HIGH,
                alarm, alarm,
                    "This channel is used to notify the user when the " + alarm + " alarm is triggered",
                AlarmNotifications.ALARM_CHANNEL_GROUP_ID, true, true);
    }

    private static void createAlarmNotificationGroup(Context context)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager manager = (NotificationManager)context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (null == manager) return;

        manager.createNotificationChannelGroup(new NotificationChannelGroup(
                AlarmNotifications.ALARM_CHANNEL_GROUP_ID, "Alarms"));
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
               .setContentTitle("Test")
               .setContentText("Test")
               .setContentIntent(pendingIntent)
               .setSmallIcon(R.drawable.ic_more);
        return builder.build();
    }

    static Notification createAlarmNotification(Context context, WCN2Alarm alarm)
    {
        createAlarmNotificationChannel(context, alarm.getName());

        Intent i = new Intent(context, OverviewActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, 0);

        Notification.Builder builder = createNotificationBuilder(context, alarm.getName());
        builder.setContentTitle(alarm.getName() + " triggered!")
                .setSmallIcon(R.drawable.ic_add)
                .setContentText(alarm.getName() + " was triggered!")
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

    private static Notification.Builder createNotificationBuilder(Context context, String id)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        {
            return new Notification.Builder(context);
        }
        else
        {
            return new Notification.Builder(context, id);
        }
    }
}
