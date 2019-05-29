package de.hs_kl.wcn2.fragments.search_sensor;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2_sensors.WCN2SensorData;
import de.hs_kl.wcn2_sensors.util.LastSeenSinceUtil;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

class FoundSensorView extends ConstraintLayout
{
    private Context context;
    private TrackedSensorsStorage trackedSensors;
    private TextView lastSeen;
    private Switch trackSwitch;
    private TextView mnemonic;
    private ImageView batteryLevel;
    private ImageView signalStrength;

    FoundSensorView(Context context, WCN2SensorData sensorData)
    {
        super(context);
        inflate(context, R.layout.sensor_list_item, this);

        this.context = context;
        this.trackedSensors = TrackedSensorsStorage.getInstance(this.context);

        boolean isTracked = this.trackedSensors.isTracked(sensorData);
        ImageButton mnemonicEdit = findViewById(R.id.mnemonic_edit);
        mnemonicEdit.setOnClickListener((v) ->
                MnemonicEditDialog.buildMnemonicEditDialog(this.context, sensorData).show());
        mnemonicEdit.setVisibility(isTracked ? View.VISIBLE: View.INVISIBLE);

        this.trackSwitch = findViewById(R.id.sensor_tracked);
        this.trackSwitch.setText((isTracked ? R.string.sensor_tracked : R.string.sensor_not_tracked));
        this.trackSwitch.setOnCheckedChangeListener(new SensorTrackedChangeListener(this.context,
                sensorData, mnemonicEdit));

        TextView sensorID = findViewById(R.id.sensor_id);
        sensorID.setText(this.context.getResources().getString(R.string.sensor_id,
                sensorData.getSensorID()));

        this.lastSeen = findViewById(R.id.last_seen);
        this.mnemonic = findViewById(R.id.mnemonic);
        this.batteryLevel = findViewById(R.id.battery_level);
        this.signalStrength = findViewById(R.id.signal_strength);
    }

    void updateView(WCN2SensorData sensorData)
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