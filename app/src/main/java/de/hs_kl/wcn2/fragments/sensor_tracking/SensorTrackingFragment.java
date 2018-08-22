package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.le.ScanFilter;
import android.content.DialogInterface;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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

        this.trackedSensors = TrackedSensorsStorage.getTrackedSensors(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = getActivity().getLayoutInflater().inflate(R.layout.sensor_tracking, container, false);

        this.measurementTime = view.findViewById(R.id.measurement_time);
        this.measurementButton = view.findViewById(R.id.measurement_button);
        this.measurementButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (!SensorTrackingFragment.this.tracking)
                {
                    if (0 == TrackedSensorsStorage.getTrackedSensors(getActivity()).size())
                    {
                        Toast.makeText(getActivity(), R.string.no_tracked_sensors, Toast.LENGTH_LONG).show();
                        return;
                    }

                    String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(SensorTrackingFragment.this.getActivity(), permission[0]))
                    {
                        ActivityCompat.requestPermissions(SensorTrackingFragment.this.getActivity(), permission, Constants.REQUEST_PERMISSIONS);
                        return;
                    }

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

                    View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.measurement_dialog, (ViewGroup)getView(), false);
                    dialogBuilder.setView(dialogView);

                    final EditText measurementHeader = dialogView.findViewById(R.id.measurement_header);
                    dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            String measurementHeaderInput = measurementHeader.getText().toString();
                            if (0 == measurementHeaderInput.trim().length())
                            {
                                measurementHeaderInput = getResources().getString(R.string.measurement_comment);
                            }
                            startTracking(measurementHeaderInput);
                        }
                    });

                    dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
        addActionToggleButtons((GridLayout)this.actionOverview.findViewById(R.id.actions));

        ImageButton edit = view.findViewById(R.id.edit_tracked_sensors);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((OverviewActivity)getActivity()).changeViewTo(Constants.WCNView.SEARCH_SENSOR);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceBundle)
    {
        super.onViewCreated(view, savedInstanceBundle);

        if (Long.MIN_VALUE != MeasurementService.startTime)
        {
            this.tracking = true;
            this.trackingStartTime = MeasurementService.startTime;

            this.sensorOverview.setVisibility(View.GONE);
            this.actionOverview.setVisibility(View.VISIBLE);
            updateTrackingTime();

            GridLayout actions = this.actionOverview.findViewById(R.id.actions);
            int amountChildren = actions.getChildCount();
            for (int i = 0; amountChildren > i; ++i)
            {
                Button button = actions.getChildAt(i).findViewById(R.id.button);
                if (null != button)
                {
                    if (MeasurementService.action.equals(button.getText().toString()))
                    {
                        this.action = button;
                        SensorTrackingFragment.this.action.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                        return;
                    }
                }
            }
        }
    }

    private void addActionToggleButtons(GridLayout gridLayout)
    {
        gridLayout.removeAllViews();

        String[] actions = DefinedActionStorage.getDefinedActions(getActivity());
        if (0 == actions.length)
        {
            View emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_actions, gridLayout, false);
            gridLayout.addView(emptyView);
            return;
        }

        for (String action: actions)
        {
            View view = getActivity().getLayoutInflater().inflate(R.layout.action_button, gridLayout, false);
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
                        SensorTrackingFragment.this.action.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                        MeasurementService.action = SensorTrackingFragment.this.action.getText().toString();
                    }
                }
            });
            if (action.equals(MeasurementService.action))
            {
                SensorTrackingFragment.this.action = button;
                button.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
            }
            gridLayout.addView(view);
        }
        if (1 == actions.length)
        {
            // add extra invisible view so the single button is not spread over the whole screen
            View view = getActivity().getLayoutInflater().inflate(R.layout.action_button, gridLayout, false);
            view.setVisibility(View.INVISIBLE);
            gridLayout.addView(view);
        }
    }

    private void startTracking(String header)
    {
        this.tracking = true;
        this.trackingStartTime = System.currentTimeMillis();
        updateTrackingTime();

        Intent intent = new Intent(getActivity(), MeasurementService.class);
        intent.setAction(MeasurementService.ACTION_START);
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

        SensorTrackingFragment.this.tracking = false;
        updateTrackingTime();

        if (null != SensorTrackingFragment.this.action)
        {
            SensorTrackingFragment.this.action.getBackground().clearColorFilter();
        }

        this.sensorOverview.setVisibility(View.VISIBLE);
        this.actionOverview.setVisibility(View.GONE);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        BLEScanner.registerScanResultListener(this);

        startUIUpdater();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        BLEScanner.unregisterScanResultListener(this);
    }

    private void startUIUpdater()
    {
        this.uiUpdater.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (SensorTrackingFragment.this.isVisible())
                {
                    updateTrackingTime();
                    showTrackedSensors();
                    SensorTrackingFragment.this.uiUpdater.postDelayed(this, Constants.UI_UPDATE_INTERVAL);
                }
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        if (!hidden)
        {
            startUIUpdater();
            this.trackedSensors = TrackedSensorsStorage.getTrackedSensors(getActivity());
            addActionToggleButtons((GridLayout)this.actionOverview.findViewById(R.id.actions));
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
            View emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_list_item, this.trackedSensorViews, false);
            TextView label = emptyView.findViewById(R.id.label);
            label.setText(R.string.no_sensors_found);
            this.trackedSensorViews.addView(emptyView);
        }
    }
}

