package de.hskl.wcn2.fragments.search_sensor;

import android.content.Context;
import android.widget.CompoundButton;

import java.util.concurrent.ArrayBlockingQueue;

import de.hskl.wcn2.R;
import de.hskl.wcn2.ble_scanner.SensorData;
import de.hskl.wcn2.util.TrackedSensorsStorage;

class SensorTrackedChangeListener implements CompoundButton.OnCheckedChangeListener
{
    private static ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(10);
    static
    {
        new Thread(() ->
        {
            try
            {
                while (true)
                {
                    SensorTrackedChangeListener.queue.take().run();
                }
            }
            catch (InterruptedException e) {}
        }).start();
    }

    private SensorData sensorData;
    private TrackedSensorsStorage trackedSensors;

    SensorTrackedChangeListener(Context context, SensorData sensorData)
    {
        this.sensorData = sensorData;
        this.trackedSensors = TrackedSensorsStorage.getInstance(context);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (isChecked)
        {
            SensorTrackedChangeListener.queue.add(() -> this.trackedSensors.trackSensor(this.sensorData));
            buttonView.setText(R.string.sensor_tracked);
        }
        else
        {
            SensorTrackedChangeListener.queue.add(() -> this.trackedSensors.untrackSensor(this.sensorData));
            buttonView.setText(R.string.sensor_not_tracked);
        }
    }
}
