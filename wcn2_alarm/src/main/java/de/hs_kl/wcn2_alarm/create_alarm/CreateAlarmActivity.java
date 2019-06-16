package de.hs_kl.wcn2_alarm.create_alarm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2_alarm.AlarmStorage;
import de.hs_kl.wcn2_alarm.OverviewActivity;
import de.hs_kl.wcn2_alarm.R;
import de.hs_kl.wcn2_alarm.alarms.Operator;
import de.hs_kl.wcn2_alarm.alarms.Threshold;
import de.hs_kl.wcn2_alarm.alarms.Type;
import de.hs_kl.wcn2_alarm.alarms.WCN2Alarm;
import de.hs_kl.wcn2_sensors.WCN2SensorData;

public class CreateAlarmActivity extends AppCompatActivity
{
    public static final String EXTRA_MODE = "mode";
    public static final String MODE_CREATE = "create";
    public static final String MODE_EDIT = "edit";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_SOUND = "sound";
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
    private Uri sound;
    private List<Threshold> thresholds = new ArrayList<>();
    private List<WCN2SensorData> selectedSensors = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_alarm);

        loadSavedInstanceState(getIntent().getExtras());
        loadSavedInstanceState(savedInstanceState);

        TextView title = findViewById(R.id.title);
        if (this.mode.equals(MODE_EDIT))
        {
            title.setText(R.string.edit_alarm);
            loadCurrentAlarmValues();
        }
        else if(this.mode.equals(MODE_CREATE))
        {
            title.setText(R.string.create_alarm);
        }

        startSelectNameFragment();
    }

    private void loadSavedInstanceState(Bundle savedInstanceState)
    {
        if (null == savedInstanceState) return;

        this.mode = savedInstanceState.getString(EXTRA_MODE, this.mode);
        this.originalName = savedInstanceState.getString(EXTRA_ORIGINAL_NAME, this.originalName);

        this.name = savedInstanceState.getString(EXTRA_NAME, this.name);
        Uri savedSound = savedInstanceState.getParcelable(EXTRA_SOUND);
        if (null != savedSound)
            this.sound = savedSound;
        List<Integer> types = savedInstanceState.getIntegerArrayList(EXTRA_TYPES);
        List<Float> values = (ArrayList<Float>)savedInstanceState.getSerializable(EXTRA_VALUES);
        List<Integer> operators = savedInstanceState.getIntegerArrayList(EXTRA_OPERATORS);
        if (null != types && null != values && null != operators)
        {
            this.thresholds = new ArrayList<>();
            for (int i = 0; types.size() > i; ++i)
            {
                this.thresholds.add(new Threshold(Type.values()[types.get(i)], values.get(i),
                                                  Operator.values()[operators.get(i)]));
            }
        }
        List<String> macAddresses = (ArrayList<String>)savedInstanceState.get(EXTRA_MAC_ADDRESSES);
        List<Byte> ids = (ArrayList<Byte>)savedInstanceState.get(EXTRA_IDS);
        if (null != ids && null != macAddresses)
        {
            this.selectedSensors = new ArrayList<>();
            for (int i = 0; ids.size() > i; ++i)
            {
                this.selectedSensors.add(new WCN2SensorData(ids.get(i), "null", macAddresses.get(i)));
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
        this.sound = currentAlarm.getSound();
        this.selectedSensors = new ArrayList<>(currentAlarm.getSensorData());
        this.thresholds = new ArrayList<>(currentAlarm.getThresholds());
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

    private void startSelectSoundFragment()
    {
        SelectSoundFragment fragment = new SelectSoundFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_MODE, this.mode);
        arguments.putParcelable(EXTRA_SOUND, this.sound);
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
        ArrayList<Integer> types = new ArrayList<>();
        ArrayList<Float> values = new ArrayList<>();
        ArrayList<Integer> operators = new ArrayList<>();
        for (Threshold threshold: this.thresholds)
        {
            types.add(threshold.getType().ordinal());
            values.add(threshold.getValue());
            operators.add(threshold.getOperator().ordinal());
        }
        arguments.putIntegerArrayList(EXTRA_TYPES, types);
        arguments.putSerializable(EXTRA_VALUES, values);
        arguments.putIntegerArrayList(EXTRA_OPERATORS, operators);
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
        for (WCN2SensorData sensorData: this.selectedSensors)
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
        state.putParcelable(EXTRA_SOUND, this.sound);
        ArrayList<Integer> types = new ArrayList<>();
        ArrayList<Float> values = new ArrayList<>();
        ArrayList<Integer> operators = new ArrayList<>();
        for (Threshold threshold: this.thresholds)
        {
            types.add(threshold.getType().ordinal());
            values.add(threshold.getValue());
            operators.add(threshold.getOperator().ordinal());
        }
        state.putIntegerArrayList(EXTRA_TYPES, types);
        state.putSerializable(EXTRA_VALUES, values);
        state.putIntegerArrayList(EXTRA_OPERATORS, operators);
        ArrayList<String> macAddresses = new ArrayList<>();
        ArrayList<Byte> ids = new ArrayList<>();
        for (WCN2SensorData sensorData: this.selectedSensors)
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
            else if (sender.equals(SelectSoundFragment.class.getSimpleName()))
            {
                handleSelectedSound(extras);
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

        startSelectSoundFragment();
    }

    private void handleSelectedSound(Bundle extras)
    {
        this.sound = extras.getParcelable(EXTRA_SOUND);

        startSelectThresholdsFragment();
    }

    private void handleSelectedThresholds(Bundle extras)
    {
        List<Integer> types = (ArrayList<Integer>)extras.get(EXTRA_TYPES);
        List<Float> values = (ArrayList<Float>)extras.get(EXTRA_VALUES);
        List<Integer> operators = (ArrayList<Integer>)extras.get(EXTRA_OPERATORS);

        this.thresholds = new ArrayList<>();
        if (null != types && null != values && null != operators)
        {
            for (int i = 0; types.size() > i; ++i)
            {
                this.thresholds.add(new Threshold(Type.values()[types.get(i)], values.get(i),
                                                  Operator.values()[operators.get(i)]));
            }
        }

        startSelectSensorsFragment();
    }

    private void handleSelectedSensors(Bundle extras)
    {
        List<String> macAddresses = (List<String>)extras.get(EXTRA_MAC_ADDRESSES);
        List<Byte> ids = (List<Byte>)extras.get(EXTRA_IDS);

        this.selectedSensors = new ArrayList<>();
        for (int i = 0; ids.size() > i; ++i)
        {
            this.selectedSensors.add(new WCN2SensorData(ids.get(i), "null", macAddresses.get(i)));
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
        WCN2Alarm alarm = new WCN2Alarm(this.name, this.sound, this.thresholds, this.selectedSensors);
        alarm.setActivated(true);
        return alarm;
    }

    private void returnToMainActivity()
    {
        Intent intent = new Intent(getBaseContext(), OverviewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
}
