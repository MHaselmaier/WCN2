package de.hs_kl.wcn2_alarm;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import de.hs_kl.wcn2_sensors.SensorData;

public abstract class WCN2Alarm
{
    protected Context context;
    private View view;
    protected List<SensorData> sensors;

    public WCN2Alarm(Context context, String name, List<SensorData> sensors)
    {
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(this.context);
        this.view = inflater.inflate(R.layout.alarm_overview, null);

        TextView alarmName = this.view.findViewById(R.id.alarm);
        alarmName.setText(name);

        View toggleConnectedSensors = this.view.findViewById(R.id.toggle_connected_sensors);
        toggleConnectedSensors.setOnClickListener((v) -> {
            View connectedSensors = this.view.findViewById(R.id.connected_sensors);
            ImageView arrow = v.findViewById(R.id.arrow);
            switch (connectedSensors.getVisibility())
            {
            case View.VISIBLE:
                arrow.setImageResource(R.drawable.ic_down);
                connectedSensors.setVisibility(View.GONE);
                break;
            case View.GONE:
                arrow.setImageResource(R.drawable.ic_up);
                connectedSensors.setVisibility(View.VISIBLE);
                break;
            case View.INVISIBLE:
            default:
                Log.e(WCN2Alarm.class.getSimpleName(), "Encountered unexpected state!" +
                        "View should be VISIBLE or GONE!");
                break;
            }
        });

        this.sensors = sensors;
        loadSensorViews();
    }

    private void loadSensorViews()
    {
        LayoutInflater inflater = LayoutInflater.from(this.context);

        LinearLayout connectedSensors = this.view.findViewById(R.id.connected_sensors);
        connectedSensors.removeAllViews();
        for (SensorData sensor: this.sensors)
        {
            View sensorView = inflater.inflate(R.layout.connected_sensor, null);

            ImageView signalStrength = sensorView.findViewById(R.id.signal_strength);
            signalStrength.setBackground(sensor.getSignalStrengthDrawable(this.context.getResources()));

            ImageView batteryLevel = sensorView.findViewById(R.id.battery_level);
            batteryLevel.setBackground(sensor.getBatteryLevelDrawable(this.context.getResources()));

            TextView sensorID = sensorView.findViewById(R.id.sensor_id);
            sensorID.setText("WCN" + sensor.getSensorID());

            TextView temperature = sensorView.findViewById(R.id.temperature);
            temperature.setText(sensor.getTemperature() + " °C");

            TextView humidity = sensorView.findViewById(R.id.humidity);
            humidity.setText(sensor.getRelativeHumidity() + " %");

            connectedSensors.addView(sensorView);
        }
    }

    public View getView()
    {
        return this.view;
    }

    public abstract boolean isTriggered();
}
