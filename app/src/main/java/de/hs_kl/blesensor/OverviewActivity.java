package de.hs_kl.blesensor;

import android.Manifest;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import de.hs_kl.blesensor.ble_scanner.BLEScanner;
import de.hs_kl.blesensor.fragments.manage_measurements.ManageMeasurementsFragment;
import de.hs_kl.blesensor.fragments.search_sensor.SearchSensorFragment;
import de.hs_kl.blesensor.fragments.sensor_tracking.SensorTrackingFragment;
import de.hs_kl.blesensor.util.Constants;

public class OverviewActivity extends AppCompatActivity
{
    private BluetoothAdapter btAdapter;
    private BroadcastReceiver btAdapterChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if (BluetoothAdapter.STATE_OFF == state)
            {
                BLEScanner.setBluetoothLeScanner(null);
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, Constants.REQUEST_ENABLE_BT);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        setTitle(R.string.app_name);

        requestPermissions();

        BLEScanner.setContext(this);
        registerReceiver(this.btAdapterChangeReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        setupBluetoothAdapter();
        ensureBluetoothIsEnabled();

        final DrawerLayout drawer = findViewById(R.id.drawer);
        View overview = findViewById(R.id.overview);
        overview.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment, new SensorTrackingFragment());
                ft.addToBackStack(null);
                ft.commit();
                drawer.closeDrawers();
            }
        });

        View measurement = findViewById(R.id.measurement);
        measurement.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment, new ManageMeasurementsFragment());
                ft.addToBackStack(null);
                ft.commit();
                drawer.closeDrawers();
            }
        });

        View sensor = findViewById(R.id.sensor);
        sensor.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment, new SearchSensorFragment());
                ft.addToBackStack(null);
                ft.commit();
                drawer.closeDrawers();
            }
        });

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, new SensorTrackingFragment());
        ft.commit();
    }

    private void requestPermissions()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        for (int i = 0; permissions.length > i; ++i)
        {
            if ((Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[i]) && PackageManager.PERMISSION_DENIED == grantResults[i]) ||
                (Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[i]) && PackageManager.PERMISSION_DENIED == grantResults[i]))
            {
                Toast.makeText(this, R.string.denied_permission_location, Toast.LENGTH_LONG).show();
                finish();
            }
            else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[i]) && PackageManager.PERMISSION_DENIED == grantResults[i])
            {
                Toast.makeText(this, R.string.denied_permission_write_external_storage, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupBluetoothAdapter()
    {
        BluetoothManager btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        this.btAdapter = btManager.getAdapter();
    }

    private void ensureBluetoothIsEnabled()
    {
        if (this.btAdapter.isEnabled())
        {
            BLEScanner.setBluetoothLeScanner(this.btAdapter.getBluetoothLeScanner());
        }
        else
        {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, Constants.REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if (Constants.REQUEST_ENABLE_BT == requestCode)
        {
            handleBluetoothEnableRequest(resultCode);
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    private void handleBluetoothEnableRequest(int resultCode)
    {
        if (RESULT_OK == resultCode)
        {
            BLEScanner.setBluetoothLeScanner(this.btAdapter.getBluetoothLeScanner());
        }
        else
        {
            Toast.makeText(this, R.string.bt_was_not_enabled, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        unregisterReceiver(this.btAdapterChangeReceiver);
    }
}
