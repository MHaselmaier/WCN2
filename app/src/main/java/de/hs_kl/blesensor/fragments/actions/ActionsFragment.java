package de.hs_kl.blesensor.fragments.actions;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.hs_kl.blesensor.R;
import de.hs_kl.blesensor.util.DefinedActionStorage;

public class ActionsFragment extends Fragment
{
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

        final LinearLayout actionList = view.findViewById(R.id.actions);
        loadActionViews(actionList);

        ImageButton add = view.findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

                View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.action_dialog, (ViewGroup)getView(), false);
                dialogBuilder.setView(dialogView);

                final EditText newAction = dialogView.findViewById(R.id.action);

                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();

                        String action = newAction.getText().toString().trim();
                        if (0 < action.length())
                        {
                            DefinedActionStorage.addAction(getActivity(), action);
                            loadActionViews(actionList);
                        }
                    }
                });

                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        return view;
    }

    private void loadActionViews(final LinearLayout actionList)
    {
        actionList.removeAllViews();

        String[] actions = DefinedActionStorage.getDefinedActions(getActivity());
        if (0 < actions.length)
        {
            for (final String action : actions)
            {
                final View actionView = getActivity().getLayoutInflater().inflate(R.layout.action, null);

                TextView label = actionView.findViewById(R.id.label);
                label.setText(action);

                ImageButton remove = actionView.findViewById(R.id.remove);
                remove.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        DefinedActionStorage.removeAction(getActivity(), action);
                        loadActionViews(actionList);
                    }
                });

                actionList.addView(actionView);
            }
        }
        else
        {
            View emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_list_item, actionList, false);
            TextView label = emptyView.findViewById(R.id.label);
            label.setText(R.string.no_actions_defined);
            actionList.addView(emptyView);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        if (!hidden)
        {
            loadActionViews((LinearLayout)getView().findViewById(R.id.actions));
        }
    }
}