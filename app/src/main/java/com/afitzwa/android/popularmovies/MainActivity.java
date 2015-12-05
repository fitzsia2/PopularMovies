package com.afitzwa.android.popularmovies;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity
        implements MoviesFragment.OnMovieSelectedListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private MoviesFragment mMoviesFragment = null;
    private DetailFragment mDetailFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.pref_movies, false);
        setContentView(R.layout.activity_main);

        // Check whether the activity is using the layout version with
        // the activity_main_frame FrameLayout. If so, we must add the first fragment
        if (findViewById(R.id.activity_main_frame) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create the main activity fragment
            mMoviesFragment = new MoviesFragment();
            mMoviesFragment.setOnMovieSelectedListener(this);

            // Start the fragment transaction and add it to the view
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction
                    .add(R.id.activity_main_frame, mMoviesFragment)
                    .commit();
        }
    }

    /**
     * Starts the detail fragment view, or updates it on wider screen devices.
     *
     * @param movieDbId Movie Id from themoviedb.org.
     */
    public void onMovieSelected(int movieDbId) {
        Log.v(LOG_TAG, "onMovieSelected");
        FragmentManager fragmentManager = getSupportFragmentManager();
        mDetailFragment = (DetailFragment) fragmentManager.findFragmentById(R.id.detail_fragment);

        if (mDetailFragment != null) {
            mDetailFragment.updateDetailView(movieDbId);
        } else {
            // Start the fragment transaction and add it to the view
            mDetailFragment = DetailFragment.newInstance(movieDbId);
            Bundle bundle = new Bundle();
            bundle.putInt(DetailFragment.MOVIE_DB_ID, movieDbId);
            mDetailFragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.activity_main_frame, mDetailFragment)
                    .addToBackStack("main")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }
}
