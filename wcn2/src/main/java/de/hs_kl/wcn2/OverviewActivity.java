package de.hs_kl.wcn2;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Stack;

import de.hs_kl.wcn2.fragments.about.AboutFragment;
import de.hs_kl.wcn2.fragments.actions.ActionsFragment;
import de.hs_kl.wcn2.fragments.manage_measurements.ManageMeasurementsFragment;
import de.hs_kl.wcn2.fragments.search_sensor.SearchSensorFragment;
import de.hs_kl.wcn2.fragments.sensor_tracking.SensorTrackingFragment;
import de.hs_kl.wcn2.util.Constants;
import de.hs_kl.wcn2_sensors.WCN2Activity;

public class OverviewActivity extends WCN2Activity
{
    public static boolean isVisible;

    private Fragment currentView;
    private Fragment[] views;
    private Stack<Fragment> viewBackStack = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null == savedInstanceState)
        {
            setContentView(R.layout.activity_overview);
            setTitle(R.string.app_name);
            ActionBar actionBar = getSupportActionBar();
            if (null != actionBar) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_home);
            }

            setupDrawer();
            setupWCNViews();
        }
    }

    private void setupDrawer()
    {
        final DrawerLayout drawer = findViewById(R.id.drawer);

        drawer.addDrawerListener(new DrawerLayout.DrawerListener()
        {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}

            @Override
            public void onDrawerOpened(@NonNull View drawerView)
            {
                ActionBar actionBar = getSupportActionBar();
                if (null != actionBar)
                {
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView)
            {
                ActionBar actionBar = getSupportActionBar();
                if (null != actionBar)
                {
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_home);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {}
        });

        View overview = findViewById(R.id.overview);
        overview.setOnClickListener((v) ->
        {
            changeViewTo(Constants.WCNView.SENSOR_TRACKING);
            drawer.closeDrawers();
        });

        View measurement = findViewById(R.id.measurement);
        measurement.setOnClickListener((v) ->
        {
            changeViewTo(Constants.WCNView.MANAGE_MEASUREMENT);
            drawer.closeDrawers();
        });

        View action = findViewById(R.id.action);
        action.setOnClickListener((v) ->
        {
            changeViewTo(Constants.WCNView.ACTIONS);
            drawer.closeDrawers();
        });

        View sensor = findViewById(R.id.sensor);
        sensor.setOnClickListener((v) ->
        {
            changeViewTo(Constants.WCNView.SEARCH_SENSOR);
            drawer.closeDrawers();
        });

        View about = findViewById(R.id.about);
        about.setOnClickListener((v) ->
        {
            changeViewTo(Constants.WCNView.ABOUT);
            drawer.closeDrawers();
        });
    }

    public void changeViewTo(Constants.WCNView view)
    {
        this.viewBackStack.remove(this.views[view.ordinal()]);
        this.viewBackStack.push(this.currentView);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(this.currentView);
        this.currentView = this.views[view.ordinal()];
        ft.show(this.currentView);
        ft.commit();
    }

    @Override
    public void onBackPressed()
    {
        if (0 < this.viewBackStack.size())
        {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.hide(this.currentView);
            this.currentView = this.viewBackStack.pop();
            ft.show(this.currentView);
            ft.commit();
        }
        else
        {
            finish();
        }
    }

    private void setupWCNViews()
    {
        this.views = new Fragment[Constants.WCNView.values().length];
        this.views[Constants.WCNView.SENSOR_TRACKING.ordinal()] = new SensorTrackingFragment();
        this.views[Constants.WCNView.SEARCH_SENSOR.ordinal()] = new SearchSensorFragment();
        this.views[Constants.WCNView.MANAGE_MEASUREMENT.ordinal()] = new ManageMeasurementsFragment();
        this.views[Constants.WCNView.ACTIONS.ordinal()] = new ActionsFragment();
        this.views[Constants.WCNView.ABOUT.ordinal()] = new AboutFragment();

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        for (Fragment view: this.views)
        {
            ft.add(R.id.fragments, view);
            ft.hide(view);
        }
        this.currentView = this.views[Constants.WCNView.SENSOR_TRACKING.ordinal()];
        ft.show(currentView);
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
        case android.R.id.home:
            DrawerLayout drawer = findViewById(R.id.drawer);
            if (drawer.isDrawerOpen(Gravity.START))
            {
                drawer.closeDrawers();
                ActionBar actionBar = getSupportActionBar();
                if (null != actionBar)
                {
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_home);
                }
            }
            else
            {
                drawer.openDrawer(Gravity.START);
                ActionBar actionBar = getSupportActionBar();
                if (null != actionBar)
                {
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
                }
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        OverviewActivity.isVisible = true;
    }

    @Override
    public void onPause()
    {
        super.onPause();

        OverviewActivity.isVisible = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        if (Constants.REQUEST_WRITE_PERMISSION == requestCode)
        {
            for (int i = 0; permissions.length > i; ++i)
            {
                if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[i]) && PackageManager.PERMISSION_DENIED == grantResults[i])
                {
                    Toast.makeText(this, R.string.denied_permission_write_external_storage, Toast.LENGTH_LONG).show();
                }
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
