package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.SortedMap;
import java.util.TreeMap;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.util.Constants;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;
import de.hs_kl.wcn2_sensors.WCN2SensorData;

class Measurement
{
    private static final int WRITING_BUFFER = 3;

    private Context context;
    private String header;
    private String filename;
    private int averageRate;
    private PrintWriter writer;

    private SortedMap<Byte, WCN2SensorData> sensors = new TreeMap<>();
    private SortedMap<Long, SortedMap<Byte, WCN2SensorData>> data = new TreeMap<>();
    private SortedMap<Long, String> actions = new TreeMap<>();
    private final long startTimestamp;
    private long lastWrittenTimestamp;

    Measurement(Context context, String header, String filename, int averageRate)
    {
        this.context = context;

        this.header = header;
        this.filename = filename;
        this.averageRate = averageRate;

        initializeSensorMap();
        this.writer = openWriter(this.writer, this.filename, this.context);
        writeHeader(this.writer);

        this.startTimestamp = System.currentTimeMillis() / 1000;
        this.lastWrittenTimestamp = this.startTimestamp - 1;
    }

    private void initializeSensorMap()
    {
        for (WCN2SensorData sensor: TrackedSensorsStorage.getInstance(this.context).getTrackedSensors())
        {
            this.sensors.put(sensor.getSensorID(), sensor);
        }
    }

    synchronized void addData(WCN2SensorData sensorData, String action)
    {
        this.actions.put(sensorData.getTimestamp() / 1000, action);

        if (!this.sensors.containsKey(sensorData.getSensorID()))
        {
            handleNewSensor(sensorData);
        }
        this.sensors.put(sensorData.getSensorID(), sensorData);

        long timestampInSec = sensorData.getTimestamp() / 1000;
        if (!this.data.containsKey(timestampInSec))
        {
            this.data.put(timestampInSec, new TreeMap<>());
        }

        this.data.get(timestampInSec).put(sensorData.getSensorID(), sensorData);

        writeDataUntilTimestamp(sensorData.getTimestamp() / 1000 - Measurement.WRITING_BUFFER);
    }

    private void writeDataUntilTimestamp(long newTimestamp)
    {
        if (this.lastWrittenTimestamp >= newTimestamp) return;

        for (long timestamp = this.lastWrittenTimestamp + 1; newTimestamp >= timestamp; ++timestamp)
        {
            if (!this.data.containsKey(timestamp)) continue;

            float relativeTimeInMinutes = (timestamp - this.startTimestamp) / 60f;
            this.writer.format("%.2f", relativeTimeInMinutes);

            SortedMap<Byte, WCN2SensorData> currentTimestampData = this.data.get(timestamp);
            for (byte sensorID : this.sensors.keySet())
            {
                if (!currentTimestampData.containsKey(sensorID))
                {
                    this.writer.write("\tNaN\tNaN");
                    continue;
                }

                WCN2SensorData sensorData = currentTimestampData.get(sensorID);
                this.writer.format("\t%.1f\t%.1f", sensorData.getTemperature(),
                        sensorData.getRelativeHumidity());
            }
            this.writer.format("\t%s\n", this.actions.get(timestamp));
        }
        this.writer.flush();

        this.lastWrittenTimestamp = newTimestamp;
    }

    private void handleNewSensor(WCN2SensorData sensorData)
    {
        this.sensors.put(sensorData.getSensorID(), sensorData);
        this.writer = openWriter(this.writer, this.filename, this.context);
        writeHeader(this.writer);

        this.lastWrittenTimestamp = this.startTimestamp - 1;
        writeDataUntilTimestamp(sensorData.getTimestamp() / 1000 - Measurement.WRITING_BUFFER);
    }

    private void writeHeader(PrintWriter writer)
    {
        writer.write(this.header + "\n\n");

        writer.write("Sensor ID\tMAC Address\tMnemonic\n");
        for (WCN2SensorData sensorData: this.sensors.values())
        {
            writer.format("%d\t%s\t%s\n", sensorData.getSensorID() & 0xFF, sensorData.getMacAddress(),
                    sensorData.getMnemonic());
        }

        writer.write("\n");

        writer.write("Time [min]\t");
        for (byte sensorID: this.sensors.keySet())
        {
            writer.format("Temperature [°C] %d\tRelative Humidity [%%] %d\t", sensorID,
                    sensorID);
        }
        writer.write("Action\n");
        writer.flush();
    }

    synchronized void finish()
    {
        writeDataUntilTimestamp(System.currentTimeMillis() / 1000);
        this.writer.close();

        if (1 < this.averageRate)
        {
            createReducedFile();
        }
    }

    private void createReducedFile()
    {
        PrintWriter writer = openWriter(null, this.filename + "_reduced", this.context);
        writeHeader(writer);

        long endTimestamp = System.currentTimeMillis() / 1000;
        long currentTimestamp = this.startTimestamp;
        while (endTimestamp >= currentTimestamp)
        {
            float relativeTimeInMinutes = (currentTimestamp + this.averageRate - this.startTimestamp) / 60f;
            writer.format("%.2f", relativeTimeInMinutes);

            String action = "";
            for (byte sensorID: this.sensors.keySet())
            {
                int count = 0;
                float temperature = Float.NaN;
                float humidity = Float.NaN;
                for (long t = currentTimestamp; currentTimestamp + this.averageRate > t; ++t)
                {
                    if (!this.data.containsKey(t)) continue;
                    action = this.actions.get(t);
                    if (!this.data.get(t).containsKey(sensorID)) continue;

                    WCN2SensorData sensorData = this.data.get(t).get(sensorID);
                    ++count;
                    if (1 == count)
                    {
                        temperature = sensorData.getTemperature();
                        humidity = sensorData.getRelativeHumidity();
                    }
                    else
                    {
                        temperature = ((count - 1) * temperature + sensorData.getTemperature()) / count;
                        humidity = ((count - 1) * humidity + sensorData.getRelativeHumidity()) / count;
                    }
                }
                writer.format("\t%.1f\t%.1f", temperature, humidity);
            }

            currentTimestamp += this.averageRate;
            writer.format("\t%s\n", action);
        }

        writer.close();
    }

    private static PrintWriter openWriter(PrintWriter writer, String filename, Context context)
    {
        if (null != writer)
        {
            writer.close();
        }

        try
        {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            {
                throw new Exception();
            }

            File file = new File(Constants.DATA_DIRECTORY_PATH + File.separator +
                    filename + ".txt");

            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs())
            {
                throw new Exception();
            }
            if (!file.exists() && !file.createNewFile())
            {
                throw  new Exception();
            }

            writer = new PrintWriter(file, StandardCharsets.UTF_16.name());
        }
        catch(Exception e)
        {
            Log.e(Measurement.class.getSimpleName(), "Failed to create measurement output stream!");
            Toast.makeText(context, R.string.failed_to_create_file, Toast.LENGTH_LONG).show();
        }

        return writer;
    }
}