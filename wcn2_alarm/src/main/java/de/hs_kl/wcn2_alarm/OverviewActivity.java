package de.hs_kl.wcn2_alarm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.hs_kl.wcn2_alarm.alarms.WCN2Alarm;
import de.hs_kl.wcn2_alarm.create_alarm.CreateAlarmActivity;
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
        Intent intent = new Intent(getBaseContext(), CreateAlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        addAlarm.setOnClickListener((v) -> startActivity(intent));
    }

    @Override
    public void onResume()
    {
        super.onResume();

        WCN2Alarm[] alarms = AlarmStorage.getInstance(this).getAlarms();
        for (WCN2Alarm alarm: alarms)
            Log.d("wcntesting", alarm.getName());
    }
}
