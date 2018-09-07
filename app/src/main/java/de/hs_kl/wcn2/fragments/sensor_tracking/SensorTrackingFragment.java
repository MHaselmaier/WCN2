package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.le.ScanFilter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2.OverviewActivity;
import de.hs_kl.wcn2.ble_scanner.BLEScanner;
import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.ble_scanner.ScanResultListener;
import de.hs_kl.wcn2.ble_scanner.SensorData;
import de.hs_kl.wcn2.util.Constants;
import de.hs_kl.wcn2.util.DefinedActionStorage;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;
import de.hs_kl.wcn2.util.LastSeenSinceUtil;

public class SensorTrackingFragment extends Fragment implements ScanResultListener
{
    private boolean tracking = false;
    private long trackingStartTime;

    private List<SensorData> trackedSensors;
    private Handler uiUpdater = new Handler();
    private Button action;
    private Button measurementButton;
    private TextView measurementTime;
    private CardView sensorOverview;
    private LinearLayout trackedSensorViews;
    private ScrollView actionOverview;
    private GridLayout actions;

    private BLEScanner bleScanner;
    private TrackedSensorsStorage trackedSensorsStorage;
    private DefinedActionStorage definedActions;

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

        this.bleScanner = BLEScanner.getInstance(getActivity());
        this.trackedSensorsStorage = TrackedSensorsStorage.getInstance(getActivity());
        this.definedActions = DefinedActionStorage.getInstance(getActivity());

