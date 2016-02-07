package com.afitzwa.android.popularmovies.app;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import junit.framework.Assert;

/**
 *
 */
public class MainActivity extends AppCompatActivity
        implements MoviesFragment.OnMovieSelectedListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String DF_TAG = "DF_TAG";

    private boolean mTwoPane;

    private static DetailFragment mDetailFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get our preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref_movies, false);

        // Our XML will inflate the fragment_posters into our main activity
        setContentView(R.layout.activity_main);

        // Find the Movies fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        MoviesFragment moviesFragment = (MoviesFragment) fragmentManager.findFragmentById(R.id.fragment_movies);
        moviesFragment.setOnMovieSelectedListener(this);

        // If we find the movie_details_container in our view, we're running in a two-pane mode
        if (findViewById(R.id.movie_details_container) != null) {
            mTwoPane = true;

            // Create a new detail fragment with the movieDbId
            mDetailFragment = new DetailFragment();

            // Start the fragment transaction and add it to the view
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction
                    .add(R.id.movie_details_container, mDetailFragment, DF_TAG)
                    .commit();
        } else {
            mTwoPane = false;
        }
        ActionBar ab = getSupportActionBar();
        Assert.assertNotNull(ab);
        ab.setElevation(0f);
    }

    /**
     * Starts the detail activity, or update the detail fragment on wider screen devices.
     */
    public void onMovieSelected(Long movieDbId) {
        Log.v(LOG_TAG, "onMovieSelected::" + movieDbId.toString());
        if (mTwoPane) {

            // Create a new detail fragment with the movieDbId
            mDetailFragment = new DetailFragment();
            Bundle args = new Bundle();
            args.putLong(DetailFragment.MOVIE_DB_ID, movieDbId);
            mDetailFragment.setArguments(args);

            // Replace the current fragment
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.movie_details_container, mDetailFragment, DF_TAG);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(DetailFragment.MOVIE_DB_ID, movieDbId);
            startActivity(intent);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy()");
    }
}
