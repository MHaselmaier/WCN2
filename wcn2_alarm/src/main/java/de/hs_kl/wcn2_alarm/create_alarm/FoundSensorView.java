package de.hs_kl.wcn2_alarm.create_alarm;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import de.hs_kl.wcn2_alarm.R;
import de.hs_kl.wcn2_sensors.SensorData;
import de.hs_kl.wcn2_sensors.util.LastSeenSinceUtil;

public class FoundSensorView
{
    private Context context;
    private View root;
    private ImageView batteryLevel;
    private ImageView signalStrength;
    private TextView lastSeen;
    private CheckBox selected;

    public FoundSensorView(Context context, SensorData sensorData)
    {
        this.context = context;
        this.root = LayoutInflater.from(context).inflate(R.layout.sensor_list_item, null);
        this.batteryLevel = this.root.findViewById(R.id.battery_level);
        this.signalStrength = this.root.findViewById(R.id.signal_strength);
        this.lastSeen = this.root.findViewById(R.id.last_seen);
        this.selected = this.root.findViewById(R.id.selected);

        TextView sensorID = this.root.findViewById(R.id.sensor_id);
        sensorID.setText(this.context.getString(R.string.sensor_id, sensorData.getSensorID()));

        update(sensorData);
    }

    public void update(SensorData sensorData)
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

    public View getRoot()
    {
        return this.root;
    }
}
