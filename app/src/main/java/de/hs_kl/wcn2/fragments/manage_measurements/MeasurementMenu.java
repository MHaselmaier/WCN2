package de.hs_kl.wcn2.fragments.manage_measurements;

import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import java.io.File;

import de.hs_kl.wcn2.R;

public class MeasurementMenu extends PopupMenu implements PopupMenu.OnMenuItemClickListener
{
    private ManageMeasurementsFragment fragment;
    private File measurement;

    public MeasurementMenu(ManageMeasurementsFragment fragment, View anchor, File measurement)
    {
        super(fragment.getActivity(), anchor);

        this.fragment = fragment;
        this.measurement = measurement;

        getMenuInflater().inflate(R.menu.measurement_menu, getMenu());
        setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.open:
            this.fragment.openMeasurement(this.measurement);
            return true;
        case R.id.share:
            this.fragment.shareMeasurements(new File[] {this.measurement});
            return true;
        case R.id.delete:
            this.fragment.deleteMeasurement(this.measurement);
            return true;
        }

        return false;
    }
}
