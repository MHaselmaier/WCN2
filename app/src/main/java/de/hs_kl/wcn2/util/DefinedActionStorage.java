package de.hs_kl.wcn2.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DefinedActionStorage
{
    private static DefinedActionStorage instance;

    private SharedPreferences actions;
    private List<String> cachedData;

    private DefinedActionStorage(Context context)
    {
        this.actions = context.getSharedPreferences(Constants.DEFINED_ACTIONS,
                Context.MODE_PRIVATE);

        Object[] entries = this.actions.getAll().entrySet().toArray();
        Arrays.sort(entries, (o1, o2) ->
            ((Map.Entry<String, Integer>)o1).getValue() - ((Map.Entry<String, Integer>)o2).getValue()
        );

        this.cachedData = new ArrayList<>();
        for (Object entry: entries)
        {
            this.cachedData.add(((Map.Entry<String, Integer>)entry).getKey());
        }
    }

    public String[] getDefinedActions()
    {
        return this.cachedData.toArray(new String[0]);
    }

    public void addAction(String action)
    {
        if (this.cachedData.contains(action)) return;

        this.cachedData.add(action);

        SharedPreferences.Editor editor = this.actions.edit();
        editor.putInt(action, this.cachedData.size() - 1);
        editor.apply();
    }

    public void removeAction(String action)
    {
        int index = this.cachedData.indexOf(action);
        this.cachedData.remove(action);

        SharedPreferences.Editor editor = this.actions.edit();
        editor.remove(action);
        moveActionsUpStartingFrom(index, editor);
        editor.apply();
    }

    private void moveActionsUpStartingFrom(int startIndex, SharedPreferences.Editor editor)
    {
        if (0 > startIndex || this.cachedData.size() <= startIndex) return;

        for (int i = startIndex; this.cachedData.size() > i; ++i)
        {
            editor.putInt(this.cachedData.get(i), i);
        }
    }

    public void moveActionUp(String action)
    {
        int actionIndex = this.cachedData.indexOf(action);
        if (0 == actionIndex) return;

        this.cachedData.set(actionIndex, this.cachedData.get(actionIndex - 1));
        this.cachedData.set(actionIndex - 1, action);

        SharedPreferences.Editor editor = this.actions.edit();
        editor.putInt(action, actionIndex - 1);
        editor.putInt(this.cachedData.get(actionIndex), actionIndex);
        editor.apply();
    }

    public void moveActionDown(String action)
    {
        int actionIndex = this.cachedData.indexOf(action);
        if (this.cachedData.size() - 1 == actionIndex) return;

        this.cachedData.set(actionIndex, this.cachedData.get(actionIndex + 1));
        this.cachedData.set(actionIndex + 1, action);

        SharedPreferences.Editor editor = this.actions.edit();
        editor.putInt(action, actionIndex + 1);
        editor.putInt(this.cachedData.get(actionIndex), actionIndex);
        editor.apply();
    }

    public static DefinedActionStorage getInstance(Context context)
    {
        if (null == DefinedActionStorage.instance)
        {
            DefinedActionStorage.instance = new DefinedActionStorage(context);
        }

        return DefinedActionStorage.instance;
    }
}
