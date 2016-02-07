package com.afitzwa.android.popularmovies.app;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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

import javax.security.auth.callback.Callback;


/**
 * Created by AndrewF on 11/13/2015.
 * <p>
 * Handles querying themoviedb.org for information related to a single movie.
 * <p>
 * Specifically gets the runtime and trailer links.
 */
class FetchMovieDetailsTask extends AsyncTask<Long, Void, JSONObject> implements Callback {
    private final String LOG_TAG = FetchMovieDetailsTask.class.getSimpleName();

    /**
     * Contains a name and url for a given trailer
     */

    private Context mContext;
    private long mMovieDbId;

    public FetchMovieDetailsTask(Context context) {
        mContext = context;
    }


    /*--------------------------------------------
        Interface method
     -------------------------------------------*/
    private Callback mCallbackCaller;

    public void setCallBackCaller(Callback callbackCaller) {
        mCallbackCaller = callbackCaller;
    }

    public interface Callback {
        void loadedDetails(MovieInfo movieInfo);
    }
    /*------------------------------------------*/

    /**
     * @param params Should be a single integer representing the id of the requested movie
     *               from themoviedb.org's database.
     * @return a JSONObject returned by themoviedb.org's API.
     */
    @Override
    protected JSONObject doInBackground(Long... params) {
        Log.v(LOG_TAG, "Querying MovieDb: " + params[0]);
        Long movieDbId = params[0];

        JSONObject movieJsonObject = null;

        // Retrieve the specific movie JSON from TheMovieDb
        try {
            String movieJsonString = getMovieJson(movieDbId);
            movieJsonObject = new JSONObject(movieJsonString);
        } catch (JSONException ex) {
            Log.e(LOG_TAG, "JSON Error: ", ex);
        }

        // Return the JSON
        return movieJsonObject;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        MovieInfo movieInfo;

        movieInfo = getMovieDetails(jsonObject);
        mCallbackCaller.loadedDetails(movieInfo);
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

    private MovieInfo getMovieDetails(JSONObject movieDetailsJson) {
        final String MDB_TITLE = "title";
        final String MDB_POSTER = "poster_path";
        final String MDB_RELEASE = "release_date";
        final String MDB_LENGTH = "runtime";
        final String MDB_OVERVIEW = "overview";
        final String MDB_ID = "id";
        final String MDB_RATING = "vote_average";
        final String MDB_TRAILERS = "trailers";
        final String MDB_REVIEWS = "reviews";
        final String movieDbImageBaseUrl = "https://image.tmdb.org/t/p/" + "w342";
        MovieInfo movieDetails = new MovieInfo();
        try {
            movieDetails.movieDbId = movieDetailsJson.getLong(MDB_ID);
            movieDetails.title = movieDetailsJson.getString(MDB_TITLE);
            movieDetails.posterUrl = movieDbImageBaseUrl + movieDetailsJson.getString(MDB_POSTER);
            movieDetails.releaseDate = movieDetailsJson.getString(MDB_RELEASE).substring(0,4);
            movieDetails.runtime = movieDetailsJson.getString(MDB_LENGTH) + "min";
            movieDetails.overview = movieDetailsJson.getString(MDB_OVERVIEW);
            movieDetails.rating = movieDetailsJson.getString(MDB_RATING) + "/10";
            movieDetails.trailers = getMovieTrailers(movieDetailsJson.getJSONObject(MDB_TRAILERS));
            movieDetails.reviews = getMovieReviews(movieDetailsJson.getJSONObject(MDB_REVIEWS));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return movieDetails;
    }

    private List<MovieInfo.Review> getMovieReviews(JSONObject reviewsJson) {
        List<MovieInfo.Review> reviews = new ArrayList<>();
        try {
            JSONArray results = reviewsJson.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = (JSONObject) results.get(i);
                MovieInfo.Review review = new MovieInfo.Review(result.getString("author"), result.getString("content"));
                reviews.add(review);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return reviews;
    }

    private List<MovieInfo.Trailer> getMovieTrailers(JSONObject trailersJson) {
        List<MovieInfo.Trailer> trailerUrls = new ArrayList<>();
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
                    String videoSource = obj.getJSONObject(ii).get("source").toString();
                    String videoName = obj.getJSONObject(ii).get("name").toString();
                    // Get the trailer's name
                    MovieInfo.Trailer trailer = new MovieInfo.Trailer( videoName, getYoutubeUrl(videoSource));
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
