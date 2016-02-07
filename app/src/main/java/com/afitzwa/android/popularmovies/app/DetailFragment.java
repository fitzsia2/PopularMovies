package com.afitzwa.android.popularmovies.app;


import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afitzwa.android.popularmovies.app.data.MovieContract;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.Vector;


/**
 * Fragment containing details of a selected movie.
 */
public class DetailFragment extends Fragment implements View.OnClickListener, FetchMovieDetailsTask.Callback {

    public static final String MOVIE_DB_ID = "movie db id";
    private static final String ADD_FAV = "Mark as favorite";
    private static final String RM_FAV = "Remove from favorites";
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private TextView mTitleTextView;
    private ImageView mMovieImageView;
    private TextView mYearTextView;
    private TextView mMovieLengthView;
    private TextView mMovieRatingView;
    private TextView mOverviewTextView;
    private Button mFavoriteButton;
    private LinearLayout mTrailersLinearLayout;
    private LinearLayout mReviewsLinearLayout;

    private long mMovieDbId;
    private boolean mIsAFavorite;
    private MovieInfo mMovieInfo;
    private ShareActionProvider mShareActionProvider;

    private static final String[] MOVIES_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
    };

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

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
        mTrailersLinearLayout = (LinearLayout) fragmentView.findViewById(R.id.detail_fragment_trailers_linear_layout);
        mReviewsLinearLayout = (LinearLayout) fragmentView.findViewById(R.id.detail_fragment_reviews_linear_layout);

        // Setup our clickListener
        fragmentView.findViewById(R.id.detail_fragment_favorite_button).setOnClickListener(this);

        // mMovieDbId will be 0 when we're loaded in two-pane mode. We won't be able to load
        // details until the user has selected a movie. Make sure it is not visible.
        if (mMovieDbId > 0) {
            FetchMovieDetailsTask fmdt = new FetchMovieDetailsTask(getContext());
            fmdt.setCallBackCaller(this);
            fmdt.execute(mMovieDbId);
        } else {
            fragmentView = inflater.inflate(R.layout.empty_detail, container, false);
        }
        return fragmentView;
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
        ContentResolver contentResolver = getContext().getContentResolver();
        // If it is in the database delete it, change the button text and finish
        if (mIsAFavorite) {
            int movieRowsDeleted = contentResolver.delete(MovieContract.MovieEntry.CONTENT_URI,
                    MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID + " = " + mMovieInfo.movieDbId,
                    null);
            int reviewRowsDeleted = contentResolver.delete(MovieContract.ReviewEntry.CONTENT_URI,
                    MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + " = " + mMovieInfo.movieDbId,
                    null);
            int trailerRowsDeleted = contentResolver.delete(MovieContract.TrailerEntry.CONTENT_URI,
                    MovieContract.TrailerEntry.COLUMN_MOVIE_KEY + " = " + mMovieInfo.movieDbId,
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
            trailersInserted = contentResolver.bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, trailerCVA);

            // Save the reviews info
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
            int reviewsInserted = contentResolver.bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, reviewCVA);

            Log.v(LOG_TAG, "Inserted " + moviesInserted + " movie row, " + trailersInserted + " trailer rows, " + reviewsInserted + " review rows");
            mIsAFavorite = true;
        }

        if (mIsAFavorite) {
            ((Button) v).setText(RM_FAV);
        } else {
            ((Button) v).setText(ADD_FAV);
        }
    }


    /**
     * Our AsyncTask has finished. Load the views.
     *
     * @param movieInfo a holder for all of our details
     */
    @Override
    public void loadedDetails(MovieInfo movieInfo) {

        mMovieInfo = movieInfo;

        // Set the title
        mTitleTextView.setText(movieInfo.title);

        // Set the poster/image
        String posterUrlString = movieInfo.posterUrl;
        RequestCreator requestCreator = Picasso.with(getActivity()).load(posterUrlString);
        requestCreator.into(mMovieImageView);
        mMovieImageView.setContentDescription(posterUrlString);

        // Set the release date
        mYearTextView.setText(movieInfo.releaseDate);

        // Set the description
        mOverviewTextView.setText(movieInfo.overview);

        // Set the rating
        mMovieRatingView.setText(movieInfo.rating);

        // Query the database to see if we have this movie is already a favorite
        // Set the text of the favorites button
        Cursor c = getContext().getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                MOVIES_COLUMNS,
                MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID + " = " + mMovieDbId,
                null,
                null
        );
        if (c != null) {
            if (c.moveToFirst()) {
                mIsAFavorite = true;
            }
            c.close();
        } else {
            mIsAFavorite = false;
        }
        String buttonText = mIsAFavorite ? RM_FAV : ADD_FAV;
        mFavoriteButton.setText(buttonText);

        // Set the length
        mMovieLengthView.setText(String.valueOf(movieInfo.runtime));

        for (MovieInfo.Trailer trailer : movieInfo.trailers) {
            TextView trailerTextView = (TextView) View.inflate(getContext(), R.layout.trailer_link_view, null);
            final String URL = trailer.mUrl;
            trailerTextView.setText(trailer.mName);
            trailerTextView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Uri builtUri = Uri.parse(URL)
                            .buildUpon()
                            .build();
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, builtUri);
                        getContext().startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        Log.e(LOG_TAG, "Could not find activity! " + ex);
                    }
                }
            });
            mTrailersLinearLayout.addView(trailerTextView);
        }

        for (MovieInfo.Review review : movieInfo.reviews) {
            LinearLayout reviewLinearLayout = (LinearLayout) View.inflate(getContext(), R.layout.review_view, null);
            ((TextView) reviewLinearLayout.findViewById(R.id.detail_fragment_review_author_text_view)).setText(review.mAuthor);
            ((TextView) reviewLinearLayout.findViewById(R.id.detail_fragment_review_text_view)).setText(review.mReview);
            mReviewsLinearLayout.addView(reviewLinearLayout);
        }

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mMovieInfo != null && mMovieInfo.title != null) {
            mShareActionProvider.setShareIntent(Utility.CreateShareMovieIntent(mMovieInfo));
        }
    }
}
