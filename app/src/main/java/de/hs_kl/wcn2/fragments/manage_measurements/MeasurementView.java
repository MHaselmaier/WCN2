package de.hs_kl.wcn2.fragments.manage_measurements;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.util.Constants;

class MeasurementView
{
    private View root;
    private CheckBox checkBox;
    private ImageButton more;

    MeasurementView(final ManageMeasurementsFragment fragment, ViewGroup parent,
                           final File measurement)
    {
        this.root = fragment.getActivity().getLayoutInflater().inflate(R.layout.measurement_item,
                parent, false);

        this.checkBox = this.root.findViewById(R.id.checkBox);
        this.checkBox.setVisibility(fragment.isSelectionModeEnabled() ? View.VISIBLE :
                View.INVISIBLE);

        TextView measurementName = this.root.findViewById(R.id.label);
        measurementName.setText(measurement.getName());
        measurementName.setOnClickListener((v) ->
        {
            if (fragment.isSelectionModeEnabled())
            {
                this.checkBox.toggle();
            }
            else
            {
                Intent openIntent = new Intent(Intent.ACTION_VIEW);
                Uri uri = FileProvider.getUriForFile(fragment.getActivity(),
                        Constants.FILE_PROVIDER_AUTHORITY, measurement);
                openIntent.setDataAndType(uri, Constants.MEASUREMENT_DATA_TYPE);
                openIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                fragment.startActivity(openIntent);
            }
        });
        measurementName.setOnLongClickListener((v) ->
        {
            fragment.enableSelectionMode();
            this.checkBox.setChecked(true);
            return true;
        });

        this.more = this.root.findViewById(R.id.more);
        PopupMenu menu = new MeasurementMenu(fragment, this.more, measurement);
        this.more.setOnClickListener((v) -> menu.show());
        this.more.setVisibility(fragment.isSelectionModeEnabled() ? View.VISIBLE : View.INVISIBLE);
    }

    View getRoot()
    {
        return this.root;
    }

    boolean isChecked()
    {
        return this.checkBox.isChecked();
    }

    void uncheck()
    {
        this.checkBox.setChecked(false);
    }

    void showCheckBox(boolean show)
    {
        this.checkBox.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        this.more.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
    }
}
