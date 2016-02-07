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
import java.util.Vector;

/**
 * Created by AndrewF on 11/7/2015.
 * <p>
 * Handles loading movies from themoviedb.org into an array adapter.
 */
class FetchMoviesTask extends AsyncTask<Integer, Void, Vector<MovieInfo>> {
    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
    private Context mContext;

    public FetchMoviesTask(Context context) {
        mContext = context;
    }

    @Override
    protected Vector<MovieInfo> doInBackground(Integer... params) {
        Log.v(LOG_TAG, "Querying MovieDb");

        String movieJsonString;
        int pageNumber = params[0];

        movieJsonString = queryMovieDb(pageNumber);

        // Create a vector containing all the necessary details
        Vector<MovieInfo> movieInfoVector = new Vector<>();
        try {
            movieInfoVector = getMovieDataFromJson(movieJsonString);
        } catch (JSONException ex) {
            Log.e(LOG_TAG, "Error getting JSON: " + ex);
        }

        return movieInfoVector;
    }

    @Override
    protected void onPostExecute(Vector<MovieInfo> result) {
        if (result == null) {
            Log.e(LOG_TAG, "Error: Could not get any results");
            return;
        }

        mCallbackCaller.loadedDetails(result);
    }

    private String queryMovieDb(int page) {
        HttpURLConnection urlConnection = null;
        String returnValue = "";
        try {
            final String apiString = "http://api.themoviedb.org/3/discover/movie";
            final String APP_ID = "api_key";
            final String PAGE_NUM = "page";

            Uri.Builder uriBuilder = Uri.parse(apiString).buildUpon()
                    .appendQueryParameter(APP_ID, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .appendQueryParameter(PAGE_NUM, "" + page);
            String sortCriteria = "";
            Utility utility = new Utility();
            String sortBy = utility.GetSortByOption(mContext);
            if (sortBy.equalsIgnoreCase("most popular")) {
                sortCriteria = "popularity.desc";
            } else if (sortBy.equalsIgnoreCase("alphabetical")) {
                sortCriteria = "original_title.desc";
            } else if (sortBy.equalsIgnoreCase("user rated")) {
                sortCriteria = "vote_average.desc";
            }
            Log.v(LOG_TAG, sortBy);
            uriBuilder.appendQueryParameter("sort_by", sortCriteria);
            Uri builtUri = uriBuilder.build();
            URL url = new URL(builtUri.toString());
            // Create the request to TheMovieDb.org, and open the connection
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

    private Vector<MovieInfo> getMovieDataFromJson(String movieJsonStr) throws JSONException {
        final String MDB_RESULTS = "results";
        final String MDB_ID = "id";
        final String MDB_POSTER = "poster_path";
        final String MDB_TITLE = "title";
        final String MOVIE_BASE_IMG_URL = "https://image.tmdb.org/t/p/";
        final String MOVIE_IMG_WIDTH = "w342";
        final JSONObject movieJsonObj = new JSONObject(movieJsonStr);

        Vector<MovieInfo> movieInfoVector = new Vector<>();

        try {
            JSONArray movies = movieJsonObj.getJSONArray(MDB_RESULTS);

            for (int i = 0; i < movies.length(); i++) {
                JSONObject movie = movies.getJSONObject(i);
                MovieInfo mi = new MovieInfo();
                mi.title = movie.getString(MDB_TITLE);
                mi.posterUrl = MOVIE_BASE_IMG_URL + MOVIE_IMG_WIDTH + movie.getString(MDB_POSTER);
                mi.movieDbId = movie.getLong(MDB_ID);
                movieInfoVector.add(mi);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "getMovieDataFromJson:: " + e);
        }
        return movieInfoVector;
    }

    /*--------------------------------------------
        Interface method
     -------------------------------------------*/
    private Callback mCallbackCaller;

    public void setCallBackCaller(Callback callbackCaller) {
        mCallbackCaller = callbackCaller;
    }

    public interface Callback {
        void loadedDetails(Vector<MovieInfo> movieInfoVector);
    }
    /*------------------------------------------*/
}
