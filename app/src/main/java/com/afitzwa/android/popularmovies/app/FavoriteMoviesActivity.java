package com.afitzwa.android.popularmovies.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import junit.framework.Assert;

/**
 * Created by AndrewF on 2/5/2016.
 */
public class FavoriteMoviesActivity extends AppCompatActivity
        implements FavoriteMoviesFragment.OnMovieSelectedListener {

    private static final String LOG_TAG = FavoriteMoviesActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane = false;

    private static FavoriteMoviesFragment mMoviesFragment = null;
//        private static FavoriteMoviesDetailFragment mDetailFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Our XML will inflate the fragment_posters into our main activity
        setContentView(R.layout.activity_main_favorites);

        // Find the Movies fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        mMoviesFragment = (FavoriteMoviesFragment) fragmentManager.findFragmentById(R.id.fragment_favorite_movies);
        mMoviesFragment.setOnMovieSelectedListener(this);

        // If we find the movie_details_container in our view, we're running in a two-pane mode
//        if (findViewById(R.id.movie_details_container) != null) {
//            mTwoPane = true;
//
//            // If we're being restored from a previous state,
//            // then we don't need to do anything and should return or else
//            // we could end up with overlapping fragments.
//            if (savedInstanceState != null && mDetailFragment != null) {
//                return;
//            }
//
//            // Create the detail fragment
//            mDetailFragment = new DetailFragment();
//
//            // Start the fragment transaction and add it to the view
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction
//                    .add(R.id.movie_details_container, mDetailFragment, DETAILFRAGMENT_TAG)
//                    .commit();
//        } else {
//            mTwoPane = false;
//        }
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
                    .replace(R.id.movie_details_container, df, DETAILFRAGMENT_TAG)
                    .commit();

            // Change number of columns in main view
            GridView gridView = (GridView) findViewById(android.R.id.list);
            gridView.setNumColumns(3);
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
