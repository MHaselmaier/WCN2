package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hs_kl.wcn2.R;

public class StartMeasurementDialog
{
    private static final DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.US);

    public static Dialog buildStartMeasurementDialog(final SensorTrackingFragment fragment)
    {
        final Activity activity = fragment.getActivity();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

        View dialogView = activity.getLayoutInflater().inflate(R.layout.measurement_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText measurementFilename = dialogView.findViewById(R.id.measurement_filename);
        final EditText measurementHeader = dialogView.findViewById(R.id.measurement_header);
        dialogBuilder.setPositiveButton(R.string.ok, (dialog, w) ->
        {
            dialog.dismiss();
            String header = measurementHeader.getText().toString().trim();
            if (0 == header.length())
            {
                header = activity.getResources().getString(R.string.measurement_comment);
            }
            String filename = measurementFilename.getText().toString().trim();
            if (0 == filename.length())
            {
                filename = StartMeasurementDialog.dateFormat.format(new Date());
            }
            fragment.startTracking(filename, header);
        });

        dialogBuilder.setNegativeButton(R.string.cancel, (dialog, w) -> dialog.dismiss());

        AlertDialog startMeasurementDialog = dialogBuilder.create();
        final Window window = startMeasurementDialog.getWindow();
        startMeasurementDialog.setOnShowListener((d) ->
        {
            measurementFilename.setText(null);
            measurementFilename.setHint(StartMeasurementDialog.dateFormat.format(new Date()));
            measurementFilename.requestFocus();
            measurementHeader.setText(null);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        });

        return startMeasurementDialog;
    }
}