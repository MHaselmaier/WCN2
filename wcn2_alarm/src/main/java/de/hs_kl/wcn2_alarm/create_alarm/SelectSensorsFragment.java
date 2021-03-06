package de.hs_kl.wcn2_alarm.create_alarm;

import android.bluetooth.le.ScanFilter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2_alarm.R;
import de.hs_kl.wcn2_sensors.WCN2Scanner;
import de.hs_kl.wcn2_sensors.WCN2SensorData;
import de.hs_kl.wcn2_sensors.WCN2SensorDataListener;

public class SelectSensorsFragment extends Fragment implements WCN2SensorDataListener
{
    private List<WCN2SensorData> foundSensors = new ArrayList<>();
    private List<FoundSensorView> foundSensorsViews = new ArrayList<>();

    private LinearLayout foundSensorsContainer;
    private View label;
    private View noSensorsFoundView;

    private Handler handler = new Handler();

    @Override
    public List<ScanFilter> getScanFilter()
    {
        return new ArrayList<>();
    }

    @Override
    public void onScanResult(WCN2SensorData result)
    {
        for (int i = 0; this.foundSensors.size() > i; ++i)
        {
            if (this.foundSensors.get(i).getMacAddress().equals(result.getMacAddress()))
            {
                this.foundSensors.set(i, result);
                return;
            }
        }

        foundSensor(result);
    }

    private void foundSensor(WCN2SensorData sensorData)
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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle b)
    {
        View view = inflater.inflate(R.layout.select_sensors, container, false);

        this.foundSensorsContainer = view.findViewById(R.id.container);
        Button save = view.findViewById(R.id.save);
        save.setOnClickListener((v) -> handleOnSaveClicked());

        this.label = view.findViewById(R.id.label);
        this.noSensorsFoundView = view.findViewById(R.id.no_sensors_found);

        Bundle arguments = getArguments();
        if (null != arguments && CreateAlarmActivity.MODE_EDIT.equals(
                arguments.getString(CreateAlarmActivity.EXTRA_MODE)))
        {
            loadDataFromArguments();
        }

        return view;
    }

    private void handleOnSaveClicked()
    {
        if (!isAtLeastOneSensorSelected())
        {
            Toast.makeText(getContext(), R.string.no_sensor_selected, Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList<String> macAddresses = getSelectedMacAddresses();
        ArrayList<Byte> ids = getSelectedIDs();
        sendMacAddressesToActivity(macAddresses, ids);
    }

    private void loadDataFromArguments()
    {
        Bundle args = getArguments();

        ArrayList<String> macAddresses = args.getStringArrayList(
                                                CreateAlarmActivity.EXTRA_MAC_ADDRESSES);
        ArrayList<Byte> ids = (ArrayList<Byte>)args.getSerializable(CreateAlarmActivity.EXTRA_IDS);

        for (int i = 0; ids.size() > i; ++i)
        {
            WCN2SensorData sensorData = new WCN2SensorData(ids.get(i), "null", macAddresses.get(i));
            this.foundSensors.add(sensorData);
            FoundSensorView view = new FoundSensorView(getActivity(), sensorData);
            view.setSelected(true);
            this.foundSensorsViews.add(view);
            this.foundSensorsContainer.addView(view);
        }
    }

    private ArrayList<String> getSelectedMacAddresses()
    {
        ArrayList<String> macAddress = new ArrayList<>();
        for (int i = 0; this.foundSensors.size() > i; ++i)
        {
            if (null == this.foundSensorsViews.get(i)) continue;

            if (this.foundSensorsViews.get(i).isSelected())
            {
                macAddress.add(this.foundSensors.get(i).getMacAddress());
            }
        }
        return macAddress;
    }

    private ArrayList<Byte> getSelectedIDs()
    {
        ArrayList<Byte> ids = new ArrayList<>();
        for (int i = 0; this.foundSensors.size() > i; ++i)
        {
            if (null == this.foundSensorsViews.get(i)) continue;

            if (this.foundSensorsViews.get(i).isSelected())
            {
                ids.add(this.foundSensors.get(i).getSensorID());
            }
        }
        return ids;
    }

    private void sendMacAddressesToActivity(ArrayList<String> macAddresses, ArrayList<Byte> ids)
    {
        Intent intent = new Intent(getActivity().getBaseContext(), CreateAlarmActivity.class);
        intent.putExtra(CreateAlarmActivity.EXTRA_SENDER, getClass().getSimpleName());
        intent.putExtra(CreateAlarmActivity.EXTRA_MAC_ADDRESSES, macAddresses);
        intent.putExtra(CreateAlarmActivity.EXTRA_IDS, ids);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().startActivity(intent);
    }

    private boolean isAtLeastOneSensorSelected()
    {
        for (FoundSensorView foundSensorsView: this.foundSensorsViews)
        {
            if (null == foundSensorsView) continue;

            if (foundSensorsView.isSelected())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        WCN2Scanner.registerSensorDataListener(this);

        this.handler.post(new Runnable() {
            @Override
            public void run()
            {
                updateFoundSensorsViews();
                SelectSensorsFragment.this.handler.postDelayed(this, 1000);
            }
        });
    }

    private void updateFoundSensorsViews()
    {
        for (int i = 0; this.foundSensors.size() > i; ++i)
        {
            if (null == this.foundSensorsViews.get(i)) continue;
            this.foundSensorsViews.get(i).update(this.foundSensors.get(i));
        }

        this.noSensorsFoundView.setVisibility(0 < this.foundSensors.size() ?
                View.GONE : View.VISIBLE);
        this.label.setVisibility(0 < this.foundSensors.size() ?
                View.VISIBLE : View.GONE);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        WCN2Scanner.unregisterSensorDataListener(this);

        this.handler.removeCallbacksAndMessages(null);
    }
}
