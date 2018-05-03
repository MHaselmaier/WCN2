package de.hs_kl.blesensor;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
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

public class SensorTrackingFragment extends Fragment implements ScanResultListener
{
    private Dataset dataset = new Dataset();

    private List<SensorData> trackedSensors;
    private Handler uiUpdater = new Handler();
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
    public void onScanResult(ScanResult result)
    {
        SensorData sensorData = new SensorData(result);
        for (int i = 0; this.trackedSensors.size() > i; ++i)
        {
            if (this.trackedSensors.get(i).getMacAddress().equals(sensorData.getMacAddress()))
            {
                this.trackedSensors.set(i, sensorData);

                DatasetEntry entry = new DatasetEntry(sensorData.getDeviceID(), sensorData.getTemperature(), sensorData.getRelativeHumidity(), "", sensorData.getTimestamp());
                this.dataset.add(entry);
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

        Button activityButton = view.findViewById(R.id.activity_button);
        activityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SensorTrackingFragment.this.dataset.writeToFile(getActivity());
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

