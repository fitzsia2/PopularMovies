package com.afitzwa.android.popularmovies.app;

import java.util.List;

/**
 * Used to pass Movie information between tasks and fragments
 */
public class MovieInfo {
    Long movieDbId;
    String title;
    String releaseDate;
    String runtime;
    String rating;
    String overview;
    String posterUrl;
    List<Trailer> trailers;
    List<Review> reviews;

    public static class Trailer {
        String mUrl;
        String mName;

        public Trailer(String desc, String link) {
            mUrl = link;
            mName = desc;
        }
    }

    public static class Review {
        String mAuthor;
        String mReview;

        public Review(String author, String review) {
            mAuthor = author;
            mReview = review;
        }
    }

    /**
     * Contains all the information needed by the task for a given movie.
     */
    public void MovieInfo() {
    }
}
