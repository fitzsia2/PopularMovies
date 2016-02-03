package com.afitzwa.android.popularmovies.app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.afitzwa.android.popularmovies.app.data.MovieContract;

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
    private Context mContext;


    private static final String[] MOVIES_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID
    };
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_DB_ID = 1;


    public FetchMoviesTask(Context context) {
        mContext = context;
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
        mContext.getContentResolver().notifyChange(MovieContract.MovieEntry.CONTENT_URI, null);
    }

    String queryMovieDb(int page) {
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

            String movieJsonStr = buffer.toString();
            getMovieDataFromJson(movieJsonStr);
            returnValue = buffer.toString();

        } catch (IOException ex) {
            Log.e(LOG_TAG, "Error: ", ex);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Ooops " + e);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return returnValue;
    }

    private void getMovieDataFromJson(String movieJsonStr) throws JSONException {
        final String MDB_PAGE = "page";
        final String MDB_RESULTS = "results";
        final String MDB_TOTAL_RESULTS = "total_results";
        final String MDB_TOTAL_PAGES = "total_pages";
        final String MDB_ID = "id";
        final String MDB_POSTER = "poster_path";
        final String MDB_TITLE = "title";
        final String MDB_RATING = "vote_average";
        final String MDB_OVERVIEW = "overview";
        final String MDB_RELEASE_DATE = "release_date";
        final String MOVIE_BASE_IMG_URL = "https://image.tmdb.org/t/p/";
        final String MOVIE_IMG_WIDTH = "w342";
        final JSONObject movieJsonObj = new JSONObject(movieJsonStr);

        try {
            JSONArray movies = movieJsonObj.getJSONArray(MDB_RESULTS);

            for (int i = 0; i < movies.length(); i++) {
                JSONObject movie = movies.getJSONObject(i);
                Long movieDbId = movie.getLong(MDB_ID);
                String moviePosterPath = MOVIE_BASE_IMG_URL + MOVIE_IMG_WIDTH + movie.getString(MDB_POSTER);
                String movieTitle = movie.getString(MDB_TITLE);
                String movieRating = movie.getString(MDB_RATING);
                String movieOverview = movie.getString(MDB_OVERVIEW);
                int movieReleaseDate;
                try {
                    movieReleaseDate = Integer.parseInt(movie.getString(MDB_RELEASE_DATE).substring(0, 4));
                } catch (StringIndexOutOfBoundsException e) {
                    movieReleaseDate = 0;
                }

                addMovie(movieDbId, movieTitle, moviePosterPath, movieRating, movieOverview, movieReleaseDate);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "getMovieDataFromJson:: " + e);
        }
    }

    /**
     * Attempts to add a movie to our database. If it already exists, we return the column id
     *
     * @param movieDbId        Id of the movie from TheMovieDb's json
     * @param movieTitle       Title of the movie
     * @param moviePosterPath  Url path to the movie's poster
     * @param movieRating      Movie's rating
     * @param movieOverview    Brief overview of the movie
     * @param movieReleaseDate Year the movie was released
     * @return The row of the movie in our local db
     */
    private Long addMovie(Long movieDbId, String movieTitle, String moviePosterPath, String movieRating, String movieOverview, int movieReleaseDate) {

        Cursor c = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                MOVIES_COLUMNS,
                MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID + " = " + movieDbId,
                null,
                null);

        Long rowId;

        // Return the column if it already exists
        if (c == null)
            return null;

        if (c.moveToFirst()) {
            rowId = c.getLong(COL_MOVIE_ID);
        } else {
            // Save the new movie
            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID, movieDbId);
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_URL, moviePosterPath);
            movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movieTitle);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RATING, movieRating);
            movieValues.put(MovieContract.MovieEntry.COLUMN_DESCRIPTION, movieOverview);
            movieValues.put(MovieContract.MovieEntry.COLUMN_YEAR, movieReleaseDate);
            Uri uri = mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, movieValues);
            rowId = ContentUris.parseId(uri);
        }
        c.close();
        return rowId;
    }
}
