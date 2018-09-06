package de.hs_kl.wcn2.fragments.search_sensor;

import android.app.Fragment;
import android.bluetooth.le.ScanFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.ble_scanner.SensorData;
import de.hs_kl.wcn2.ble_scanner.BLEScanner;
import de.hs_kl.wcn2.ble_scanner.ScanResultListener;
import de.hs_kl.wcn2.util.Constants;

public class SearchSensorFragment extends Fragment implements ScanResultListener
{
    private FoundSensorAdapter foundSensorAdapter;
    private Handler uiUpdater = new Handler();
    private BLEScanner bleScanner;

    @Override
    public List<ScanFilter> getScanFilter()
    {
        return new ArrayList<>();
    }

    @Override
    public void onScanResult(SensorData result)
    {
        this.foundSensorAdapter.add(result);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        this.foundSensorAdapter = new FoundSensorAdapter(getActivity());
        this.bleScanner = BLEScanner.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.search_sensor, container, false);

        ListView foundSensorListView = view.findViewById(R.id.sensors);
        foundSensorListView.setEmptyView(view.findViewById(R.id.empty_list_item));
        foundSensorListView.setAdapter(this.foundSensorAdapter);

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        this.bleScanner.registerScanResultListener(this);

        startUIUpdater();
    }

    private void startUIUpdater()
    {
        this.uiUpdater.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (SearchSensorFragment.this.isHidden()) return;

                SearchSensorFragment.this.foundSensorAdapter.notifyDataSetChanged();
                SearchSensorFragment.this.uiUpdater.postDelayed(this, Constants.UI_UPDATE_INTERVAL);
            }
        });
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

        this.bleScanner.unregisterScanResultListener(this);
    }
}
