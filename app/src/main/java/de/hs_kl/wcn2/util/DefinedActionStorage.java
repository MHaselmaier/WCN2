package de.hs_kl.wcn2.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DefinedActionStorage
{
    private static List<String> actions;

    public static String[] getDefinedActions(Context context)
    {
        if (null == DefinedActionStorage.actions)
        {
            Object[] entries = context.getSharedPreferences(Constants.DEFINED_ACTIONS,
                    Context.MODE_PRIVATE).getAll().entrySet().toArray();
            Arrays.sort(entries, new Comparator<Object>()
            {
                @Override
                public int compare(Object o1, Object o2)
                {
                    return ((Map.Entry<String, Integer>)o1).getValue() - ((Map.Entry<String, Integer>)o2).getValue();
                }
            });

            DefinedActionStorage.actions = new ArrayList<>();
            for (Object entry: entries)
            {
                DefinedActionStorage.actions.add(((Map.Entry<String, Integer>)entry).getKey());
            }
        }
        return DefinedActionStorage.actions.toArray(new String[DefinedActionStorage.actions.size()]);
    }

    public static void addAction(Context context, String action)
    {
        getDefinedActions(context);
        if (DefinedActionStorage.actions.contains(action))
        {
            return;
        }
        DefinedActionStorage.actions.add(action);

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.DEFINED_ACTIONS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(action, DefinedActionStorage.actions.size() - 1);
        editor.apply();
    }

    public static void removeAction(Context context, String action)
    {
        getDefinedActions(context);
        int index = DefinedActionStorage.actions.indexOf(action);
        DefinedActionStorage.actions.remove(action);

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.DEFINED_ACTIONS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(action);
        moveActionsUpStartingFrom(index, editor);
        editor.apply();
    }

    private static void moveActionsUpStartingFrom(int startIndex, SharedPreferences.Editor editor)
    {
        if (0 > startIndex || DefinedActionStorage.actions.size() <= startIndex) return;

        for (int i = startIndex; DefinedActionStorage.actions.size() > i; ++i)
        {
            editor.putInt(DefinedActionStorage.actions.get(i), i);
        }
    }
}
