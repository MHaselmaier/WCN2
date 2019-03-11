package de.hs_kl.wcn2.fragments.search_sensor;

import android.app.Fragment;
import android.bluetooth.le.ScanFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2_sensors.SensorData;
import de.hs_kl.wcn2_sensors.BLEScanner;
import de.hs_kl.wcn2_sensors.ScanResultListener;
import de.hs_kl.wcn2.util.Constants;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

public class SearchSensorFragment extends Fragment implements ScanResultListener
{
    private Handler uiUpdater = new Handler();
    private TrackedSensorsStorage trackedSensorsStorage;
    private LinearLayout foundSensorsContainer;
    private View emptyListItem;
    private List<SensorData> foundSensors = new ArrayList<>();
    private List<FoundSensorView> foundSensorsViews = new ArrayList<>();

    @Override
    public List<ScanFilter> getScanFilter()
    {
        return new ArrayList<>();
    }

    @Override
    public void onScanResult(SensorData result)
    {
        result.setMnemonic(this.trackedSensorsStorage.getMnemonic(result.getMacAddress()));
        for (int i = 0; this.foundSensors.size() > i; ++i)
        {
            if (this.foundSensors.get(i).getMacAddress().equals(result.getMacAddress()))
            {
                this.foundSensors.set(i, result);
                return;
            }
        }

        addSensor(result);
    }

    private void addSensor(SensorData sensorData)
    {
        this.foundSensors.add(sensorData);
        FoundSensorView view = new FoundSensorView(getActivity(), sensorData);
        this.foundSensorsViews.add(view);
        this.foundSensorsContainer.addView(view.getRoot());
        updateFoundSensorsViews();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        this.trackedSensorsStorage = TrackedSensorsStorage.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.search_sensor, container, false);

        this.foundSensorsContainer = view.findViewById(R.id.sensors);
        this.emptyListItem = view.findViewById(R.id.empty_list_item);
        TextView label = this.emptyListItem.findViewById(R.id.label);
        label.setText(R.string.no_sensors_found);

        for (SensorData sensorData: TrackedSensorsStorage.getInstance(getActivity()).getTrackedSensors())
        {
            addSensor(sensorData);
        }

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        BLEScanner.registerScanResultListener(this);

        startUIUpdater();
    }

    private void startUIUpdater()
    {
        this.uiUpdater.removeCallbacksAndMessages(null);
        this.uiUpdater.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (isHidden()) return;

                updateFoundSensorsViews();
                SearchSensorFragment.this.uiUpdater.postDelayed(this, Constants.UI_UPDATE_INTERVAL);
            }
        });
    }

    private void updateFoundSensorsViews()
    {
        for (int i = 0; this.foundSensors.size() > i; ++i)
        {
            this.foundSensorsViews.get(i).updateView(this.foundSensors.get(i));
        }

        this.emptyListItem.setVisibility(this.foundSensors.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        if (hidden) return;


        startUIUpdater();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        BLEScanner.unregisterScanResultListener(this);
    }
}
