package de.hs_kl.wcn2.fragments.manage_measurements;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.util.Constants;

public class MeasurementAdapter extends BaseAdapter
{
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
        String measurementPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +
                File.separator + Constants.DATA_DIRECTORY;
        File[] files = new File(measurementPath).listFiles();
        if (null != files)
        {
            this.measurements = new ArrayList<>(Arrays.asList(files));
        }
        else
        {
            this.measurements = new ArrayList<>();
        }

        notifyDataSetInvalidated();
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
            view = this.inflater.inflate(R.layout.measurement_item, null);
        }

        TextView label = view.findViewById(R.id.label);
        label.setText(measurement.getName());
        label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openIntent = new Intent(Intent.ACTION_VIEW);
                openIntent.setDataAndType(FileProvider.getUriForFile(MeasurementAdapter.this.context, "de.hs_kl.fileprovider", measurement), "text/plain");
                openIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                MeasurementAdapter.this.context.startActivity(openIntent);
            }
        });

        ImageButton more = view.findViewById(R.id.more);
        more.setOnClickListener(new MeasurementMenu(this.context, more, measurement, this));

        return view;
    }

    public void add(File measurement)
    {
        int position = getPosition(measurement.getName());

        if (0 <= position)
        {
            this.measurements.set(position, measurement);
        }
        else
        {
            this.measurements.add(measurement);
        }
    }

    public void remove(File measurement)
    {
        int position = getPosition(measurement.getName());

        if (0 <= position)
        {
            this.measurements.remove(position);
        }
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
