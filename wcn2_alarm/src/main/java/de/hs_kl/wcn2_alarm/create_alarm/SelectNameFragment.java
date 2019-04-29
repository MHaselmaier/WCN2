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

import de.hs_kl.wcn2_alarm.AlarmStorage;
import de.hs_kl.wcn2_alarm.R;

public class SelectNameFragment extends Fragment
{
    private EditText name;

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

        Button next = view.findViewById(R.id.next);
        next.setOnClickListener((v) -> handleOnNextClicked());

        return view;
    }

    private void handleOnNextClicked()
    {
        String enteredName = this.name.getText().toString().trim();
        if (enteredName.isEmpty())
        {
            Toast.makeText(getContext(), "A name must be provided!", Toast.LENGTH_LONG).show();
            return;
        }

        if (AlarmStorage.getInstance(getContext()).isSaved(enteredName))
        {
            Toast.makeText(getContext(), "This name is already in use!", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(getActivity().getBaseContext(), CreateAlarmActivity.class);
        intent.putExtra(CreateAlarmActivity.EXTRA_SENDER, getClass().getSimpleName());
        intent.putExtra(CreateAlarmActivity.EXTRA_NAME, enteredName);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().startActivity(intent);
    }
}
