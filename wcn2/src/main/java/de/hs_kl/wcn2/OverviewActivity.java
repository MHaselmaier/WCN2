package de.hs_kl.wcn2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;


import de.hs_kl.wcn2.fragments.about.AboutFragment;
import de.hs_kl.wcn2.fragments.actions.ActionsFragment;
import de.hs_kl.wcn2.fragments.manage_measurements.ManageMeasurementsFragment;
import de.hs_kl.wcn2.fragments.search_sensor.SearchSensorFragment;
import de.hs_kl.wcn2.fragments.sensor_tracking.SensorTrackingFragment;
import de.hs_kl.wcn2.util.Constants;
import de.hs_kl.wcn2_sensors.WCN2Activity;

public class OverviewActivity extends WCN2Activity implements
        NavigationView.OnNavigationItemSelectedListener, FragmentManager.OnBackStackChangedListener
{
    public static boolean isVisible;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_overview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.drawer = findViewById(R.id.drawer);

        this.navigationView = findViewById(R.id.navigation);
        this.navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, this.drawer, toolbar,
                R.string.drawer_open, R.string.drawer_closed);
        this.drawer.addDrawerListener(toggle);
        toggle.syncState();

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (null == savedInstanceState)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragments, new SensorTrackingFragment())
                    .commit();
            this.navigationView.setCheckedItem(R.id.overview);
        }
    }

    @Override
    public void onBackStackChanged()
    {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragments);
        int itemID = R.id.overview;
        if (currentFragment instanceof ManageMeasurementsFragment)
        {
            itemID = R.id.measurement;
        }
        else if (currentFragment instanceof ActionsFragment)
        {
            itemID = R.id.action;
        }
        else if (currentFragment instanceof  SearchSensorFragment)
        {
            itemID = R.id.sensor;
        }
        else if (currentFragment instanceof  AboutFragment)
        {
            itemID = R.id.about;
        }
        this.navigationView.setCheckedItem(itemID);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        FragmentManager manager = getSupportFragmentManager();

        switch (item.getItemId())
        {
            case R.id.overview:
                if (!manager.popBackStackImmediate(SensorTrackingFragment.class.getName(), 0))
                {
                    manager.beginTransaction()
                            .replace(R.id.fragments, new SensorTrackingFragment())
                            .addToBackStack(SensorTrackingFragment.class.getName())
                            .commit();
                }
                break;
            case R.id.measurement:
                if (!manager.popBackStackImmediate(ManageMeasurementsFragment.class.getName(), 0))
                {
                    manager.beginTransaction()
                            .replace(R.id.fragments, new ManageMeasurementsFragment())
                            .addToBackStack(ManageMeasurementsFragment.class.getName())
                            .commit();
                }
                break;
            case R.id.action:
                if (!manager.popBackStackImmediate(ActionsFragment.class.getName(), 0))
                {
                    manager.beginTransaction()
                            .replace(R.id.fragments, new ActionsFragment())
                            .addToBackStack(ActionsFragment.class.getName())
                            .commit();
                }
                break;
            case R.id.sensor:
                if (!manager.popBackStackImmediate(SearchSensorFragment.class.getName(), 0))
                {
                    manager.beginTransaction()
                            .replace(R.id.fragments, new SearchSensorFragment())
                            .addToBackStack(SearchSensorFragment.class.getName())
                            .commit();
                }
                break;
            case R.id.about:
                if (!manager.popBackStackImmediate(AboutFragment.class.getName(), 0))
                {
                    manager.beginTransaction()
                            .replace(R.id.fragments, new AboutFragment())
                            .addToBackStack(AboutFragment.class.getName())
                            .commit();
                }
                break;
        }

        this.drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void changeToSearchSensorFragment()
    {
        FragmentManager manager = getSupportFragmentManager();
        if (!manager.popBackStackImmediate(SearchSensorFragment.class.getName(), 0))
        {
            manager.beginTransaction()
                    .replace(R.id.fragments, new SearchSensorFragment())
                    .addToBackStack(SearchSensorFragment.class.getName())
                    .commit();
        }
        this.navigationView.setCheckedItem(R.id.sensor);
    }

    @Override
    public void onBackPressed()
    {
        if (this.drawer.isDrawerOpen(GravityCompat.START))
        {
            this.drawer.closeDrawer(GravityCompat.START);
            return;
        }

        super.onBackPressed();
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
                if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[i]) &&
                        PackageManager.PERMISSION_DENIED == grantResults[i])
                {
                    Toast.makeText(this, R.string.denied_permission_write_external_storage,
                            Toast.LENGTH_LONG).show();
                }
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
