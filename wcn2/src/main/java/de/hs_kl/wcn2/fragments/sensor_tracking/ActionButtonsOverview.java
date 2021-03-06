package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Arrays;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.util.DefinedActionStorage;

class ActionButtonsOverview extends LinearLayout
{
    private Context context;
    private GridLayout container;
    private View noActionsDefinedView;

    private DefinedActionStorage definedActionStorage;
    private String[] definedActions;

    private Button selectedActionButton;

    public ActionButtonsOverview(@NonNull Context context)
    {
        this(context, null);
    }

    public ActionButtonsOverview(@NonNull Context context, AttributeSet attrs)
    {
        this(context, attrs, R.attr.cardViewStyle);
    }

    public ActionButtonsOverview(@NonNull Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.action_buttons_overview, this);

        this.context = context;
        this.container = findViewById(R.id.actions);
        this.noActionsDefinedView = findViewById(R.id.no_actions_defined);
        this.definedActionStorage = DefinedActionStorage.getInstance(this.context);
    }

    void show()
    {
        setVisibility(View.VISIBLE);
        updateViews();
    }

    void updateViews()
    {
        String[] newDefinedActions = this.definedActionStorage.getDefinedActions();
        if (!Arrays.equals(this.definedActions, newDefinedActions))
        {
            this.definedActions = newDefinedActions;
            setupActionToggleButtons();
        }
    }

    private void setupActionToggleButtons()
    {
        this.container.removeAllViews();
        this.noActionsDefinedView.setVisibility(0 == this.definedActions.length ?
                View.VISIBLE : View.GONE);

        Handler handler = new Handler();
        new Thread(() -> {
            for (String action : this.definedActions)
            {
                View actionToggleButton = createActionToggleButton(action);
                handler.post(() -> this.container.addView(actionToggleButton));
            }

            if (1 == this.definedActions.length)
            {
                // add extra invisible view so the single button is not spread over the whole screen
                View view = LayoutInflater.from(this.context).inflate(R.layout.action_button,
                        this.container, false);
                view.setVisibility(View.INVISIBLE);
                handler.post(() -> this.container.addView(view));
            }
        }).start();
    }

    private View createActionToggleButton(String action)
    {
        View view = LayoutInflater.from(this.context).inflate(R.layout.action_button,
                this.container, false);
        Button button = view.findViewById(R.id.button);
        button.setText(action);
        button.setOnClickListener(this::actionSelected);
        if (action.equals(MeasurementService.action))
        {
            actionSelected(button);
        }
        return view;
    }

    private void actionSelected(View clickedView)
    {
        deselectActionButton();

        Button actionButton = (Button)clickedView;
        if (actionButton == this.selectedActionButton)
        {
            this.selectedActionButton = null;
            MeasurementService.action = "";
        }
        else
        {
            this.selectedActionButton = actionButton;
            actionButton.getBackground().setColorFilter(this.context.getResources()
                    .getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
            MeasurementService.action = this.selectedActionButton.getText().toString();
        }
    }

    private void deselectActionButton()
    {
        if (null != this.selectedActionButton)
        {
            this.selectedActionButton.getBackground().clearColorFilter();
        }
    }

    void hide()
    {
        deselectActionButton();
        setVisibility(View.GONE);
    }
}
