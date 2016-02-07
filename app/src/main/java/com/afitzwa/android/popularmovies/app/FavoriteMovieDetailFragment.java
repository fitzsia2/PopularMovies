package com.afitzwa.android.popularmovies.app;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afitzwa.android.popularmovies.app.data.MovieContract;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Fragment used for loading the details of a favorite movie
 */
public class FavoriteMovieDetailFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = FavoriteMovieDetailFragment.class.getSimpleName();

    private MovieInfo mMovieInfo = new MovieInfo();

    public static final String MOVIE_DB_ID = "movie db id";
    private static final String ADD_FAV = "Mark as favorite";
    private static final String RM_FAV = "Remove from favorites";
    private boolean mIsAFavorite = true;

    private long mMovieDbId;
    private static ShareActionProvider mShareActionProvider;

    private TextView mTitleTextView;
    private TextView mYearTextView;
    private TextView mMovieLengthView;
    private TextView mMovieRatingView;
    private TextView mOverviewTextView;
    private Button mFavoriteButton;
    private LinearLayout mTrailersLinearLayout;
    private LinearLayout mReviewsLinearLayout;

    private static final int MOVIE_LOADER = 1;
    private static final String[] MOVIE_COLS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_YEAR,
            MovieContract.MovieEntry.COLUMN_LENGTH,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_DESCRIPTION
    };
    static final int MOVIE_COL_TITLE = 1;
    static final int MOVIE_COL_YEAR = 2;
    static final int MOVIE_COL_LEN = 3;
    static final int MOVIE_COL_RATING = 4;
    static final int MOVIE_COL_DESCR = 5;

    static final int TRAILER_LOADER = 2;
    private static final String[] TRAILER_COLS = {
            MovieContract.TrailerEntry.TABLE_NAME + "." + MovieContract.TrailerEntry._ID,
            MovieContract.TrailerEntry.COLUMN_DESCRIPTION,
            MovieContract.TrailerEntry.COLUMN_TRAILER_URL
    };
    static final int TRAILER_COL_DESCR = 1;
    static final int TRAILER_COL_URL = 2;

    private static final int REVIEW_LOADER = 3;
    private static final String[] REVIEW_COLS = {
            MovieContract.ReviewEntry.TABLE_NAME + "." + MovieContract.ReviewEntry._ID,
            MovieContract.ReviewEntry.COLUMN_USER,
            MovieContract.ReviewEntry.COLUMN_DESCRIPTION
    };
    static final int REVIEW_COL_USER = 1;
    static final int REVIEW_COL_DESCR = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Get movie db Id from our arguments
            mMovieDbId = getArguments().getLong(MOVIE_DB_ID);
        } else {
            mMovieDbId = 0;
        }
    }

    public FavoriteMovieDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView;
        if (mMovieDbId == 0) {
            fragmentView = inflater.inflate(R.layout.empty_detail, container, false);
        } else {
            fragmentView = inflater.inflate(R.layout.fragment_detail, container, false);
            fragmentView.setVerticalScrollbarPosition(0);
            // Get all the views
            mTitleTextView = (TextView) fragmentView.findViewById(R.id.detail_fragment_title);
            mYearTextView = (TextView) fragmentView.findViewById(R.id.detail_fragment_year_text_view);
            mMovieLengthView = (TextView) fragmentView.findViewById(R.id.detail_fragment_movie_length_text_view);
            mMovieRatingView = (TextView) fragmentView.findViewById(R.id.detail_fragment_rating_text_view);
            mOverviewTextView = (TextView) fragmentView.findViewById(R.id.detail_fragment_overview);
            mFavoriteButton = (Button) fragmentView.findViewById(R.id.detail_fragment_favorite_button);
            mTrailersLinearLayout = (LinearLayout) fragmentView.findViewById(R.id.detail_fragment_trailers_linear_layout);
            mReviewsLinearLayout = (LinearLayout) fragmentView.findViewById(R.id.detail_fragment_reviews_linear_layout);

            // Setup our clickListener
            fragmentView.findViewById(R.id.detail_fragment_favorite_button).setOnClickListener(this);
        }
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onActivityCreated()");
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        getLoaderManager().initLoader(TRAILER_LOADER, null, this);
        getLoaderManager().initLoader(REVIEW_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public void onClick(View v) {

        // If it is in the database delete it, change the button text and finish
        if (mIsAFavorite) {
            int movieRowsDeleted = getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                    MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID + " = " + mMovieInfo.movieDbId,
                    null);
            int trailerRowsDeleted = getContext().getContentResolver().delete(MovieContract.TrailerEntry.CONTENT_URI,
                    MovieContract.TrailerEntry.COLUMN_MOVIE_KEY + " = " + mMovieInfo.movieDbId,
                    null);
            int reviewRowsDeleted = getContext().getContentResolver().delete(MovieContract.ReviewEntry.CONTENT_URI,
                    MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + " = " + mMovieInfo.movieDbId,
                    null);
            Log.v(LOG_TAG, "Deleted " + movieRowsDeleted + " movie row, " + trailerRowsDeleted + " trailer rows, " + reviewRowsDeleted + " review rows");
            mIsAFavorite = false;
        } else {

            // Save the Movie info
            ContentValues movieCV = new ContentValues();
            movieCV.put(MovieContract.MovieEntry.COLUMN_POSTER_URL, mMovieInfo.posterUrl);
            movieCV.put(MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID, mMovieInfo.movieDbId);
            movieCV.put(MovieContract.MovieEntry.COLUMN_TITLE, mMovieInfo.title);
            movieCV.put(MovieContract.MovieEntry.COLUMN_YEAR, mMovieInfo.releaseDate);
            movieCV.put(MovieContract.MovieEntry.COLUMN_LENGTH, mMovieInfo.runtime);
            movieCV.put(MovieContract.MovieEntry.COLUMN_RATING, mMovieInfo.rating);
            movieCV.put(MovieContract.MovieEntry.COLUMN_DESCRIPTION, mMovieInfo.overview);
            getContext().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, movieCV);
            long moviesInserted = 1;

            // Save the trailers info
            long trailersInserted;
            Vector<ContentValues> trailerCVV = new Vector<>();
            for (MovieInfo.Trailer trailer : mMovieInfo.trailers) {
                ContentValues trailerCV = new ContentValues();
                trailerCV.put(MovieContract.TrailerEntry.COLUMN_MOVIE_KEY, mMovieInfo.movieDbId);
                trailerCV.put(MovieContract.TrailerEntry.COLUMN_DESCRIPTION, trailer.mName);
                trailerCV.put(MovieContract.TrailerEntry.COLUMN_TRAILER_URL, trailer.mUrl);
                trailerCVV.add(trailerCV);
            }
            ContentValues[] trailerCVA = new ContentValues[trailerCVV.size()];
            trailerCVV.toArray(trailerCVA);
            trailersInserted = getContext().getContentResolver().bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, trailerCVA);

            // Save the reviews info
            long reviewsInserted;
            Vector<ContentValues> reviewCVV = new Vector<>();
            for (MovieInfo.Review review : mMovieInfo.reviews) {
                ContentValues reviewCV = new ContentValues();
                reviewCV.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, mMovieInfo.movieDbId);
                reviewCV.put(MovieContract.ReviewEntry.COLUMN_USER, review.mAuthor);
                reviewCV.put(MovieContract.ReviewEntry.COLUMN_DESCRIPTION, review.mReview);
                reviewCVV.add(reviewCV);
            }
            ContentValues[] reviewCVA = new ContentValues[reviewCVV.size()];
            reviewCVV.toArray(reviewCVA);
            reviewsInserted = getContext().getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, reviewCVA);

            Log.v(LOG_TAG, "Inserted " + moviesInserted + " movie row, " + trailersInserted + " trailer rows, " + reviewsInserted + " review rows");
            mIsAFavorite = true;
        }

        if (mIsAFavorite) {
            ((Button) v).setText(RM_FAV);
        } else {
            ((Button) v).setText(ADD_FAV);
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MOVIE_LOADER:
                Log.v(LOG_TAG, "onCreateLoader()::MOVIE_LOADER");
                return new CursorLoader(getActivity(), MovieContract.MovieEntry.CONTENT_URI,
                        MOVIE_COLS,
                        MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID + " = " + mMovieDbId,
                        null,
                        null);
            case TRAILER_LOADER:
                Log.v(LOG_TAG, "onCreateLoader()::TRAILER_LOADER");
                return new CursorLoader(getActivity(), MovieContract.TrailerEntry.CONTENT_URI,
                        TRAILER_COLS,
                        MovieContract.TrailerEntry.COLUMN_MOVIE_KEY + " = " + mMovieDbId,
                        null,
                        null);
            case REVIEW_LOADER:
                Log.v(LOG_TAG, "onCreateLoader()::REVIEW_LOADER");
                return new CursorLoader(getActivity(), MovieContract.ReviewEntry.CONTENT_URI,
                        REVIEW_COLS,
                        MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + " = " + mMovieDbId,
                        null,
                        null);
            default:
                break;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        switch (id) {
            case MOVIE_LOADER:
                Log.v(LOG_TAG, "onLoadFinished()::MOVIE_LOADER");
                if (data.moveToFirst()) {
                    mMovieInfo.movieDbId = mMovieDbId;

                    mMovieInfo.title = data.getString(MOVIE_COL_TITLE);
                    mTitleTextView.setText(mMovieInfo.title);

                    mMovieInfo.releaseDate = String.valueOf(data.getInt(MOVIE_COL_YEAR));
                    mYearTextView.setText(mMovieInfo.releaseDate);

                    mMovieInfo.runtime = String.valueOf(data.getInt(MOVIE_COL_LEN));
                    mMovieLengthView.setText(mMovieInfo.runtime);

                    mMovieInfo.rating = data.getString(MOVIE_COL_RATING);
                    mMovieRatingView.setText(mMovieInfo.rating);

                    mMovieInfo.overview = data.getString(MOVIE_COL_DESCR);
                    mOverviewTextView.setText(mMovieInfo.overview);

                    mFavoriteButton.setText(RM_FAV);
                }
                break;
            case TRAILER_LOADER:
                Log.v(LOG_TAG, "onLoadFinished()::TRAILER_LOADER");
                List<MovieInfo.Trailer> trailerList = new ArrayList<>();
                while (data.moveToNext()) {
                    TextView trailerTextView = (TextView) View.inflate(getContext(), R.layout.trailer_link_view, null);
                    final String URL = data.getString(TRAILER_COL_URL);
                    final String DESC = data.getString(TRAILER_COL_DESCR);
                    trailerList.add(new MovieInfo.Trailer(DESC, URL));
                    trailerTextView.setText(DESC);

                    // Set the onClick Listener to pull up the Youtube links
                    trailerTextView.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Uri builtUri = Uri.parse(URL)
                                    .buildUpon()
                                    .build();
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, builtUri);
                                getContext().startActivity(intent);
                            } catch (ActivityNotFoundException ex) {
                                Intent youtubeIntent = new Intent(Intent.ACTION_VIEW,
                                        builtUri);
                                getContext().startActivity(youtubeIntent);
                            }
                        }
                    });
                    mTrailersLinearLayout.addView(trailerTextView);
                }
                mMovieInfo.trailers = trailerList;
                break;
            case REVIEW_LOADER:
                Log.v(LOG_TAG, "onLoadFinished()::REVIEW_LOADER");
                List<MovieInfo.Review> reviewList = new ArrayList<>();
                while (data.moveToNext()) {
                    String author = data.getString(REVIEW_COL_USER);
                    String review = data.getString(REVIEW_COL_DESCR);
                    reviewList.add(new MovieInfo.Review(author, review));
                    LinearLayout reviewLinearLayout = (LinearLayout) View.inflate(getContext(), R.layout.review_view, null);
                    ((TextView) reviewLinearLayout.findViewById(R.id.detail_fragment_review_author_text_view)).setText(author);
                    ((TextView) reviewLinearLayout.findViewById(R.id.detail_fragment_review_text_view)).setText(review);
                    mReviewsLinearLayout.addView(reviewLinearLayout);
                }
                mMovieInfo.reviews = reviewList;
                break;
            default:
                break;
        }

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mMovieInfo != null && mMovieInfo.title != null) {
            Intent intent = Utility.CreateShareMovieIntent(mMovieInfo);
            mShareActionProvider.setShareIntent(intent);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG, "onLoaderReset()");
    }
}
