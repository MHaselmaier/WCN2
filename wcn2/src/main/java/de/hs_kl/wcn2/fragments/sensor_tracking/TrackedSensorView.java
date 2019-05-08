package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2_sensors.SensorData;
import de.hs_kl.wcn2_sensors.util.LastSeenSinceUtil;

class TrackedSensorView extends LinearLayout
{
    private Context context;
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
        super(context);

        this.context = context;
        inflate(context, R.layout.tracked_sensor_view, this);

        this.warning = findViewById(R.id.sensor_warning);
        this.sensorID = findViewById(R.id.sensor_id);
        this.mnemonic = findViewById(R.id.mnemonic);
        this.lastSeen = findViewById(R.id.last_seen);
        this.temperature = findViewById(R.id.temperature);
        this.humidity = findViewById(R.id.humidity);
        this.batteryLevel = findViewById(R.id.battery_level);
        this.signalStrength = findViewById(R.id.signal_strength);

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
}
