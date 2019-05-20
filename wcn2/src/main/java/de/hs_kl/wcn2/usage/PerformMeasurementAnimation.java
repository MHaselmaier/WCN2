package de.hs_kl.wcn2.usage;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.hs_kl.wcn2.R;

class PerformMeasurementAnimation extends Animation
{
    private Context context;
    private DrawerLayout drawer;
    private FrameLayout fragments;
    private LinearLayout sensorTrackingFragment;
    private TextView time;
    private Button startMeasurementButton;
    private AlertDialog startMeasurementDialog;
    private View sensorOverview;
    private View actionOverview;
    private Button actionButton;

    PerformMeasurementAnimation(Context context)
    {
        this.context = context;

        createViews();

        addStep(this::clickStartMeasurementButton, 1000);
        addStep(this.startMeasurementDialog::show, 250);
        addStep(this::startMeasurement, 1500);
        addStep(this::firstSecondPassed, 1000);
        addStep(this::secondSecondPassed, 1000);
        addStep(this::chooseAction, 500);
        addStep(this::thirdSecondPassed, 500);
        addStep(this::fourthSecondPassed, 1000);
        addStep(this::reset, 750);
    }

    private void createViews()
    {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        this.drawer = (DrawerLayout) inflater.inflate(R.layout.activity_overview, null);

        Toolbar toolbar = this.drawer.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) this.context;
        activity.setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, this.drawer, toolbar,
                R.string.drawer_open, R.string.drawer_closed);
        toggle.syncState();

        this.fragments = this.drawer.findViewById(R.id.fragments);

        createSensorTrackingFragment(inflater);
        createStartMeasurementDialog(inflater);
    }

    private void createSensorTrackingFragment(LayoutInflater inflater)
    {
        this.sensorTrackingFragment = (LinearLayout)inflater.inflate(R.layout.sensor_tracking,
                                                                     this.fragments, false);

        this.time = this.sensorTrackingFragment.findViewById(R.id.measurement_time);
        this.startMeasurementButton = this.sensorTrackingFragment
                .findViewById(R.id.measurement_button);
        this.startMeasurementButton.setClickable(false);

        createSensorOverview(inflater);
        createActionOverview(inflater);
    }

    private void createSensorOverview(LayoutInflater inflater)
    {
        this.sensorOverview = this.sensorTrackingFragment
                .findViewById(R.id.tracked_sensors_overview);

        Drawable battery = this.context.getDrawable(R.drawable.ic_battery_full);
        Drawable signal = this.context.getDrawable(R.drawable.ic_signal_100);
        String lastSeen = this.context.getString(R.string.sensor_last_seen) + " " +
                this.context.getString(R.string.sensor_seen_just_now);
        LinearLayout trackedSensors = this.sensorTrackingFragment
                .findViewById(R.id.tracked_sensors);
        createFirstTrackedSensor(inflater, battery, signal, lastSeen, trackedSensors);
        createSecondTrackedSensor(inflater, battery, signal, lastSeen, trackedSensors);
    }

    private void createFirstTrackedSensor(LayoutInflater inflater, Drawable battery,
                                          Drawable signal, String lastSeen, LinearLayout layout)
    {
        View trackedSensor1 = inflater.inflate(R.layout.tracked_sensor_view, layout);
        trackedSensor1.findViewById(R.id.sensor_warning).setVisibility(View.GONE);
        ((TextView)trackedSensor1.findViewById(R.id.sensor_id)).setText(
                this.context.getString(R.string.sensor_id, 1));
        ((TextView)trackedSensor1.findViewById(R.id.last_seen)).setText(lastSeen);
        ((TextView)trackedSensor1.findViewById(R.id.temperature)).setText(
                this.context.getString(R.string.temperature, 21.5));
        ((TextView)trackedSensor1.findViewById(R.id.humidity)).setText(
                this.context.getString(R.string.humidity, 36.9));
        ((ImageView)trackedSensor1.findViewById(R.id.battery_level)).setImageDrawable(battery);
        ((ImageView)trackedSensor1.findViewById(R.id.signal_strength)).setImageDrawable(signal);
    }

    private void createSecondTrackedSensor(LayoutInflater inflater, Drawable battery,
                                          Drawable signal, String lastSeen, LinearLayout layout)
    {
        View trackedSensor2 = inflater.inflate(R.layout.tracked_sensor_view, layout, false);
        trackedSensor2.findViewById(R.id.sensor_warning).setVisibility(View.GONE);
        ((TextView)trackedSensor2.findViewById(R.id.sensor_id)).setText(
                this.context.getString(R.string.sensor_id, 3));
        ((TextView)trackedSensor2.findViewById(R.id.last_seen)).setText(lastSeen);
        ((TextView)trackedSensor2.findViewById(R.id.temperature)).setText(
                this.context.getString(R.string.temperature, 21.4));
        ((TextView)trackedSensor2.findViewById(R.id.humidity)).setText(
                this.context.getString(R.string.humidity, 36.8));
        ((ImageView)trackedSensor2.findViewById(R.id.battery_level)).setImageDrawable(battery);
        ((ImageView)trackedSensor2.findViewById(R.id.signal_strength)).setImageDrawable(signal);
        layout.addView(trackedSensor2);
    }

    private void createActionOverview(LayoutInflater inflater)
    {
        this.actionOverview = this.sensorTrackingFragment
                .findViewById(R.id.action_buttons_overview);
        actionOverview.findViewById(R.id.no_actions_defined).setVisibility(View.GONE);
        GridLayout gridLayout = actionOverview.findViewById(R.id.actions);
        View actionButton = inflater.inflate(R.layout.action_button, gridLayout, false);
        this.actionButton = actionButton.findViewById(R.id.button);
        this.actionButton.setText("Push-Ups");
        this.actionButton.setClickable(false);
        gridLayout.addView(actionButton);
        actionButton = inflater.inflate(R.layout.action_button, gridLayout, false);
        Button button = actionButton.findViewById(R.id.button);
        button.setText("Sit-Ups");
        button.setClickable(false);
        gridLayout.addView(actionButton);
        View view = inflater.inflate(R.layout.action_button, gridLayout, false);
        view.setVisibility(View.INVISIBLE);
        gridLayout.addView(view);
    }

    private void createStartMeasurementDialog(LayoutInflater inflater)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.context);
        View dialogView = inflater.inflate(R.layout.measurement_dialog, this.fragments, false);
        dialogView.findViewById(R.id.measurement_filename).setEnabled(false);
        dialogView.findViewById(R.id.measurement_header).setEnabled(false);
        dialogView.findViewById(R.id.average).setEnabled(false);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton(R.string.ok, null);
        dialogBuilder.setNegativeButton(R.string.cancel, null);
        this.startMeasurementDialog = dialogBuilder.create();
        Window window = this.startMeasurementDialog.getWindow();
        if (null != window)
        {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        }
    }

    @Override
    View getRootView()
    {
        return this.drawer;
    }

    @Override
    protected void initialize()
    {
        this.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        this.fragments.removeAllViews();
        this.fragments.addView(this.sensorTrackingFragment);
    }

    private void clickStartMeasurementButton()
    {
        this.startMeasurementButton.performClick();
        this.startMeasurementButton.setPressed(true);
    }

    private void startMeasurement()
    {
        this.startMeasurementDialog.hide();
        this.sensorOverview.setVisibility(View.GONE);
        this.actionOverview.setVisibility(View.VISIBLE);
        this.time.setText(this.context.getString(R.string.time, 0, 0));
    }

    private void firstSecondPassed()
    {
        this.time.setText(this.context.getString(R.string.time, 0, 1));
    }

    private void secondSecondPassed()
    {
        this.time.setText(this.context.getString(R.string.time, 0, 2));
    }

    private void chooseAction()
    {
        this.actionButton.performClick();
        this.actionButton.getBackground().setColorFilter(this.context.getResources()
                .getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
    }

    private void thirdSecondPassed()
    {
        this.time.setText(this.context.getString(R.string.time, 0, 3));
    }

    private void fourthSecondPassed()
    {
        this.time.setText(this.context.getString(R.string.time, 0, 4));
    }

    private void reset()
    {
        this.sensorOverview.setVisibility(View.VISIBLE);
        this.actionOverview.setVisibility(View.GONE);
        this.time.setText("--:--");
        this.actionButton.getBackground().clearColorFilter();
    }

    @Override
    String getDescription()
    {
        return this.context.getString(R.string.perform_measurement_description);
    }
}
