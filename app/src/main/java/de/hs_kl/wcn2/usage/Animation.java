package de.hs_kl.wcn2.usage;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

public class Animation
{
    private Handler handler = new Handler();
    private Runnable initialize;
    private int descriptionResourceID;
    private int currentStep = 0;
    private List<Step> steps = new ArrayList<>();

    public Animation(Runnable initialize, int descriptionResourceID)
    {
        this.initialize = initialize;
        this.descriptionResourceID = descriptionResourceID;
    }

    public int getDescriptionResourceID()
    {
        return this.descriptionResourceID;
    }

    public void addStep(Step step)
    {
        this.steps.add(step);
    }

    public void start()
    {
        stop();
        this.handler.post(this.initialize);
        step();
    }

    private void step()
    {
        Step currentStep = this.steps.get(this.currentStep++);
        this.handler.postDelayed(currentStep.runnable, currentStep.delay);

        if (this.steps.size() <= this.currentStep)
        {
            this.currentStep = 0;
        }
    }

    public void stop()
    {
        this.handler.removeCallbacksAndMessages(null);
        this.currentStep = 0;
    }

    public static class Step
    {
        private Runnable runnable;
        private int delay;

        public Step(final Animation animation, final Runnable runnable, int delay)
        {
            this.runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    runnable.run();
                    animation.step();
                }
            };
            this.delay = delay;
        }
    }
}