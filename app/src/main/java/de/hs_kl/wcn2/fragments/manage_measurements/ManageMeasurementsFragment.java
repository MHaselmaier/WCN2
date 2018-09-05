package de.hs_kl.wcn2.fragments.manage_measurements;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import de.hs_kl.wcn2.R;

public class ManageMeasurementsFragment extends Fragment
{
    private MeasurementAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.manage_measurement, container, false);

        this.adapter = new MeasurementAdapter(getActivity(), inflater);

        ListView measurements = view.findViewById(R.id.measurements);
        measurements.setAdapter(this.adapter);
        measurements.setEmptyView(view.findViewById(R.id.empty_list_item));

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        if (!hidden)
        {
            this.adapter.loadFiles();
        }
    }
}
