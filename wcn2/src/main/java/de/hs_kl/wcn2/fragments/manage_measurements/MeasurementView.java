package de.hs_kl.wcn2.fragments.manage_measurements;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.util.Constants;

class MeasurementView extends LinearLayout
{
    private CheckBox checkBox;
    private ImageButton more;

    MeasurementView(final ManageMeasurementsFragment fragment, final File measurement)
    {
        super(fragment.getContext());
        inflate(fragment.getContext(), R.layout.measurement_item, this);

        this.checkBox = findViewById(R.id.checkBox);
        this.checkBox.setVisibility(fragment.isSelectionModeEnabled() ? View.VISIBLE :
                View.GONE);

        TextView measurementName = findViewById(R.id.label);
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

        this.more = findViewById(R.id.more);
        PopupMenu menu = new MeasurementMenu(fragment, this.more, measurement);
        this.more.setOnClickListener((v) -> menu.show());
        this.more.setVisibility(fragment.isSelectionModeEnabled() ? View.GONE : View.VISIBLE);
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
        this.checkBox.setVisibility(show ? View.VISIBLE : View.GONE);
        this.more.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
