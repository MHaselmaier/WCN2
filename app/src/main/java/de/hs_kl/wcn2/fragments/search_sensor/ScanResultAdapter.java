package de.hs_kl.wcn2.fragments.search_sensor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.ble_scanner.SensorData;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;
import de.hs_kl.wcn2.util.LastSeenSinceUtil;

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

    public SensorData getItem(String macAddress)
    {
        return this.sensorData.get(getPosition(macAddress));
    }

    @Override
    public long getItemId(int position)
    {
        return this.sensorData.get(position).getMacAddress().hashCode();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent)
    {
        final SensorData sensorData = this.sensorData.get(position);

        if (null == view)
        {
            view = this.inflater.inflate(R.layout.sensor_list_item, null);

            ImageButton mnemonicEdit = view.findViewById(R.id.mnemonic_edit);
            mnemonicEdit.setOnClickListener(new MnemonicEditOnClickListener(this.context, this));

            Switch trackSwitch = view.findViewById(R.id.sensor_tracked);
            trackSwitch.setOnCheckedChangeListener(new SensorTrackedChangeListener(this.context, this));
        }

        TextView sensorMacAddress = view.findViewById(R.id.sensor_mac_address);
        sensorMacAddress.setText(sensorData.getMacAddress());

        Switch trackSwitch = view.findViewById(R.id.sensor_tracked);
        trackSwitch.setChecked(TrackedSensorsStorage.isTracked(this.context, sensorData));
        trackSwitch.setText(trackSwitch.isChecked() ? R.string.sensor_tracked : R.string.sensor_not_tracked);

        TextView mnemonic = view.findViewById(R.id.mnemonic);
        mnemonic.setText(this.context.getResources().getString(R.string.mnemonic, sensorData.getMnemonic()));
        if (sensorData.getMnemonic().equals("null"))
        {
            mnemonic.setVisibility(View.GONE);
        }
        else
        {
            mnemonic.setVisibility(View.VISIBLE);
        }

        TextView sensorID = view.findViewById(R.id.sensor_id);
        sensorID.setText(this.context.getResources().getString(R.string.sensor_id, sensorData.getSensorID()));

        TextView lastSeenView = view.findViewById(R.id.last_seen);
        lastSeenView.setText(LastSeenSinceUtil.getTimeSinceString(this.context, sensorData.getTimestamp()));

        ImageView batteryLevel = view.findViewById(R.id.battery_level);
        batteryLevel.setImageDrawable(sensorData.getBatteryLevelDrawable(this.context.getResources()));

        ImageView signalStrength = view.findViewById(R.id.signal_strength);
        signalStrength.setImageDrawable(sensorData.getSignalStrengthDrawable(this.context.getResources()));

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