        this.trackedSensors = this.trackedSensorsStorage.getTrackedSensors();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.sensor_tracking, container, false);

        this.measurementTime = view.findViewById(R.id.measurement_time);
        final Dialog dialog = StartMeasurementDialog.buildStartMeasurementDialog(this);
        this.measurementButton = view.findViewById(R.id.measurement_button);
        this.measurementButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!SensorTrackingFragment.this.tracking && arePrerequisitesForMeasurementsMet())
                {
                    dialog.show();
                }
                else
                {
                    stopTracking();
                }
            }
        });
        this.trackedSensorViews = view.findViewById(R.id.tracked_sensors);
        this.sensorOverview = view.findViewById(R.id.sensor_overview);
        this.actionOverview = view.findViewById(R.id.action_overview);
        this.actionOverview.setVisibility(View.GONE);
        this.actions = this.actionOverview.findViewById(R.id.actions);
        addActionToggleButtons();

        ImageButton edit = view.findViewById(R.id.edit_tracked_sensors);
        edit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ((OverviewActivity)getActivity()).changeViewTo(Constants.WCNView.SEARCH_SENSOR);
            }
        });

        return view;
    }

    private boolean arePrerequisitesForMeasurementsMet()
    {
        return ensureAtLeastOneSensorIsTracked() && ensurePermissionToWriteFilesIsGranted();
    }

    private boolean ensureAtLeastOneSensorIsTracked()
    {
        if (0 == this.trackedSensors.size())
        {
            Toast.makeText(getActivity(), R.string.no_tracked_sensors, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean ensurePermissionToWriteFilesIsGranted()
    {
        String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getActivity(),
                permission[0]))
        {
            ActivityCompat.requestPermissions(getActivity(), permission,
                    Constants.REQUEST_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceBundle)
    {
        super.onViewCreated(view, savedInstanceBundle);

        if (Long.MIN_VALUE == MeasurementService.startTime) return;

        this.tracking = true;
        this.trackingStartTime = MeasurementService.startTime;
        updateTrackingTime();

        this.sensorOverview.setVisibility(View.GONE);
        this.actionOverview.setVisibility(View.VISIBLE);

        GridLayout actions = this.actionOverview.findViewById(R.id.actions);
        for (int i = 0; actions.getChildCount() > i; ++i)
        {
            Button button = actions.getChildAt(i).findViewById(R.id.button);
            if (null == button) continue;

            if (MeasurementService.action.equals(button.getText().toString()))
            {
                this.action = button;
                this.action.getBackground().setColorFilter(getResources()
                        .getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                return;
            }
        }
    }

    private void addActionToggleButtons()
    {
        this.actions.removeAllViews();

        String[] actions = this.definedActions.getDefinedActions();
        if (0 == actions.length)
        {
            getActivity().getLayoutInflater().inflate(R.layout.empty_actions, this.actions);
            return;
        }

        for (String action: actions)
        {
            this.actions.addView(createActionToggleButton(action));
        }
        if (1 == actions.length)
        {
            // add extra invisible view so the single button is not spread over the whole screen
            View view = getActivity().getLayoutInflater()
                    .inflate(R.layout.action_button, this.actions, false);
            view.setVisibility(View.INVISIBLE);
            this.actions.addView(view);
        }
    }

    private View createActionToggleButton(String action)
    {
        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.action_button, this.actions, false);
        Button button = view.findViewById(R.id.button);
        button.setText(action);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != SensorTrackingFragment.this.action)
                {
                    SensorTrackingFragment.this.action.getBackground().clearColorFilter();
                }

                if (view == SensorTrackingFragment.this.action)
                {
                    SensorTrackingFragment.this.action = null;
                    MeasurementService.action = "";
                }
                else
                {
                    SensorTrackingFragment.this.action = (Button)view;
                    view.getBackground().setColorFilter(getResources()
                            .getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                    String action = SensorTrackingFragment.this.action.getText().toString();
                    MeasurementService.action = action;
                }
            }
        });
        if (action.equals(MeasurementService.action))
        {
            SensorTrackingFragment.this.action = button;
            button.getBackground().setColorFilter(getResources()
                    .getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }
        return view;
    }

    public void startTracking(String filename, String header)
    {
        this.tracking = true;
        this.trackingStartTime = System.currentTimeMillis();
        updateTrackingTime();

        Intent intent = new Intent(getActivity(), MeasurementService.class);
        intent.setAction(MeasurementService.ACTION_START);
        intent.putExtra(Constants.MEASUREMENT_FILENAME, filename);
        intent.putExtra(Constants.MEASUREMENT_HEADER, header);
        getActivity().startService(intent);

        this.sensorOverview.setVisibility(View.GONE);
        this.actionOverview.setVisibility(View.VISIBLE);
    }

    private void stopTracking()
    {
        Intent intent = new Intent(getActivity(), MeasurementService.class);
        intent.setAction(MeasurementService.ACTION_STOP);
        getActivity().stopService(intent);

        this.tracking = false;
        updateTrackingTime();

        if (null != this.action)
        {
            this.action.getBackground().clearColorFilter();
        }

        this.sensorOverview.setVisibility(View.VISIBLE);
        this.actionOverview.setVisibility(View.GONE);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        this.bleScanner.registerScanResultListener(this);

        startUIUpdater();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        this.bleScanner.unregisterScanResultListener(this);
    }

    private void startUIUpdater()
    {
        this.uiUpdater.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (!SensorTrackingFragment.this.isVisible()) return;

                updateTrackingTime();
                showTrackedSensors();
                SensorTrackingFragment.this.uiUpdater.postDelayed(this, Constants.UI_UPDATE_INTERVAL);
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        if (hidden) return;

        startUIUpdater();
        this.trackedSensors = this.trackedSensorsStorage.getTrackedSensors();
        if (null != this.actionOverview)
        {
            addActionToggleButtons();
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
        this.trackedSensorViews.removeAllViews();
        for (SensorData sensorData: this.trackedSensors)
        {
            View tracked_sensor_overview = getActivity().getLayoutInflater()
                    .inflate(R.layout.tracked_sensor_overview, this.trackedSensorViews, false);

            TextView sensorID = tracked_sensor_overview.findViewById(R.id.sensor_id);
            sensorID.setText(getResources().getString(R.string.sensor_id, sensorData.getSensorID()));
            TextView mnemonic = tracked_sensor_overview.findViewById(R.id.mnemonic);
            mnemonic.setText(getResources().getString(R.string.mnemonic, sensorData.getMnemonic()));
            if (sensorData.getMnemonic().equals("null"))
            {
                mnemonic.setVisibility(View.GONE);
            }
            TextView lastSeen = tracked_sensor_overview.findViewById(R.id.last_seen);
            lastSeen.setText(LastSeenSinceUtil.getTimeSinceString(getActivity(), sensorData.getTimestamp()));
            TextView temperature = tracked_sensor_overview.findViewById(R.id.temperature);
            temperature.setText(getResources().getString(R.string.temperature, sensorData.getTemperature()));
            TextView humidity = tracked_sensor_overview.findViewById(R.id.humidity);
            humidity.setText(getResources().getString(R.string.humidity, sensorData.getRelativeHumidity()));
            ImageView batteryLevel = tracked_sensor_overview.findViewById(R.id.battery_level);
            batteryLevel.setImageDrawable(sensorData.getBatteryLevelDrawable(getResources()));
            ImageView signalStrength = tracked_sensor_overview.findViewById(R.id.signal_strength);
            signalStrength.setImageDrawable(sensorData.getSignalStrengthDrawable(getResources()));

            this.trackedSensorViews.addView(tracked_sensor_overview);
        }

        if (0 == this.trackedSensors.size())
        {
            View emptyView = getActivity().getLayoutInflater()
                    .inflate(R.layout.empty_list_item, this.trackedSensorViews);
            TextView label = emptyView.findViewById(R.id.label);
            label.setText(R.string.no_sensors_found);
        }
    }
}