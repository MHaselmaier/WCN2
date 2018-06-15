package de.hs_kl.blesensor.fragments.manage_measurements;

import android.content.Context;
import android.os.Environment;
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

import de.hs_kl.blesensor.R;
import de.hs_kl.blesensor.util.Constants;

public class MeasurementAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater inflater;
    private List<File> measurements;

    public MeasurementAdapter(Context context, LayoutInflater inflater)
    {
        this.context = context;
        this.inflater = inflater;

        String measurementPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +
                                 File.separator + Constants.DATA_DIRECTORY;
        this.measurements = new ArrayList<>(Arrays.asList(new File(measurementPath).listFiles()));
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
        File measurement = this.measurements.get(position);

        if (null == view)
        {
            view = this.inflater.inflate(R.layout.measurement_item, null);

            TextView label = view.findViewById(R.id.label);
            label.setText(measurement.getName());

            ImageButton more = view.findViewById(R.id.more);
            more.setOnClickListener(new MeasurementMenu(this.context, more, measurement, this));
        }

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
