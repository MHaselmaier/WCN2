package de.hs_kl.wcn2.fragments.search_sensor;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import de.hs_kl.wcn2_sensors.WCN2Scanner;
import de.hs_kl.wcn2.util.Constants;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;
import de.hs_kl.wcn2_sensors.WCN2SensorData;
import de.hs_kl.wcn2_sensors.WCN2SensorDataListener;

public class SearchSensorFragment extends Fragment implements WCN2SensorDataListener
{
    private Handler uiUpdater = new Handler();
    private TrackedSensorsStorage trackedSensorsStorage;
    private LinearLayout foundSensorsContainer;
    private View emptyListItem;
    private List<WCN2SensorData> foundSensors = new ArrayList<>();
    private List<FoundSensorView> foundSensorsViews = new ArrayList<>();

    @Override
    public List<ScanFilter> getScanFilter()
    {
        return new ArrayList<>();
    }

    @Override
    public void onScanResult(WCN2SensorData result)
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

    private void addSensor(WCN2SensorData sensorData)
    {
        int position = this.foundSensors.size();
        this.foundSensors.add(sensorData);
        this.foundSensorsViews.add(null);

        Handler handler = new Handler();
        new Thread(() -> {
            FoundSensorView view = new FoundSensorView(getActivity(), sensorData);
            handler.post(() -> {
                this.foundSensorsViews.set(position, view);
                this.foundSensorsContainer.addView(view);
                updateFoundSensorsViews();
            });
        }).start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        this.trackedSensorsStorage = TrackedSensorsStorage.getInstance(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle b)
    {
        View view = inflater.inflate(R.layout.search_sensor, container, false);

        this.foundSensorsContainer = view.findViewById(R.id.sensors);
        this.emptyListItem = view.findViewById(R.id.empty_list_item);
        TextView label = this.emptyListItem.findViewById(R.id.label);
        label.setText(R.string.no_sensors_found);

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        WCN2Scanner.registerScanResultListener(this);

        this.foundSensors.clear();
        this.foundSensorsViews.clear();
        for (WCN2SensorData sensorData: this.trackedSensorsStorage.getTrackedSensors())
        {
            addSensor(sensorData);
        }

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
                updateFoundSensorsViews();
                SearchSensorFragment.this.uiUpdater.postDelayed(this, Constants.UI_UPDATE_INTERVAL);
            }
        });
    }

    private void updateFoundSensorsViews()
    {
        for (int i = 0; this.foundSensors.size() > i; ++i)
        {
            if (null == foundSensorsViews.get(i)) continue;
            this.foundSensorsViews.get(i).updateView(this.foundSensors.get(i));
        }

        this.emptyListItem.setVisibility(this.foundSensors.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        WCN2Scanner.unregisterScanResultListener(this);
        this.uiUpdater.removeCallbacksAndMessages(null);
    }
}
