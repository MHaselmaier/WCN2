package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.PrintWriter;
import java.util.SortedMap;
import java.util.TreeMap;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.ble_scanner.SensorData;
import de.hs_kl.wcn2.util.Constants;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

public class Measurement
{
    private static final int WRITING_BUFFER = 3;

    private Context context;
    private String header;
    private String filename;
    private PrintWriter writer;

    private SortedMap<Byte, SensorData> sensors = new TreeMap<>();
    private SortedMap<Long, SortedMap<Byte, SensorData>> data = new TreeMap<>();
    private SortedMap<Long, String> actions = new TreeMap<>();
    private final long startTimestamp;
    private long lastWrittenTimestamp;

    public Measurement(Context context, String header, String filename)
    {
        this.context = context;

        this.header = header;
        this.filename = filename;

        initializeSensorMap();
        openWriter();
        writeHeader();

        this.startTimestamp = System.currentTimeMillis() / 1000;
        this.lastWrittenTimestamp = this.startTimestamp - 1;
    }

    private void initializeSensorMap()
    {
        for (SensorData sensor: TrackedSensorsStorage.getInstance(this.context).getTrackedSensors())
        {
            this.sensors.put(sensor.getSensorID(), sensor);
        }
    }

    private void openWriter()
    {
        if (null != this.writer)
        {
            this.writer.close();
        }

        try
        {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                throw new Exception();
            }

            File file = new File(Constants.DATA_DIRECTORY_PATH + File.separator +
                    this.filename + ".txt");
            file.getParentFile().mkdirs();
            file.createNewFile();

            this.writer = new PrintWriter(file);
        }
        catch(Exception e)
        {
            Log.e(Measurement.class.getSimpleName(), "Failed to create measurement output stream!");
            Toast.makeText(this.context, R.string.failed_to_create_file, Toast.LENGTH_LONG).show();
        }
    }

    public synchronized void addData(SensorData sensorData, String action)
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

            SortedMap<Byte, SensorData> currentTimestampData = this.data.get(timestamp);
            for (byte sensorID: this.sensors.keySet())
            {
                if (!currentTimestampData.containsKey(sensorID))
                {
                    this.writer.write("\tNaN\tNaN");
                    continue;
                }

                SensorData sensorData = currentTimestampData.get(sensorID);
                this.writer.format("\t%.1f\t%.1f", sensorData.getTemperature(),
                        sensorData.getRelativeHumidity());
            }
            this.writer.format("\t%s\n", this.actions.get(timestamp));
        }
        this.writer.flush();

        this.lastWrittenTimestamp = newTimestamp;
    }

    private void handleNewSensor(SensorData sensorData)
    {
        this.sensors.put(sensorData.getSensorID(), sensorData);
        openWriter();

        this.lastWrittenTimestamp = this.startTimestamp - 1;
        writeHeader();
    }

    private void writeHeader()
    {
        this.writer.write(this.header + "\n\n");

        this.writer.write("Sensor ID\tMAC Address\tMnemonic\n");
        for (SensorData sensorData: this.sensors.values())
        {
            this.writer.format("%d\t%s\t%s\n", sensorData.getSensorID(), sensorData.getMacAddress(),
                    sensorData.getMnemonic());
        }

        this.writer.write("\n");

        this.writer.write("Time [min]\t");
        for (byte sensorID: this.sensors.keySet())
        {
            this.writer.format("Temperature [°C] %d\tRelative Humidity [%%] %d\t", sensorID,
                    sensorID);
        }
        this.writer.write("Action\n");
        this.writer.flush();
    }

    public synchronized void finish()
    {
        writeDataUntilTimestamp(System.currentTimeMillis() / 1000);
        this.writer.close();
    }
}