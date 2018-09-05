package de.hs_kl.wcn2.fragments.manage_measurements;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.File;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.util.Constants;

public class MeasurementMenu extends PopupMenu implements PopupMenu.OnMenuItemClickListener
{
    private Context context;
    private File measurement;
    private Uri uri;
    private MeasurementAdapter adapter;

    public MeasurementMenu(Context context, View anchor, File measurement,
                           MeasurementAdapter adapter)
    {
        super(context, anchor);

        this.context = context;
        this.measurement = measurement;
        this.uri = FileProvider.getUriForFile(this.context, Constants.FILE_PROVIDER_AUTHORITY,
                this.measurement);
        this.adapter = adapter;

        getMenuInflater().inflate(R.menu.measurement_menu, getMenu());
        setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.open:
            onOpenClicked();
            return true;
        case R.id.share:
            onShareClicked();
            return true;
        case R.id.delete:
            onDeleteClicked();
            return true;
        }

        return false;
    }

    private void onOpenClicked()
    {
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setDataAndType(this.uri, Constants.MEASUREMENT_DATA_TYPE);
        openIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.context.startActivity(openIntent);
    }

    private void onShareClicked()
    {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(Constants.MEASUREMENT_DATA_TYPE);
        shareIntent.putExtra(Intent.EXTRA_STREAM, this.uri);
        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String title = this.context.getResources().getString(R.string.share);
        this.context.startActivity(Intent.createChooser(shareIntent, title));
    }

    private void onDeleteClicked()
    {
        if (!this.measurement.delete())
        {
            Toast.makeText(this.context, R.string.delete_failed, Toast.LENGTH_LONG).show();
            return;
        }

        this.adapter.remove(this.measurement);
        this.adapter.notifyDataSetChanged();
        Toast.makeText(this.context, R.string.deleted, Toast.LENGTH_LONG).show();
    }
}
