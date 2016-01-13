package com.afitzwa.android.popularmovies;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Fragment containing details of a selected movie.
 */
public class DetailFragment extends Fragment implements View.OnClickListener {
    public static final String MOVIE_DB_ID = "movie id";
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private int mMovieDbId;
    private static View mFragmentView;
    private static Context mContext;

    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param movieDbId id of the movie in the database.
     * @return A new instance of fragment DetailFragment.
     */
    public static DetailFragment newInstance(int movieDbId) {
        Log.v(LOG_TAG, "newInstance()");
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putInt(MOVIE_DB_ID, movieDbId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Get themoviedb.org's Id from our arguments
            mMovieDbId = getArguments().getInt(MOVIE_DB_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView()");
        // Inflate the layout for this fragment
        mFragmentView = inflater.inflate(R.layout.fragment_detail, container, false);

        mFragmentView.findViewById(R.id.detail_fragment_favorite_button).setOnClickListener(this);

        mContext = getContext();
        FetchMovieDetailsTask fetchMovieDetailsTask = new FetchMovieDetailsTask(mContext, mFragmentView);
        fetchMovieDetailsTask.execute(mMovieDbId);

        return mFragmentView;
    }

    @Override
    public void onClick(View v) {
        Log.v(LOG_TAG, "onClick(): " + mMovieDbId);
        /* // TODO: Add functionality for saving movies
        Button button = (Button) v;
        List<Movie> favoriteMovies = Movie.find(Movie.class, "MOVIE_DB_ID = ?", "" + mMovieDbId);
        favoriteMovies.size();

        if (favoriteMovies.size() == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                button.setBackgroundColor(getResources().getColor(R.color.colorAccent, getContext().getTheme()));
            } else {
                button.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }
            String title = ((TextView) mFragmentView.findViewById(R.id.detail_fragment_title)).getText().toString();
            Drawable poster = ((ImageView) mFragmentView.findViewById(R.id.detail_fragment_poster_image_view)).getDrawable();
            String year = ((TextView) mFragmentView.findViewById(R.id.detail_fragment_year_text_view)).getText().toString();
            String length = ((TextView) mFragmentView.findViewById(R.id.detail_fragment_movie_length_text_view)).getText().toString() + "min";
            String rating = ((TextView) mFragmentView.findViewById(R.id.detail_fragment_rating_text_view)).getText().toString();
            String overview = ((TextView) mFragmentView.findViewById(R.id.detail_fragment_overview)).getText().toString();
            Movie favoriteMovie = new Movie(mMovieDbId, title, poster, year, length, rating, overview);
            favoriteMovie.save();
            button.setText(getResources().getText(R.string.remove_favorite_button_text));
        } else {
            favoriteMovies.get(0).delete();
            button.setText(getResources().getText(R.string.add_favorite_button_text));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                button.setBackgroundColor(getResources().getColor(R.color.colorLightPrimary, getContext().getTheme()));
            } else {
                button.setBackgroundColor(getResources().getColor(R.color.colorLightPrimary));
            }
        }
        */
    }

    // Called on wide screen devices
    public void updateDetailView(int movieDbId) {
        FetchMovieDetailsTask fetchMovieDetailsTask = new FetchMovieDetailsTask(mContext, mFragmentView);
        fetchMovieDetailsTask.execute(movieDbId);
    }
}
