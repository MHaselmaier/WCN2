package de.hs_kl.wcn2.usage;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import de.hs_kl.wcn2.R;

class SelectTrackedSensorsAnimation extends Animation
{
    private Context context;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private FrameLayout fragments;
    private LinearLayout sensorTrackingFragment;
    private LinearLayout searchSensorFragment;
    private Switch firstSensorSwitch;
    private ImageButton firstSensorMnemonicEdit;
    private Switch secondSensorSwitch;
    private ImageButton secondSensorMnemonicEdit;

    SelectTrackedSensorsAnimation(Context context)
    {
        this.context = context;

        createViews();

        addStep(this::openDrawer, 1000);
        addStep(this::chooseSearchSensorsFragment, 1000);
        addStep(this::showSearchSensorsFragment, 750);
        addStep(this::selectFirstSensor, 1000);
        addStep(this::selectSecondSensor, 750);
        addStep(this::backToSensorTrackingFragment, 2000);
    }

    private void createViews()
    {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        this.drawer = (DrawerLayout) inflater.inflate(R.layout.activity_overview, null);
        this.navigationView = this.drawer.findViewById(R.id.navigation);
        this.navigationView.setClickable(false);

        Toolbar toolbar = this.drawer.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) this.context;
        activity.setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, this.drawer, toolbar,
                R.string.drawer_open, R.string.drawer_closed);
        toggle.syncState();

        this.fragments = this.drawer.findViewById(R.id.fragments);

        createSensorTrackingFragment(inflater);
        createSearchSensorFragment(inflater);
    }

    private void createSensorTrackingFragment(LayoutInflater inflater)
    {
        this.sensorTrackingFragment = (LinearLayout)inflater.inflate(R.layout.sensor_tracking,
                this.fragments, false);

        View emptyView = this.sensorTrackingFragment.findViewById(R.id.tracked_sensors)
                .findViewById(R.id.empty_list_item);
        emptyView.setVisibility(View.VISIBLE);
        ((TextView)emptyView.findViewById(R.id.label)).setText(R.string.no_sensors_found);

        this.sensorTrackingFragment.findViewById(R.id.measurement_button).setClickable(false);
    }

    private void createSearchSensorFragment(LayoutInflater inflater)
    {
        this.searchSensorFragment = (LinearLayout)inflater.inflate(R.layout.search_sensor,
                                                                   this.fragments, false);
        LinearLayout layout = this.searchSensorFragment.findViewById(R.id.layout);
        layout.removeView(layout.findViewById(R.id.empty_list_item));

        Drawable battery = this.context.getDrawable(R.drawable.ic_battery_full);
        Drawable signal = this.context.getDrawable(R.drawable.ic_signal_100);
        String lastSeen = this.context.getString(R.string.sensor_last_seen) + " " +
                this.context.getString(R.string.sensor_seen_just_now);

        createFirstSensor(inflater, battery, signal, lastSeen, layout);
        createSecondSensor(inflater, battery, signal, lastSeen, layout);
        createThirdSensor(inflater, battery, signal, lastSeen, layout);
    }

    private void createFirstSensor(LayoutInflater inflater, Drawable battery,
                                   Drawable signal, String lastSeen, LinearLayout layout)
    {
        View sensorListItem = inflater.inflate(R.layout.sensor_list_item, this.fragments, false);
        ((TextView)sensorListItem.findViewById(R.id.sensor_id)).setText(
                this.context.getString(R.string.sensor_id, 1));
        ((ImageView)sensorListItem.findViewById(R.id.battery_level)).setImageDrawable(battery);
        ((ImageView)sensorListItem.findViewById(R.id.signal_strength)).setImageDrawable(signal);
        this.firstSensorSwitch = sensorListItem.findViewById(R.id.sensor_tracked);
        this.firstSensorSwitch.setClickable(false);
        this.firstSensorMnemonicEdit = sensorListItem.findViewById(R.id.mnemonic_edit);
        this.firstSensorMnemonicEdit.setVisibility(View.INVISIBLE);
        ((TextView)sensorListItem.findViewById(R.id.last_seen)).setText(lastSeen);
        layout.addView(sensorListItem, 1);
    }

    private void createSecondSensor(LayoutInflater inflater, Drawable battery,
                                    Drawable signal, String lastSeen, LinearLayout layout)
    {
        View sensorListItem = inflater.inflate(R.layout.sensor_list_item, this.fragments, false);
        ((TextView)sensorListItem.findViewById(R.id.sensor_id)).setText(
                this.context.getString(R.string.sensor_id, 2));
        ((ImageView)sensorListItem.findViewById(R.id.battery_level)).setImageDrawable(battery);
        ((ImageView)sensorListItem.findViewById(R.id.signal_strength)).setImageDrawable(signal);
        sensorListItem.findViewById(R.id.sensor_tracked).setClickable(false);
        sensorListItem.findViewById(R.id.mnemonic_edit).setVisibility(View.INVISIBLE);
        ((TextView)sensorListItem.findViewById(R.id.last_seen)).setText(lastSeen);
        layout.addView(sensorListItem, 2);
    }

    private void createThirdSensor(LayoutInflater inflater, Drawable battery,
                                   Drawable signal, String lastSeen, LinearLayout layout)
    {
        View sensorListItem = inflater.inflate(R.layout.sensor_list_item, this.fragments, false);
        ((TextView)sensorListItem.findViewById(R.id.sensor_id)).setText(
                this.context.getString(R.string.sensor_id, 3));
        ((ImageView)sensorListItem.findViewById(R.id.battery_level)).setImageDrawable(battery);
        ((ImageView)sensorListItem.findViewById(R.id.signal_strength)).setImageDrawable(signal);
        this.secondSensorSwitch = sensorListItem.findViewById(R.id.sensor_tracked);
        this.secondSensorSwitch.setClickable(false);
        this.secondSensorMnemonicEdit= sensorListItem.findViewById(R.id.mnemonic_edit);
        this.secondSensorMnemonicEdit.setVisibility(View.INVISIBLE);
        ((TextView)sensorListItem.findViewById(R.id.last_seen)).setText(lastSeen);
        layout.addView(sensorListItem, 3);
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

    private void openDrawer()
    {
        this.navigationView.setCheckedItem(R.id.overview);
        this.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
    }

    private void chooseSearchSensorsFragment()
    {
        this.navigationView.setCheckedItem(R.id.sensor);
    }

    private void showSearchSensorsFragment()
    {
        this.fragments.removeAllViews();
        this.fragments.addView(this.searchSensorFragment);
        this.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void selectFirstSensor()
    {
        this.firstSensorSwitch.setChecked(true);
        this.firstSensorSwitch.setText(R.string.sensor_tracked);
        this.firstSensorMnemonicEdit.setVisibility(View.VISIBLE);
    }

    private void selectSecondSensor()
    {
        this.secondSensorSwitch.setChecked(true);
        this.secondSensorSwitch.setText(R.string.sensor_tracked);
        this.secondSensorMnemonicEdit.setVisibility(View.VISIBLE);
    }

    private void backToSensorTrackingFragment()
    {
        this.firstSensorSwitch.setChecked(false);
        this.firstSensorSwitch.setText(R.string.sensor_not_tracked);
        this.firstSensorMnemonicEdit.setVisibility(View.INVISIBLE);
        this.secondSensorSwitch.setChecked(false);
        this.secondSensorSwitch.setText(R.string.sensor_not_tracked);
        this.secondSensorMnemonicEdit.setVisibility(View.INVISIBLE);
        this.fragments.removeAllViews();
        this.fragments.addView(this.sensorTrackingFragment);
    }

    @Override
    String getDescription()
    {
        return this.context.getString(R.string.select_tracked_sensors_description);
    }
}
