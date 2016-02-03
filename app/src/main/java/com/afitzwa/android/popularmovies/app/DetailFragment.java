package com.afitzwa.android.popularmovies.app;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afitzwa.android.popularmovies.app.data.MovieContract;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;


/**
 * Fragment containing details of a selected movie.
 */
public class DetailFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final String MOVIE_URI = "movie uri";
    public static final String LOAD_FAVORITES = "favorites flag";
    private static final String ADD_FAV = "Mark as favorite";
    private static final String RM_FAV = "Remove from favorites";
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private TrailersAdapter mTrailerAdapter;
    private ReviewsAdapter mReviewAdapter;
    private TextView mTitleTextView;
    private ImageView mMovieImageView;
    private TextView mYearTextView;
    private TextView mMovieLengthView;
    private TextView mMovieRatingView;
    private TextView mOverviewTextView;
    private Button mFavoriteButton;
    private AbsListView mTrailerListView;
    private AbsListView mReviewListView;

    private boolean mIsAFavorite;
    private Uri mUri;

    // TODO
//    private boolean mLoadFavorites = false;

    private static final int MOVIES_LOADER = 0;
    private static final String[] MOVIES_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_URL,
            MovieContract.MovieEntry.COLUMN_LENGTH,
            MovieContract.MovieEntry.COLUMN_YEAR,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_DESCRIPTION,
            MovieContract.MovieEntry.COLUMN_FAVORITE
    };
    static final int COL_MOVIE_DB_ID = 1;
    static final int COL_MOVIE_TITLE = 2;
    static final int COL_MOVIE_POSTER_URL = 3;
    static final int COL_MOVIE_LENGTH = 4;
    static final int COL_MOVIE_YEAR = 5;
    static final int COL_MOVIE_RATING = 6;
    static final int COL_MOVIE_DESCRIPTION = 7;
    static final int COL_MOVIE_FAVORITE = 8;

    private static final int TRAILERS_LOADER = 1;
    private static final String[] TRAILER_COLUMNS = {
            MovieContract.TrailerEntry.TABLE_NAME + "." + MovieContract.TrailerEntry._ID,
            MovieContract.TrailerEntry.COLUMN_TRAILER_URL,
            MovieContract.TrailerEntry.COLUMN_DESCRIPTION
    };
    static final int COL_TRAILER_URL = 1;
    static final int COL_TRAILER_DESCRIPTION = 2;

    private static final int REVIEWS_LOADER = 2;
    private static final String[] REVIEWS_COLUMNS = {
            MovieContract.ReviewEntry.TABLE_NAME + "." + MovieContract.ReviewEntry._ID,
            MovieContract.ReviewEntry.COLUMN_USER,
            MovieContract.ReviewEntry.COLUMN_DESCRIPTION
    };
    static final int COL_REVIEW_USER = 1;
    static final int COL_REVIEW_DESCRIPTION = 2;

    private FetchMovieDetailsTask mFetchMovieDetailsTask;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Get movie db Id from our arguments
            mUri = getArguments().getParcelable(MOVIE_URI);
