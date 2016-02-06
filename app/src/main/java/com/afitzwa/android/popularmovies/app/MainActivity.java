package com.afitzwa.android.popularmovies.app;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.style.BulletSpan;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import junit.framework.Assert;

/**
 *
 */
public class MainActivity extends AppCompatActivity
        implements MoviesFragment.OnMovieSelectedListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String DF_TAG = "DF_TAG";

    private boolean mIsFavorites = false;
    private int mFragmentToLoad;

    private static final String IS_FAVORITES_KEY = "is favorites";

    private boolean mTwoPane;

    private static MoviesFragment mMoviesFragment = null;
    private static DetailFragment mDetailFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mIsFavorites = savedInstanceState.getBoolean(IS_FAVORITES_KEY, false);
        }

        if (mIsFavorites) {
            mFragmentToLoad = R.id.fragment_favorite_movies;
        } else {
            mFragmentToLoad = R.id.fragment_movies;
        }

        PreferenceManager.setDefaultValues(this, R.xml.pref_movies, false);

        // Our XML will inflate the fragment_posters into our main activity
        setContentView(R.layout.activity_main);

        // Find the Movies fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        mMoviesFragment = (MoviesFragment) fragmentManager.findFragmentById(R.id.fragment_movies);
        mMoviesFragment.setOnMovieSelectedListener(this);

        // If we find the movie_details_container in our view, we're running in a two-pane mode
        if (findViewById(R.id.movie_details_container) != null) {
            mTwoPane = true;

            // If we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (mDetailFragment != null) {
                return;
            }

            // Create the detail fragment indicating whether it should load favorites.
            mDetailFragment = new DetailFragment();
            Bundle args = new Bundle();
            args.putBoolean(IS_FAVORITES_KEY, mIsFavorites);
            mDetailFragment.setArguments(args);

            // Start the fragment transaction and add it to the view
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
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
            // Bring it into visibility
            findViewById(R.id.movie_details_container).setVisibility(View.VISIBLE);

            Bundle args = new Bundle();
            args.putLong(DetailFragment.MOVIE_DB_ID, movieDbId);
            DetailFragment df = new DetailFragment();
            df.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_details_container, df, DF_TAG)
                    .commit();

            // Change number of columns in main view
            GridView gridView = (GridView) findViewById(android.R.id.list);
            gridView.setNumColumns(3);
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
