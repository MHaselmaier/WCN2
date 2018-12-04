package de.hs_kl.wcn2.util;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import de.hs_kl.wcn2.R;

public class LastSeenSinceUtil
{
    public static String getTimeSinceString(Context context, long timeMilliseconds) {
        if (Long.MAX_VALUE == timeMilliseconds)
        {
            return context.getResources().getString(R.string.sensor_not_seen);
        }

        String lastSeenText = context.getResources().getString(R.string.sensor_last_seen) + " ";

        long timeSince = System.currentTimeMillis() - timeMilliseconds;
        long secondsSince = TimeUnit.SECONDS.convert(timeSince, TimeUnit.MILLISECONDS);
        long minutesSince = TimeUnit.MINUTES.convert(secondsSince, TimeUnit.SECONDS);
        long hoursSince = TimeUnit.HOURS.convert(minutesSince, TimeUnit.MINUTES);

        if (60 > secondsSince)
        {
            lastSeenText += getSecondsSinceString(context, secondsSince);
        }
        else if (60 > minutesSince)
        {
            lastSeenText += getMinutesSinceString(context, minutesSince);
        }
        else
        {
            lastSeenText += getHoursSinceString(context, hoursSince);
        }

        return lastSeenText;
    }

    private static String getSecondsSinceString(Context context, long secondsSince)
    {
        if (secondsSince < 5)
        {
            return context.getResources().getString(R.string.sensor_seen_just_now);
        }

        return context.getResources().getString(R.string.sensor_seen_seconds_ago, secondsSince);
    }

    private static String getMinutesSinceString(Context context, long minutesSince)
    {
        if (minutesSince == 1)
        {
            return context.getResources().getString(R.string.sensor_seen_a_minute_ago);
        }

        return context.getResources().getString(R.string.sensor_seen_minutes_ago, minutesSince);
    }

    private static String getHoursSinceString(Context context, long hoursSince)
    {
        if (hoursSince == 1)
        {
            return context.getResources().getString(R.string.sensor_seen_an_hour_ago);
        }

        return context.getResources().getString(R.string.sensor_seen_hours_ago, hoursSince);
    }
}
