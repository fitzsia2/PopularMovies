package com.afitzwa.android.popularmovies.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import junit.framework.Assert;

/**
 * Activity used to for loading favorite movies
 */
public class FavoriteMoviesActivity extends AppCompatActivity
        implements FavoriteMoviesFragment.OnMovieSelectedListener {

    private static final String LOG_TAG = FavoriteMoviesActivity.class.getSimpleName();
    private static final String DF_TAG = "DFTAG";

    private boolean mTwoPane = false;

    private static FavoriteMovieDetailFragment mFavoriteMovieDetailFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Our XML will inflate the fragment_posters into our main activity
        setContentView(R.layout.activity_main_favorites);

        // Find the Movies fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FavoriteMoviesFragment fmv = (FavoriteMoviesFragment) fragmentManager.findFragmentById(R.id.fragment_favorite_movies);
        fmv.setOnMovieSelectedListener(this);

        // If we find the movie_details_container in our view, we're running in a two-pane mode
        if (findViewById(R.id.movie_details_container) != null) {
            mTwoPane = true;

            // If we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null && mFavoriteMovieDetailFragment != null) {
                return;
            }

            // Create the detail fragment
            mFavoriteMovieDetailFragment = new FavoriteMovieDetailFragment();

            // Start the fragment transaction and add it to the view
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction
                    .add(R.id.movie_details_container, mFavoriteMovieDetailFragment, DF_TAG)
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
            Bundle args = new Bundle();
            args.putLong(DetailFragment.MOVIE_DB_ID, movieDbId);
            mFavoriteMovieDetailFragment = new FavoriteMovieDetailFragment();
            mFavoriteMovieDetailFragment.setArguments(args);
            FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
            fm.replace(R.id.movie_details_container, mFavoriteMovieDetailFragment, DF_TAG);
            fm.addToBackStack(null);
            fm.commit();
        } else {
            Intent intent = new Intent(this, FavoriteMovieDetailActivity.class)
                    .putExtra(FavoriteMovieDetailFragment.MOVIE_DB_ID, movieDbId);
            startActivity(intent);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy()");
    }
}
