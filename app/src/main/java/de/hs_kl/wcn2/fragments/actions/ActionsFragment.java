package de.hs_kl.wcn2.fragments.actions;

import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.util.DefinedActionStorage;

public class ActionsFragment extends Fragment
{
    private LinearLayout actionList;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
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
        add.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createActionDialog.show();
            }
        });

        return view;
    }

    public void loadActionViews()
    {
        this.actionList.removeAllViews();

        String[] actions = DefinedActionStorage.getDefinedActions(getActivity());
        if (0 < actions.length)
        {
            for (final String action : actions)
            {
                View actionView = getActivity().getLayoutInflater().inflate(R.layout.action, this.actionList, false);

                TextView label = actionView.findViewById(R.id.label);
                label.setText(action);

                ImageButton up = actionView.findViewById(R.id.up);
                up.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        DefinedActionStorage.moveActionUp(ActionsFragment.this.getActivity(), action);
                        loadActionViews();
                    }
                });

                ImageButton down = actionView.findViewById(R.id.down);
                down.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        DefinedActionStorage.moveActionDown(ActionsFragment.this.getActivity(), action);
                        loadActionViews();
                    }
                });

                ImageButton remove = actionView.findViewById(R.id.remove);
                remove.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        DefinedActionStorage.removeAction(getActivity(), action);
                        loadActionViews();
                    }
                });

                this.actionList.addView(actionView);
            }
        }
        else
        {
            View emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_list_item, this.actionList);
            TextView label = emptyView.findViewById(R.id.label);
            label.setText(R.string.no_actions_defined);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        if (!hidden)
        {
            loadActionViews();
        }
    }
}