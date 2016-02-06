package com.afitzwa.android.popularmovies.app;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import junit.framework.Assert;

/**
 * Detail Activity for favorites
 */
public class FavoriteMovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            Long l = getIntent().getLongExtra(FavoriteMovieDetailFragment.MOVIE_DB_ID, 0);
            arguments.putLong(FavoriteMovieDetailFragment.MOVIE_DB_ID, l);

            FavoriteMovieDetailFragment fragment = new FavoriteMovieDetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_details_container, fragment)
                    .commit();
        }
        ActionBar ab = getSupportActionBar();
        Assert.assertNotNull(ab);
        ab.setElevation(0f);
    }
}
