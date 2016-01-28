package com.afitzwa.android.popularmovies.app;


import android.content.ActivityNotFoundException;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private Uri mUri;
    private boolean mLoadFavorites = false;
    private static View mFragmentView;
    private static Context mContext;

    private static final int MOVIES_LOADER = 0;
    private static final String[] MOVIES_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_URL,
            MovieContract.MovieEntry.COLUMN_LENGTH,
            MovieContract.MovieEntry.COLUMN_YEAR,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_DESCRIPTION
    };
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_DB_ID = 1;
    static final int COL_MOVIE_TITLE = 2;
    static final int COL_MOVIE_POSTER_URL = 3;
    static final int COL_MOVIE_LENGTH = 4;
    static final int COL_MOVIE_YEAR = 5;
    static final int COL_MOVIE_RATING = 6;
    static final int COL_MOVIE_DESCRIPTION = 7;

    private static final int TRAILERS_LOADER = 1;
    private static final String[] TRAILER_COLUMNS = {
            MovieContract.TrailerEntry.TABLE_NAME + "." + MovieContract.TrailerEntry._ID,
            MovieContract.TrailerEntry.COLUMN_MOVIE_KEY,
            MovieContract.TrailerEntry.COLUMN_TRAILER_URL,
            MovieContract.TrailerEntry.COLUMN_DESCRIPTION
    };
    static final int COL_TRAILER_ID = 0;
    static final int COL_TRAILER_MOVIE_ID = 1;
    static final int COL_TRAILER_URL = 2;
    static final int COL_TRAILER_DESCRIPTION = 3;

    private static final int REVIEWS_LOADER = 2;
    private static final String[] REVIEWS_COLUMNS = {
            MovieContract.ReviewEntry.TABLE_NAME + "." + MovieContract.ReviewEntry._ID,
            MovieContract.ReviewEntry.COLUMN_MOVIE_KEY,
            MovieContract.ReviewEntry.COLUMN_USER,
            MovieContract.ReviewEntry.COLUMN_DESCRIPTION
    };
    static final int COL_REVIEW_ID = 0;
    static final int COL_REVIEW_MOVIE_KEY = 1;
    static final int COL_REVIEW_USER = 2;
    static final int COL_REVIEW_DESCRIPTION = 3;

    private FetchMovieDetailsTask mFetchMovieDetailsTask;

    private TextView mTitleView;
    private ImageView mMovieImageView;
    private TextView mMovieYearView;
    private TextView mMovieLengthView;
    private TextView mMovieRatingView;
    private TextView mMovieOverviewView;
    private LinearLayout mTrailerLayout;
    private LinearLayout mReviewLayout;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Get movie db Id from our arguments
            mUri = getArguments().getParcelable(MOVIE_URI);
            mLoadFavorites = getArguments().getBoolean(LOAD_FAVORITES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView()");

        // Inflate the layout for this fragment
        mFragmentView = inflater.inflate(R.layout.fragment_detail, container, false);
        mTitleView = (TextView) mFragmentView.findViewById(R.id.detail_fragment_title);
        mMovieImageView = (ImageView) mFragmentView.findViewById(R.id.detail_fragment_poster_image_view);
        mMovieYearView = (TextView) mFragmentView.findViewById(R.id.detail_fragment_year_text_view);
        mMovieLengthView = (TextView) mFragmentView.findViewById(R.id.detail_fragment_movie_length_text_view);
        mMovieRatingView = (TextView) mFragmentView.findViewById(R.id.detail_fragment_rating_text_view);
        mMovieOverviewView = (TextView) mFragmentView.findViewById(R.id.detail_fragment_overview);
        mTrailerLayout = (LinearLayout) mFragmentView.findViewById(R.id.detail_fragment_trailers);
        mReviewLayout = (LinearLayout) mFragmentView.findViewById(R.id.detail_fragment_reviews);

        // Setup our clickListener
        mFragmentView.findViewById(R.id.detail_fragment_favorite_button).setOnClickListener(this);

        mContext = getContext();
        mFetchMovieDetailsTask = new FetchMovieDetailsTask(mContext);
        if (mUri != null) {
            Cursor c = mContext.getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,
                    MOVIES_COLUMNS,
                    MovieContract.MovieEntry._ID + " = " + MovieContract.MovieEntry.getMovieIdFromUri(mUri),
                    null,
                    null);
            assert c != null;
            if (c.moveToFirst()) {
                mFetchMovieDetailsTask.execute(c.getLong(COL_MOVIE_DB_ID));
                Log.v(LOG_TAG, "Fetching details for movieDbId=" + c.getLong(COL_MOVIE_DB_ID));
            }
        }

        return mFragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        getLoaderManager().initLoader(TRAILERS_LOADER, null, this);
        getLoaderManager().initLoader(REVIEWS_LOADER, null, this);
    }

    @Override
    public void onClick(View v) {
        Log.v(LOG_TAG, "onClick(): " + mUri);
        //TODO: Add functionality for saving movies
    }

    // Called on wide screen devices
    public void updateDetailView(Uri uri) {
        Long movieDbId = Long.parseLong(uri.getQueryParameter(MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID));
        mFetchMovieDetailsTask.execute(movieDbId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MOVIES_LOADER:
                return new CursorLoader(getActivity(), mUri, MOVIES_COLUMNS, null, null, null);
            case TRAILERS_LOADER:
                return new CursorLoader(getActivity(), MovieContract.TrailerEntry.CONTENT_URI, TRAILER_COLUMNS, null, null, null);
            case REVIEWS_LOADER:
                return new CursorLoader(getActivity(), MovieContract.ReviewEntry.CONTENT_URI, REVIEWS_COLUMNS, null, null, null);
            default:
                break;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();

        int movieId = Integer.valueOf(MovieContract.MovieEntry.getMovieIdFromUri(mUri));
        String[] colNames = data.getColumnNames();
        for (String colName : colNames) {
            Log.v(LOG_TAG, colName);
        }

        switch (loaderId) {
            case MOVIES_LOADER:

                while (data.moveToNext() && data.getInt(COL_MOVIE_ID) != movieId) {
                }

                // Set the title
                mTitleView.setText(data.getString(COL_MOVIE_TITLE));

                // Set the poster/image
                String posterUrlString = data.getString(COL_MOVIE_POSTER_URL);
                RequestCreator requestCreator = Picasso.with(getActivity()).load(posterUrlString);
                requestCreator.into(mMovieImageView);
                mMovieImageView.setContentDescription(posterUrlString);

                // Set the release date
                mMovieLengthView.setText(String.valueOf(data.getInt(COL_MOVIE_LENGTH)));

                // Set the description
                mMovieOverviewView.setText(data.getString(COL_MOVIE_DESCRIPTION));

                // Set the rating
                mMovieRatingView.setText(data.getString(COL_MOVIE_RATING));
                break;

            case TRAILERS_LOADER:
                while (data.moveToNext() && (data.getInt(COL_TRAILER_MOVIE_ID) == movieId)) {
                    final String URL = data.getString(COL_TRAILER_URL);
                    final String name = data.getString(COL_TRAILER_DESCRIPTION);

                    // Add the view
                    TextView trailer = (TextView) View.inflate(mContext, R.layout.trailer_link_view, null);
                    trailer.setText(name);

                    // Set the onClick Listener to pull up the Youtube links
                    trailer.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Uri builtUri = Uri.parse(URL)
                                    .buildUpon()
                                    .build();
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, builtUri);
                                mContext.startActivity(intent);
                            } catch (ActivityNotFoundException ex) {
                                Intent youtubeIntent = new Intent(Intent.ACTION_VIEW,
                                        builtUri);
                                mContext.startActivity(youtubeIntent);
                            }
                        }
                    });
                    mTrailerLayout.addView(trailer);
                }
                break;
            case REVIEWS_LOADER:
                while (data.moveToNext() && (data.getInt(COL_REVIEW_MOVIE_KEY) == movieId)) {
                    LinearLayout reviewLayout = (LinearLayout) View.inflate(mContext, R.layout.review_view, null);
                    ((TextView) reviewLayout.findViewById(R.id.detail_view_author_text_view)).setText(data.getString(COL_REVIEW_USER));
                    ((TextView) reviewLayout.findViewById(R.id.movie_review_text_view)).setText(data.getString(COL_REVIEW_DESCRIPTION));
                    mReviewLayout.addView(reviewLayout);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
