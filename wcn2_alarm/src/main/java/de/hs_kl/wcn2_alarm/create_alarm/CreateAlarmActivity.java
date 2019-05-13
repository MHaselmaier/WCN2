package de.hs_kl.wcn2_alarm.create_alarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2_alarm.AlarmStorage;
import de.hs_kl.wcn2_alarm.OverviewActivity;
import de.hs_kl.wcn2_alarm.R;
import de.hs_kl.wcn2_alarm.alarms.WCN2Alarm;
import de.hs_kl.wcn2_alarm.alarms.WCN2CompoundAlarm;
import de.hs_kl.wcn2_sensors.SensorData;

public class CreateAlarmActivity extends AppCompatActivity
{
    static final String EXTRA_SENDER = "sender";
    static final String EXTRA_NAME = "name";
    static final String EXTRA_TYPES = "types";
    static final String EXTRA_OPERATORS = "operators";
    static final String EXTRA_VALUES = "values";
    static final String EXTRA_MAC_ADDRESSES = "macAddresses";
    static final String EXTRA_IDS = "ids";

    private String name;
    private ArrayList<Integer> thresholdTypes;
    private ArrayList<Integer> thresholdOperators;
    private ArrayList<Float> thresholdValues;
    private ArrayList<SensorData> selectedSensors = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_alarm);

        loadSavedInstanceState(savedInstanceState);

        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.fragment, new SelectNameFragment())
                                   .commit();
    }

    private void loadSavedInstanceState(Bundle savedInstanceState)
    {
        if (null == savedInstanceState) return;

        String name;
        if (null != (name = savedInstanceState.getString(EXTRA_NAME)))
            this.name = name;
        ArrayList<Integer> intList;
        if (null != (intList = savedInstanceState.getIntegerArrayList(EXTRA_TYPES)))
            this.thresholdTypes = intList;
        if (null != (intList = savedInstanceState.getIntegerArrayList(EXTRA_OPERATORS)))
            this.thresholdOperators = intList;
        ArrayList<Float> floatList;
        if (null != (floatList = (ArrayList<Float>)savedInstanceState.getSerializable(EXTRA_VALUES)))
            this.thresholdValues = floatList;
        List<String> macAddresses = (ArrayList<String>)savedInstanceState.get(EXTRA_MAC_ADDRESSES);
        List<Byte> ids = (ArrayList<Byte>)savedInstanceState.get(EXTRA_IDS);
        if (null != ids && null != macAddresses)
        {
            this.selectedSensors = new ArrayList<>();
            for (int i = 0; ids.size() > i; ++i)
            {
                this.selectedSensors.add(new SensorData(ids.get(i), "null", macAddresses.get(i)));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state)
    {
        super.onSaveInstanceState(state);

        state.putString(EXTRA_NAME, this.name);
        state.putIntegerArrayList(EXTRA_TYPES, this.thresholdTypes);
        state.putIntegerArrayList(EXTRA_OPERATORS, this.thresholdOperators);
        state.putSerializable(EXTRA_VALUES, this.thresholdValues);
        ArrayList<String> macAddresses = new ArrayList<>();
        ArrayList<Byte> ids = new ArrayList<>();
        for (SensorData sensorData: this.selectedSensors)
        {
            macAddresses.add(sensorData.getMacAddress());
            ids.add(sensorData.getSensorID());
        }
        state.putStringArrayList(EXTRA_MAC_ADDRESSES, macAddresses);
        state.putSerializable(EXTRA_IDS, ids);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        loadSavedInstanceState(savedInstanceState);
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        loadSavedInstanceState(intent.getExtras());

        Bundle extras = intent.getExtras();
        if (null != extras && null != extras.get(EXTRA_SENDER))
        {
            if (extras.get(EXTRA_SENDER).equals(SelectNameFragment.class.getSimpleName()))
            {
                handleSelectedName(extras);
            }
            else if (extras.get(EXTRA_SENDER).equals(SelectThresholdsFragment.class.getSimpleName()))
            {
                handleSelectedThresholds(extras);
            }
            else if (extras.get(EXTRA_SENDER).equals(SelectSensorsFragment.class.getSimpleName()))
            {
                handleSelectedSensors(extras);
            }
        }
    }

    private void handleSelectedName(Bundle extras)
    {
        this.name = extras.getString(EXTRA_NAME);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new SelectThresholdsFragment())
                .commit();
        findViewById(R.id.fragment).invalidate();
    }

    private void handleSelectedThresholds(Bundle extras)
    {
        this.thresholdTypes = (ArrayList<Integer>)extras.get(EXTRA_TYPES);
        this.thresholdOperators = (ArrayList<Integer>)extras.get(EXTRA_OPERATORS);
        this.thresholdValues = (ArrayList<Float>)extras.get(EXTRA_VALUES);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new SelectSensorsFragment())
                .commit();
    }

    private void handleSelectedSensors(Bundle extras)
    {
        List<String> macAddresses = (List<String>)extras.get(EXTRA_MAC_ADDRESSES);
        List<Byte> ids = (List<Byte>)extras.get(EXTRA_IDS);

        this.selectedSensors = new ArrayList<>();
        for (int i = 0; ids.size() > i; ++i)
        {
            this.selectedSensors.add(new SensorData(ids.get(i), "null", macAddresses.get(i)));
        }

        WCN2Alarm alarm = createAlarm();
        AlarmStorage.getInstance(this).saveAlarm(alarm);
        returnToMainActivity();
    }

    private WCN2Alarm createAlarm()
    {
        List<WCN2Alarm> alarms = new ArrayList<>();
        for (int i = 0; this.thresholdTypes.size() > i; ++i)
        {
            WCN2Alarm.Operator operator = WCN2Alarm.Operator.values()[this.thresholdOperators.get(i)];
            String name = this.name + (this.thresholdTypes.size() > 1 ? i : "");
            WCN2Alarm alarm = WCN2Alarm.createAlarm(name, this.thresholdTypes.get(i), operator,
                    this.thresholdValues.get(i), this.selectedSensors);
            alarms.add(alarm);
        }

        WCN2Alarm newAlarm = alarms.get(0);
        if (1 < alarms.size())
        {
            newAlarm = new WCN2CompoundAlarm(this.name, alarms);
        }

        newAlarm.setActivated(true);

        return newAlarm;
    }

    private void returnToMainActivity()
    {
        Intent intent = new Intent(getBaseContext(), OverviewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
}
