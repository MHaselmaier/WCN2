package de.hs_kl.wcn2.fragments.search_sensor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.ble_scanner.SensorData;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

public class MnemonicEditOnClickListener implements View.OnClickListener
{
    private Context context;
    private SensorData sensorData;

    public MnemonicEditOnClickListener(Context context, SensorData sensorData)
    {
        this.context = context;
        this.sensorData = sensorData;
    }

    @Override
    public void onClick(View v)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.context);

        View dialogView = LayoutInflater.from(this.context).inflate(R.layout.mnemonic_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText mnemonicView = dialogView.findViewById(R.id.mnemonic);
        String mnemonic = TrackedSensorsStorage.getMnemonic(this.context, this.sensorData.getMacAddress());
        if (!mnemonic.equals("null"))
        {
            mnemonicView.append(mnemonic);
        }

        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();

                String newMnemonic = mnemonicView.getText().toString().trim();
                if (0 == newMnemonic.length())
                {
                    newMnemonic = "null";
                }


                sensorData.setMnemonic(newMnemonic);
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
