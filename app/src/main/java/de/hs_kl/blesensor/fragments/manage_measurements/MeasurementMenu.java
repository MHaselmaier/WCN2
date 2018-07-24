package de.hs_kl.blesensor.fragments.manage_measurements;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
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
        case R.id.open:
            Intent openIntent = new Intent(Intent.ACTION_VIEW);
            openIntent.setDataAndType(FileProvider.getUriForFile(this.context, "de.hs_kl.fileprovider", this.measurement), "text/plain");
            openIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.context.startActivity(openIntent);
            return true;
        case R.id.share:
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this.context, "de.hs_kl.fileprovider", this.measurement));
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String title = this.context.getResources().getString(R.string.share);
            this.context.startActivity(Intent.createChooser(shareIntent, title));
            return true;
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
