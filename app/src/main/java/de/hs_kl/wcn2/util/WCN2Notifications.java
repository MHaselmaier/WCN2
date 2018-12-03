package de.hs_kl.wcn2.util;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import de.hs_kl.wcn2.OverviewActivity;
import de.hs_kl.wcn2.R;

public class WCN2Notifications
{
    public static Notification buildMeasurementNotification(Context context, String measurementHeader)
    {
        String name = context.getString(R.string.measurement_channel_name);
        String description = context.getString(R.string.measurement_channel_description);
        createNotificationChannel(context, Constants.MEASUREMENT_CHANNEL_ID, name, description);
        Notification.Builder builder = getNotificationBuilder(context, Constants.MEASUREMENT_CHANNEL_ID);

        Intent intent = new Intent(context, OverviewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        builder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_measurement)
                .setContentTitle(context.getString(R.string.measurement_notification_title))
                .setContentText(measurementHeader)
                .setContentIntent(pendingIntent);

        return builder.build();
    }

    public static Notification buildSensorDataNotification(Context context, byte sensorID,
                                                           String mnemonic)
    {
        String name = context.getString(R.string.sensor_data_channel_name);
        String description = context.getString(R.string.sensor_data_channel_description);
        createNotificationChannel(context, Constants.SENSOR_DATA_CHANNEL_ID, name, description);
        Notification.Builder builder = getNotificationBuilder(context, Constants.SENSOR_DATA_CHANNEL_ID);

        Intent intent = new Intent(context, OverviewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        String content = context.getString(R.string.sensor_data_content_mnemonic, sensorID,
                mnemonic);
        if (mnemonic.equals("null"))
        {
            content = context.getString(R.string.sensor_data_content, sensorID);
        }
        builder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_sensor_warning)
                .setContentTitle(name)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true);

        return builder.build();
    }

    public static Notification buildSensorBatteryLowNotification(Context context, byte sensorID,
                                                                 String mnemonic)
    {
        String name = context.getString(R.string.sensor_battery_low_channel_name);
        String description = context.getString(R.string.sensor_battery_low_channel_description);
        createNotificationChannel(context, Constants.SENSOR_BATTERY_LOW_CHANNEL_ID, name, description);
        Notification.Builder builder = getNotificationBuilder(context, Constants.SENSOR_BATTERY_LOW_CHANNEL_ID);

        Intent intent = new Intent(context, OverviewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        String content = context.getString(R.string.sensor_battery_low_content_mnemonic, sensorID, mnemonic);
        if (mnemonic.equals("null"))
        {
            content = context.getString(R.string.sensor_battery_low_content, sensorID);
        }
        builder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_sensor_warning)
                .setContentTitle(name)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true);

        return builder.build();
    }

    private static void createNotificationChannel(Context context, String channelID, String name,
                                                  String description)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        String service = Activity.NOTIFICATION_SERVICE;
        NotificationManager manager = (NotificationManager)context.getSystemService(service);
        if (null != manager)
        {
            if (null != manager.getNotificationChannel(channelID)) return;

            NotificationChannel channel = new NotificationChannel(channelID, name,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            manager.createNotificationChannel(channel);
        }
    }

    private static Notification.Builder getNotificationBuilder(Context context, String channelID)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        {
            return new Notification.Builder(context);
        }

        return new Notification.Builder(context, channelID);
    }
}