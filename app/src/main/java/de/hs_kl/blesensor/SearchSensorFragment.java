package de.hs_kl.blesensor;

import android.app.ListFragment;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SearchSensorFragment extends ListFragment
{
    private BLEScanner bleScanner;

    private ScanResultAdapter scanResultAdapter;

    private ScanCallback scanCallback = new ScanCallback()
    {
        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            super.onBatchScanResults(results);

            for (ScanResult result: results)
            {
                SensorData data = new SensorData(result);
                SearchSensorFragment.this.scanResultAdapter.add(data);
                Log.d(SearchSensorFragment.class.getSimpleName(), data.toString());
            }
            SearchSensorFragment.this.scanResultAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            super.onScanResult(callbackType, result);

            SensorData data = new SensorData(result);
            SearchSensorFragment.this.scanResultAdapter.add(data);
            SearchSensorFragment.this.scanResultAdapter.notifyDataSetChanged();
            Log.d(SearchSensorFragment.class.getSimpleName(), data.toString());
        }

        @Override
        public void onScanFailed(int errorCode)
        {
            super.onScanFailed(errorCode);
            switch(errorCode)
            {
                case SCAN_FAILED_ALREADY_STARTED:
                    Log.d(SearchSensorFragment.class.getSimpleName(),
                            "Failed to start scanning: already scanning!");
                    break;
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
            Toast.makeText(getActivity(), R.string.scan_failed, Toast.LENGTH_LONG).show();
        }
    };

    public void setBLEScanner(BLEScanner bleScanner)
    {
        this.bleScanner = bleScanner;
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

        this.bleScanner.scanForSensors(this.scanCallback);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_sensor_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.refresh:
                this.bleScanner.scanForSensors(this.scanCallback);
                Toast.makeText(getActivity(), R.string.scan_stated, Toast.LENGTH_LONG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        this.bleScanner.stopScanning();
    }
}
