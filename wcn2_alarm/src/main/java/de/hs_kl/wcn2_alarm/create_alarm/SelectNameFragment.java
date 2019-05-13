package de.hs_kl.wcn2_alarm.create_alarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.zip.CheckedOutputStream;

import de.hs_kl.wcn2_alarm.AlarmStorage;
import de.hs_kl.wcn2_alarm.R;

public class SelectNameFragment extends Fragment
{
    private EditText name;
    private String originalName;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle b)
    {
        View view = inflater.inflate(R.layout.select_name, container, false);

        this.name = view.findViewById(R.id.name);

        Bundle arguments = getArguments();
        if (null != arguments && CreateAlarmActivity.MODE_EDIT.equals(
                arguments.getString(CreateAlarmActivity.EXTRA_MODE)))
        {
            this.name.append(arguments.getString(CreateAlarmActivity.EXTRA_NAME, ""));
            this.originalName = arguments.getString(CreateAlarmActivity.EXTRA_ORIGINAL_NAME);
        }

        Button next = view.findViewById(R.id.next);
        next.setOnClickListener((v) -> handleOnNextClicked());

        return view;
    }

    private void handleOnNextClicked()
    {
        String enteredName = this.name.getText().toString().trim();
        if (enteredName.isEmpty())
        {
            Toast.makeText(getContext(), R.string.no_name_provided, Toast.LENGTH_LONG).show();
            return;
        }

        if (getArguments().getString(CreateAlarmActivity.EXTRA_MODE)
            .equals(CreateAlarmActivity.MODE_CREATE) || !enteredName.equals(this.originalName))
        {
            if (AlarmStorage.getInstance(getContext()).isSaved(enteredName))
            {
                Toast.makeText(getContext(), R.string.name_already_used, Toast.LENGTH_LONG).show();
                return;
            }
        }

        Intent intent = new Intent(getActivity().getBaseContext(), CreateAlarmActivity.class);
        intent.putExtra(CreateAlarmActivity.EXTRA_SENDER, getClass().getSimpleName());
        intent.putExtra(CreateAlarmActivity.EXTRA_NAME, enteredName);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().startActivity(intent);
    }
}
