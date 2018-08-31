package de.hs_kl.wcn2.fragments.search_sensor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.ble_scanner.SensorData;
import de.hs_kl.wcn2.util.LastSeenSinceUtil;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

public class FoundSensorAdapter extends BaseAdapter
{
    private Context context;
    private List<SensorData> foundSensors = new ArrayList<>();
    private List<FoundSensorView> foundSensorsViews = new ArrayList<>();

    public FoundSensorAdapter(Context context)
    {
        this.context = context;

        initFoundSensors();
    }

    private void initFoundSensors()
    {
        for (SensorData sensorData: TrackedSensorsStorage.getTrackedSensors(this.context))
        {
            this.foundSensors.add(sensorData);
            this.foundSensorsViews.add(createFoundSensorView(sensorData));
        }
    }

    public void add(SensorData sensorData)
    {
        int position = findPositionByMacAddress(sensorData.getMacAddress());

        if (0 <= position)
        {
            this.foundSensors.set(position, sensorData);
        }
        else
        {
            this.foundSensors.add(sensorData);
            this.foundSensorsViews.add(createFoundSensorView(sensorData));
        }
    }

    private int findPositionByMacAddress(String macAddress)
    {
        for (int i = 0; this.foundSensors.size() > i; ++i)
        {
            if (this.foundSensors.get(i).getMacAddress().equals(macAddress))
            {
                return i;
            }
        }
        return -1;
    }

    private FoundSensorView createFoundSensorView(SensorData sensorData)
    {
        View view = LayoutInflater.from(this.context).inflate(R.layout.sensor_list_item, null);
        FoundSensorView foundSensorView = new FoundSensorView(view);

        foundSensorView.mnemonicEdit.setOnClickListener(new MnemonicEditOnClickListener(this.context, sensorData));

        foundSensorView.trackSwitch.setChecked(TrackedSensorsStorage.isTracked(this.context, sensorData));
        foundSensorView.trackSwitch.setText(foundSensorView.trackSwitch.isChecked() ? R.string.sensor_tracked : R.string.sensor_not_tracked);
        foundSensorView.trackSwitch.setOnCheckedChangeListener(new SensorTrackedChangeListener(this.context, sensorData));

        foundSensorView.macAddress.setText(sensorData.getMacAddress());
        foundSensorView.sensorID.setText(this.context.getResources().getString(R.string.sensor_id, sensorData.getSensorID()));

        return foundSensorView;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        SensorData sensorData = this.foundSensors.get(position);
        FoundSensorView foundSensorView = this.foundSensorsViews.get(position);

        foundSensorView.lastSeen.setText(LastSeenSinceUtil.getTimeSinceString(this.context, sensorData.getTimestamp()));
        foundSensorView.batteryLevel.setImageDrawable(sensorData.getBatteryLevelDrawable(this.context.getResources()));
        foundSensorView.signalStrength.setImageDrawable(sensorData.getSignalStrengthDrawable(this.context.getResources()));
        foundSensorView.mnemonic.setText(this.context.getResources().getString(R.string.mnemonic, sensorData.getMnemonic()));
        if (sensorData.getMnemonic().equals("null"))
        {
            foundSensorView.mnemonic.setVisibility(View.GONE);
        }
        else
        {
            foundSensorView.mnemonic.setVisibility(View.VISIBLE);
        }

        return foundSensorView.root;
    }

    @Override
    public int getCount()
    {
        return this.foundSensors.size();
    }

    @Override
    public long getItemId(int position)
    {
        return this.foundSensors.get(position).getMacAddress().hashCode();
    }

    @Override
    public SensorData getItem(int position)
    {
        return this.foundSensors.get(position);
    }
}
