package com.afitzwa.android.popularmovies.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Methods used by multiple classes
 */
public class Utility {

    public String GetSortByOption(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(context.getString(R.string.pref_sort_by_key), context.getString(R.string.pref_sort_by_default));
    }

    public static Intent CreateShareMovieIntent(MovieInfo movieInfo) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        String movieString = movieInfo.title;
        if (movieInfo.trailers.size() > 0)
            movieString += " - " + movieInfo.trailers.get(0).mUrl;
        shareIntent.putExtra(Intent.EXTRA_TEXT, movieString);
        return shareIntent;
    }
}
