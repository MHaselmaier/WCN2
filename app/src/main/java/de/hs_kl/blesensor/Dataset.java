package de.hs_kl.blesensor;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Dataset
{
    private Set<Byte> sensorIDs = new TreeSet<>();
    private List<DatasetEntry> entries = new ArrayList<>();

    public void add(DatasetEntry entry)
    {
        this.sensorIDs.add(entry.getSensorID());

        if (this.entries.contains(entry))
        {
            this.entries.remove(entry);
        }

        this.entries.add(entry);
    }

    public void writeToFile(Context context)
    {
        try
        {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            {
                throw new Exception();
            }
            SortUtil.sort(this.entries, new DatasetEntryComparator());
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                                 Constants.DATA_DIRECTORY + File.separator + System.currentTimeMillis() + ".csv");
            if (!file.getParentFile().exists())
            {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            PrintWriter outputStream = new PrintWriter(file);

            outputStream.write("timestamp");
            for (Byte id: this.sensorIDs)
            {
                outputStream.write(", sensor" + id + "_temperature, sensor" + id + "_humidity");
            }
            outputStream.write("\n");

            long lastTimestamp = this.entries.get(0).getTimestamp();
            outputStream.write("" + lastTimestamp);
            for (DatasetEntry entry: this.entries)
            {
                if (entry.getTimestamp() > lastTimestamp)
                {
                    outputStream.write("\n" + entry.getTimestamp());
                }

                outputStream.write(", " + entry.getTemperature() + ", " + entry.getHumidity());
            }
            outputStream.close();
        }
        catch (Exception e)
        {
            Log.e(Dataset.class.getSimpleName(), e.getClass() + " " + e.getMessage());
            Toast.makeText(context, R.string.failed_to_write_file, Toast.LENGTH_LONG).show();
        }
    }

    private static class DatasetEntryComparator implements Comparator<DatasetEntry>
    {
        @Override
        public int compare(DatasetEntry a, DatasetEntry b)
        {
            if (a.getTimestamp() == b.getTimestamp())
            {
                return a.getSensorID() - b.getSensorID();
            }
            else
            {
                return (int)(a.getTimestamp() - b.getTimestamp());
            }
        }
    }
}