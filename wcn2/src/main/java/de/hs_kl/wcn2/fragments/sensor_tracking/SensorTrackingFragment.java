package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.le.ScanFilter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2.OverviewActivity;
import de.hs_kl.wcn2_sensors.BLEScanner;
import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2_sensors.ScanResultListener;
import de.hs_kl.wcn2_sensors.SensorData;
import de.hs_kl.wcn2.util.Constants;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

public class SensorTrackingFragment extends Fragment implements ScanResultListener
{
    private boolean tracking = false;
    private long trackingStartTime;

    private Handler uiUpdater = new Handler();

    private Button measurementButton;
    private TextView measurementTime;

    private BLEScanner bleScanner;
    private TrackedSensorsStorage trackedSensorsStorage;

    private TrackedSensorsOverview trackedSensorsOverview;
    private ActionButtonsOverview actionButtonsOverview;

    @Override
    public List<ScanFilter> getScanFilter()
    {
        List<ScanFilter> scanFilters = new ArrayList<>();
        for (SensorData sensorData: this.trackedSensorsStorage.getTrackedSensors())
        {
            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setDeviceAddress(sensorData.getMacAddress());
            scanFilters.add(builder.build());
        }

        if (0 == scanFilters.size())
        {
            // If no sensors are tracked, no filters will be set.
            // This results in showing all found sensors.
            // Therefor add a dummy filter, so no sensor will be accepted:
            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setDeviceAddress("00:00:00:00:00:00");
            scanFilters.add(builder.build());
        }
        return scanFilters;
    }

    @Override
    public void onScanResult(SensorData result)
    {
        result.setMnemonic(this.trackedSensorsStorage.getMnemonic(result.getMacAddress()));
        this.trackedSensorsOverview.addSensor(result);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        this.bleScanner = BLEScanner.getInstance(getActivity());
        this.trackedSensorsStorage = TrackedSensorsStorage.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.sensor_tracking, container, false);

        this.measurementTime = view.findViewById(R.id.measurement_time);
        final Dialog dialog = StartMeasurementDialog.buildStartMeasurementDialog(this);
        this.measurementButton = view.findViewById(R.id.measurement_button);
        this.measurementButton.setOnClickListener((v) ->
        {
            if (!this.tracking && arePrerequisitesForMeasurementsMet())
            {
                dialog.show();
            }
            else
            {
                stopTracking();
            }
        });

        ImageButton edit = view.findViewById(R.id.edit_tracked_sensors);
        edit.setOnClickListener((v) ->
                ((OverviewActivity)getActivity()).changeViewTo(Constants.WCNView.SEARCH_SENSOR));

        this.trackedSensorsOverview = new TrackedSensorsOverview(getActivity(),
                view.findViewById(R.id.sensor_overview));
        this.trackedSensorsOverview.show();
        this.actionButtonsOverview = new ActionButtonsOverview(getActivity(),
                view.findViewById(R.id.action_overview));
        this.actionButtonsOverview.hide();

        return view;
    }

    private boolean arePrerequisitesForMeasurementsMet()
    {
        return ensureAtLeastOneSensorIsTracked() && ensurePermissionToWriteFilesIsGranted();
    }

    private boolean ensureAtLeastOneSensorIsTracked()
    {
        if (0 == this.trackedSensorsStorage.getTrackedSensors().size())
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

        this.trackedSensorsOverview.hide();
        this.actionButtonsOverview.show();
    }

    public void startTracking(String filename, String header, int averageRate)
    {
        this.tracking = true;
        this.trackingStartTime = System.currentTimeMillis();
        updateTrackingTime();

        Intent intent = new Intent(getActivity(), MeasurementService.class);
        intent.setAction(MeasurementService.ACTION_START);
        intent.putExtra(Constants.MEASUREMENT_FILENAME, filename);
        intent.putExtra(Constants.MEASUREMENT_HEADER, header);
        intent.putExtra(Constants.MEASUREMENT_RATE, averageRate);
        getActivity().startService(intent);

        this.trackedSensorsOverview.hide();
        this.actionButtonsOverview.show();
    }

    private void stopTracking()
    {
        Intent intent = new Intent(getActivity(), MeasurementService.class);
        intent.setAction(MeasurementService.ACTION_STOP);
        getActivity().stopService(intent);

        this.tracking = false;
        updateTrackingTime();

        this.trackedSensorsOverview.show();
        this.actionButtonsOverview.hide();
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
        this.uiUpdater.removeCallbacksAndMessages(null);
        this.uiUpdater.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (!SensorTrackingFragment.this.isVisible()) return;

                updateTrackingTime();
                if (!SensorTrackingFragment.this.tracking)
                {
                    SensorTrackingFragment.this.trackedSensorsOverview.updateViews();
                }
                SensorTrackingFragment.this.uiUpdater.postDelayed(this, Constants.UI_UPDATE_INTERVAL);
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        if (hidden) return;
        // On older devices this method seems to be called even before the onCreate method.
        // In this case the members are not initialized on this method can return.
        if (null == this.trackedSensorsStorage || null == this.actionButtonsOverview) return;

        startUIUpdater();
        this.actionButtonsOverview.updateViews();
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
}