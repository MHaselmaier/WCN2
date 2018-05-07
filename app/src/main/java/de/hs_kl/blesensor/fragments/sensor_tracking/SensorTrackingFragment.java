package de.hs_kl.blesensor.fragments.sensor_tracking;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.le.ScanFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.blesensor.ble_scanner.BLEScanner;
import de.hs_kl.blesensor.R;
import de.hs_kl.blesensor.ble_scanner.ScanResultListener;
import de.hs_kl.blesensor.fragments.search_sensor.SearchSensorFragment;
import de.hs_kl.blesensor.ble_scanner.SensorData;
import de.hs_kl.blesensor.util.TrackedSensorsStorage;
import de.hs_kl.blesensor.util.LastSeenSinceUtil;

public class SensorTrackingFragment extends Fragment implements ScanResultListener
{
    private Dataset dataset = new Dataset();
    private boolean tracking = false;
    private long trackingStartTime;

    private List<SensorData> trackedSensors;
    private Handler uiUpdater = new Handler();
    private Button activityButton;
    private TextView activityTime;
    private LinearLayout trackedSensorViews;

    @Override
    public List<ScanFilter> getScanFilter()
    {
        List<ScanFilter> scanFilters = new ArrayList<>();
        for (SensorData sensorData: this.trackedSensors)
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
        for (int i = 0; this.trackedSensors.size() > i; ++i)
        {
            if (this.trackedSensors.get(i).getMacAddress().equals(result.getMacAddress()))
            {
                this.trackedSensors.set(i, result);

                if (this.tracking)
                {
                    DatasetEntry entry = new DatasetEntry(result.getDeviceID(), result.getTemperature(), result.getRelativeHumidity(), "", result.getTimestamp());
                    this.dataset.add(entry);
                }
                return;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = getActivity().getLayoutInflater().inflate(R.layout.sensor_tracking, container, false);

        this.activityTime = view.findViewById(R.id.activity_time);
        this.activityButton = view.findViewById(R.id.activity_button);
        this.activityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SensorTrackingFragment.this.tracking)
                {
                    startTracking();
                }
                else
                {
                    stopTracking();
                }
            }
        });

        this.trackedSensorViews = new LinearLayout(getActivity());
        this.trackedSensorViews.setOrientation(LinearLayout.VERTICAL);

        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.addView(trackedSensorViews);

        LinearLayout trackedSensors = view.findViewById(R.id.tracked_sensors);
        trackedSensors.addView(scrollView);

        ImageButton edit = view.findViewById(R.id.edit_tracked_sensors);
        edit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction())
                {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.fragment, new SearchSensorFragment());
                    ft.addToBackStack(null);
                    ft.commit();
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    private void startTracking()
    {
        SensorTrackingFragment.this.dataset.clear();
        SensorTrackingFragment.this.tracking = true;
        SensorTrackingFragment.this.trackingStartTime = System.currentTimeMillis();
    }

    private void stopTracking()
    {
        SensorTrackingFragment.this.dataset.writeToFile(getActivity());
        SensorTrackingFragment.this.tracking = false;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        BLEScanner.registerScanResultListener(this);

        this.trackedSensors = TrackedSensorsStorage.getTrackedSensors(getActivity());

        this.uiUpdater.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (SensorTrackingFragment.this.isResumed())
                {
                    updateTrackingTime();
                    showTrackedSensors();
                    SensorTrackingFragment.this.uiUpdater.postDelayed(this, 1000);
                }
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();

        BLEScanner.unregisterScanResultListener(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if (this.tracking)
        {
            this.dataset.writeToFile(getActivity());
        }
    }

    private void updateTrackingTime()
    {
        if (this.tracking)
        {
            int trackedTime = (int)(System.currentTimeMillis() - this.trackingStartTime) / 1000;
            int seconds = trackedTime % 60;
            int minutes = trackedTime / 60;
            this.activityTime.setText(getResources().getString(R.string.time, minutes, seconds));
            this.activityButton.setText(R.string.activity_button_stop);
        }
        else
        {
            this.activityTime.setText(R.string.no_time);
            this.activityButton.setText(R.string.activity_button_start);
        }
    }

    private void showTrackedSensors()
    {
        Activity activity = getActivity();
        if (null == activity) return;

        this.trackedSensorViews.removeAllViews();
        for (SensorData sensorData: this.trackedSensors) {
            View tracked_sensor_overview = activity.getLayoutInflater().inflate(R.layout.tracked_sensor_overview, null, false);

            TextView sensorName = tracked_sensor_overview.findViewById(R.id.sensor_name);
            sensorName.setText(sensorData.getDeviceName());
            TextView lastSeen = tracked_sensor_overview.findViewById(R.id.last_seen);
            lastSeen.setText(LastSeenSinceUtil.getTimeSinceString(getActivity(), sensorData.getTimestamp()));
            TextView temperature = tracked_sensor_overview.findViewById(R.id.temperature);
            temperature.setText(getResources().getString(R.string.temperature, sensorData.getTemperature()));
            TextView humidity = tracked_sensor_overview.findViewById(R.id.humidity);
            humidity.setText(getResources().getString(R.string.humidity, sensorData.getRelativeHumidity()));
            TextView batteryLevel = tracked_sensor_overview.findViewById(R.id.battery_level);
            batteryLevel.setText(getResources().getString(R.string.battery_voltage, sensorData.getBatteryVoltage()));

            this.trackedSensorViews.addView(tracked_sensor_overview);
        }
    }
}

