package de.hs_kl.blesensor;

import android.app.ListFragment;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class SearchSensorFragment extends ListFragment implements ScanResultListener
{
    private ScanResultAdapter scanResultAdapter;
    private Handler uiUpdater = new Handler();

    @Override
    public List<ScanFilter> getScanFilter()
    {
        return new ArrayList<>();
    }

    @Override
    public void onScanResult(ScanResult result)
    {
        this.scanResultAdapter.add(new SensorData(result));
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        this.scanResultAdapter = new ScanResultAdapter(getActivity().getApplicationContext(),
                LayoutInflater.from(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        setListAdapter(this.scanResultAdapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        getListView().setDivider(null);
        getListView().setDividerHeight(0);

        setEmptyText(getString(R.string.no_sensors_found));
    }

    @Override
    public void onResume()
    {
        super.onResume();

        BLEScanner.registerScanResultListener(this);

        this.uiUpdater.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (SearchSensorFragment.this.isResumed())
                {
                    SearchSensorFragment.this.scanResultAdapter.notifyDataSetChanged();
                    SearchSensorFragment.this.uiUpdater.postDelayed(this, 1000);
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
}
