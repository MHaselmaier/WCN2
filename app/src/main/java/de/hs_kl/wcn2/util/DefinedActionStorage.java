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

    public static void init(Context context)
    {
        if (null != actions) return;

        Object[] entries = context.getSharedPreferences(Constants.DEFINED_ACTIONS,
                Context.MODE_PRIVATE).getAll().entrySet().toArray();
        Arrays.sort(entries, new Comparator<Object>()
        {
            @Override
            public int compare(Object o1, Object o2)
            {
                int index1 = ((Map.Entry<String, Integer>)o1).getValue();
                int index2 = ((Map.Entry<String, Integer>)o2).getValue();
                return index1 - index2;
            }
        });

        DefinedActionStorage.actions = new ArrayList<>();
        for (Object entry: entries)
        {
            DefinedActionStorage.actions.add(((Map.Entry<String, Integer>)entry).getKey());
        }
    }

    public static String[] getDefinedActions()
    {
        return DefinedActionStorage.actions.toArray(new String[DefinedActionStorage.actions.size()]);
    }

    public static void addAction(Context context, String action)
    {
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

    public static void moveActionUp(Context context, String action)
    {
        int actionIndex = DefinedActionStorage.actions.indexOf(action);
        if (0 == actionIndex) return;

        DefinedActionStorage.actions.set(actionIndex, DefinedActionStorage.actions.get(actionIndex - 1));
        DefinedActionStorage.actions.set(actionIndex - 1, action);

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.DEFINED_ACTIONS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(action, actionIndex - 1);
        editor.putInt(DefinedActionStorage.actions.get(actionIndex), actionIndex);
        editor.apply();
    }

    public static void moveActionDown(Context context, String action)
    {
        int actionIndex = DefinedActionStorage.actions.indexOf(action);
        if (DefinedActionStorage.actions.size() - 1 == actionIndex) return;

        DefinedActionStorage.actions.set(actionIndex, DefinedActionStorage.actions.get(actionIndex + 1));
        DefinedActionStorage.actions.set(actionIndex + 1, action);

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.DEFINED_ACTIONS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(action, actionIndex + 1);
        editor.putInt(DefinedActionStorage.actions.get(actionIndex), actionIndex);
        editor.apply();
    }
}
