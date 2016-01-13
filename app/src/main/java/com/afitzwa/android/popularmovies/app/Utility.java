package com.afitzwa.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by AndrewF on 1/8/2016.
 */
public class Utility {

    public String GetSortByOption(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(context.getString(R.string.pref_sort_by_key), context.getString(R.string.pref_sort_by_default));
    }
}
