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

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MoviesFragment.OnMovieSelectedListener} interface
 * to handle interaction events.
 */
public class MoviesFragment extends Fragment
        implements AdapterView.OnItemClickListener {
    private static final String LOG_TAG = MoviesFragment.class.getSimpleName();

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
    private boolean mReloadOnResume = true;
    PosterAdapter mPosterAdapter;
    List<Poster> mPosterList;
    AbsListView mListView;

    public MoviesFragment() {
        super();
        Log.v(LOG_TAG, "MainActivityFragment()");
        // Required empty public constructor
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.v(LOG_TAG, "onItemClick()");
        if (mMovieSelectedListener != null) {
            mReloadOnResume = false;
            mMovieSelectedListener.onMovieSelected(mPosterList.get(position).movieDbId);
        }
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate()");
        mPosterList = new ArrayList<>(new ArrayList<Poster>());
        mPosterAdapter = new PosterAdapter(getActivity(), mPosterList);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView()");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movies, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mPosterAdapter);
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(LOG_TAG, "onStart()");
    }

    @Override
    public void onResume() {
        super.onStart();
        if (mReloadOnResume) {
            mPosterAdapter.clear();
            FetchMoviesTask moviesTask = new FetchMoviesTask(getContext(), mPosterAdapter);
            moviesTask.execute(1);
        }
        Log.v(LOG_TAG, "onResume()");
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

    public void setOnMovieSelectedListener(OnMovieSelectedListener listener) {
        Log.v(LOG_TAG, "setOnMovieSelectedListener()");
        mMovieSelectedListener = listener;
    }

    public interface OnMovieSelectedListener {
        void onMovieSelected(int position);
    }
}
