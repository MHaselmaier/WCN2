package de.hs_kl.wcn2.fragments.sensor_tracking;

import android.content.Context;
import android.graphics.PorterDuff;
import android.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.util.DefinedActionStorage;

public class ActionButtonsOverview
{
    private Context context;
    private View root;
    private GridLayout container;

    private DefinedActionStorage definedActionStorage;
    private String[] definedActions;

    private Button selectedActionButton;

    public ActionButtonsOverview(Context context, View root)
    {
        this.context = context;
        this.root = root;
        this.container = this.root.findViewById(R.id.actions);
        this.definedActionStorage = DefinedActionStorage.getInstance(this.context);
    }

    public void show()
    {
        this.root.setVisibility(View.VISIBLE);
        updateViews();
    }

    public void updateViews()
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

        if (0 == this.definedActions.length)
        {
            LayoutInflater.from(this.context).inflate(R.layout.empty_actions, this.container);
            return;
        }

        for (String action: this.definedActions)
        {
            this.container.addView(createActionToggleButton(action));
        }

        if (1 == this.definedActions.length)
        {
            // add extra invisible view so the single button is not spread over the whole screen
            View view = LayoutInflater.from(this.context).inflate(R.layout.action_button,
                    this.container, false);
            view.setVisibility(View.INVISIBLE);
            this.container.addView(view);
        }
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

    public void hide()
    {
        deselectActionButton();
        this.root.setVisibility(View.GONE);
    }
}
