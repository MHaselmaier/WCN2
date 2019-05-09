package de.hs_kl.wcn2_alarm;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import de.hs_kl.wcn2_alarm.alarms.WCN2Alarm;
import de.hs_kl.wcn2_sensors.SensorData;

public class WCN2AlarmView extends LinearLayout
{
    private Context context;
    private  WCN2Alarm alarm;

    private LinearLayout connectedSensors;

    private Map<String, ImageView> batteryLevelViews = new HashMap<>();
    private Map<String, ImageView> signalStrengthViews = new HashMap<>();
    private Map<String, TextView> temperatureViews = new HashMap<>();
    private Map<String, TextView> humidityViews = new HashMap<>();

    public WCN2AlarmView(Context context, WCN2Alarm alarm)
    {
        super(context);
        inflate(context, R.layout.alarm_overview, this);

        this.context = context;
        this.alarm = alarm;

        this.connectedSensors = findViewById(R.id.connected_sensors);

        Switch activate = findViewById(R.id.activate);
        activate.setChecked(this.alarm.isActivated());
        activate.setOnCheckedChangeListener((b, isChecked) -> {
            this.alarm.setActivated(isChecked);
            AlarmStorage.getInstance(this.context).saveAlarm(this.alarm);
        });

        TextView alarmName = findViewById(R.id.name);
        alarmName.setText(this.alarm.getName());

        View toggleConnectedSensors = findViewById(R.id.toggle_connected_sensors);
        toggleConnectedSensors.setOnClickListener((v) -> {
            ImageView arrow = v.findViewById(R.id.arrow);
            switch (this.connectedSensors.getVisibility())
            {
                case View.VISIBLE:
                    arrow.setImageResource(R.drawable.ic_down);
                    this.connectedSensors.setVisibility(View.GONE);
                    break;
                case View.GONE:
                    arrow.setImageResource(R.drawable.ic_up);
                    this.connectedSensors.setVisibility(View.VISIBLE);
                    break;
                case View.INVISIBLE:
                default:
                    Log.e(WCN2AlarmView.class.getSimpleName(), "Encountered unexpected state!" +
                            "View should be VISIBLE or GONE!");
                    break;
            }
        });

        for (SensorData sensorData: this.alarm.getSensorData())
        {
            addSensorDataView(sensorData);
        }
        updateView();
    }

    private void addSensorDataView(SensorData sensorData)
    {
        View sensorView = inflate(this.context, R.layout.connected_sensor, null);

        ImageView signalStrength = sensorView.findViewById(R.id.signal_strength);
        this.signalStrengthViews.put(sensorData.getMacAddress(), signalStrength);

        ImageView batteryLevel = sensorView.findViewById(R.id.battery_level);
        this.batteryLevelViews.put(sensorData.getMacAddress(), batteryLevel);

        TextView sensorID = sensorView.findViewById(R.id.sensor_id);
        sensorID.setText(this.context.getString(R.string.sensor_id, sensorData.getSensorID()));

        TextView temperature = sensorView.findViewById(R.id.temperature);
        this.temperatureViews.put(sensorData.getMacAddress(), temperature);

        TextView humidity = sensorView.findViewById(R.id.humidity);
        this.humidityViews.put(sensorData.getMacAddress(), humidity);

        this.connectedSensors.addView(sensorView);
    }

    public void updateView()
    {
        if (this.alarm.isTriggered())
        {
            int padding = getPaddingTop();
            setBackgroundResource(R.drawable.border_shape);
            setPadding(padding, padding, padding, padding);
        }
        else
        {
            setBackgroundResource(0);
        }

        for (SensorData sensorData: this.alarm.getSensorData())
        {
            if (!this.batteryLevelViews.containsKey(sensorData.getMacAddress()))
                addSensorDataView(sensorData);

            this.batteryLevelViews.get(sensorData.getMacAddress()).setImageDrawable(
                    sensorData.getBatteryLevelDrawable(this.context.getResources()));
            this.signalStrengthViews.get(sensorData.getMacAddress()).setImageDrawable(
                    sensorData.getSignalStrengthDrawable(this.context.getResources()));
            this.temperatureViews.get(sensorData.getMacAddress()).setText(
                    this.context.getString(R.string.temperature, sensorData.getTemperature()));
            this.humidityViews.get(sensorData.getMacAddress()).setText(
                    this.context.getString(R.string.humidity, sensorData.getRelativeHumidity()));
        }
    }
}