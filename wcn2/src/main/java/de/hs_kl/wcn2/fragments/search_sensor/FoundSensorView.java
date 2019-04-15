package de.hs_kl.wcn2.fragments.search_sensor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2_sensors.SensorData;
import de.hs_kl.wcn2.util.LastSeenSinceUtil;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

class FoundSensorView
{
    private Context context;
    private TrackedSensorsStorage trackedSensors;
    private View root;
    private TextView lastSeen;
    private Switch trackSwitch;
    private TextView mnemonic;
    private ImageView batteryLevel;
    private ImageView signalStrength;

    FoundSensorView(Context context, SensorData sensorData)
    {
        this.context = context;
        this.trackedSensors = TrackedSensorsStorage.getInstance(this.context);
        this.root = LayoutInflater.from(this.context).inflate(R.layout.sensor_list_item, null);

        boolean isTracked = this.trackedSensors.isTracked(sensorData);
        ImageButton mnemonicEdit = this.root.findViewById(R.id.mnemonic_edit);
        mnemonicEdit.setOnClickListener((v) ->
                MnemonicEditDialog.buildMnemonicEditDialog(this.context, sensorData).show());
        mnemonicEdit.setVisibility(isTracked ? View.VISIBLE: View.INVISIBLE);

        this.trackSwitch = this.root.findViewById(R.id.sensor_tracked);
        this.trackSwitch.setText((isTracked ? R.string.sensor_tracked : R.string.sensor_not_tracked));
        this.trackSwitch.setOnCheckedChangeListener(new SensorTrackedChangeListener(this.context,
                sensorData, mnemonicEdit));

        TextView sensorID = this.root.findViewById(R.id.sensor_id);
        sensorID.setText(this.context.getResources().getString(R.string.sensor_id,
                sensorData.getSensorID()));

        this.lastSeen = this.root.findViewById(R.id.last_seen);
        this.mnemonic = this.root.findViewById(R.id.mnemonic);
        this.batteryLevel = this.root.findViewById(R.id.battery_level);
        this.signalStrength = this.root.findViewById(R.id.signal_strength);
    }

    View getRoot()
    {
        return this.root;
    }

    void updateView(SensorData sensorData)
    {
        this.lastSeen.setText(LastSeenSinceUtil.getTimeSinceString(this.context,
                sensorData.getTimestamp()));
        this.batteryLevel.setImageDrawable(sensorData
                .getBatteryLevelDrawable(this.context.getResources()));
        this.signalStrength.setImageDrawable(sensorData
                .getSignalStrengthDrawable(this.context.getResources()));
        boolean isTracked = this.trackedSensors.isTracked(sensorData);
        this.trackSwitch.setChecked(isTracked);
        String mnemonic = this.trackedSensors.getMnemonic(sensorData.getMacAddress());
        if (mnemonic.equals("null"))
        {
            this.mnemonic.setVisibility(View.GONE);
        }
        else
        {
            this.mnemonic.setText(this.context.getString(R.string.mnemonic, mnemonic));
            this.mnemonic.setVisibility(View.VISIBLE);
        }
    }
}