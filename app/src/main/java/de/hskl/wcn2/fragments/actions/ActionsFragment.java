package de.hskl.wcn2.fragments.actions;

import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.hskl.wcn2.R;
import de.hskl.wcn2.util.DefinedActionStorage;

public class ActionsFragment extends Fragment
{
    private LinearLayout actionList;
    private DefinedActionStorage definedActions;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        this.definedActions = DefinedActionStorage.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = getActivity().getLayoutInflater().inflate(R.layout.actions, container, false);

        this.actionList = view.findViewById(R.id.actions);
        loadActionViews();

        final Dialog createActionDialog = CreateActionDialog.buildCreateActionDialog(this);
        ImageButton add = view.findViewById(R.id.add);
        add.setOnClickListener((v) -> createActionDialog.show());

        return view;
    }

    public void loadActionViews()
    {
        this.actionList.removeAllViews();

        String[] actions = this.definedActions.getDefinedActions();
        if (0 == actions.length)
        {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View emptyView = inflater.inflate(R.layout.empty_list_item, this.actionList);
            TextView label = emptyView.findViewById(R.id.label);
            label.setText(R.string.no_actions_defined);
            return;
        }

        for (final String action: actions)
        {
            this.actionList.addView(createActionView(action));
        }
    }

    private View createActionView(final String action)
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

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        if (hidden) return;

        loadActionViews();
    }
}