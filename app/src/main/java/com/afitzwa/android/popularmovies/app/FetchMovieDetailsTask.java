package com.afitzwa.android.popularmovies.app;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afitzwa.android.popularmovies.app.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * Created by AndrewF on 11/13/2015.
 * <p>
 * Handles querying themoviedb.org for information related to a single movie.
 * <p>
 * Specifically gets the runtime and trailer links.
 */
class FetchMovieDetailsTask extends AsyncTask<Long, Void, JSONObject> {
    private final String LOG_TAG = FetchMovieDetailsTask.class.getSimpleName();
    private static final String[] MOVIES_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID
    };
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_DB_ID = 1;

    /**
     * Contains a name and url for a given trailer
     */
    private class Trailer {
        String url;
        String name;
    }

    private class Review {
        String author;
        String review;
    }

    /**
     * Contains all the information needed by the task for a given movie.
     */
    private class MovieDetails {
        String title;
        String releaseDate;
        String runtime;
        String rating;
        String overview;
        String posterUrl;
        List<Trailer> trailers;
        List<Review> reviews;
    }

    private Context mContext;
    private View mRootView;
    private MovieDetails mMovieDetails;
    private long mMovieDbId;

    public FetchMovieDetailsTask(Context context, View rootView) {
        mContext = context;
        mRootView = rootView;
    }

    /**
     * @param params Should be a single integer representing the id of the requested movie
     *               from themoviedb.org's database.
     * @return a JSONObject returned by themoviedb.org's API.
     */
    @Override
    protected JSONObject doInBackground(Long... params) {
        Log.v(LOG_TAG, "Querying MovieDb: " + params[0]);
        mMovieDbId = params[0];

        JSONObject movieJsonObject = null;

        // Retrieve the specific movie JSON from themoviedb.org's Db
        for (Long param : params) {
            try {
                String movieJsonString = getMovieJson(param);
                movieJsonObject = new JSONObject(movieJsonString);
            } catch (JSONException ex) {
                Log.e(LOG_TAG, "JSON Error: ", ex);
            }
        }

        mMovieDetails = getMovieDetails(movieJsonObject);

        // Update the movie entry with the length
        ContentValues cv = new ContentValues();
        cv.put(MovieContract.MovieEntry.COLUMN_LENGTH, mMovieDetails.runtime);
        int count = mContext.getContentResolver().update(
                MovieContract.MovieEntry.CONTENT_URI,
                cv,
                MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID + " = ?",
                new String[]{Long.toString(mMovieDbId)});
        if ((count == -1)) throw new AssertionError();

        // Get the row of Id of our movie for the trailers table and reviews table
        Cursor c = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                MOVIES_COLUMNS,
                MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID + " = " + mMovieDbId,
                null,
                null
        );

        if(c == null)
            return null;

        c.moveToFirst();
        int rowId = c.getInt(COL_MOVIE_ID);
        c.close();

        Log.v(LOG_TAG, "Query returned " + c.getCount() + " rows");

        // Save the trailers
        Vector<ContentValues> trailerCCV = new Vector<>(mMovieDetails.trailers.size());
        for (int ii = 0; ii < mMovieDetails.trailers.size(); ii++) {
            final String URL = mMovieDetails.trailers.get(ii).url;
            final String NAME = mMovieDetails.trailers.get(ii).name;

            cv.clear();
            cv.put(MovieContract.TrailerEntry.COLUMN_MOVIE_KEY, rowId);
            cv.put(MovieContract.TrailerEntry.COLUMN_TRAILER_URL, URL);
            cv.put(MovieContract.TrailerEntry.COLUMN_DESCRIPTION, NAME);
            trailerCCV.add(cv);
        }
        if (trailerCCV.size() > 0) {
            ContentValues[] cvArray = new ContentValues[trailerCCV.size()];
            trailerCCV.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, cvArray);
        }

        // Save the reviews
        Vector<ContentValues> reviewCCV = new Vector<>(mMovieDetails.reviews.size());
        for (int ii = 0; ii < mMovieDetails.reviews.size(); ii++) {
            Review review = mMovieDetails.reviews.get(ii);
            cv.clear();
            cv.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, rowId);
            cv.put(MovieContract.ReviewEntry.COLUMN_USER, review.author);
            cv.put(MovieContract.ReviewEntry.COLUMN_DESCRIPTION, review.review);
            reviewCCV.add(cv);
        }
        if (reviewCCV.size() > 0) {
            ContentValues[] cvArray = new ContentValues[reviewCCV.size()];
            reviewCCV.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, cvArray);
        }

        // Return the JSON
        return movieJsonObject;
    }

    /**
     * Parses the JSON object for runtime and trailer links.
     *
     * @param result JSONObject containing
     */
    @Override
    protected void onPostExecute(JSONObject result) {
        if (result == null) {
            Log.e(LOG_TAG, "Error: Could not get any results");
            return;
        }
        mContext.getContentResolver().notifyChange(MovieContract.MovieEntry.CONTENT_URI, null);
        mContext.getContentResolver().notifyChange(MovieContract.TrailerEntry.CONTENT_URI, null);
        mContext.getContentResolver().notifyChange(MovieContract.ReviewEntry.CONTENT_URI, null);

//        // Get all the information out of the JSON
//        mMovieDetails = getMovieDetails(result);
//
//        // Update the movie entry with the length
//        ContentValues cv = new ContentValues();
//        cv.put(MovieContract.MovieEntry.COLUMN_LENGTH, mMovieDetails.runtime);
//        int count = mContext.getContentResolver().update(
//                MovieContract.MovieEntry.CONTENT_URI,
//                cv,
//                MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID + " = ",
//                new String[]{Long.toString(mMovieDbId)});
//        if ((count == -1)) throw new AssertionError();
//
//        // Get the row of Id of our movie for the trailers table and reviews table
//        Cursor c = mContext.getContentResolver().query(
//                MovieContract.MovieEntry.CONTENT_URI,
//                MOVIES_COLUMNS,
//                MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID + " = " + mMovieDbId,
//                null,
//                null
//                );
//        assert c != null && c.moveToFirst();
//        int rowId = c.getInt(COL_MOVIE_ID);
//
//        Vector<ContentValues> trailerCCV = new Vector<>(mMovieDetails.trailers.size());
//
//        // Creates a TextView for each trailer
//        // Get the layout we want to load our views into
//        LinearLayout ll = (LinearLayout) mRootView.findViewById(R.id.detail_fragment_trailers);
//        for (int ii = 0; ii < mMovieDetails.trailers.size(); ii++) {
//            final String URL = mMovieDetails.trailers.get(ii).url;
//            final String NAME = mMovieDetails.trailers.get(ii).name;
//
//            cv.clear();
//            cv.put(MovieContract.TrailerEntry.COLUMN_MOVIE_KEY, rowId);
//            cv.put(MovieContract.TrailerEntry.COLUMN_TRAILER_URL, URL);
//            cv.put(MovieContract.TrailerEntry.COLUMN_DESCRIPTION, NAME);
//            trailerCCV.add(cv);

//            // Add the view
//            TextView trailer = (TextView) View.inflate(mContext, R.layout.trailer_link_view, null);
//            trailer.setText(name);
//
//            // Set the onClick Listener to pull up the Youtube links
//            trailer.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    Uri builtUri = Uri.parse(URL)
//                            .buildUpon()
//                            .build();
//                    try {
//                        Intent intent = new Intent(Intent.ACTION_VIEW, builtUri);
//                        mContext.startActivity(intent);
//                    } catch (ActivityNotFoundException ex) {
//                        Intent youtubeIntent = new Intent(Intent.ACTION_VIEW,
//                                builtUri);
//                        mContext.startActivity(youtubeIntent);
//                    }
//                }
//            });
//            ll.addView(trailer);
//        }
//        if (trailerCCV.size() > 0) {
//            ContentValues[] cvArray = new ContentValues[trailerCCV.size()];
//            trailerCCV.toArray(cvArray);
//            mContext.getContentResolver().bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, cvArray);
//        }
//
//
//        Vector<ContentValues> reviewCCV = new Vector<>(mMovieDetails.reviews.size());
//
//        // Creates a TextView for each review
//        // Get the layout we want to load our views into
//        ll = (LinearLayout) mRootView.findViewById(R.id.detail_fragment_reviews);
//        for (int ii = 0; ii < mMovieDetails.reviews.size(); ii++) {
//            Review review = mMovieDetails.reviews.get(ii);
//            cv.clear();
//            cv.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, rowId);
//            cv.put(MovieContract.ReviewEntry.COLUMN_USER, review.author);
//            cv.put(MovieContract.ReviewEntry.COLUMN_DESCRIPTION, review.review);
//            reviewCCV.add(cv);
//
//            LinearLayout reviewLayout = (LinearLayout) View.inflate(mContext, R.layout.review_view, null);
//            ((TextView) reviewLayout.findViewById(R.id.detail_view_author_text_view)).setText(review.author);
//            ((TextView) reviewLayout.findViewById(R.id.movie_review_text_view)).setText(review.review);
//            ll.addView(reviewLayout);
//        }
//        if (reviewCCV.size() > 0) {
//            ContentValues[] cvArray = new ContentValues[reviewCCV.size()];
//            reviewCCV.toArray(cvArray);
//            mContext.getContentResolver().bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, cvArray);
//        }
//
//        mRootView.scrollTo(0, 0);
    }

    /**
     * Sets the view's text and images.
     */
    private void setDetailsView() {
        ContentValues cv = new ContentValues();
        cv.put(MovieContract.MovieEntry.COLUMN_LENGTH, mMovieDetails.runtime);
        int count = mContext.getContentResolver().update(
                MovieContract.MovieEntry.CONTENT_URI,
                cv,
                MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID + " = ",
                new String[]{Long.toString(mMovieDbId)});
        if ((count == -1)) throw new AssertionError();

//        TextView detail_title = (TextView) mRootView.findViewById(R.id.detail_fragment_title);
//        ImageView detail_poster = (ImageView) mRootView.findViewById(R.id.detail_fragment_poster_image_view);
//        TextView detail_year = (TextView) mRootView.findViewById(R.id.detail_fragment_year_text_view);
//        TextView detail_length = (TextView) mRootView.findViewById(R.id.detail_fragment_movie_length_text_view);
//        TextView detail_rating = (TextView) mRootView.findViewById(R.id.detail_fragment_rating_text_view);
//        TextView detail_overview = (TextView) mRootView.findViewById(R.id.detail_fragment_overview);
//
//        if (mMovieDetails.title != null)
//            detail_title.setText(mMovieDetails.title);
//        if (mMovieDetails.posterUrl != null)
//            Picasso.with(mContext).load(mMovieDetails.posterUrl).into(detail_poster);
//        int strLen = mMovieDetails.releaseDate.length();
//        if (strLen >= 4)
//            detail_year.setText(mMovieDetails.releaseDate.substring(0, 4));
//        else
//            detail_year.setText(mMovieDetails.releaseDate.substring(0, strLen));
//        detail_length.setText(mMovieDetails.runtime);
//        detail_rating.setText(mMovieDetails.rating);
//        detail_overview.setText(mMovieDetails.overview);
    }

    /**
     * Looks up a specific movie id from themoviedb.org's web API.
     *
     * @param movieId corresponds to the Id in themoviedb.org's database
     * @return the json string return value.
     */
    private String getMovieJson(long movieId) {
        HttpURLConnection urlConnection = null;
        String returnValue = "";
        try {
            final String apiString = "http://api.themoviedb.org/3/movie/";
            final String ADD_TRAILERS_REVIEWS = "&append_to_response=trailers,reviews";
            final String APP_ID = "api_key";

            Uri builtUri = Uri.parse(apiString).buildUpon()
                    .appendEncodedPath("" + movieId)
                    .appendQueryParameter(APP_ID, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();
            URL url = new URL(builtUri.toString() + ADD_TRAILERS_REVIEWS);
            // Create the request to themoviedb.org, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into an image
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            returnValue = buffer.toString();

        } catch (IOException ex) {
            Log.e(LOG_TAG, "Error: ", ex);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return returnValue;
    }

    private MovieDetails getMovieDetails(JSONObject movieDetailsJson) {
        final String movieDbImageBaseUrl = "https://image.tmdb.org/t/p/" + "w342";
        MovieDetails movieDetails = new MovieDetails();
        try {
            movieDetails.title = movieDetailsJson.getString("title");
            movieDetails.posterUrl = movieDbImageBaseUrl + movieDetailsJson.getString("poster_path");
            movieDetails.releaseDate = movieDetailsJson.getString("release_date");
            movieDetails.runtime = movieDetailsJson.getString("runtime") + "min";
            movieDetails.overview = movieDetailsJson.getString("overview");
            movieDetails.rating = movieDetailsJson.getString("vote_average") + "/10";
            movieDetails.trailers = getMovieTrailers(movieDetailsJson.getJSONObject("trailers"));
            movieDetails.reviews = getMovieReviews(movieDetailsJson.getJSONObject("reviews"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return movieDetails;
    }

    private List<Review> getMovieReviews(JSONObject reviewsJson) {
        List<Review> reviews = new ArrayList<>();
        try {
            JSONArray results = reviewsJson.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = (JSONObject) results.get(i);
                Review review = new Review();
                review.author = result.getString("author");
                review.review = result.getString("content");
                reviews.add(review);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return reviews;
    }

    private List<Trailer> getMovieTrailers(JSONObject trailersJson) {
        List<Trailer> trailerUrls = new ArrayList<>();
        try {
            Iterator<String> keys = trailersJson.keys();
            JSONArray obj;
            String key;
            while (keys.hasNext()) {
                key = keys.next();

                // Don't use quicktime videos
                if (key.equalsIgnoreCase("quicktime"))
                    continue;

                obj = (JSONArray) trailersJson.get(key);
                for (int ii = 0; ii < obj.length(); ii++) {
                    // Get the trailer's name
                    Trailer trailer = new Trailer();
                    trailer.name = (String) obj.getJSONObject(ii).get("name");
                    trailer.url = getYoutubeUrl((String) obj.getJSONObject(ii).get("source"));
                    trailerUrls.add(ii, trailer);
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return trailerUrls;
    }

    private String getYoutubeUrl(String source) {
        final String YOUTUBE_LINK = "https://www.youtube.com/watch?";
        final String LINK_KEY = "v";
        Uri builtUri = Uri.parse(YOUTUBE_LINK).buildUpon()
                .appendQueryParameter(LINK_KEY, source)
                .build();
        return builtUri.toString();
    }
}
