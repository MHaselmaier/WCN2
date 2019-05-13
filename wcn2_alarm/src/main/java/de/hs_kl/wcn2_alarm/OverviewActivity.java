package de.hs_kl.wcn2_alarm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2_alarm.alarms.WCN2Alarm;
import de.hs_kl.wcn2_alarm.create_alarm.CreateAlarmActivity;
import de.hs_kl.wcn2_sensors.WCN2Activity;

public class OverviewActivity extends WCN2Activity
{
    private LinearLayout alarms;
    private List<WCN2AlarmView> alarmViews = new ArrayList<>();
    private Handler uiUpdater = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        this.alarms = findViewById(R.id.alarms);

        ImageButton addAlarm = findViewById(R.id.add_alarm);
        addAlarm.setOnClickListener((v) -> createAlarm());
    }

    private void createAlarm()
    {
        Intent intent = new Intent(getBaseContext(), CreateAlarmActivity.class);
        intent.putExtra(CreateAlarmActivity.EXTRA_MODE, CreateAlarmActivity.MODE_CREATE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        this.alarms.removeAllViews();
        WCN2Alarm[] alarms = AlarmStorage.getInstance(this).getAlarms();
        for (WCN2Alarm alarm: alarms)
        {
            WCN2AlarmView alarmView = new WCN2AlarmView(this, alarm);
            this.alarmViews.add(alarmView);
            this.alarms.addView(alarmView);
        }
        if (0 == alarms.length)
        {
            TextView noAlarms = new TextView(this);
            noAlarms.setText(R.string.no_alarms_defined);
            this.alarms.addView(noAlarms);
        }

        this.uiUpdater.post(this::updateUI);
    }

    private void updateUI()
    {
        for (WCN2AlarmView alarmView: this.alarmViews)
            alarmView.updateView();

        this.uiUpdater.postDelayed(this::updateUI, 1000);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        this.uiUpdater.removeCallbacksAndMessages(null);
    }
}
