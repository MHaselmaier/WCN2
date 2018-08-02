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
    private ScanResultAdapter scanResultAdapter;
    private Handler uiUpdater = new Handler();

    @Override
    public List<ScanFilter> getScanFilter()
    {
        return new ArrayList<>();
    }

    @Override
    public void onScanResult(SensorData result)
    {
        this.scanResultAdapter.add(result);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        this.scanResultAdapter = new ScanResultAdapter(getActivity(), LayoutInflater.from(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = getActivity().getLayoutInflater().inflate(R.layout.search_sensor, container, false);


        ListView sensors = view.findViewById(R.id.sensors);
        sensors.setAdapter(this.scanResultAdapter);

        sensors.setEmptyView(view.findViewById(R.id.empty_list_item));

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
        this.uiUpdater.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (!SearchSensorFragment.this.isHidden())
                {
                    SearchSensorFragment.this.scanResultAdapter.notifyDataSetChanged();
                    SearchSensorFragment.this.uiUpdater.postDelayed(this, Constants.UI_UPDATE_INTERVAL);
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
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        BLEScanner.unregisterScanResultListener(this);
    }
}
