package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.util.Constants;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

public class Dataset
{
    private String measurementHeader = null;
    private SortedMap<Byte, String> sensorInfo = new TreeMap<>();
    private SortedMap<Integer, SortedMap<Byte, DatasetEntry>> entries = new TreeMap<>();

    public void setMeasurementHeader(String measurementHeader)
    {
        this.measurementHeader = measurementHeader;
    }

    public void add(DatasetEntry entry)
    {
        this.sensorInfo.put(entry.getSensorID(), entry.getSensorMACAddress());

        int key = (int)(entry.getTimestamp() * 100);
        if (!this.entries.containsKey(key))
        {
            this.entries.put(key, new TreeMap<Byte, DatasetEntry>());
        }
        this.entries.get(key).put(entry.getSensorID(), entry);
    }

    public void clear()
    {
        this.sensorInfo.clear();
        this.entries.clear();
    }

    public void writeToFile(Context context)
    {
        writeToFile(context, null);
    }

    public void writeToFile(Context context, String filename)
    {
        try
        {
            PrintWriter outputStream = createOutputStream(filename);

            writeMeasurementHeader(outputStream, context);
            writeSensorInfo(outputStream, context);
            writeSensorData(outputStream);

            outputStream.close();
        }
        catch (Exception e)
        {
            Log.e(Dataset.class.getSimpleName(), e.getClass() + " " + e.getMessage());
            Toast.makeText(context, R.string.failed_to_write_file, Toast.LENGTH_LONG).show();
        }
    }

    private PrintWriter createOutputStream(String filename) throws Exception
    {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            throw new Exception();
        }

        if (null == filename || filename.equals(""))
        {
            filename = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        }

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                Constants.DATA_DIRECTORY + File.separator + filename + ".txt");
        file.getParentFile().mkdirs();
        file.createNewFile();

        return new PrintWriter(file);
    }

    private void writeMeasurementHeader(PrintWriter outputStream, Context context)
    {
        if (null != this.measurementHeader)
        {
            outputStream.write(this.measurementHeader + "\n\n");
        }
        else
        {
            outputStream.write(context.getResources().getString(R.string.measurement_comment) + "\n\n");
        }
    }

    private void writeSensorInfo(PrintWriter outputStream, Context context)
    {
        outputStream.write("Sensor ID\tMAC Address\tMnemonic\n");

        TrackedSensorsStorage trackedSensors = TrackedSensorsStorage.getInstance(context);
        for (Map.Entry<Byte, String> entry: this.sensorInfo.entrySet())
        {
            outputStream.format("%d\t%s\t%s\n", entry.getKey(), entry.getValue(),
                    trackedSensors.getMnemonic(entry.getValue()));
        }

        outputStream.write("\n");
    }

    private void writeSensorData(PrintWriter outputStream)
    {
        outputStream.write("Time [min]\t");
        for (Map.Entry<Byte, String> entry: this.sensorInfo.entrySet())
        {
            outputStream.format("Temperature [°C] %d\tRelative Humidity [%%] %d\t", entry.getKey(), entry.getKey());
        }
        outputStream.write("Action\n");

        prepareEntriesForWrite();

        for (Map.Entry<Integer, SortedMap<Byte, DatasetEntry>> timestamp: this.entries.entrySet())
        {
            outputStream.format("%.2f", timestamp.getKey() / 100f);

            for (Map.Entry<Byte, DatasetEntry> entry: timestamp.getValue().entrySet())
            {
                outputStream.format("\t%.1f\t%.1f", entry.getValue().getTemperature(), entry.getValue().getHumidity());
            }

            DatasetEntry firstEntry = (DatasetEntry)timestamp.getValue().values().toArray()[0];
            outputStream.format("\t%s\n", firstEntry.getAction());
        }
    }

    private void prepareEntriesForWrite()
    {
        Byte[] sensorIDs = new Byte[this.sensorInfo.size()];
        this.sensorInfo.keySet().toArray(sensorIDs);

        for (Map.Entry<Integer, SortedMap<Byte, DatasetEntry>> entry: this.entries.entrySet())
        {
            if (entry.getValue().size() != sensorIDs.length)
            {
                int minutes = entry.getKey() / 100;
                int seconds = (int)(((entry.getKey() % 100) / 100f) * 60);
                int timestamp = minutes * 60 * 1000 + seconds * 1000;
                String action = ((DatasetEntry)entry.getValue().values().toArray()[0]).getAction();

                for (byte sensorID: sensorIDs)
                {
                    if (!entry.getValue().containsKey(sensorID))
                    {
                        entry.getValue().put(sensorID, new DatasetEntry(sensorID, "",
                                Float.NaN, Float.NaN, action, timestamp));
                    }
                }
            }
        }
    }
}