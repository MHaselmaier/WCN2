package de.hs_kl.wcn2.fragments.manage_measurements;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.util.Constants;

public class ManageMeasurementsFragment extends Fragment
{
    private LinearLayout measurementsContainer;
    private View emptyListItem;
    private List<File> measurements = new ArrayList<>();
    private List<MeasurementView> measurementViews = new ArrayList<>();

    private boolean selectionModeEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.measurement_selection_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.share:
            List<File> selectedMeasurements = new ArrayList<>();
            for (int i = 0; this.measurements.size() > i; ++i)
            {
                if (this.measurementViews.get(i).isChecked())
                {
                    selectedMeasurements.add(this.measurements.get(i));
                }
            }
            shareMeasurements(selectedMeasurements.toArray(new File[0]));
            disableSelectionMode();
            return true;
        case R.id.delete:
            for (int i = this.measurements.size() - 1; 0 <= i; --i)
            {
                if (this.measurementViews.get(i).isChecked())
                {
                    deleteMeasurement(this.measurements.get(i));
                }
            }
            disableSelectionMode();
            return true;
        case R.id.cancel:
            disableSelectionMode();
            return true;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.manage_measurement, container, false);

        this.measurementsContainer = view.findViewById(R.id.measurements);
        this.emptyListItem = view.findViewById(R.id.empty_list_item);
        TextView label = this.emptyListItem.findViewById(R.id.label);
        label.setText(R.string.no_measurements_saved);

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        if (hidden) return;

        disableSelectionMode();
        loadFiles();
    }

    public boolean isSelectionModeEnabled()
    {
        return this.selectionModeEnabled;
    }

    public void enableSelectionMode()
    {
        this.selectionModeEnabled = true;
        setHasOptionsMenu(true);
        updateMeasurementViews();
    }

    public void disableSelectionMode()
    {
        for (MeasurementView view: this.measurementViews)
        {
            view.uncheck();
        }

        this.selectionModeEnabled = false;
        setHasOptionsMenu(false);
        updateMeasurementViews();
    }

    private void loadFiles()
    {
        File[] files = new File(Constants.DATA_DIRECTORY_PATH).listFiles();
        if (null == files) files = new File[0];

        for (File file: files)
        {
            if (!this.measurements.contains(file))
            {
                this.measurements.add(file);
                MeasurementView view = new MeasurementView(this, this.measurementsContainer, file);
                this.measurementViews.add(view);
                this.measurementsContainer.addView(view.getRoot());
            }
        }

        updateMeasurementViews();
    }

    private void updateMeasurementViews()
    {
        for (MeasurementView view: this.measurementViews)
        {
            view.showCheckBox(this.selectionModeEnabled);
        }

        this.emptyListItem.setVisibility(this.measurements.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void deleteMeasurement(File measurement)
    {
        if (!measurement.delete())
        {
            Toast.makeText(getActivity(), R.string.delete_failed, Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(getActivity(), R.string.deleted, Toast.LENGTH_LONG).show();

        int index = this.measurements.indexOf(measurement);
        if (0 <= index)
        {
            this.measurements.remove(index);
            this.measurementsContainer.removeView(this.measurementViews.remove(index).getRoot());
        }
    }

    public void shareMeasurements(File[] measurements)
    {
        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.setType(Constants.MEASUREMENT_DATA_TYPE);
        ArrayList<Uri> uris = new ArrayList<>();
        for (File measurement: measurements)
        {
            uris.add(FileProvider.getUriForFile(getActivity(), Constants.FILE_PROVIDER_AUTHORITY,
                    measurement));
        }
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share)));
    }

    public void openMeasurement(File measurement)
    {
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(getActivity(), Constants.FILE_PROVIDER_AUTHORITY,
                measurement);
        openIntent.setDataAndType(uri, Constants.MEASUREMENT_DATA_TYPE);
        openIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(openIntent);
    }
}
