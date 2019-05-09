package de.hs_kl.wcn2_alarm.create_alarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hs_kl.wcn2_alarm.R;

public class SelectThresholdsFragment extends Fragment
{
    private LinearLayout thresholds;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle b)
    {
        View view = inflater.inflate(R.layout.select_thresholds, container, false);

        this.thresholds = view.findViewById(R.id.thresholds);

        ImageButton addThreshold = view.findViewById(R.id.add_threshold);
        addThreshold.setOnClickListener((v) -> createAlarmThreshold(inflater));

        createAlarmThreshold(inflater);

        Button next = view.findViewById(R.id.next);
        next.setOnClickListener((v) -> handleOnNextClicked());

        return view;
    }

    private void createAlarmThreshold(LayoutInflater inflater)
    {
        View threshold = inflater.inflate(R.layout.alarm_threshold, this.thresholds, false);

        String[] units = getResources().getStringArray(R.array.units);
        TextView unit = threshold.findViewById(R.id.unit);
        Spinner type = threshold.findViewById(R.id.type);
        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                unit.setText(units[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {}
        });

        EditText input = threshold.findViewById(R.id.threshold);
        input.requestFocus();

        this.thresholds.addView(threshold);
    }

    private void handleOnNextClicked()
    {
        if (!isAtLeastOneThresholdSet())
        {
            Toast.makeText(getContext(), R.string.no_thresholds_defined, Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = createIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().startActivity(intent);
    }

    private boolean isAtLeastOneThresholdSet()
    {
        for (int i = 0; this.thresholds.getChildCount() > i; ++i)
        {
            EditText threshold = this.thresholds.getChildAt(i).findViewById(R.id.threshold);
            if (!threshold.getText().toString().isEmpty()) return true;
        }
        return false;
    }

    private Intent createIntent()
    {
        List<String> allTypes = Arrays.asList(getResources().getStringArray(R.array.types));
        List<String> allOperators = Arrays.asList(getResources().getStringArray(R.array.operators));

        ArrayList<Integer> types = new ArrayList<>();
        ArrayList<Integer> operators = new ArrayList<>();
        ArrayList<Float> values = new ArrayList<>();
        for (int i = 0; this.thresholds.getChildCount() > i; ++i)
        {
            View thresholdRoot = this.thresholds.getChildAt(i);
            EditText threshold = thresholdRoot.findViewById(R.id.threshold);

            if (threshold.getText().toString().isEmpty()) continue;

            values.add(Float.parseFloat(threshold.getText().toString()));
            Spinner type = thresholdRoot.findViewById(R.id.type);
            types.add(allTypes.indexOf(type.getSelectedItem().toString()));
            Spinner operator = thresholdRoot.findViewById(R.id.operator);
            operators.add(allOperators.indexOf(operator.getSelectedItem().toString()));
        }

        Intent intent = new Intent(getActivity().getBaseContext(), CreateAlarmActivity.class);
        intent.putExtra(CreateAlarmActivity.EXTRA_SENDER, getClass().getSimpleName());
        intent.putExtra(CreateAlarmActivity.EXTRA_TYPES, types);
        intent.putExtra(CreateAlarmActivity.EXTRA_OPERATORS, operators);
        intent.putExtra(CreateAlarmActivity.EXTRA_VALUES, values);

        return intent;
    }
}
