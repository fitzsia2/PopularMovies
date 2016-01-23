package com.afitzwa.android.popularmovies.app.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Test module for MovieContract class
 */
public class TestMovieContract extends AndroidTestCase {

    private static final Long TEST_MOVIE_ID = 1419033600L;

    public void testBuildMovieUri() {
        Uri movieUri = MovieContract.MovieEntry.buildMovieUri(TEST_MOVIE_ID);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildMovieUri in " +
                        "MovieContract.",
                movieUri);
        assertEquals("Error: movie id not properly appended to the end of the Uri",
                "" + TEST_MOVIE_ID, movieUri.getLastPathSegment());
        assertEquals("Error: movie id Uri doesn't match our expected result",
                movieUri.toString(),
                "content://com.afitzwa.android.popularmovies.app/movies/1419033600");
    }

    public void testBuildMovieTrailersUri() {
        Uri trailersUri = MovieContract.MovieEntry.buildMovieTrailers(TEST_MOVIE_ID);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildMovieTrailers in " +
                        "MovieContract.",
                trailersUri);
        assertEquals("Error: trailer path not properly appended to the end of the Uri",
                "trailers", trailersUri.getLastPathSegment());
        assertEquals("Error: trailers Uri doesn't match our expected result",
                trailersUri.toString(),
                "content://com.afitzwa.android.popularmovies.app/movies/1419033600/trailers");
    }

    public void testBuildMovieReviewsUri() {
        Uri reviewsUri = MovieContract.MovieEntry.buildMovieReviews(TEST_MOVIE_ID);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildMovieReviews in " +
                        "MovieContract.",
                reviewsUri);
        assertEquals("Error: reviews path not properly appended to the end of the Uri",
                "reviews", reviewsUri.getLastPathSegment());
        assertEquals("Error: trailers Uri doesn't match our expected result",
                reviewsUri.toString(),
                "content://com.afitzwa.android.popularmovies.app/movies/1419033600/reviews");
    }
}
