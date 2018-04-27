package de.hs_kl.blesensor;

import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class OverviewActivity extends AppCompatActivity
{
    private final static int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter btAdapter;
    private BLEScanner bleScanner;

    private List<BLEScannerChangedListener> bleScannerChangedListeners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        setTitle(R.string.app_name);

        setupBluetoothAdapter();
        ensureBluetoothIsEnabled();


        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, new SensorTrackingFragment());
        ft.commit();
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
            createBLEScannerAndNotifyBLEScannerChangedListeners();
        }
        else
        {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, OverviewActivity.REQUEST_ENABLE_BT);
        }
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
        if (RESULT_OK == resultCode)
        {
            createBLEScannerAndNotifyBLEScannerChangedListeners();
        }
        else
        {
            Toast.makeText(this, R.string.bt_was_not_enabled, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void createBLEScannerAndNotifyBLEScannerChangedListeners()
    {
        this.bleScanner = new BLEScanner(this.btAdapter.getBluetoothLeScanner());
        notifyBLEScannerChangedListeners();
    }

    private void notifyBLEScannerChangedListeners()
    {
        for (BLEScannerChangedListener listener: this.bleScannerChangedListeners)
        {
            listener.onBLEScannerChanged(this.bleScanner);
        }
    }

    public void registerBLEScannerChangedListener(BLEScannerChangedListener listener)
    {
        listener.onBLEScannerChanged(this.bleScanner);
        this.bleScannerChangedListeners.add(listener);
    }

    public void unregisterBLEScannerChangedListener(BLEScannerChangedListener listener)
    {
        this.bleScannerChangedListeners.remove(listener);
    }
}
