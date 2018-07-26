package de.hs_kl.wcn2.fragments.search_sensor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.ble_scanner.SensorData;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;
import de.hs_kl.wcn2.util.LastSeenSinceUtil;

public class ScanResultAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater inflater;
    private List<SensorData> sensorData;

    public ScanResultAdapter(Context context, LayoutInflater inflater)
    {
        this.context = context;
        this.inflater = inflater;
        this.sensorData = TrackedSensorsStorage.getTrackedSensors(this.context);
    }

    @Override
    public int getCount()
    {
        return this.sensorData.size();
    }

    @Override
    public SensorData getItem(int position)
    {
        return this.sensorData.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return this.sensorData.get(position).getMacAddress().hashCode();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent)
    {
        final SensorData sensorData = this.sensorData.get(position);

        view = this.inflater.inflate(R.layout.sensor_list_item, null);

        final ImageButton mnemonicEdit = view.findViewById(R.id.mnemonic_edit);
        mnemonicEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ScanResultAdapter.this.context);

                View dialogView = LayoutInflater.from(ScanResultAdapter.this.context).inflate(R.layout.mnemonic_dialog, null);
                dialogBuilder.setView(dialogView);

                final EditText mnemonic = dialogView.findViewById(R.id.mnemonic);
                if (!sensorData.getMnemonic().equals("null"))
                {
                    mnemonic.append(sensorData.getMnemonic());
                }

                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();

                        String newMnemonic = mnemonic.getText().toString().trim();
                        if (0 == newMnemonic.length())
                        {
                            newMnemonic = "null";
                        }

                        sensorData.setMnemonic(newMnemonic);
                        ScanResultAdapter.this.notifyDataSetInvalidated();

                        TrackedSensorsStorage.trackSensor(ScanResultAdapter.this.context, sensorData);
                    }
                });

                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        TextView sensorMacAddress = view.findViewById(R.id.sensor_mac_address);
        sensorMacAddress.setText(sensorData.getMacAddress());

        Switch trackSwitch = view.findViewById(R.id.sensor_tracked);
        trackSwitch.setChecked(TrackedSensorsStorage.isTracked(this.context, sensorData));
        trackSwitch.setText(trackSwitch.isChecked() ? R.string.sensor_tracked : R.string.sensor_not_tracked);
        trackSwitch.setOnCheckedChangeListener(new SensorTrackedChangeListener(this.context, sensorData));

        TextView mnemonic = view.findViewById(R.id.mnemonic);
        mnemonic.setText(this.context.getResources().getString(R.string.mnemonic, sensorData.getMnemonic()));
        if (sensorData.getMnemonic().equals("null"))
        {
            mnemonic.setVisibility(View.GONE);
        }

        TextView sensorID = view.findViewById(R.id.sensor_id);
        sensorID.setText(this.context.getResources().getString(R.string.sensor_id, sensorData.getSensorID()));

        TextView lastSeenView = view.findViewById(R.id.last_seen);
        lastSeenView.setText(LastSeenSinceUtil.getTimeSinceString(this.context, sensorData.getTimestamp()));

        ImageView batteryLevel = view.findViewById(R.id.battery_level);
        batteryLevel.setImageDrawable(sensorData.getBatteryLevelDrawable(this.context.getResources()));

        ImageView signalStrength = view.findViewById(R.id.signal_strength);
        signalStrength.setImageDrawable(sensorData.getSignalStrengthDrawable(this.context.getResources()));

        return view;
    }

    public void add(SensorData result)
    {
        int position = getPosition(result.getMacAddress());

        if (0 <= position)
        {
            this.sensorData.set(position, result);
        }
        else
        {
            this.sensorData.add(result);
        }
    }

    private int getPosition(String address)
    {
        for (int i = 0; i < this.sensorData.size(); ++i)
        {
            if (this.sensorData.get(i).getMacAddress().equals(address))
            {
                return i;
            }
        }
        return -1;
    }
}