//            mLoadFavorites = getArguments().getBoolean(LOAD_FAVORITES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_detail, container, false);
        fragmentView.setVerticalScrollbarPosition(0);

        // Get all the views
        mTitleTextView = (TextView) fragmentView.findViewById(R.id.detail_fragment_title);
        mMovieImageView = (ImageView) fragmentView.findViewById(R.id.detail_fragment_poster_image_view);
        mYearTextView = (TextView) fragmentView.findViewById(R.id.detail_fragment_year_text_view);
        mMovieLengthView = (TextView) fragmentView.findViewById(R.id.detail_fragment_movie_length_text_view);
        mMovieRatingView = (TextView) fragmentView.findViewById(R.id.detail_fragment_rating_text_view);
        mOverviewTextView = (TextView) fragmentView.findViewById(R.id.detail_fragment_overview);
        mFavoriteButton = (Button) fragmentView.findViewById(R.id.detail_fragment_favorite_button);
        mTrailerListView = (AbsListView) fragmentView.findViewById(R.id.trailers_list_view);
        mReviewListView = (AbsListView) fragmentView.findViewById(R.id.reviews_list_view);

        // These views are populated through our adapter
        mTrailerAdapter = new TrailersAdapter(getContext(), null, 0);
        mTrailerListView.setAdapter(mTrailerAdapter);
        mReviewAdapter = new ReviewsAdapter(getContext(), null, 0);
        mReviewListView.setAdapter(mReviewAdapter);

        // Setup our clickListener
        fragmentView.findViewById(R.id.detail_fragment_favorite_button).setOnClickListener(this);

        Context context = getContext();
        mFetchMovieDetailsTask = new FetchMovieDetailsTask(context);
        if (mUri != null) {
            Cursor c = context.getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,
                    MOVIES_COLUMNS,
                    MovieContract.MovieEntry._ID + " = " + MovieContract.MovieEntry.getMovieIdFromUri(mUri),
                    null,
                    null);
            assert c != null;
            if (c.moveToFirst()) {
                long movieDbId = c.getLong(COL_MOVIE_DB_ID);
                c.close();
                mFetchMovieDetailsTask.execute(movieDbId);
            }
        }
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v(LOG_TAG, "Initializing loaders");
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        getLoaderManager().initLoader(TRAILERS_LOADER, null, this);
        getLoaderManager().initLoader(REVIEWS_LOADER, null, this);
    }

    @Override
    public void onClick(View v) {
        //TODO: Add functionality for saving movies
        Log.v(LOG_TAG, "Movie _id=" + MovieContract.MovieEntry.getMovieIdFromUri(mUri) + " added to favorites");
        ContentValues newValues = new ContentValues();
        if (mIsAFavorite) {
            newValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 0);
            ((Button) v).setText(RM_FAV);
        } else {
            newValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 1);
            ((Button) v).setText(ADD_FAV);
        }
        int cnt = getContext().getContentResolver().update(
                MovieContract.MovieEntry.CONTENT_URI,
                newValues,
                MovieContract.MovieEntry._ID + " = ?",
                new String[]{MovieContract.MovieEntry.getMovieIdFromUri(mUri)}
        );
        Log.d(LOG_TAG, "Updated " + cnt + " rows");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri == null)
            return null;

        String rowId = MovieContract.MovieEntry.getMovieIdFromUri(mUri);
        switch (id) {
            case MOVIES_LOADER:
                Log.v(LOG_TAG, "Creating movies loader");
                return new CursorLoader(getActivity(),
                        MovieContract.MovieEntry.CONTENT_URI,
                        MOVIES_COLUMNS,
                        MovieContract.MovieEntry._ID + " = " + rowId,
                        null,
                        null);
            case TRAILERS_LOADER:
                Log.v(LOG_TAG, "Creating trailers loader");
                return new CursorLoader(getActivity(),
                        MovieContract.TrailerEntry.CONTENT_URI,
                        TRAILER_COLUMNS,
                        MovieContract.TrailerEntry.COLUMN_MOVIE_KEY + " = " + rowId,
                        null,
                        null);
            case REVIEWS_LOADER:
                Log.v(LOG_TAG, "Creating reviews loader");
                return new CursorLoader(getActivity(),
                        MovieContract.ReviewEntry.CONTENT_URI,
                        REVIEWS_COLUMNS,
                        MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + " = " + rowId,
                        null,
                        null);
            default:
                break;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        switch (loaderId) {
            case MOVIES_LOADER:
                Log.v(LOG_TAG, "Loaded " + data.getCount() + " movies");
                setDetails(data);
                break;
            case TRAILERS_LOADER:
                Log.v(LOG_TAG, "Loaded " + data.getCount() + " trailers");
                mTrailerAdapter.swapCursor(data);
                break;
            case REVIEWS_LOADER:
                Log.v(LOG_TAG, "Loaded " + data.getCount() + " reviews");
                mReviewAdapter.swapCursor(data);
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTrailerAdapter.swapCursor(null);
        mReviewAdapter.swapCursor(null);
    }

    private void setDetails(Cursor c) {
        if (c.moveToFirst()) {
            // Set the title
            mTitleTextView.setText(c.getString(COL_MOVIE_TITLE));

            // Set the poster/image
            String posterUrlString = c.getString(COL_MOVIE_POSTER_URL);
            RequestCreator requestCreator = Picasso.with(getActivity()).load(posterUrlString);
            requestCreator.into(mMovieImageView);
            mMovieImageView.setContentDescription(posterUrlString);

            // Set the release date
            mYearTextView.setText(String.valueOf(c.getInt(COL_MOVIE_YEAR)));

            // Set the description
            mOverviewTextView.setText(c.getString(COL_MOVIE_DESCRIPTION));

            // Set the rating
            mMovieRatingView.setText(c.getString(COL_MOVIE_RATING));

            // Set the text of the favorites button
            mIsAFavorite = (c.getInt(COL_MOVIE_FAVORITE) == 1);
            String buttonText = mIsAFavorite ? RM_FAV : ADD_FAV;
            mFavoriteButton.setText(buttonText);

            // Set the length
            final String lengthSuffix = "min";
            mMovieLengthView.setText(String.valueOf(c.getInt(COL_MOVIE_LENGTH)) + lengthSuffix);
        }
    }
}
