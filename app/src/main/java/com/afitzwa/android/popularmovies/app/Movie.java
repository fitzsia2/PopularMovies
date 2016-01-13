package com.afitzwa.android.popularmovies;

import android.graphics.drawable.Drawable;

import com.orm.SugarRecord;

/**
 * Keeps all the information about movies that we need.
 * Created by AndrewF on 10/18/2015.
 */
public class Movie extends SugarRecord<Movie> {
    String title = null;
    int movieDbId;
    String posterUrl = null;
    String overview = null;
    String year = null;
    String length = null;
    Drawable posterDrawable = null;
    private String voteAverage = null;

    public Movie(int movieDbId, String title, Drawable posterDrawable, String year, String length, String voteAverage, String overview) {
        super();
        this.title = title;
        this.posterDrawable = posterDrawable;
        this.year = year;
        this.length = length;
        this.movieDbId = movieDbId;
        this.voteAverage = voteAverage;
        this.overview = overview;
    }
}
