package de.hs_kl.wcn2.fragments.search_sensor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.ble_scanner.SensorData;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

public class MnemonicEditOnClickListener implements View.OnClickListener
{
    private Context context;
    private ScanResultAdapter adapter;

    public MnemonicEditOnClickListener(Context context, ScanResultAdapter adapter)
    {
        this.context = context;
        this.adapter = adapter;
    }

    @Override
    public void onClick(View v)
    {
        ConstraintLayout parent = (ConstraintLayout)v.getParent();
        TextView sensorMACAddressView = ((View)parent.getParent()).findViewById(R.id.sensor_mac_address);
        String macAddress = sensorMACAddressView.getText().toString();

        final SensorData sensorData = MnemonicEditOnClickListener.this.adapter.getItem(macAddress);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.context);

        View dialogView = LayoutInflater.from(this.context).inflate(R.layout.mnemonic_dialog, null);
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
                MnemonicEditOnClickListener.this.adapter.notifyDataSetChanged();

                TrackedSensorsStorage.trackSensor(MnemonicEditOnClickListener.this.context, sensorData);
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
}
