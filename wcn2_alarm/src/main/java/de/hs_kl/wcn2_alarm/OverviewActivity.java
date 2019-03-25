package de.hs_kl.wcn2_alarm;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2_sensors.SensorData;
import de.hs_kl.wcn2_sensors.WCN2Activity;

public class OverviewActivity extends WCN2Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        LinearLayout alarms = findViewById(R.id.alarms);
        TextView t = new TextView(this);
        t.setText("No alarms");
        alarms.addView(t);

        ImageButton addAlarm = findViewById(R.id.add_alarm);
        addAlarm.setOnClickListener((v) -> {

            List<SensorData> sensors = new ArrayList<>();
            for (byte i = 0; 3 > i; ++i)
            {
                sensors.add(new SensorData(i, "Test", "00:00:00:00:00:00"));
            }

            WCN2Alarm alarm = new WCN2TemperatureAlarm(this, "Test", sensors, 20);

            alarms.addView(alarm.getView());

        });
    }
}
