package de.hs_kl.wcn2.usage;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import de.hs_kl.wcn2.R;

class IntroductionAnimation extends Animation
{
    private Context context;
    private DrawerLayout drawer;

    IntroductionAnimation(Context context)
    {
        this.context = context;

        createViews();
    }

    private void createViews()
    {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        this.drawer = (DrawerLayout)inflater.inflate(R.layout.activity_overview, null);

        Toolbar toolbar = this.drawer.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity)this.context;
        activity.setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, this.drawer, toolbar,
                R.string.drawer_open, R.string.drawer_closed);
        toggle.syncState();

        NavigationView navigationView = this.drawer.findViewById(R.id.navigation);
        navigationView.setClickable(false);

        createSensorTrackingFragment(inflater);
    }

    private void createSensorTrackingFragment(LayoutInflater inflater)
    {
        View sensorTracking = inflater.inflate(R.layout.sensor_tracking,
                this.drawer.findViewById(R.id.fragments));

        View emptyView = sensorTracking.findViewById(R.id.tracked_sensors)
                .findViewById(R.id.empty_list_item);
        emptyView.setVisibility(View.VISIBLE);
        ((TextView)emptyView.findViewById(R.id.label)).setText(R.string.no_sensors_found);

        sensorTracking.findViewById(R.id.measurement_button).setClickable(false);
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
    }

    @Override
    String getDescription()
    {
        return this.context.getString(R.string.introduction_description);
    }
}
