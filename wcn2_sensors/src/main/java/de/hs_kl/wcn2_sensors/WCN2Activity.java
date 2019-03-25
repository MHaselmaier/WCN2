package de.hs_kl.wcn2_sensors;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicBoolean;

public class WCN2Activity extends AppCompatActivity
{
    private BluetoothAdapter btAdapter;
    private BroadcastReceiver btAdapterChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if (BluetoothAdapter.STATE_OFF == state)
            {
                WCN2Scanner.setBluetoothLeScanner(null);
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, Constants.REQUEST_ENABLE_BT);
            }
        }
    };

    private BroadcastReceiver locationModeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            ensureLocationIsEnabled();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestPermissions();

        registerReceiver(this.btAdapterChangeReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        setupBluetoothAdapter();
        ensureBluetoothIsEnabled();

        registerReceiver(this.locationModeChangeReceiver,
                new IntentFilter((LocationManager.MODE_CHANGED_ACTION)));
        ensureLocationIsEnabled();
    }

    private void requestPermissions()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                Constants.REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        if (Constants.REQUEST_LOCATION_PERMISSION == requestCode)
        {
            for (int i = 0; permissions.length > i; ++i)
            {
                if ((Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[i]) && PackageManager.PERMISSION_DENIED == grantResults[i]) ||
                    (Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[i]) && PackageManager.PERMISSION_DENIED == grantResults[i]))
                {
                    Toast.makeText(this, R.string.denied_permission_location, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
        else
        {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setupBluetoothAdapter()
    {
        BluetoothManager btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        if (null != btManager)
        {
            this.btAdapter = btManager.getAdapter();
        }
    }

    private void ensureBluetoothIsEnabled()
    {
        if (this.btAdapter.isEnabled())
        {
            WCN2Scanner.setBluetoothLeScanner(this.btAdapter.getBluetoothLeScanner());
        }
        else
        {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, Constants.REQUEST_ENABLE_BT);
        }
    }

    private void ensureLocationIsEnabled()
    {
        try
        {
            if (Settings.Secure.LOCATION_MODE_OFF != Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE))
            {
                WCN2Scanner.setBluetoothLeScanner(this.btAdapter.getBluetoothLeScanner());
                return;
            }
        }
        catch (Exception e) {}

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.enable_location);

        final AtomicBoolean accepted = new AtomicBoolean(false);
        dialogBuilder.setPositiveButton(R.string.allow, (d, w) ->
        {
            Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(enableLocationIntent, Constants.REQUEST_ENABLE_LOCATION);
            accepted.set(true);
        });

        dialogBuilder.setNegativeButton(R.string.deny, (dialog, w) -> dialog.dismiss());

        dialogBuilder.setOnDismissListener((d) ->
        {
            if (accepted.get()) return;

            Toast.makeText(this, R.string.denied_permission_location, Toast.LENGTH_LONG).show();
            finish();
        });

        dialogBuilder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if (Constants.REQUEST_ENABLE_BT == requestCode)
        {
            handleBluetoothEnableRequest(resultCode);
        }
        else if (Constants.REQUEST_ENABLE_LOCATION == requestCode)
        {
            handleLocationEnableRequest();
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
            WCN2Scanner.setBluetoothLeScanner(this.btAdapter.getBluetoothLeScanner());
        }
        else
        {
            Toast.makeText(this, R.string.bt_was_not_enabled, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void handleLocationEnableRequest()
    {
        try {
            if (0 == Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE)) {
                Toast.makeText(this, R.string.denied_permission_location, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            WCN2Scanner.setBluetoothLeScanner(this.btAdapter.getBluetoothLeScanner());
        }
        catch(Exception e) {}
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        unregisterReceiver(this.btAdapterChangeReceiver);
        unregisterReceiver(this.locationModeChangeReceiver);
    }
}
