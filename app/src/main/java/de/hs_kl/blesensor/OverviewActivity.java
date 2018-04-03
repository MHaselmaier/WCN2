package de.hs_kl.blesensor;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class OverviewActivity extends AppCompatActivity
{
    private final static int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter btAdapter;
    private BLEScanner bleScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        setTitle(R.string.app_name);

        if (null != savedInstanceState) return;

        setupBluetoothAdapter();
        ensureBluetoothIsEnabled();

        this.bleScanner = new BLEScanner(this.btAdapter.getBluetoothLeScanner());
    }

    private void setupBluetoothAdapter()
    {
        BluetoothManager btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        this.btAdapter = btManager.getAdapter();
    }

    private void ensureBluetoothIsEnabled()
    {
        if (this.btAdapter.isEnabled()) return;

        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetoothIntent, OverviewActivity.REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if (OverviewActivity.REQUEST_ENABLE_BT == requestCode)
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
        if (RESULT_OK == resultCode) return;

        Toast.makeText(this, R.string.bt_was_not_enabled, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onAttachFragment(Fragment fragment)
    {
        if (fragment instanceof SearchSensorFragment)
        {
            handleAttachmentOfSearchSensorFragment((SearchSensorFragment)fragment);
        }
    }

    private void handleAttachmentOfSearchSensorFragment(SearchSensorFragment fragment)
    {
        fragment.setBLEScanner(this.bleScanner);
    }
}
