package de.hs_kl.wcn2_alarm.create_alarm;

import android.content.Context;
import android.content.res.Resources;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.hs_kl.wcn2_alarm.R;
import de.hs_kl.wcn2_sensors.WCN2SensorData;
import de.hs_kl.wcn2_sensors.util.LastSeenSinceUtil;

public class FoundSensorView extends LinearLayout
{
    private Context context;
    private ImageView batteryLevel;
    private ImageView signalStrength;
    private TextView lastSeen;
    private CheckBox selected;

    public FoundSensorView(Context context, WCN2SensorData sensorData)
    {
        super(context);
        inflate(context, R.layout.sensor_list_item, this);

        this.context = context;
        this.batteryLevel = findViewById(R.id.battery_level);
        this.signalStrength = findViewById(R.id.signal_strength);
        this.lastSeen = findViewById(R.id.last_seen);
        this.selected = findViewById(R.id.selected);

        TextView sensorID = findViewById(R.id.sensor_id);
        sensorID.setText(this.context.getString(R.string.sensor_id, sensorData.getSensorID() & 0xFF));

        update(sensorData);
    }

    public void update(WCN2SensorData sensorData)
    {
        Resources resources = this.context.getResources();
        this.batteryLevel.setImageDrawable(sensorData.getBatteryLevelDrawable(resources));
        this.signalStrength.setImageDrawable(sensorData.getSignalStrengthDrawable(resources));
        this.lastSeen.setText(LastSeenSinceUtil.getTimeSinceString(this.context,
                sensorData.getTimestamp()));
    }

    public void setSelected(boolean selected)
    {
        this.selected.setChecked(selected);
    }

    public boolean isSelected()
    {
        return this.selected.isChecked();
    }
}
