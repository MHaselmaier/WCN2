package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.ble_scanner.SensorData;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

public class TrackedSensorsOverview
{
    private Context context;
    private TrackedSensorsStorage trackedSensorsStorage;
    private View root;
    private LinearLayout container;
    private View emptyListItem;
    private List<SensorData> trackedSensors = new ArrayList<>();
    private List<TrackedSensorView> trackedSensorViews = new ArrayList<>();

    public TrackedSensorsOverview(Context context, View root)
    {
        this.context = context;
        this.trackedSensorsStorage = TrackedSensorsStorage.getInstance(this.context);
        this.root = root;
        this.container = this.root.findViewById(R.id.tracked_sensors);
        this.emptyListItem = this.container.findViewById(R.id.empty_list_item);
        TextView label = this.emptyListItem.findViewById(R.id.label);
        label.setText(R.string.no_sensors_tracked);
    }

    public void addSensor(SensorData sensorData)
    {
        for (int i = 0; this.trackedSensors.size() > i; ++i)
        {
            if (this.trackedSensors.get(i).getMacAddress().equals(sensorData.getMacAddress()))
            {
                this.trackedSensors.set(i, sensorData);
                return;
            }
        }

        this.trackedSensors.add(sensorData);
        TrackedSensorView view = new TrackedSensorView(this.context, sensorData);
        this.trackedSensorViews.add(view);
        this.container.addView(view.getRoot());
    }

    public void show()
    {
        updateViews();
        this.root.setVisibility(View.VISIBLE);
    }

    public void updateViews()
    {
        updateTrackedSensors();
        for (int i = 0; this.trackedSensors.size() > i; ++i)
        {
            this.trackedSensorViews.get(i).updateView(this.trackedSensors.get(i));
        }

        this.emptyListItem.setVisibility(this.trackedSensors.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateTrackedSensors()
    {
        removeUntrackedSensors();
        addNewlyTrackedSensors();
    }

    private void removeUntrackedSensors()
    {
        for (int i = this.trackedSensors.size() - 1; 0 <= i; --i)
        {
            if (!this.trackedSensorsStorage.isTracked(this.trackedSensors.get(i)))
            {
                this.trackedSensors.remove(i);
                this.container.removeView(this.trackedSensorViews.remove(i).getRoot());
            }
        }
    }

    private void addNewlyTrackedSensors()
    {
        for (SensorData sensorData: this.trackedSensorsStorage.getTrackedSensors())
        {
            boolean alreadyExists = false;
            for (SensorData alreadyTrackedSensor: this.trackedSensors)
            {
                if (alreadyTrackedSensor.getMacAddress().equals(sensorData.getMacAddress()))
                {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists)
            {
                addSensor(sensorData);
            }
        }
    }

    public void hide()
    {
        this.root.setVisibility(View.GONE);
    }
}
