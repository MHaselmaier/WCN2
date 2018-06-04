package de.hs_kl.blesensor.fragments.search_sensor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import de.hs_kl.blesensor.R;
import de.hs_kl.blesensor.ble_scanner.SensorData;
import de.hs_kl.blesensor.util.TrackedSensorsStorage;
import de.hs_kl.blesensor.util.LastSeenSinceUtil;

public class ScanResultAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater inflater;
    private List<SensorData> sensorData;

    public ScanResultAdapter(Context context, LayoutInflater inflater)
    {
        this.context = context;
        this.inflater = inflater;
        this.sensorData = TrackedSensorsStorage.getTrackedSensors(this.context);
    }

    @Override
    public int getCount()
    {
        return this.sensorData.size();
    }

    @Override
    public SensorData getItem(int position)
    {
        return this.sensorData.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return this.sensorData.get(position).getMacAddress().hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        SensorData sensorData = this.sensorData.get(position);

        if (null == view) {
            view = this.inflater.inflate(R.layout.sensor_list_item, null);

            TextView sensorMacAddress = view.findViewById(R.id.sensor_mac_address);
            sensorMacAddress.setText(sensorData.getMacAddress());

            Switch trackSwitch = view.findViewById(R.id.sensor_tracked);
            trackSwitch.setChecked(TrackedSensorsStorage.isTracked(this.context, sensorData));
            trackSwitch.setText(trackSwitch.isChecked() ? R.string.sensor_tracked : R.string.sensor_not_tracked);
            trackSwitch.setOnCheckedChangeListener(new SensorTrackedChangeListener(this.context, sensorData));
        }

        TextView sensorID = view.findViewById(R.id.sensor_id);
        sensorID.setText(this.context.getResources().getString(R.string.sensor_id, sensorData.getSensorID()));

        TextView lastSeenView = view.findViewById(R.id.last_seen);
        lastSeenView.setText(LastSeenSinceUtil.getTimeSinceString(this.context, sensorData.getTimestamp()));

        return view;
    }

    public void add(SensorData result)
    {
        int position = getPosition(result.getMacAddress());

        if (0 <= position)
        {
            this.sensorData.set(position, result);
        }
        else
        {
            this.sensorData.add(result);
        }
    }

    private int getPosition(String address)
    {
        for (int i = 0; i < this.sensorData.size(); ++i)
        {
            if (this.sensorData.get(i).getMacAddress().equals(address))
            {
                return i;
            }
        }
        return -1;
    }
}
