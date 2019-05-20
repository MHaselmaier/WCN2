package de.hs_kl.wcn2.usage;

import android.os.Handler;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

abstract class Animation
{
    private Handler handler = new Handler();
    private int currentStep = 0;
    private List<Step> steps = new ArrayList<>();

    abstract View getRootView();
    abstract protected void initialize();
    abstract String getDescription();

    protected void addStep(Runnable step, int delay)
    {
        this.steps.add(new Step(step, delay));
    }

    void start()
    {
        stop();
        this.handler.post(this::initialize);
        step();
    }

    private void step()
    {
        if (this.steps.isEmpty()) return;

        Step currentStep = this.steps.get(this.currentStep++);
        this.handler.postDelayed(currentStep.runnable, currentStep.delay);

        if (this.steps.size() <= this.currentStep)
        {
            this.currentStep = 0;
        }
    }

    void stop()
    {
        this.handler.removeCallbacksAndMessages(null);
        this.currentStep = 0;
    }

    private class Step
    {
        private Runnable runnable;
        private int delay;

        Step(Runnable runnable, int delay)
        {
            this.runnable = () ->
            {
                runnable.run();
                step();
            };
            this.delay = delay;
        }
    }
}