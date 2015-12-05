package com.afitzwa.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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

/**
 * Created by AndrewF on 11/7/2015.
 * <p>
 * Handles loading movies from themoviedb.org into an array adapter.
 */
class FetchMoviesTask extends AsyncTask<Integer, Void, JSONArray> {
    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
    private PosterAdapter posterAdapter;
    private Context mContext;

    public FetchMoviesTask(Context context, PosterAdapter moviesAdapter) {
        mContext = context;
        posterAdapter = moviesAdapter;
    }

    @Override
    protected JSONArray doInBackground(Integer... params) {
        Log.v(LOG_TAG, "Querying MovieDb");

        String movieJsonString;
        JSONArray movieJsonArray = null;
        int pageNumber = params[0];

        movieJsonString = queryMovieDb(pageNumber);

        // Convert to JSON Object
        try {
            JSONObject movieJsonObj = new JSONObject(movieJsonString);
            movieJsonArray = movieJsonObj.getJSONArray("results");
        } catch (JSONException ex) {
            Log.e(LOG_TAG, "JSON Error: ", ex);
        }

        return movieJsonArray;
    }

    @Override
    protected void onPostExecute(JSONArray result) {
        if (result == null) {
            Log.e(LOG_TAG, "Error: Could not get any results");
            return;
        }

        // Get the id and poster url for each movie
        for (int i = 0; i < result.length(); i++) {
            try {
                final String MOVIE_DB_BASE_IMG_URL = "https://image.tmdb.org/t/p/";
                final String MOVIE_DB_IMG_WIDTH = "w342";

                // Add the base url and image width to the poster path string
                int movieDbId = result.getJSONObject(i).getInt("id");
                String posterUrl = MOVIE_DB_BASE_IMG_URL
                        + MOVIE_DB_IMG_WIDTH
                        + result.getJSONObject(i).getString("poster_path");

                Poster poster = new Poster(movieDbId, posterUrl);
                int posterPos = posterAdapter.getPosition(poster);
                if (posterPos != -1) {
                    Log.v(LOG_TAG, "Poster already in adapter");
                    continue;
                }

                posterAdapter.add(poster);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private String queryMovieDb(int page) {
        HttpURLConnection urlConnection = null;
        String returnValue = "";
        try {
            final String apiString = "http://api.themoviedb.org/3/discover/movie";
            final String APP_ID = "api_key";
            final String PAGE_NUM = "page";

            Uri.Builder uriBuilder = Uri.parse(apiString).buildUpon()
                    .appendQueryParameter(APP_ID, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                    .appendQueryParameter(PAGE_NUM, "" + page);
            String sortCriteria = "";
            String sortBy = GetSortByOption();
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
            // Create the request to OpenWeatherMap, and open the connection
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

    private String GetSortByOption() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPrefs.getString(mContext.getString(R.string.pref_sort_by_key), mContext.getString(R.string.pref_sort_by_default));
    }
}
