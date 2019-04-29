package de.hs_kl.wcn2_alarm.alarms;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collection;
import java.util.List;

import de.hs_kl.wcn2_alarm.R;
import de.hs_kl.wcn2_sensors.SensorData;

public abstract class WCN2Alarm
{
    public enum Operator
    {
        GREATER, EQUAL, LESS
    }

    private Context context;
    private View view;
    protected Collection<SensorData> sensors;
    protected String name;
    protected Operator operator;

    public WCN2Alarm(Context context, String name, Operator operator, Collection<SensorData> sensors)
    {
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(this.context);
        this.view = inflater.inflate(R.layout.alarm_overview, null);
        this.name = name;

        TextView alarmName = this.view.findViewById(R.id.alarm);
        alarmName.setText(this.name);

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

        this.operator = operator;
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
            sensorID.setText(this.context.getString(R.string.sensor_id, sensor.getSensorID()));

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

    public String getName()
    {
        return this.name;
    }

    public Operator getOperator()
    {
        return this.operator;
    }

    public void setOperator(Operator operator)
    {
        this.operator = operator;
    }

    public Collection<SensorData> getSensorData()
    {
        return this.sensors;
    }

    public abstract boolean isTriggered();
    public abstract float getThreshold();

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof WCN2Alarm)) return false;

        WCN2Alarm alarm = (WCN2Alarm) o;

        if (null != this.context ? !this.context.equals(alarm.context) : null != alarm.context)
            return false;
        if (null != this.view ? !this.view.equals(alarm.view) : null != alarm.view)
            return false;
        if (null != this.sensors ? !this.sensors.equals(alarm.sensors) : null != alarm.sensors)
            return false;
        return this.operator == alarm.operator;
    }

    @Override
    public int hashCode() {
        int result = null != this.context? this.context.hashCode() : 0;
        result = 31 * result + (null != this.view ? this.view.hashCode() : 0);
        result = 31 * result + (null != this.sensors ? this.sensors.hashCode() : 0);
        result = 31 * result + (null != this.operator ? this.operator.hashCode() : 0);
        return result;
    }

    public static WCN2Alarm createAlarm(Context context, String name, int type, Operator operator,
                                        float value, List<SensorData> sensorData)
    {
        switch (type)
        {
        case 0:
            return new WCN2TemperatureAlarm(context, name, operator, value, sensorData);
        case 1:
            return new WCN2TemperatureAlarm(context, name, operator, value, sensorData);
        case 2:
            return new WCN2TemperatureAlarm(context, name, operator, value, sensorData);
        }

        return null;
    }
}
