package com.afitzwa.android.popularmovies.app;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

public class MainActivity extends AppCompatActivity
        implements MoviesFragment.OnMovieSelectedListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private boolean mTwoPane;

    private static MoviesFragment mMoviesFragment = null;
    private static DetailFragment mDetailFragment = null;

    private boolean mIsDetailVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.pref_movies, false);

        // Our XML will inflate the fragment_posters into our main activity
        setContentView(R.layout.activity_main);

        // Find the Movies fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        mMoviesFragment = (MoviesFragment) fragmentManager.findFragmentById(R.id.fragment_posters);
        mMoviesFragment.setOnMovieSelectedListener(this);

        // If we find the movie_details_container in our view, we're running in a two-pane mode
        if (findViewById(R.id.movie_details_container) != null) {
            mTwoPane = true;

            // If we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null && mDetailFragment != null) {
                return;
            }

            // Create the detail fragment
            mDetailFragment = new DetailFragment();

            // Start the fragment transaction and add it to the view
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction
                    .add(R.id.movie_details_container, mDetailFragment)
                    .commit();
        } else {
            mTwoPane = false;
        }
        getSupportActionBar().setElevation(0f);
    }

    /**
     * Starts the detail activity, or update the detail fragment on wider screen devices.
     *
     * @param movieDbId Movie Id from themoviedb.org.
     */
    public void onMovieSelected(int movieDbId) {
        Log.v(LOG_TAG, "onMovieSelected");
        if (mTwoPane) {
            if (!mIsDetailVisible) {
                // Bring it into visibility
                findViewById(R.id.movie_details_container).setVisibility(View.VISIBLE);
                GridView gridView = (GridView) findViewById(android.R.id.list);
                gridView.setNumColumns(3);
            }
            mDetailFragment.updateDetailView(movieDbId);
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(DetailFragment.MOVIE_DB_ID, movieDbId);
            startActivity(intent);
        }
    }
}
