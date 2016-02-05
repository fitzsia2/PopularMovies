package com.afitzwa.android.popularmovies.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MoviesFragment.OnMovieSelectedListener} interface
 * to handle interaction events.
 */
public class MoviesFragment extends Fragment
        implements FetchMoviesTask.Callback {
    private static final String LOG_TAG = MoviesFragment.class.getSimpleName();

    private PosterAdapter mPosterAdapter;

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
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

        mPosterAdapter = new PosterAdapter(getContext(), new ArrayList<MovieInfo>());

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movies, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mPosterAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                MovieInfo movieInfo = (MovieInfo) adapterView.getItemAtPosition(position);
                ((OnMovieSelectedListener) getActivity())
                        .onMovieSelected(movieInfo.movieDbId);
                Log.v(LOG_TAG, "Clicked movie with _id=" + movieInfo.movieDbId);
                mPosition = position;
            }
        });


        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        } else {
//            Log.d(LOG_TAG, "Could not get position key :(");
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        if (mReloadOnResume) {
            mReloadOnResume = false;
            mPosterAdapter.clear();
            FetchMoviesTask moviesTask = new FetchMoviesTask(getContext());
            moviesTask.setCallBackCaller(this);
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
        } else if (id == R.id.action_favorites) {
//            Intent favoritesIntent = new Intent(getActivity(), FavoriteMoviesActivity.class);
//            startActivity(favoritesIntent);
//            return true;
        }
        /* TODO Add a button for viewing favorites
           This will launch a favorites activity+fragment
           */

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMovieSelectedListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPosition != ListView.INVALID_POSITION) {
//            Log.d(LOG_TAG, "Saving instance state with position " + mPosition);
            outState.putInt(SELECTED_KEY, mPosition);
        }
    }

    /*--------------------------------------------
        Interface methods
     -------------------------------------------*/
    // Communicate with activity
    public void setOnMovieSelectedListener(OnMovieSelectedListener listener) {
        mMovieSelectedListener = listener;
    }

    // Communicate with activity
    public interface OnMovieSelectedListener {
        void onMovieSelected(Long movieDbId);
    }

    // Receive data from FetchMoviesTask
    @Override
    public void loadedDetails(Vector<MovieInfo> movieInfoVector) {
        for (MovieInfo movieInfo : movieInfoVector) {
            mPosterAdapter.add(movieInfo);
        }
        if(mPosition != ListView.INVALID_POSITION)
            mListView.smoothScrollToPosition(mPosition);
    }
}
