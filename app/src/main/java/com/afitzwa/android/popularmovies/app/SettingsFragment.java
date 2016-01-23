package com.afitzwa.android.popularmovies.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Fragment based settings menu.
 */
public class SettingsFragment extends PreferenceFragment {
    final String LOG_TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate()");
        Activity currentActivity = getActivity();
        Resources res = currentActivity.getResources();
        addPreferencesFromResource(R.xml.pref_movies);
        Preference sortByPref = findPreference(res.getString(R.string.pref_sort_by_key));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(currentActivity);
        sortByPref.setSummary(prefs.getString(sortByPref.getKey(), res.getString(R.string.pref_sort_by_default)));
    }

    SharedPreferences.OnSharedPreferenceChangeListener mListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    // listener implementation
                    Log.v(LOG_TAG, "onSharedPreferenceChanged()");

                    if (key.equalsIgnoreCase("pref_sortBy")) {
                        Preference sortByPref = findPreference(key);
                        sortByPref.setSummary(prefs.getString(key, ""));
                    }
                }
            };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onSaveInstanceState()");
        // Save the user's current game state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "onResume()");
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(LOG_TAG, "onPause()");
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onDestroy() {
        super.onResume();
        Log.v(LOG_TAG, "onDestroy()");
    }

}
