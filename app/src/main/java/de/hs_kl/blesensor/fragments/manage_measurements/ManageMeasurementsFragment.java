package de.hs_kl.blesensor.fragments.manage_measurements;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import de.hs_kl.blesensor.R;

public class ManageMeasurementsFragment extends Fragment
{
    private ListView measurements;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = getActivity().getLayoutInflater().inflate(R.layout.manage_measurement, container, false);

        this.measurements = view.findViewById(R.id.measurements);
        this.measurements.setAdapter(new MeasurementAdapter(getActivity(), inflater));

        return view;
    }
}
