package de.hs_kl.wcn2.usage;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2.R;

public class UsageActivity extends AppCompatActivity
{
    private FrameLayout content;
    private TextView description;
    private LinearLayout stepIndicator;
    private List<Animation> animations = new ArrayList<>();
    private int state;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.usage_activity);
        setTitle("   " + getResources().getString(R.string.app_name));
        ActionBar bar = getSupportActionBar();
        if (null != bar)
        {
            getSupportActionBar().setIcon(R.drawable.ic_home);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        this.content = findViewById(R.id.content);
        this.description = findViewById(R.id.description);
        this.stepIndicator = findViewById(R.id.step_indicator);

        Button next = findViewById(R.id.next);
        next.setOnClickListener((v) ->
        {
            UsageActivity.this.animations.get(UsageActivity.this.state).stop();

            if (UsageActivity.this.animations.size() - 1 > UsageActivity.this.state)
            {
                UsageActivity.this.animations.get(++UsageActivity.this.state).start();
                updateDescription();
                updateStepIndicator();
            }
            else
            {
                finish();
            }
        });

        Button skip = findViewById(R.id.skip);
        skip.setOnClickListener((v) -> finish());

        this.animations.add(makeIntroductionAnimation());
        this.animations.add(makeCreateActionAnimation());
        this.animations.add(makeSelectTrackedSensorsAnimation());
        this.animations.add(makePerformMeasurementAnimation());
    }

    private void updateDescription()
    {
        this.description.setText(this.animations.get(this.state).getDescriptionResourceID());
    }

    private void updateStepIndicator()
    {
        this.stepIndicator.removeAllViews();
        float dpsScale = getResources().getDisplayMetrics().density;
        Drawable circle = getResources().getDrawable(R.drawable.ic_circle);
        for (int i = 0; this.animations.size() > i; ++i)
        {
            ImageView indicator = new ImageView(this);
            indicator.setBackground(circle);
            indicator.setColorFilter(Color.WHITE);
            int size = (int)(this.state == i ? 8 * dpsScale : 5 * dpsScale);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            int margin = (int)(2 * dpsScale);
            params.setMargins(margin, margin, margin, margin);
            indicator.setLayoutParams(params);
            this.stepIndicator.addView(indicator);
        }
    }

    private Animation makeIntroductionAnimation()
    {
        DrawerLayout drawer = (DrawerLayout)getLayoutInflater()
                .inflate(R.layout.activity_overview, this.content, false);
        ConstraintLayout container = drawer.findViewById(R.id.fragments);

        View sensorTracking = getLayoutInflater()
                .inflate(R.layout.sensor_tracking, container, false);
        LinearLayout trackedSensorViews = sensorTracking.findViewById(R.id.tracked_sensors);
        View emptyView = trackedSensorViews.findViewById(R.id.empty_list_item);
        emptyView.setVisibility(View.VISIBLE);
        ((TextView)emptyView.findViewById(R.id.label)).setText(R.string.no_sensors_found);
        sensorTracking.findViewById(R.id.measurement_button).setClickable(false);
        Animation introduction = new Animation(() ->
        {
            this.content.removeAllViews();
            this.content.addView(drawer);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            container.removeAllViews();
            container.addView(sensorTracking);
        }, R.string.introduction_description);

        introduction.addStep(new Animation.Step(introduction, () -> {}, 5000));

        return introduction;
    }

    private Animation makeCreateActionAnimation()
    {
        DrawerLayout drawer = (DrawerLayout)getLayoutInflater()
                .inflate(R.layout.activity_overview, this.content, false);
        ConstraintLayout container = drawer.findViewById(R.id.fragments);

        View sensorTracking = getLayoutInflater()
                .inflate(R.layout.sensor_tracking, container, false);
        sensorTracking.findViewById(R.id.measurement_button).setClickable(false);
        LinearLayout trackedSensorViews = sensorTracking.findViewById(R.id.tracked_sensors);
        View emptyView = trackedSensorViews.findViewById(R.id.empty_list_item);
        emptyView.setVisibility(View.VISIBLE);
        ((TextView)emptyView.findViewById(R.id.label)).setText(R.string.no_sensors_found);
        Animation createAction = new Animation(() ->
        {
            this.content.removeAllViews();
            this.content.addView(drawer);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            container.removeAllViews();
            container.addView(sensorTracking);
        }, R.string.create_action_description);

        View action = drawer.findViewById(R.id.action);
        createAction.addStep(new Animation.Step(createAction, () ->
        {
            action.setBackgroundColor(Color.argb(255, 255, 255, 255));
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        }, 1000));

        createAction.addStep(new Animation.Step(createAction, () ->
            action.setBackgroundColor(Color.argb(50, 0, 0, 0)),
        1000));

        View actions = getLayoutInflater().inflate(R.layout.actions, container, false);
        emptyView = getLayoutInflater().inflate(R.layout.empty_list_item, actions.findViewById(
                R.id.actions));
        ((TextView)emptyView.findViewById(R.id.label)).setText(R.string.no_actions_defined);
        createAction.addStep(new Animation.Step(createAction, () ->
        {
            container.removeAllViews();
            container.addView(actions);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }, 750));

        View newAction = actions.findViewById(R.id.add);
        createAction.addStep(new Animation.Step(createAction, () ->
                newAction.setBackgroundColor(Color.argb(50, 0, 0, 0)), 1000));

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.action_dialog, container, false);
        dialogView.findViewById(R.id.action).setEnabled(false);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton(R.string.ok, null);
        dialogBuilder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = dialogBuilder.create();
        Window window = dialog.getWindow();
        if (null != window)
        {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        }
        createAction.addStep(new Animation.Step(createAction, () ->
        {
            newAction.setBackgroundColor(Color.argb(255, 255, 255, 255));
            dialog.show();
        }, 750));

        createAction.addStep(new Animation.Step(createAction, () ->
        {
            dialog.hide();
            container.removeAllViews();
            container.addView(sensorTracking);
        }, 2000));

        return createAction;
    }

    private Animation makeSelectTrackedSensorsAnimation()
    {
        DrawerLayout drawer = (DrawerLayout)getLayoutInflater()
                .inflate(R.layout.activity_overview, this.content, false);
        ConstraintLayout container = drawer.findViewById(R.id.fragments);

        View sensorTracking = getLayoutInflater()
                .inflate(R.layout.sensor_tracking, container, false);
        sensorTracking.findViewById(R.id.measurement_button).setClickable(false);
        LinearLayout trackedSensorViews = sensorTracking.findViewById(R.id.tracked_sensors);
        View emptyView = trackedSensorViews.findViewById(R.id.empty_list_item);
        emptyView.setVisibility(View.VISIBLE);
        ((TextView)emptyView.findViewById(R.id.label)).setText(R.string.no_sensors_found);
        Animation selectSensors = new Animation(() ->
        {
            this.content.removeAllViews();
            this.content.addView(drawer);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            container.removeAllViews();
            container.addView(sensorTracking);
        }, R.string.select_tracked_sensors_description);

        View sensor = drawer.findViewById(R.id.sensor);
        selectSensors.addStep(new Animation.Step(selectSensors, () ->
        {
            sensor.setBackgroundColor(Color.argb(255, 255, 255, 255));
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        }, 1000));

        selectSensors.addStep(new Animation.Step(selectSensors, () ->
                sensor.setBackgroundColor(Color.argb(50, 0, 0, 0)), 1000));

        View searchSensor = getLayoutInflater()
                .inflate(R.layout.search_sensor, container, false);
        LinearLayout layout = searchSensor.findViewById(R.id.layout);
        layout.removeView(layout.findViewById(R.id.empty_list_item));

        Drawable battery = getDrawable(R.drawable.ic_battery_full);
        Drawable signal = getDrawable(R.drawable.ic_signal_100);

        View sensorListItem = getLayoutInflater()
                .inflate(R.layout.sensor_list_item, container, false);
        ((TextView)sensorListItem.findViewById(R.id.sensor_id)).setText("WCN1");
        ((ImageView)sensorListItem.findViewById(R.id.battery_level)).setImageDrawable(battery);
        ((ImageView)sensorListItem.findViewById(R.id.signal_strength)).setImageDrawable(signal);
        Switch firstSwitch = sensorListItem.findViewById(R.id.sensor_tracked);
        firstSwitch.setClickable(false);
        ImageView firstMnemonicEdit = sensorListItem.findViewById(R.id.mnemonic_edit);
        firstMnemonicEdit.setVisibility(View.INVISIBLE);
        ((TextView)sensorListItem.findViewById(R.id.last_seen)).setText(getString(R.string.sensor_last_seen) + " " + getResources().getString(R.string.sensor_seen_just_now));
        layout.addView(sensorListItem, 1);

        sensorListItem = getLayoutInflater().inflate(R.layout.sensor_list_item, container, false);
        ((TextView)sensorListItem.findViewById(R.id.sensor_id)).setText("WCN2");
        ((ImageView)sensorListItem.findViewById(R.id.battery_level)).setImageDrawable(battery);
        ((ImageView)sensorListItem.findViewById(R.id.signal_strength)).setImageDrawable(signal);
        sensorListItem.findViewById(R.id.sensor_tracked).setClickable(false);
        sensorListItem.findViewById(R.id.mnemonic_edit).setVisibility(View.INVISIBLE);
        ((TextView)sensorListItem.findViewById(R.id.last_seen)).setText(getString(R.string.sensor_last_seen) + " " + getResources().getString(R.string.sensor_seen_just_now));
        layout.addView(sensorListItem, 2);

        sensorListItem = getLayoutInflater().inflate(R.layout.sensor_list_item, container, false);
        ((TextView)sensorListItem.findViewById(R.id.sensor_id)).setText("WCN3");
        ((ImageView)sensorListItem.findViewById(R.id.battery_level)).setImageDrawable(battery);
        ((ImageView)sensorListItem.findViewById(R.id.signal_strength)).setImageDrawable(signal);
        Switch secondSwitch = sensorListItem.findViewById(R.id.sensor_tracked);
        secondSwitch.setClickable(false);
        ImageView secondMnemonicEdit = sensorListItem.findViewById(R.id.mnemonic_edit);
        secondMnemonicEdit.setVisibility(View.INVISIBLE);
        ((TextView)sensorListItem.findViewById(R.id.last_seen)).setText(getString(R.string.sensor_last_seen) + " " + getResources().getString(R.string.sensor_seen_just_now));
        layout.addView(sensorListItem, 3);

        selectSensors.addStep(new Animation.Step(selectSensors, () ->
        {
            container.removeAllViews();
            container.addView(searchSensor);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }, 750));

        selectSensors.addStep(new Animation.Step(selectSensors, () ->
        {
            firstSwitch.setChecked(true);
            firstSwitch.setText(R.string.sensor_tracked);
            firstMnemonicEdit.setVisibility(View.VISIBLE);
        }, 1000));

        selectSensors.addStep(new Animation.Step(selectSensors, () ->
        {
            secondSwitch.setChecked(true);
            secondSwitch.setText(R.string.sensor_tracked);
            secondMnemonicEdit.setVisibility(View.VISIBLE);
        }, 750));

        selectSensors.addStep(new Animation.Step(selectSensors, () ->
        {
            firstSwitch.setChecked(false);
            firstSwitch.setText(R.string.sensor_not_tracked);
            firstMnemonicEdit.setVisibility(View.INVISIBLE);
            secondSwitch.setChecked(false);
            secondSwitch.setText(R.string.sensor_not_tracked);
            secondMnemonicEdit.setVisibility(View.INVISIBLE);
            container.removeAllViews();
            container.addView(sensorTracking);
        }, 2000));

        return selectSensors;
    }

    private Animation makePerformMeasurementAnimation()
    {
        DrawerLayout drawer = (DrawerLayout)getLayoutInflater()
                .inflate(R.layout.activity_overview, this.content, false);
        ConstraintLayout container = drawer.findViewById(R.id.fragments);

        View sensorTracking = getLayoutInflater()
                .inflate(R.layout.sensor_tracking, container, false);
        LinearLayout trackedSensors = sensorTracking.findViewById(R.id.tracked_sensors);
        View trackedSensor1 = getLayoutInflater().inflate(R.layout.tracked_sensor_overview, trackedSensors);
        trackedSensor1.findViewById(R.id.sensor_warning).setVisibility(View.GONE);
        ((TextView)trackedSensor1.findViewById(R.id.sensor_id)).setText("WCN1");
        ((TextView)trackedSensor1.findViewById(R.id.last_seen)).setText(getString(R.string.sensor_last_seen) + " " + getResources().getString(R.string.sensor_seen_just_now));
        ((TextView)trackedSensor1.findViewById(R.id.temperature)).setText("21,5 °C");
        ((TextView)trackedSensor1.findViewById(R.id.humidity)).setText("36,9 %");
        ((ImageView)trackedSensor1.findViewById(R.id.battery_level)).setImageDrawable(getDrawable(R.drawable.ic_battery_full));
        ((ImageView)trackedSensor1.findViewById(R.id.signal_strength)).setImageDrawable(getDrawable(R.drawable.ic_signal_100));

        View trackedSensor2 = getLayoutInflater().inflate(R.layout.tracked_sensor_overview, trackedSensors, false);
        trackedSensor2.findViewById(R.id.sensor_warning).setVisibility(View.GONE);
        ((TextView)trackedSensor2.findViewById(R.id.sensor_id)).setText("WCN3");
        ((TextView)trackedSensor2.findViewById(R.id.last_seen)).setText(getString(R.string.sensor_last_seen) + " " + getResources().getString(R.string.sensor_seen_just_now));
        ((TextView)trackedSensor2.findViewById(R.id.temperature)).setText("21,4 °C");
        ((TextView)trackedSensor2.findViewById(R.id.humidity)).setText("36,8 %");
        ((ImageView)trackedSensor2.findViewById(R.id.battery_level)).setImageDrawable(getDrawable(R.drawable.ic_battery_full));
        ((ImageView)trackedSensor2.findViewById(R.id.signal_strength)).setImageDrawable(getDrawable(R.drawable.ic_signal_100));
        trackedSensors.addView(trackedSensor2);

        sensorTracking.findViewById(R.id.measurement_button).setClickable(false);
        Animation performMeasurement = new Animation(() ->
        {
            this.content.removeAllViews();
            this.content.addView(drawer);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            container.removeAllViews();
            container.addView(sensorTracking);
        }, R.string.perform_measurement_description);

        Button startMeasurement = sensorTracking.findViewById(R.id.measurement_button);
        performMeasurement.addStep(new Animation.Step(performMeasurement, () ->
        {
            startMeasurement.performClick();
            startMeasurement.setPressed(true);
        }, 1000));

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.measurement_dialog, container, false);
        dialogView.findViewById(R.id.measurement_filename).setEnabled(false);
        dialogView.findViewById(R.id.measurement_header).setEnabled(false);
        dialogView.findViewById(R.id.average).setEnabled(false);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton(R.string.ok, null);
        dialogBuilder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = dialogBuilder.create();
        Window window = dialog.getWindow();
        if (null != window)
        {
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        }
        performMeasurement.addStep(new Animation.Step(performMeasurement, dialog::show, 250));

        View sensorOverview = sensorTracking.findViewById(R.id.sensor_overview);
        View actionOverview = sensorTracking.findViewById(R.id.action_overview);
        GridLayout gridLayout = actionOverview.findViewById(R.id.actions);
        View actionButton = getLayoutInflater().inflate(R.layout.action_button, gridLayout, false);
        Button button = actionButton.findViewById(R.id.button);
        button.setText("Push-Ups");
        button.setClickable(false);
        gridLayout.addView(actionButton);
        actionButton = getLayoutInflater().inflate(R.layout.action_button, gridLayout, false);
        Button button2 = actionButton.findViewById(R.id.button);
        button2.setText("Sit-Ups");
        button2.setClickable(false);
        gridLayout.addView(actionButton);
        View view = getLayoutInflater().inflate(R.layout.action_button, gridLayout, false);
        view.setVisibility(View.INVISIBLE);
        gridLayout.addView(view);
        TextView time = sensorTracking.findViewById(R.id.measurement_time);
        performMeasurement.addStep(new Animation.Step(performMeasurement, () ->
        {
            dialog.hide();
            sensorOverview.setVisibility(View.GONE);
            actionOverview.setVisibility(View.VISIBLE);
            time.setText("00:00");
        }, 1500));

        performMeasurement.addStep(new Animation.Step(performMeasurement, () -> time.setText("00:01")
                , 1000));

        performMeasurement.addStep(new Animation.Step(performMeasurement, () -> time.setText("00:02")
                , 1000));

        performMeasurement.addStep(new Animation.Step(performMeasurement, () ->
        {
            button.performClick();
            button.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }, 500));

        performMeasurement.addStep(new Animation.Step(performMeasurement, () -> time.setText("00:03")
                , 500));

        performMeasurement.addStep(new Animation.Step(performMeasurement, () -> time.setText("00:04")
                , 1000));

        performMeasurement.addStep(new Animation.Step(performMeasurement, () ->
        {
            sensorOverview.setVisibility(View.VISIBLE);
            actionOverview.setVisibility(View.GONE);
            time.setText("--:--");
            button.getBackground().clearColorFilter();
        }, 750));

        return performMeasurement;
    }

    protected void onResume()
    {
        super.onResume();
        this.animations.get(this.state).start();
        updateDescription();
        updateStepIndicator();
    }

    protected void onPause()
    {
        super.onPause();
        this.animations.get(this.state).stop();
    }
}