package com.afitzwa.android.popularmovies.app;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import junit.framework.Assert;

/**
 * Manages launching the detail activity
 */
public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            Long l = getIntent().getLongExtra(DetailFragment.MOVIE_DB_ID, 0);
            arguments.putLong(DetailFragment.MOVIE_DB_ID, l);

            DetailFragment fragment = new DetailFragment();
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
