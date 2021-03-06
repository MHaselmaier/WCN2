package de.hs_kl.wcn2.fragments.actions;

import android.app.Activity;
import android.app.Dialog;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.util.DefinedActionStorage;

class CreateActionDialog
{
    static Dialog buildCreateActionDialog(final ActionsFragment fragment)
    {
        final Activity activity = fragment.getActivity();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

        View dialogView = activity.getLayoutInflater().inflate(R.layout.action_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText newAction = dialogView.findViewById(R.id.action);
        dialogBuilder.setPositiveButton(R.string.ok, (dialog, w) ->
        {
            dialog.dismiss();

            String action = newAction.getText().toString().trim();
            if (0 < action.length())
            {
                DefinedActionStorage.getInstance(activity).addAction(action);
                fragment.loadActionViews();
            }
        });

        dialogBuilder.setNegativeButton(R.string.cancel, (dialog, w) -> dialog.dismiss());

        Dialog createActionDialog = dialogBuilder.create();
        final Window window = createActionDialog.getWindow();
        createActionDialog.setOnShowListener((d) ->
        {
            newAction.setText(null);
            newAction.requestFocus();
            if (null != window)
            {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        return createActionDialog;
    }
}