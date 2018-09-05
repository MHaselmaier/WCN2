package de.hs_kl.wcn2.fragments.search_sensor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.ble_scanner.SensorData;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

public class MnemonicEditDialog
{
    public static Dialog buildMnemonicEditDialog(final Context context, final SensorData sensorData)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.mnemonic_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText mnemonicView = dialogView.findViewById(R.id.mnemonic);
        String mnemonic = TrackedSensorsStorage.getMnemonic(sensorData.getMacAddress());
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
                TrackedSensorsStorage.trackSensor(context, sensorData);
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

        AlertDialog mnemonicEditDialog = dialogBuilder.create();
        final Window window = mnemonicEditDialog.getWindow();
        mnemonicEditDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialog)
            {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        return mnemonicEditDialog;
    }
}
