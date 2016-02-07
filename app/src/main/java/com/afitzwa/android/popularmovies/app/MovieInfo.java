package com.afitzwa.android.popularmovies.app;

import java.util.ArrayList;
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

    public MovieInfo() {
        trailers = new ArrayList<>();
        reviews = new ArrayList<>();
    }

    public static class Trailer {
        String mUrl;
        String mName;

        /**
         * @param desc Description of the trailer
         * @param link to the youtube video
         */
        public Trailer(String desc, String link) {
            mUrl = link;
            mName = desc;
        }
    }

    public static class Review {
        String mAuthor;
        String mReview;

        /**
         * @param author of the review
         * @param review of the movie
         */
        public Review(String author, String review) {
            mAuthor = author;
            mReview = review;
        }
    }

    public void clear() {
        movieDbId = null;
        title = null;
        releaseDate = null;
        runtime = null;
        rating = null;
        overview = null;
        posterUrl = null;
        trailers.clear();
        reviews.clear();
    }
}
