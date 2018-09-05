package de.hs_kl.wcn2.fragments.manage_measurements;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.util.Constants;

public class MeasurementAdapter extends BaseAdapter
{
    private final static String MEASUREMENT_PATH =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +
            File.separator + Constants.DATA_DIRECTORY;

    private Context context;
    private LayoutInflater inflater;
    private List<File> measurements;

    public MeasurementAdapter(Context context, LayoutInflater inflater)
    {
        this.context = context;
        this.inflater = inflater;

        loadFiles();
    }

    public void loadFiles()
    {
        File[] files = new File(MeasurementAdapter.MEASUREMENT_PATH).listFiles();
        if (null == files) files = new File[0];

        this.measurements = new ArrayList<>(Arrays.asList(files));
        notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return this.measurements.size();
    }

    @Override
    public File getItem(int position)
    {
        return this.measurements.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return this.measurements.get(position).getName().hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        final File measurement = this.measurements.get(position);

        if (null == view)
        {
            view = this.inflater.inflate(R.layout.measurement_item, parent, false);
        }

        TextView label = view.findViewById(R.id.label);
        label.setText(measurement.getName());
        label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openIntent = new Intent(Intent.ACTION_VIEW);
                Uri uri = FileProvider.getUriForFile(MeasurementAdapter.this.context,
                        Constants.FILE_PROVIDER_AUTHORITY, measurement);
                openIntent.setDataAndType(uri, Constants.MEASUREMENT_DATA_TYPE);
                openIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                MeasurementAdapter.this.context.startActivity(openIntent);
            }
        });

        ImageButton more = view.findViewById(R.id.more);
        final PopupMenu menu = new MeasurementMenu(this.context, more, measurement, this);
        more.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                menu.show();
            }
        });

        return view;
    }

    public void add(File measurement)
    {
        int position = getPosition(measurement.getName());

        if (-1 == position)
        {
            this.measurements.add(measurement);
            return;
        }

        this.measurements.set(position, measurement);
    }

    public void remove(File measurement)
    {
        int position = getPosition(measurement.getName());

        if (-1 == position) return;

        this.measurements.remove(position);
    }

    private int getPosition(String name)
    {
        for (int i = 0; i < this.measurements.size(); ++i)
        {
            if (this.measurements.get(i).getName().equals(name))
            {
                return i;
            }
        }
        return -1;
    }
}
