package de.hs_kl.wcn2.usage;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.hs_kl.wcn2.R;

class CreateActionAnimation extends Animation
{
    private Context context;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private FrameLayout fragments;
    private LinearLayout sensorTrackingFragment;
    private LinearLayout actionsFragment;
    private ImageButton newActionButton;
    private AlertDialog createActionDialog;

    CreateActionAnimation(Context context)
    {
        this.context = context;

        createViews();

        addStep(this::openDrawer, 1000);
        addStep(this::chooseActionFragment, 1000);
        addStep(this::showActionFragment, 750);
        addStep(this::clickNewActionButton, 1000);
        addStep(this::showCreateActionDialog, 750);
        addStep(this::reset, 2000);
    }

    private void createViews()
    {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        this.drawer = (DrawerLayout)inflater.inflate(R.layout.activity_overview, null);
        this.navigationView = this.drawer.findViewById(R.id.navigation);
        this.navigationView.setClickable(false);

        Toolbar toolbar = this.drawer.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity)this.context;
        activity.setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, this.drawer, toolbar,
                R.string.drawer_open, R.string.drawer_closed);
        toggle.syncState();

        this.fragments = this.drawer.findViewById(R.id.fragments);

        createSensorTrackingFragment(inflater);
        createActionsFragment(inflater);
        createCreateActionDialog(inflater);
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

    private void createActionsFragment(LayoutInflater inflater)
    {
        this.actionsFragment = (LinearLayout)inflater.inflate(R.layout.actions,
                this.fragments, false);
        View emptyView = inflater.inflate(R.layout.empty_list_item,
                this.actionsFragment.findViewById(R.id.actions));
        ((TextView)emptyView.findViewById(R.id.label)).setText(R.string.no_actions_defined);

        this.newActionButton = this.actionsFragment.findViewById(R.id.add);
    }

    private void createCreateActionDialog(LayoutInflater inflater)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.context);
        View dialogView = inflater.inflate(R.layout.action_dialog, this.drawer, false);
        dialogView.findViewById(R.id.action).setEnabled(false);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton(R.string.ok, null);
        dialogBuilder.setNegativeButton(R.string.cancel, null);
        this.createActionDialog = dialogBuilder.create();
        Window window = this.createActionDialog.getWindow();
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

    private void openDrawer()
    {
        this.navigationView.setCheckedItem(R.id.overview);
        this.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
    }

    private void chooseActionFragment()
    {
        this.navigationView.setCheckedItem(R.id.action);
    }

    private void showActionFragment()
    {
        this.fragments.removeAllViews();
        this.fragments.addView(this.actionsFragment);
        this.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void clickNewActionButton()
    {
        this.newActionButton.setBackgroundColor(Color.argb(50, 0, 0, 0));
    }

    private void showCreateActionDialog()
    {
        this.newActionButton.setBackgroundColor(Color.argb(255, 255, 255, 255));
        this.createActionDialog.show();
    }

    private void reset()
    {
        this.createActionDialog.hide();
        this.fragments.removeAllViews();
        this.fragments.addView(this.sensorTrackingFragment);
    }

    @Override
    String getDescription()
    {
        return this.context.getString(R.string.create_action_description);
    }
}
