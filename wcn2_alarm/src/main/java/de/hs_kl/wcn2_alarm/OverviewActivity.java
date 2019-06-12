package de.hs_kl.wcn2_alarm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hs_kl.wcn2_alarm.alarm_triggered_service.AlarmTriggeredService;
import de.hs_kl.wcn2_alarm.alarm_triggered_service.AlarmTriggeredWatchdog;
import de.hs_kl.wcn2_alarm.alarms.WCN2Alarm;
import de.hs_kl.wcn2_alarm.create_alarm.CreateAlarmActivity;
import de.hs_kl.wcn2_sensors.WCN2Activity;

public class OverviewActivity extends WCN2Activity
{
    public static volatile boolean isVisible;
    private LinearLayout alarms;
    private Map<String, WCN2AlarmView> alarmViews = new HashMap<>();
    private View noAlarmsDefined;
    private Handler uiUpdater = new Handler();

    private AlarmStorage alarmStorage;
    private boolean hasActivatedAlarms;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        this.alarms = findViewById(R.id.alarms);
        this.noAlarmsDefined = findViewById(R.id.no_alarms_defined);

        ImageButton addAlarm = findViewById(R.id.add_alarm);
        addAlarm.setOnClickListener((v) -> createAlarm());

        this.alarmStorage = AlarmStorage.getInstance(this);

        Intent intent = new Intent(this, AlarmTriggeredService.class);
        startService(intent);
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

        this.uiUpdater.post(this::updateUI);
        OverviewActivity.isVisible = true;
    }

    private void updateUI()
    {
        WCN2Alarm[] alarms = this.alarmStorage.getAlarms();
        this.noAlarmsDefined.setVisibility(0 == alarms.length ? View.VISIBLE : View.GONE);

        addNewAlarms(alarms);
        removeDeletedAlarms(alarms);

        for (WCN2AlarmView alarmView: this.alarmViews.values())
            alarmView.updateView();

        checkForActivatedAlarms(alarms);

        this.uiUpdater.postDelayed(this::updateUI, 1000);
    }

    private void addNewAlarms(WCN2Alarm[] alarms)
    {
        for (WCN2Alarm alarm: alarms)
        {
            if (this.alarmViews.containsKey(alarm.getName()))
                continue;

            WCN2AlarmView alarmView = new WCN2AlarmView(this, alarm);
            this.alarmViews.put(alarm.getName(), alarmView);
            this.alarms.addView(alarmView);
            this.alarms.invalidate();
        }
    }

    private void removeDeletedAlarms(WCN2Alarm[] alarms)
    {
        Iterator<String> iterator = this.alarmViews.keySet().iterator();

        while (iterator.hasNext())
        {
            String alarmView = iterator.next();
            boolean wasDeleted = true;
            for (WCN2Alarm alarm: alarms)
            {
                if (alarm.getName().equals(alarmView))
                {
                    wasDeleted = false;
                    break;
                }
            }
            if (wasDeleted)
            {
                this.alarms.removeView(this.alarmViews.get(alarmView));
                iterator.remove();
            }
        }
    }

    private void checkForActivatedAlarms(WCN2Alarm[] alarms)
    {
        boolean hasActivatedAlarms = false;
        for (WCN2Alarm alarm: alarms)
        {
            hasActivatedAlarms |= alarm.isActivated();
        }

        if (this.hasActivatedAlarms && !hasActivatedAlarms)
        {
            AlarmTriggeredWatchdog.disable(getApplicationContext());
        }
        if (!this.hasActivatedAlarms && hasActivatedAlarms)
        {
            AlarmTriggeredWatchdog.enable(getApplicationContext());
        }

        this.hasActivatedAlarms = hasActivatedAlarms;
    }

    @Override
    public void onPause()
    {
        super.onPause();

        this.uiUpdater.removeCallbacksAndMessages(null);
        OverviewActivity.isVisible = false;
    }
}
