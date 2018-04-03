package de.hs_kl.blesensor;

import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ScanResultAdapter extends BaseAdapter
{
    private Context context;
    private TrackedSensorsStorage trackedSensorsStorage;
    private LayoutInflater inflater;
    private List<SensorData> sensorData;

    public ScanResultAdapter(Context context, LayoutInflater inflater)
    {
        this.context = context;
        this.trackedSensorsStorage = new TrackedSensorsStorage(this.context);
        this.inflater = inflater;
        this.sensorData = this.trackedSensorsStorage.getTrackedSensors();
    }

    @Override
    public int getCount()
    {
        return this.sensorData.size();
    }

    @Override
    public SensorData getItem(int position)
    {
        return this.sensorData.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return this.sensorData.get(position).getMacAddress().hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        if (null == view)
        {
            view = this.inflater.inflate(R.layout.sensor_list_item, null);
        }

        TextView deviceNameView = view.findViewById(R.id.device_name);
        TextView deviceAddressView = view.findViewById(R.id.device_address);
        TextView lastSeenView = view.findViewById(R.id.last_seen);
        Switch trackSwitch = view.findViewById(R.id.sensor_tracked);

        SensorData sensorData = this.sensorData.get(position);

        String name = sensorData.getDeviceName();
        if (null == name)
        {
            name = this.context.getResources().getString(R.string.sensor_without_name);
        }
        deviceNameView.setText(name);
        deviceAddressView.setText(sensorData.getMacAddress());
        lastSeenView.setText(getTimeSinceString(sensorData.getTimestamp()));
        trackSwitch.setChecked(this.trackedSensorsStorage.isTracked(sensorData));
        trackSwitch.setText(trackSwitch.isChecked() ? R.string.sensor_tracked : R.string.sensor_not_tracked);
        trackSwitch.setOnCheckedChangeListener(
                new SensorTrackedChangeListener(this.trackedSensorsStorage, sensorData));

        return view;
    }

    private String getTimeSinceString(long timeNanoseconds) {
        if (Long.MAX_VALUE == timeNanoseconds)
        {
            return this.context.getResources().getString(R.string.sensor_not_seen);
        }

        String lastSeenText = this.context.getResources().getString(R.string.sensor_last_seen) + " ";

        long timeSince = SystemClock.elapsedRealtimeNanos() - timeNanoseconds;
        long secondsSince = TimeUnit.SECONDS.convert(timeSince, TimeUnit.NANOSECONDS);
        long minutesSince = TimeUnit.MINUTES.convert(secondsSince, TimeUnit.SECONDS);
        long hoursSince = TimeUnit.HOURS.convert(minutesSince, TimeUnit.MINUTES);

        if (60 > secondsSince)
        {
            lastSeenText += getSecondsSinceString(secondsSince);
        }
        else if (60 > minutesSince)
        {
            lastSeenText += getMinutesSinceString(minutesSince);
        }
        else
        {
            lastSeenText += getHoursSinceString(hoursSince);
        }

        return lastSeenText;
    }

    private String getSecondsSinceString(long secondsSince)
    {
        if (secondsSince < 5)
        {
            return this.context.getResources().getString(R.string.sensor_seen_just_now);
        }

        return this.context.getResources().getString(R.string.sensor_seen_seconds_ago, secondsSince);
    }

    private String getMinutesSinceString(long minutesSince)
    {
        if (minutesSince == 1)
        {
            return this.context.getResources().getString(R.string.sensor_seen_a_minute_ago);
        }

        return this.context.getResources().getString(R.string.sensor_seen_minutes_ago, minutesSince);
    }

    private String getHoursSinceString(long hoursSince)
    {
        if (hoursSince == 1)
        {
            return this.context.getResources().getString(R.string.sensor_seen_an_hour_ago);
        }

        return this.context.getResources().getString(R.string.sensor_seen_hours_ago, hoursSince);
    }

    public void add(SensorData result)
    {
        int position = getPosition(result.getMacAddress());

        if (0 <= position)
        {
            this.sensorData.set(position, result);
        }
        else
        {
            this.sensorData.add(result);
        }
    }

    private int getPosition(String address)
    {
        for (int i = 0; i < this.sensorData.size(); ++i)
        {
            if (this.sensorData.get(i).getMacAddress().equals(address))
            {
                return i;
            }
        }
        return -1;
    }
}
