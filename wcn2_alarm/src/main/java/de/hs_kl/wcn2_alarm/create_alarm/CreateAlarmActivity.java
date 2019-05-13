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
import de.hs_kl.wcn2_alarm.alarms.WCN2HumidityAlarm;
import de.hs_kl.wcn2_alarm.alarms.WCN2PresenceAlarm;
import de.hs_kl.wcn2_alarm.alarms.WCN2TemperatureAlarm;
import de.hs_kl.wcn2_sensors.SensorData;

public class CreateAlarmActivity extends AppCompatActivity
{
    public static final String EXTRA_MODE = "mode";
    public static final String MODE_CREATE = "create";
    public static final String MODE_EDIT = "edit";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_TYPES = "types";
    public static final String EXTRA_OPERATORS = "operators";
    public static final String EXTRA_VALUES = "values";
    public static final String EXTRA_MAC_ADDRESSES = "macAddresses";
    public static final String EXTRA_IDS = "ids";
    static final String EXTRA_SENDER = "sender";

    static final String EXTRA_ORIGINAL_NAME = "originalName";
    private String originalName;

    private String mode = MODE_CREATE;
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

        loadSavedInstanceState(getIntent().getExtras());
        loadSavedInstanceState(savedInstanceState);

        if (this.mode.equals(MODE_EDIT))
        {
            loadCurrentAlarmValues();
        }

        startSelectNameFragment();
    }

    private void loadSavedInstanceState(Bundle savedInstanceState)
    {
        if (null == savedInstanceState) return;

        this.mode = savedInstanceState.getString(EXTRA_MODE, this.mode);
        this.originalName = savedInstanceState.getString(EXTRA_ORIGINAL_NAME, this.originalName);

        this.name = savedInstanceState.getString(EXTRA_NAME, this.name);
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

    private void loadCurrentAlarmValues()
    {
        WCN2Alarm currentAlarm = null;
        for (WCN2Alarm alarm: AlarmStorage.getInstance(this).getAlarms())
        {
            if (alarm.getName().equals(this.name))
            {
                currentAlarm = alarm;
                break;
            }
        }
        if (null == currentAlarm) return;

        this.originalName = this.name;
        this.selectedSensors = new ArrayList<>(currentAlarm.getSensorData());

        List<WCN2Alarm> allAlarms = new ArrayList<>();
        if (currentAlarm instanceof WCN2CompoundAlarm)
        {
            WCN2CompoundAlarm compoundAlarm = (WCN2CompoundAlarm)currentAlarm;
            allAlarms.addAll(compoundAlarm.getAlarms());
        }
        else
        {
            allAlarms.add(currentAlarm);
        }

        this.thresholdTypes = new ArrayList<>();
        this.thresholdValues = new ArrayList<>();
        this.thresholdOperators = new ArrayList<>();
        for (WCN2Alarm alarm: allAlarms)
        {
            int type = -1;
            if (alarm instanceof WCN2TemperatureAlarm)
                type = 0;
            if (alarm instanceof WCN2HumidityAlarm)
                type = 1;
            if (alarm instanceof WCN2PresenceAlarm)
                type = 2;

            this.thresholdTypes.add(type);
            this.thresholdValues.add(alarm.getThreshold());
            this.thresholdOperators.add(alarm.getOperator().ordinal());
        }
    }

    private void startSelectNameFragment()
    {
        SelectNameFragment fragment = new SelectNameFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_MODE, this.mode);
        arguments.putString(EXTRA_NAME, this.name);
        arguments.putString(EXTRA_ORIGINAL_NAME, this.originalName);
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, fragment)
                .commit();
    }

    private void startSelectThresholdsFragment()
    {
        SelectThresholdsFragment fragment = new SelectThresholdsFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_MODE, this.mode);
        arguments.putIntegerArrayList(EXTRA_TYPES, this.thresholdTypes);
        arguments.putIntegerArrayList(EXTRA_OPERATORS, this.thresholdOperators);
        arguments.putSerializable(EXTRA_VALUES, this.thresholdValues);
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, fragment)
                .commit();
    }

    private void startSelectSensorsFragment()
    {
        SelectSensorsFragment fragment = new SelectSensorsFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_MODE, this.mode);
        ArrayList<String> macAddresses = new ArrayList<>();
        ArrayList<Byte> ids = new ArrayList<>();
        for (SensorData sensorData: this.selectedSensors)
        {
            macAddresses.add(sensorData.getMacAddress());
            ids.add(sensorData.getSensorID());
        }
        arguments.putStringArrayList(EXTRA_MAC_ADDRESSES, macAddresses);
        arguments.putSerializable(EXTRA_IDS, ids);
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, fragment)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle state)
    {
        super.onSaveInstanceState(state);

        state.putString(EXTRA_ORIGINAL_NAME, this.originalName);
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
        String sender;
        if (null != extras && null != (sender = extras.getString(EXTRA_SENDER)))
        {
            if (sender.equals(SelectNameFragment.class.getSimpleName()))
            {
                handleSelectedName(extras);
            }
            else if (sender.equals(SelectThresholdsFragment.class.getSimpleName()))
            {
                handleSelectedThresholds(extras);
            }
            else if (sender.equals(SelectSensorsFragment.class.getSimpleName()))
            {
                handleSelectedSensors(extras);
            }
        }
    }

    private void handleSelectedName(Bundle extras)
    {
        this.name = extras.getString(EXTRA_NAME);

        startSelectThresholdsFragment();
    }

    private void handleSelectedThresholds(Bundle extras)
    {
        this.thresholdTypes = (ArrayList<Integer>)extras.get(EXTRA_TYPES);
        this.thresholdOperators = (ArrayList<Integer>)extras.get(EXTRA_OPERATORS);
        this.thresholdValues = (ArrayList<Float>)extras.get(EXTRA_VALUES);

        startSelectSensorsFragment();
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
        AlarmStorage alarmStorage = AlarmStorage.getInstance(this);
        if (null != this.originalName && !this.originalName.equals(this.name))
            alarmStorage.deleteAlarm(this.originalName);
        alarmStorage.saveAlarm(alarm);
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
