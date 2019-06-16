package de.hs_kl.wcn2_alarm.create_alarm;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Map;
import java.util.TreeMap;

import de.hs_kl.wcn2_alarm.R;

public class SelectSoundFragment extends Fragment
{
    private RadioGroup sounds;
    private Uri sound;

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Map<String, Uri> systemRingtones;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle b)
    {
        View view = inflater.inflate(R.layout.select_sound, container, false);

        this.sounds = view.findViewById(R.id.sounds);

        showSounds();

        Bundle arguments = getArguments();
        if (null != arguments && CreateAlarmActivity.MODE_EDIT.equals(
                arguments.getString(CreateAlarmActivity.EXTRA_MODE)))
        {
            this.sound = arguments.getParcelable(CreateAlarmActivity.EXTRA_SOUND);
            selectSound(this.sound);
        }

        Button next = view.findViewById(R.id.next);
        next.setOnClickListener((v) -> handleOnNextClicked());

        return view;
    }

    private void showSounds()
    {
        loadSystemRingtones();

        this.sounds.removeAllViews();
        for (Map.Entry<String, Uri> sound: this.systemRingtones.entrySet())
        {
            RadioButton button = new RadioButton(getContext());
            button.setText(sound.getKey());
            button.setOnClickListener((v) ->
            {
                this.mediaPlayer.stop();
                this.mediaPlayer = MediaPlayer.create(getContext(), sound.getValue());
                this.mediaPlayer.setLooping(false);
                this.mediaPlayer.start();
            });
            this.sounds.addView(button);
        }
        ((RadioButton)this.sounds.getChildAt(0)).setChecked(true);
    }

    private void loadSystemRingtones()
    {
        RingtoneManager manager = new RingtoneManager(getContext());
        manager.setType(RingtoneManager.TYPE_ALL);
        Cursor cursor = manager.getCursor();

        this.systemRingtones = new TreeMap<>();
        while (cursor.moveToNext())
        {
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String notificationUri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" +
                    cursor.getString(RingtoneManager.ID_COLUMN_INDEX);

            this.systemRingtones.put(notificationTitle, Uri.parse(notificationUri));
        }
    }

    private void selectSound(Uri soundTitle)
    {
        for (int i = 0; this.sounds.getChildCount() > i; ++i)
        {
            RadioButton button = (RadioButton)this.sounds.getChildAt(i);
            if (this.systemRingtones.get(button.getText().toString()).equals(soundTitle))
            {
                this.sounds.check(button.getId());
                return;
            }
        }
    }

    private void handleOnNextClicked()
    {
        Intent intent = new Intent(getActivity().getBaseContext(), CreateAlarmActivity.class);
        intent.putExtra(CreateAlarmActivity.EXTRA_SENDER, getClass().getSimpleName());
        intent.putExtra(CreateAlarmActivity.EXTRA_SOUND, this.sound);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().startActivity(intent);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        this.mediaPlayer.stop();
    }
}
