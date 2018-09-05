package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import de.hs_kl.wcn2.R;

public class StartMeasurementDialog
{
    public static Dialog buildStartMeasurementDialog(final SensorTrackingFragment fragment)
    {
        final Activity activity = fragment.getActivity();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

        View dialogView = activity.getLayoutInflater().inflate(R.layout.measurement_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText measurementHeader = dialogView.findViewById(R.id.measurement_header);
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                String header = measurementHeader.getText().toString();
                if (0 == header.trim().length())
                {
                    header = activity.getResources().getString(R.string.measurement_comment);
                }
                fragment.startTracking(header);
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

        AlertDialog startMeasurementDialog = dialogBuilder.create();
        final Window window = startMeasurementDialog.getWindow();
        startMeasurementDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialog)
            {
                measurementHeader.setText(null);
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        return startMeasurementDialog;
    }
}