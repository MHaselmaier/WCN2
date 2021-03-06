package de.hs_kl.wcn2.fragments.search_sensor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;
import de.hs_kl.wcn2_sensors.WCN2SensorData;

class MnemonicEditDialog
{
    static Dialog buildMnemonicEditDialog(final Context context, final WCN2SensorData sensorData)
    {
        final TrackedSensorsStorage trackedSensors = TrackedSensorsStorage.getInstance(context);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.mnemonic_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText mnemonicView = dialogView.findViewById(R.id.mnemonic);

        dialogBuilder.setPositiveButton(R.string.ok, (dialog, w) ->
        {
            dialog.dismiss();

            String newMnemonic = mnemonicView.getText().toString().trim();
            if (0 == newMnemonic.length())
            {
                newMnemonic = "null";
            }

            sensorData.setMnemonic(newMnemonic);
            trackedSensors.trackSensor(sensorData);
        });

        dialogBuilder.setNegativeButton(R.string.cancel, (dialog, w) -> dialog.dismiss());

        AlertDialog mnemonicEditDialog = dialogBuilder.create();
        final Window window = mnemonicEditDialog.getWindow();
        mnemonicEditDialog.setOnShowListener((d) ->
        {
            mnemonicView.setText(null);
            String mnemonic = trackedSensors.getMnemonic(sensorData.getMacAddress());
            if (!mnemonic.equals("null"))
            {
                mnemonicView.setText(mnemonic);
            }
            mnemonicView.selectAll();
            mnemonicView.requestFocus();
            if (null != window)
            {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        return mnemonicEditDialog;
    }
}
