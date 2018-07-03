package de.hs_kl.blesensor.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class DefinedActionStorage
{
    public static String[] getDefinedActions(Context context)
    {
        Set<String> actions = context.getSharedPreferences(Constants.DEFINED_ACTIONS, Context.MODE_PRIVATE)
                                     .getAll().keySet();
        return actions.toArray(new String[0]);
    }

    public static void addAction(Context context, String action)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.DEFINED_ACTIONS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(action, action);
        editor.commit();
    }

    public static void removeAction(Context context, String action)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.DEFINED_ACTIONS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(action);
        editor.commit();
    }
}
