package de.hs_kl.wcn2.fragments.actions;

import android.app.Dialog;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.util.DefinedActionStorage;

public class ActionsFragment extends Fragment
{
    private View noActionsDefinedView;
    private LinearLayout actionList;
    private DefinedActionStorage definedActions;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        this.definedActions = DefinedActionStorage.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle b)
    {
        View view = inflater.inflate(R.layout.actions, container, false);

        this.noActionsDefinedView = view.findViewById(R.id.no_actions_defined);
        this.actionList = view.findViewById(R.id.actions);

        Dialog createActionDialog = CreateActionDialog.buildCreateActionDialog(this);
        ImageButton add = view.findViewById(R.id.add);
        add.setOnClickListener((v) -> createActionDialog.show());

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        loadActionViews();
    }

    public void loadActionViews()
    {
        this.actionList.removeAllViews();
        this.noActionsDefinedView.setVisibility(View.VISIBLE);

        Handler handler = new Handler();
        new Thread(() -> {
            String[] actions = this.definedActions.getDefinedActions();
            if (0 < actions.length)
            {
                handler.post(() -> this.noActionsDefinedView.setVisibility(View.GONE));
            }

            for (String action: actions)
            {
                View actionView = createActionView(action);
                handler.post(() -> this.actionList.addView(actionView));
            }
        }).start();
    }

    private View createActionView(String action)
    {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View actionView = inflater.inflate(R.layout.action, this.actionList, false);

        TextView label = actionView.findViewById(R.id.label);
        label.setText(action);

        ImageButton up = actionView.findViewById(R.id.up);
        up.setOnClickListener((v) ->
        {
            this.definedActions.moveActionUp(action);
            loadActionViews();
        });

        ImageButton down = actionView.findViewById(R.id.down);
        down.setOnClickListener((v) ->
        {
            this.definedActions.moveActionDown(action);
            loadActionViews();
        });

        ImageButton remove = actionView.findViewById(R.id.remove);
        remove.setOnClickListener((v) ->
        {
            this.definedActions.removeAction(action);
            loadActionViews();
        });

        return actionView;
    }
}