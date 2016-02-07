package com.afitzwa.android.popularmovies.app;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.afitzwa.android.popularmovies.app.data.MovieContract;

/**
 * Fragment handling favorite movies list.
 */
public class FavoriteMoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = FavoriteMoviesFragment.class.getSimpleName();

    private AbsListView mListView;
    private FavoritesPosterAdapter mFavoritesPosterAdapter;

    private static final int MOVIES_LOADER = 1;
    private static final String[] MOVIES_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID
    };
    static final int MOVIE_COL_TITLE = 1;
    static final int MOVIE_COL_DB_ID = 2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_movies_list, container, false);
        mFavoritesPosterAdapter = new FavoritesPosterAdapter(getContext());
        mListView = (AbsListView) view.findViewById(R.id.favorites_list);
        mListView.setAdapter(mFavoritesPosterAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor c = (Cursor)adapterView.getItemAtPosition(position);
                if(c != null && c.moveToPosition(position)) {
                    long movieDbId = c.getLong(MOVIE_COL_DB_ID);
                    ((OnMovieSelectedListener) getActivity())
                            .onMovieSelected(movieDbId);
                    Log.v(LOG_TAG, "Clicked movie with _id=" + movieDbId);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    /*--------------------------------------------
                Interface methods
     -------------------------------------------*/
    public OnMovieSelectedListener mMovieSelectedListener = null;

    // Communicate with activity
    public void setOnMovieSelectedListener(OnMovieSelectedListener listener) {
        mMovieSelectedListener = listener;
    }

    // Communicate with activity
    public interface OnMovieSelectedListener {
        void onMovieSelected(Long movieDbId);
    }
    /*------------------------------------------*/

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), MovieContract.MovieEntry.CONTENT_URI,
                MOVIES_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mFavoritesPosterAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mFavoritesPosterAdapter.swapCursor(null);
    }
}
