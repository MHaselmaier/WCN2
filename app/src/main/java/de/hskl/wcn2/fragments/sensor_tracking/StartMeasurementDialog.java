package de.hskl.wcn2.fragments.sensor_tracking;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hskl.wcn2.R;

class StartMeasurementDialog
{
    private static final DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.US);

    static Dialog buildStartMeasurementDialog(final SensorTrackingFragment fragment)
    {
        Activity activity = fragment.getActivity();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

        View dialogView = activity.getLayoutInflater().inflate(R.layout.measurement_dialog, null);
        dialogBuilder.setView(dialogView);

        CheckBox checkBox = dialogView.findViewById(R.id.average);
        View selectAverageRateView = dialogView.findViewById(R.id.select_average_rate);
        checkBox.setOnCheckedChangeListener((CompoundButton v, boolean isChecked) ->
            selectAverageRateView.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        EditText measurementFilename = dialogView.findViewById(R.id.measurement_filename);
        EditText measurementHeader = dialogView.findViewById(R.id.measurement_header);
        EditText averageRate = dialogView.findViewById(R.id.average_rate);
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
            int rate = checkBox.isChecked() ? Integer.parseInt(averageRate.getText().toString()) : 1;
            fragment.startTracking(filename, header, rate);
        });

        dialogBuilder.setNegativeButton(R.string.cancel, (dialog, w) -> dialog.dismiss());

        AlertDialog startMeasurementDialog = dialogBuilder.create();
        Window window = startMeasurementDialog.getWindow();
        startMeasurementDialog.setOnShowListener((d) ->
        {
            measurementFilename.setText(null);
            measurementFilename.setHint(StartMeasurementDialog.dateFormat.format(new Date()));
            measurementFilename.requestFocus();
            measurementHeader.setText(null);
            checkBox.setChecked(false);
            selectAverageRateView.setVisibility(View.GONE);
            averageRate.setText(R.string.default_rate);
            if (null != window)
            {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        return startMeasurementDialog;
    }
}