package de.hs_kl.blesensor.fragments.manage_measurements;

import android.content.Context;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.File;

import de.hs_kl.blesensor.R;

public class MeasurementMenu extends PopupMenu implements View.OnClickListener, PopupMenu.OnMenuItemClickListener
{
    private Context context;
    private File measurement;
    private MeasurementAdapter adapter;

    public MeasurementMenu(Context context, View anchor, File measurement, MeasurementAdapter adapter)
    {
        super(context, anchor);

        this.context = context;
        this.measurement = measurement;
        this.adapter = adapter;

        getMenuInflater().inflate(R.menu.measurement_menu, getMenu());
        setOnMenuItemClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.share:
            // TODO
            Toast.makeText(this.context, "Not yet implemented!", Toast.LENGTH_LONG).show();
            Log.e(MeasurementMenu.class.getSimpleName(), "Sharing not yet implemented!");
            return false;
        case R.id.delete:
            if (this.measurement.delete())
            {
                this.adapter.remove(this.measurement);
                this.adapter.notifyDataSetChanged();
                Toast.makeText(this.context, R.string.deleted, Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(this.context, R.string.delete_failed, Toast.LENGTH_LONG).show();
            }
            return true;
        }

        return false;
    }
}
