package de.hs_kl.wcn2_alarm;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import de.hs_kl.wcn2_alarm.alarms.WCN2Alarm;
import de.hs_kl.wcn2_sensors.SensorData;

public class WCN2AlarmView
{
    private Context context;
    private  WCN2Alarm alarm;

    private LinearLayout root;
    private LinearLayout connectedSensors;

    private Map<String, ImageView> batteryLevelViews = new HashMap<>();
    private Map<String, ImageView> signalStrengthViews = new HashMap<>();
    private Map<String, TextView> temperatureViews = new HashMap<>();
    private Map<String, TextView> humidityViews = new HashMap<>();

    public WCN2AlarmView(Context context, WCN2Alarm alarm)
    {
        this.context = context;
        this.alarm = alarm;

        this.root = (LinearLayout)LayoutInflater.from(this.context)
                                                .inflate(R.layout.alarm_overview, null);
        this.connectedSensors = this.root.findViewById(R.id.connected_sensors);

        TextView alarmName = this.root.findViewById(R.id.name);
        alarmName.setText(alarm.getName());

        View toggleConnectedSensors = this.root.findViewById(R.id.toggle_connected_sensors);
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
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View sensorView = inflater.inflate(R.layout.connected_sensor, null);

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

    public View getRoot()
    {
        return  this.root;
    }

    public void updateView()
    {
        if (this.alarm.isTriggered())
        {
            int padding = this.root.getPaddingTop();
            this.root.setBackgroundResource(R.drawable.border_shape);
            this.root.setPadding(padding, padding, padding, padding);
        }
        else
        {
            this.root.setBackgroundResource(0);
        }

        for (SensorData sensorData: this.alarm.getSensorData())
        {
            Log.d("wcntesting", sensorData.getMacAddress());
            this.batteryLevelViews.get(sensorData.getMacAddress()).setImageDrawable(
                    sensorData.getBatteryLevelDrawable(this.context.getResources()));
            this.signalStrengthViews.get(sensorData.getMacAddress()).setImageDrawable(
                    sensorData.getSignalStrengthDrawable(this.context.getResources()));
            this.temperatureViews.get(sensorData.getMacAddress()).setText(sensorData.getTemperature() + " °C");
            this.humidityViews.get(sensorData.getMacAddress()).setText(sensorData.getRelativeHumidity() + " %");
        }
    }
}