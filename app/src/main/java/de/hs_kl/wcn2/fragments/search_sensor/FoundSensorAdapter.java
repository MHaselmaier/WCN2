package de.hs_kl.wcn2.fragments.search_sensor;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
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
    private TrackedSensorsStorage trackedSensors;
    private List<SensorData> foundSensors = new ArrayList<>();
    private List<FoundSensorView> foundSensorsViews = new ArrayList<>();

    public FoundSensorAdapter(Context context)
    {
        this.context = context;
        this.trackedSensors = TrackedSensorsStorage.getInstance(this.context);
        initFoundSensors();
    }

    private void initFoundSensors()
    {
        for (SensorData sensorData: this.trackedSensors.getTrackedSensors())
        {
            this.foundSensors.add(sensorData);
            this.foundSensorsViews.add(createFoundSensorView(sensorData));
        }
    }

    public void add(SensorData sensorData)
    {
        int position = findPositionByMacAddress(sensorData.getMacAddress());

        if (-1 == position)
        {
            this.foundSensors.add(sensorData);
            this.foundSensorsViews.add(createFoundSensorView(sensorData));
            return;
        }

        this.foundSensors.set(position, sensorData);
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
        FoundSensorView foundSensorView = new FoundSensorView(this.context);

        final Dialog mnemonicEditDialog = MnemonicEditDialog.buildMnemonicEditDialog(this.context,
                sensorData);
        foundSensorView.mnemonicEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mnemonicEditDialog.show();
            }
        });

        boolean isTracked = this.trackedSensors.isTracked(sensorData);
        foundSensorView.trackSwitch.setChecked(isTracked);
        int labelResourceID = (isTracked ? R.string.sensor_tracked : R.string.sensor_not_tracked);
        foundSensorView.trackSwitch.setText(labelResourceID);
        SensorTrackedChangeListener listener = new SensorTrackedChangeListener(this.context,
                sensorData);
        foundSensorView.trackSwitch.setOnCheckedChangeListener(listener);
        foundSensorView.macAddress.setText(sensorData.getMacAddress());
        String sensorID = this.context.getResources().getString(R.string.sensor_id,
                sensorData.getSensorID());
        foundSensorView.sensorID.setText(sensorID);

        return foundSensorView;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        SensorData sensorData = this.foundSensors.get(position);
        FoundSensorView foundSensorView = this.foundSensorsViews.get(position);

        String lastSeen = LastSeenSinceUtil.getTimeSinceString(this.context,
                sensorData.getTimestamp());
        foundSensorView.lastSeen.setText(lastSeen);
        Drawable batteryLevel = sensorData.getBatteryLevelDrawable(this.context.getResources());
        foundSensorView.batteryLevel.setImageDrawable(batteryLevel);
        Drawable signalStrength = sensorData.getSignalStrengthDrawable(this.context.getResources());
        foundSensorView.signalStrength.setImageDrawable(signalStrength);
        String mnemonic = this.trackedSensors.getMnemonic(sensorData.getMacAddress());
        if (mnemonic.equals("null"))
        {
            foundSensorView.mnemonic.setVisibility(View.GONE);
        }
        else
        {
            foundSensorView.mnemonic.setText(this.context.getString(R.string.mnemonic, mnemonic));
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
