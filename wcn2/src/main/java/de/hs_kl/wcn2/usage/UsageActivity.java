package de.hs_kl.wcn2.usage;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2.R;

public class UsageActivity extends AppCompatActivity
{
    private FrameLayout container;
    private TextView description;
    private LinearLayout stepIndicator;
    private List<Animation> animations = new ArrayList<>();
    private int state;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.usage_activity);

        this.container = findViewById(R.id.container);
        this.description = findViewById(R.id.description);
        this.stepIndicator = findViewById(R.id.step_indicator);

        Button next = findViewById(R.id.next);
        next.setOnClickListener((v) ->
        {
            this.animations.get(this.state).stop();

            if (this.animations.size() - 1 > this.state)
            {
                this.animations.get(++this.state).start();
                updateAnimationView();
                updateDescription();
                updateStepIndicator();
            }
            else
            {
                finish();
            }
        });

        Button skip = findViewById(R.id.skip);
        skip.setOnClickListener((v) -> finish());

        this.animations.add(new IntroductionAnimation(this));
        this.animations.add(new CreateActionAnimation(this));
        this.animations.add(new SelectTrackedSensorsAnimation(this));
        this.animations.add(new PerformMeasurementAnimation(this));
    }

    private void updateAnimationView()
    {
        this.container.removeAllViews();
        this.container.addView(this.animations.get(this.state).getRootView());
    }

    private void updateDescription()
    {
        this.description.setText(this.animations.get(this.state).getDescription());
    }

    private void updateStepIndicator()
    {
        this.stepIndicator.removeAllViews();
        float dpsScale = getResources().getDisplayMetrics().density;
        Drawable circle = getResources().getDrawable(R.drawable.ic_circle);
        for (int i = 0; this.animations.size() > i; ++i)
        {
            ImageView indicator = new ImageView(this);
            indicator.setBackground(circle);
            indicator.setColorFilter(Color.WHITE);
            int size = (int)(this.state == i ? 8 * dpsScale : 5 * dpsScale);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            int margin = (int)(2 * dpsScale);
            params.setMargins(margin, margin, margin, margin);
            indicator.setLayoutParams(params);
            this.stepIndicator.addView(indicator);
        }
    }

    protected void onResume()
    {
        super.onResume();
        this.animations.get(this.state).start();
        updateAnimationView();
        updateDescription();
        updateStepIndicator();
    }

    protected void onPause()
    {
        super.onPause();
        this.animations.get(this.state).stop();
    }
}