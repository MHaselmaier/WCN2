package de.hs_kl.blesensor.fragments.sensor_tracking;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.le.ScanFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

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
    private ToggleButton action;
    private Button measurementButton;
    private TextView measurementTime;
    private CardView sensorOverview;
    private LinearLayout trackedSensorViews;
    private ScrollView actionOverview;

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
                    DatasetEntry entry = new DatasetEntry(result.getSensorID(), result.getMacAddress(),
                            result.getTemperature(), result.getRelativeHumidity(), this.action.getText().toString(),
                            result.getTimestamp() - this.trackingStartTime);
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

        this.measurementTime = view.findViewById(R.id.measurement_time);
        this.measurementButton = view.findViewById(R.id.measurement_button);
        this.measurementButton.setOnClickListener(new View.OnClickListener() {
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

        this.sensorOverview = view.findViewById(R.id.sensor_overview);
        this.actionOverview = view.findViewById(R.id.action_overview);
        this.actionOverview.setVisibility(View.GONE);
        addActionToggleButtons((GridLayout)this.actionOverview.findViewById(R.id.actions));

        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.addView(trackedSensorViews);

        LinearLayout trackedSensors = view.findViewById(R.id.tracked_sensors);
        trackedSensors.addView(scrollView);

        ImageButton edit = view.findViewById(R.id.edit_tracked_sensors);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment, new SearchSensorFragment());
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        return view;
    }

    private void addActionToggleButtons(GridLayout gridLayout)
    {
        String[] actions = getResources().getStringArray(R.array.actions);
        for (String action: actions)
        {
            ConstraintLayout constraintLayout = new ConstraintLayout(getActivity());
            constraintLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));

            ToggleButton toggleButton = new ToggleButton(getActivity());
            toggleButton.setTextOff(action);
            toggleButton.setTextOn(action);
            toggleButton.setText(action);
            constraintLayout.addView(toggleButton);
            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null != SensorTrackingFragment.this.action)
                    {
                        SensorTrackingFragment.this.action.setChecked(false);
                    }
                    if (view == SensorTrackingFragment.this.action)
                    {
                        SensorTrackingFragment.this.action = null;
                    }
                    else
                    {
                        SensorTrackingFragment.this.action = (ToggleButton)view;
                        SensorTrackingFragment.this.action.setChecked(true);
                    }
                }
            });

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.setDimensionRatio(toggleButton.getId(), "1:1");
            constraintSet.connect(toggleButton.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.connect(toggleButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            constraintSet.connect(toggleButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(toggleButton.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.applyTo(constraintLayout);

            GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
            gridParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            gridLayout.addView(constraintLayout, gridParams);
        }
    }

    private void startTracking()
    {
        SensorTrackingFragment.this.dataset.clear();
        SensorTrackingFragment.this.tracking = true;
        SensorTrackingFragment.this.trackingStartTime = System.currentTimeMillis();
        updateTrackingTime();

        this.sensorOverview.setVisibility(View.GONE);
        this.actionOverview.setVisibility(View.VISIBLE);
    }

    private void stopTracking()
    {
        SensorTrackingFragment.this.dataset.writeToFile(getActivity());
        SensorTrackingFragment.this.tracking = false;
        updateTrackingTime();

        this.sensorOverview.setVisibility(View.VISIBLE);
        this.actionOverview.setVisibility(View.GONE);
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
            this.measurementTime.setText(getResources().getString(R.string.time, minutes, seconds));
            this.measurementButton.setText(R.string.measurement_button_stop);
        }
        else
        {
            this.measurementTime.setText(R.string.no_time);
            this.measurementButton.setText(R.string.measurement_button_start);
        }
    }

    private void showTrackedSensors()
    {
        Activity activity = getActivity();
        if (null == activity) return;

        this.trackedSensorViews.removeAllViews();
        for (SensorData sensorData: this.trackedSensors) {
            View tracked_sensor_overview = activity.getLayoutInflater().inflate(R.layout.tracked_sensor_overview, this.trackedSensorViews, false);

            TextView sensorID = tracked_sensor_overview.findViewById(R.id.sensor_id);
            sensorID.setText(getResources().getString(R.string.sensor_id, sensorData.getSensorID()));
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

