package de.hs_kl.blesensor;

import android.app.Activity;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SensorTrackingFragment extends Fragment implements BLEScannerChangedListener
{
    private BLEScanner bleScanner;
    private List<SensorData> trackedSensors;

    private LinearLayout trackedSensorViews;

    private ScanCallback scanCallback = new ScanCallback()
    {
        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            super.onBatchScanResults(results);

            for (ScanResult result: results)
            {
                SensorData data = new SensorData(result);

                boolean found = false;
                for (int i = 0; SensorTrackingFragment.this.trackedSensors.size() > i; ++i)
                {
                    if (SensorTrackingFragment.this.trackedSensors.get(i).getMacAddress()
                            .equals(data.getMacAddress()))
                    {
                        SensorTrackingFragment.this.trackedSensors.set(i, data);
                        found = true;
                        break;
                    }
                }
                if (!found)
                {
                    SensorTrackingFragment.this.trackedSensors.add(data);
                }

                Log.d(SensorTrackingFragment.class.getSimpleName(), data.toString());
            }
            SensorTrackingFragment.this.showTrackedSensors();
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            super.onScanResult(callbackType, result);

            SensorData data = new SensorData(result);

            boolean found = false;
            for (int i = 0; SensorTrackingFragment.this.trackedSensors.size() > i; ++i)
            {
                if (SensorTrackingFragment.this.trackedSensors.get(i).getMacAddress()
                        .equals(data.getMacAddress()))
                {
                    SensorTrackingFragment.this.trackedSensors.set(i, data);
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                SensorTrackingFragment.this.trackedSensors.add(data);
            }

            Log.d(SensorTrackingFragment.class.getSimpleName(), data.toString());
            SensorTrackingFragment.this.showTrackedSensors();
        }

        @Override
        public void onScanFailed(int errorCode)
        {
            super.onScanFailed(errorCode);
            switch(errorCode)
            {
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    Log.d(SearchSensorFragment.class.getSimpleName(),
                            "Failed to start scanning: app cannot be registered!");
                    break;
                case SCAN_FAILED_FEATURE_UNSUPPORTED:
                    Log.d(SearchSensorFragment.class.getSimpleName(),
                            "Failed to start scanning: power optimized scan not supported!");
                    break;
                case SCAN_FAILED_INTERNAL_ERROR:
                    Log.d(SearchSensorFragment.class.getSimpleName(),
                            "Failed to start scanning: internal error!");
                    break;
                default:
                    Log.d(SearchSensorFragment.class.getSimpleName(),
                            "Failed to start scanning!");
                    break;
            }
        }
    };

    @Override
    public void onBLEScannerChanged(BLEScanner bleScanner)
    {
        if (null != this.bleScanner)
        {
            this.bleScanner.stopScanning();
        }
        if (isVisible() && null != bleScanner)
        {
            bleScanner.scanForSensors(this.scanCallback);
        }
        this.bleScanner = bleScanner;
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

        this.trackedSensorViews = new LinearLayout(getActivity());
        this.trackedSensorViews.setOrientation(LinearLayout.VERTICAL);

        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.addView(trackedSensorViews);

        LinearLayout trackedSensors = view.findViewById(R.id.tracked_sensors);
        trackedSensors.addView(scrollView);

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Activity activity = getActivity();
        if (activity instanceof OverviewActivity)
        {
            ((OverviewActivity)activity).registerBLEScannerChangedListener(this);
        }

        this.trackedSensors = TrackedSensorsStorage.getTrackedSensors(getActivity());
        showTrackedSensors();
        scanForSensorData();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        Activity activity = getActivity();
        if (activity instanceof OverviewActivity)
        {
            ((OverviewActivity)activity).unregisterBLEScannerChangedListener(this);
        }

        if (null != this.bleScanner)
        {
            this.bleScanner.stopScanning();
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

    private void scanForSensorData()
    {
        if (null != this.bleScanner)
        {
            List<String> deviceAddresses = new ArrayList<>();
            for (SensorData sensorData : this.trackedSensors)
            {
                deviceAddresses.add(sensorData.getMacAddress());
            }

            this.bleScanner.scanForSensorData(this.scanCallback, deviceAddresses);
        }
    }
}

