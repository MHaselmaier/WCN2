package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2_sensors.SensorData;
import de.hs_kl.wcn2_sensors.util.LastSeenSinceUtil;

class TrackedSensorView
{
    private Context context;
    private View root;
    private ImageView warning;
    private TextView sensorID;
    private TextView mnemonic;
    private TextView lastSeen;
    private TextView temperature;
    private TextView humidity;
    private ImageView batteryLevel;
    private ImageView signalStrength;

    TrackedSensorView(Context context, SensorData sensorData)
    {
        this.context = context;
        this.root = LayoutInflater.from(this.context).inflate(R.layout.tracked_sensor_overview, null);

        this.warning = this.root.findViewById(R.id.sensor_warning);
        this.sensorID = this.root.findViewById(R.id.sensor_id);
        this.mnemonic = this.root.findViewById(R.id.mnemonic);
        this.lastSeen = this.root.findViewById(R.id.last_seen);
        this.temperature = this.root.findViewById(R.id.temperature);
        this.humidity = this.root.findViewById(R.id.humidity);
        this.batteryLevel = this.root.findViewById(R.id.battery_level);
        this.signalStrength = this.root.findViewById(R.id.signal_strength);

        updateView(sensorData);
    }

    void updateView(SensorData sensorData)
    {
        if (sensorData.isTimedOut() || sensorData.isBatteryLow())
        {
            this.warning.setVisibility(View.VISIBLE);
        }
        else
        {
            this.warning.setVisibility(View.GONE);
        }

        this.sensorID.setText(this.context.getResources().getString(R.string.sensor_id,
                sensorData.getSensorID()));

        String mnemonic = sensorData.getMnemonic();
        if (mnemonic.equals("null"))
        {
            this.mnemonic.setVisibility(View.GONE);
        }
        else
        {
            this.mnemonic.setText(this.context.getResources().getString(R.string.mnemonic, mnemonic));
            this.mnemonic.setVisibility(View.VISIBLE);
        }

        this.lastSeen.setText(LastSeenSinceUtil.getTimeSinceString(this.context,
                sensorData.getTimestamp()));

        this.temperature.setText(this.context.getResources().getString(R.string.temperature,
                sensorData.getTemperature()));

        this.humidity.setText(this.context.getResources().getString(R.string.humidity,
                sensorData.getRelativeHumidity()));

        this.batteryLevel.setImageDrawable(sensorData.getBatteryLevelDrawable(this.context
                .getResources()));

        this.signalStrength.setImageDrawable(sensorData.getSignalStrengthDrawable(this.context
                .getResources()));
    }

    View getRoot()
    {
        return this.root;
    }
}
