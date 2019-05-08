package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2_sensors.SensorData;
import de.hs_kl.wcn2.util.TrackedSensorsStorage;

public class TrackedSensorsOverview extends CardView
{
    private Context context;
    private TrackedSensorsStorage trackedSensorsStorage;
    private LinearLayout container;
    private View emptyListItem;
    private List<SensorData> trackedSensors = new ArrayList<>();
    private List<TrackedSensorView> trackedSensorViews = new ArrayList<>();

    public TrackedSensorsOverview(@NonNull Context context)
    {
        this(context, null);
    }

    public TrackedSensorsOverview(@NonNull Context context, AttributeSet attrs)
    {
        this(context, attrs, R.attr.cardViewStyle);
    }

    public TrackedSensorsOverview(@NonNull Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.tracked_sensor_overview, this);

        this.context = context;
        this.trackedSensorsStorage = TrackedSensorsStorage.getInstance(this.context);
        this.container = findViewById(R.id.tracked_sensors);
        this.emptyListItem = this.container.findViewById(R.id.empty_list_item);
        TextView label = this.emptyListItem.findViewById(R.id.label);
        label.setText(R.string.no_sensors_tracked);
    }

    void addSensor(SensorData sensorData)
    {
        for (int i = 0; this.trackedSensors.size() > i; ++i)
        {
            if (this.trackedSensors.get(i).getMacAddress().equals(sensorData.getMacAddress()))
            {
                this.trackedSensors.set(i, sensorData);
                return;
            }
        }

        int position = this.trackedSensors.size();
        this.trackedSensors.add(sensorData);
        this.trackedSensorViews.add(null);
        Handler handler = new Handler();
        new Thread(() -> {
            TrackedSensorView view = new TrackedSensorView(this.context, sensorData);
            handler.post(() -> {
                this.trackedSensorViews.set(position, view);
                this.container.addView(view);
            });
        }).start();
    }

    void show()
    {
        updateViews();
        setVisibility(View.VISIBLE);
    }

    void updateViews()
    {
        updateTrackedSensors();
        for (int i = 0; this.trackedSensors.size() > i; ++i)
        {
            if (null == this.trackedSensorViews.get(i)) continue;
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
                this.container.removeView(this.trackedSensorViews.remove(i));
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

    void hide()
    {
        setVisibility(View.GONE);
    }
}
