package com.afitzwa.android.popularmovies.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afitzwa.android.popularmovies.app.data.MovieContract;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MoviesFragment.OnMovieSelectedListener} interface
 * to handle interaction events.
 */
public class MoviesFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MoviesFragment.class.getSimpleName();

    private PosterAdapter mPosterAdapter;

    private static final int MOVIES_LOADER = 0;
    private static final String[] MOVIES_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_URL
    };
    static final int COL_ID = 0;
    static final int COL_MOVIE_DB_ID = 1;
    static final int COL_MOVIE_TITLE = 2;
    static final int COL_MOVIE_POSTER_URL = 3;

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public OnMovieSelectedListener mMovieSelectedListener = null;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    List<Poster> mPosterList;
    AbsListView mListView;
    private static final String SELECTED_KEY = "selected_key";
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mReloadOnResume = true;

    public MoviesFragment() {
        super();
        Log.v(LOG_TAG, "New MainActivityFragment() instantiated");
        // Required empty public constructor
    }

    /*--------------------------------------------
        Lifecycle overrides
     -------------------------------------------*/
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate()");
        mPosterList = new ArrayList<>(new ArrayList<Poster>());
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView()");

        mPosterAdapter = new PosterAdapter(getActivity(), null, 0);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movies, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mPosterAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((OnMovieSelectedListener) getActivity())
                            .onMovieSelected(MovieContract.MovieEntry.buildMovieUriWithMovie(cursor.getLong(COL_MOVIE_DB_ID)));
                }
                mPosition = position;
            }
        });


        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.v(LOG_TAG, "onAttach()");
        try {
            mMovieSelectedListener = (OnMovieSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // If favorites => populate views using a loader
        // else => need to load from themoviedb.org
        if (mReloadOnResume) {
            Log.v(LOG_TAG, "onResume()::Reloading mPosterAdapter");
            mPosterAdapter.swapCursor(null);
            FetchMoviesTask moviesTask = new FetchMoviesTask(getContext(), mPosterAdapter);
            moviesTask.execute(1);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort_by) {
            mReloadOnResume = true;
            Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        /* TODO Add a button for viewing favorites
           This will launch a favorites activity+fragment
           */
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.v(LOG_TAG, "onDetach()");
        mMovieSelectedListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
    }

    /*--------------------------------------------
        Interface methods
     -------------------------------------------*/
    public void setOnMovieSelectedListener(OnMovieSelectedListener listener) {
        Log.v(LOG_TAG, "setOnMovieSelectedListener()");
        mMovieSelectedListener = listener;
    }

    public interface OnMovieSelectedListener {
        void onMovieSelected(Uri uri);
    }

    /*--------------------------------------------
        Loader Callbacks
     -------------------------------------------*/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "onCreateLoader()");
        return new CursorLoader(
                getActivity(),
                MovieContract.MovieEntry.CONTENT_URI,
                MOVIES_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished()");
        mPosterAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION)
            mListView.smoothScrollToPosition(mPosition);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        Log.v(LOG_TAG, "onLoaderReset()");
        mPosterAdapter.swapCursor(null);
    }
}
