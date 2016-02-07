package com.afitzwa.android.popularmovies.app;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * Loads all relevant settings fragments.
 */
public class SettingsActivity extends PreferenceActivity {
    final String LOG_TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate()");

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onSaveInstanceState()");
        // Save the user's current game state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onRestoreInstanceState()");
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
    }
}
