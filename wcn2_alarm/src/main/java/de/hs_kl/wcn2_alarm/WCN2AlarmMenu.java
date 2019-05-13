package de.hs_kl.wcn2_alarm;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import de.hs_kl.wcn2_alarm.create_alarm.CreateAlarmActivity;

public class WCN2AlarmMenu extends PopupMenu implements PopupMenu.OnMenuItemClickListener
{
    private Context context;
    private String alarm;

    public WCN2AlarmMenu(Context context, View anchor, String alarm)
    {
        super(context, anchor);

        this.context = context;
        this.alarm = alarm;

        getMenuInflater().inflate(R.menu.alarm_menu, getMenu());
        setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.edit:
            Intent intent = new Intent(this.context, CreateAlarmActivity.class);
            intent.putExtra(CreateAlarmActivity.EXTRA_MODE, CreateAlarmActivity.MODE_EDIT);
            intent.putExtra(CreateAlarmActivity.EXTRA_NAME, this.alarm);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            this.context.startActivity(intent);
            return true;
        case R.id.delete:
            AlarmStorage.getInstance(this.context).deleteAlarm(this.alarm);
            return true;
        }

        return false;
    }
}
