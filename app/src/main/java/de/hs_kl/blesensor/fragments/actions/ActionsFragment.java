package de.hs_kl.blesensor.fragments.actions;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.hs_kl.blesensor.R;

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

        return view;
    }
}