package com.afitzwa.android.popularmovies.app;

import android.graphics.drawable.Drawable;

/**
 * Used for keeping track of details relating to movie posters.
 */
public class Poster {
    int movieDbId;
    String posterUrl;
    Drawable drawable;

    public Poster(int movieDbId, String posterUrl) {
        this.movieDbId = movieDbId;
        this.posterUrl = posterUrl;
    }
}
