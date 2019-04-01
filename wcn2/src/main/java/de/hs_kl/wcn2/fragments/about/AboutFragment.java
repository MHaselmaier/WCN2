package de.hs_kl.wcn2.fragments.about;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.hs_kl.wcn2.R;
import de.hs_kl.wcn2.usage.UsageActivity;

public class AboutFragment extends Fragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle b)
    {
        View view = inflater.inflate(R.layout.about, container, false);

        Button help = view.findViewById(R.id.help_button);
        Intent intent = new Intent(getActivity(), UsageActivity.class);
        help.setOnClickListener((v) -> startActivity(intent));

        return view;
    }
}
