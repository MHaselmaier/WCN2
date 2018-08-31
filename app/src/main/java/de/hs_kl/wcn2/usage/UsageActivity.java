package de.hs_kl.wcn2.usage;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2.R;

public class UsageActivity extends AppCompatActivity
{
    private List<Animation> animations = new ArrayList<>();
    private int state;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.usage_activity);
        setTitle("   " + getResources().getString(R.string.app_name));
        getSupportActionBar().setIcon(R.drawable.ic_home);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        this.animations.add(makeCreateActionAnimation());
        this.animations.add(makeSelectTrackedSensorsAnimation());
        this.animations.add(makePerformMeasurementAnimation());
    }

    private Animation makeCreateActionAnimation()
    {
        final DrawerLayout drawer = findViewById(R.id.drawer);
        final ConstraintLayout container = findViewById(R.id.fragments);

        final View sensorTracking = LayoutInflater.from(this).inflate(R.layout.sensor_tracking, container, false);
        sensorTracking.findViewById(R.id.measurement_button).setClickable(false);
        Animation createAction = new Animation(new Runnable()
        {
            @Override
            public void run()
            {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                container.removeAllViews();
                container.addView(sensorTracking);
            }
        });

        final View action = findViewById(R.id.action);
        createAction.addStep(new Animation.Step(createAction, new Runnable()
        {
            @Override
            public void run()
            {
                action.setBackgroundColor(Color.argb(255, 255, 255, 255));
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            }
        }, 1000));

        createAction.addStep(new Animation.Step(createAction, new Runnable()
        {
            @Override
            public void run()
            {
                action.setBackgroundColor(Color.argb(50, 0, 0, 0));
            }
        }, 500));

        final View actions = LayoutInflater.from(this).inflate(R.layout.actions, container, false);
        createAction.addStep(new Animation.Step(createAction, new Runnable()
        {
            @Override
            public void run()
            {
                container.removeAllViews();
                container.addView(actions);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        }, 250));

        final View newAction = actions.findViewById(R.id.add);
        createAction.addStep(new Animation.Step(createAction, new Runnable()
        {
            @Override
            public void run()
            {
                newAction.setBackgroundColor(Color.argb(50, 0, 0, 0));
            }
        }, 500));

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.action_dialog, container, false);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton(R.string.ok, null);
        dialogBuilder.setNegativeButton(R.string.cancel, null);
        final AlertDialog dialog = dialogBuilder.create();
        createAction.addStep(new Animation.Step(createAction, new Runnable()
        {
            @Override
            public void run()
            {
                newAction.setBackgroundColor(Color.argb(255, 255, 255, 255));
                dialog.show();
            }
        }, 250));

        createAction.addStep(new Animation.Step(createAction, new Runnable()
        {
            @Override
            public void run()
            {
                dialog.hide();
                container.removeAllViews();
                container.addView(sensorTracking);
            }
        }, 1000));

        return createAction;
    }

    private Animation makeSelectTrackedSensorsAnimation()
    {
        final DrawerLayout drawer = findViewById(R.id.drawer);
        final ConstraintLayout container = findViewById(R.id.fragments);

        final View sensorTracking = LayoutInflater.from(this).inflate(R.layout.sensor_tracking, container, false);
        sensorTracking.findViewById(R.id.measurement_button).setClickable(false);
        Animation selectSensors = new Animation(new Runnable()
        {
            @Override
            public void run()
            {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                container.removeAllViews();
                container.addView(sensorTracking);
            }
        });

        final View sensor = findViewById(R.id.sensor);
        selectSensors.addStep(new Animation.Step(selectSensors, new Runnable()
        {
            @Override
            public void run()
            {
                sensor.setBackgroundColor(Color.argb(255, 255, 255, 255));
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            }
        }, 1000));

        selectSensors.addStep(new Animation.Step(selectSensors, new Runnable()
        {
            @Override
            public void run()
            {
                sensor.setBackgroundColor(Color.argb(50, 0, 0, 0));
            }
        }, 500));

        final View searchSensor = LayoutInflater.from(this).inflate(R.layout.search_sensor, container, false);
        LinearLayout layout = searchSensor.findViewById(R.id.layout);
        layout.removeView(layout.findViewById(R.id.empty_list_item));

        View sensorListItem = LayoutInflater.from(this).inflate(R.layout.sensor_list_item, container, false);
        ((TextView)sensorListItem.findViewById(R.id.sensor_id)).setText("WCN1");
        ((TextView)sensorListItem.findViewById(R.id.sensor_mac_address)).setText("1A:6E:64:F0:68:7A");
        final Switch firstSwitch = sensorListItem.findViewById(R.id.sensor_tracked);
        firstSwitch.setClickable(false);
        layout.addView(sensorListItem, 1);

        sensorListItem = LayoutInflater.from(this).inflate(R.layout.sensor_list_item, container, false);
        ((TextView)sensorListItem.findViewById(R.id.sensor_id)).setText("WCN2");
        ((TextView)sensorListItem.findViewById(R.id.sensor_mac_address)).setText("D9:0A:58:E0:99:E5");
        sensorListItem.findViewById(R.id.sensor_tracked).setClickable(false);
        layout.addView(sensorListItem, 2);

        sensorListItem = LayoutInflater.from(this).inflate(R.layout.sensor_list_item, container, false);
        ((TextView)sensorListItem.findViewById(R.id.sensor_id)).setText("WCN3");
        ((TextView)sensorListItem.findViewById(R.id.sensor_mac_address)).setText("C7:45:1B:C8:8F:90");
        final Switch secondSwitch = sensorListItem.findViewById(R.id.sensor_tracked);
        secondSwitch.setClickable(false);
        layout.addView(sensorListItem, 3);

        selectSensors.addStep(new Animation.Step(selectSensors, new Runnable()
        {
            @Override
            public void run()
            {
                container.removeAllViews();
                container.addView(searchSensor);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        }, 250));

        selectSensors.addStep(new Animation.Step(selectSensors, new Runnable()
        {
            @Override
            public void run()
            {
                firstSwitch.setChecked(true);
                firstSwitch.setText(R.string.sensor_tracked);
            }
        }, 500));

        selectSensors.addStep(new Animation.Step(selectSensors, new Runnable()
        {
            @Override
            public void run()
            {
                secondSwitch.setChecked(true);
                secondSwitch.setText(R.string.sensor_tracked);
            }
        }, 500));

        selectSensors.addStep(new Animation.Step(selectSensors, new Runnable()
        {
            @Override
            public void run()
            {
                firstSwitch.setChecked(false);
                firstSwitch.setText(R.string.sensor_not_tracked);
                secondSwitch.setChecked(false);
                secondSwitch.setText(R.string.sensor_not_tracked);
                container.removeAllViews();
                container.addView(sensorTracking);
            }
        }, 1000));

        return selectSensors;
    }

    private Animation makePerformMeasurementAnimation()
    {
        final DrawerLayout drawer = findViewById(R.id.drawer);
        final ConstraintLayout container = findViewById(R.id.fragments);

        final View sensorTracking = LayoutInflater.from(this).inflate(R.layout.sensor_tracking, container, false);
        sensorTracking.findViewById(R.id.measurement_button).setClickable(false);
        Animation performMeasurement = new Animation(new Runnable()
        {
            @Override
            public void run()
            {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                container.removeAllViews();
                container.addView(sensorTracking);
            }
        });

        final Button startMeasurement = sensorTracking.findViewById(R.id.measurement_button);
        performMeasurement.addStep(new Animation.Step(performMeasurement, new Runnable()
        {
            @Override
            public void run()
            {
                startMeasurement.performClick();
                startMeasurement.setPressed(true);
            }
        }, 500));

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.measurement_dialog, container, false);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton(R.string.ok, null);
        dialogBuilder.setNegativeButton(R.string.cancel, null);
        final AlertDialog dialog = dialogBuilder.create();
        performMeasurement.addStep(new Animation.Step(performMeasurement, new Runnable()
        {
            @Override
            public void run()
            {
                dialog.show();
            }
        }, 250));

        final View sensorOverview = sensorTracking.findViewById(R.id.sensor_overview);
        final View actionOverview = sensorTracking.findViewById(R.id.action_overview);
        GridLayout gridLayout = actionOverview.findViewById(R.id.actions);
        View actionButton = LayoutInflater.from(this).inflate(R.layout.action_button, gridLayout, false);
        final Button button = actionButton.findViewById(R.id.button);
        button.setText("Sit-Ups");
        button.setClickable(false);
        gridLayout.addView(actionButton);
        View view = LayoutInflater.from(this).inflate(R.layout.action_button, gridLayout, false);
        view.setVisibility(View.INVISIBLE);
        gridLayout.addView(view);
        final TextView time = sensorTracking.findViewById(R.id.measurement_time);
        performMeasurement.addStep(new Animation.Step(performMeasurement, new Runnable()
        {
            @Override
            public void run()
            {
                dialog.hide();
                sensorOverview.setVisibility(View.GONE);
                actionOverview.setVisibility(View.VISIBLE);
                time.setText("00:00");
            }
        }, 500));

        performMeasurement.addStep(new Animation.Step(performMeasurement, new Runnable()
        {
            @Override
            public void run()
            {
                button.performClick();
                button.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
            }
        }, 500));

        performMeasurement.addStep(new Animation.Step(performMeasurement, new Runnable()
        {
            @Override
            public void run()
            {
                time.setText("00:01");
            }
        }, 500));

        performMeasurement.addStep(new Animation.Step(performMeasurement, new Runnable()
        {
            @Override
            public void run()
            {
                time.setText("00:02");
            }
        }, 1000));

        performMeasurement.addStep(new Animation.Step(performMeasurement, new Runnable()
        {
            @Override
            public void run()
            {
                sensorOverview.setVisibility(View.VISIBLE);
                actionOverview.setVisibility(View.GONE);
                time.setText("--:--");
                button.getBackground().clearColorFilter();
            }
        }, 500));

        return performMeasurement;
    }

    protected void onResume()
    {
        super.onResume();
        this.animations.get(2).start();
    }

    protected void onPause()
    {
        super.onPause();
        this.animations.get(2).stop();
    }
}
