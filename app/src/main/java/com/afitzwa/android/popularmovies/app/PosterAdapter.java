package com.afitzwa.android.popularmovies.app;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;


public class PosterAdapter extends CursorAdapter {
    public static final String LOG_TAG = PosterAdapter.class.getSimpleName();

    public PosterAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context)
                .inflate(R.layout.list_item_poster,
                        parent,
                        false
                );
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView posterView = (ImageView) view;
        String posterUrl = cursor.getString(MoviesFragment.COL_MOVIE_POSTER_URL);
        posterView.setContentDescription(cursor.getString(MoviesFragment.COL_MOVIE_TITLE));
        RequestCreator requestCreator = Picasso.with(context).load(posterUrl);
        requestCreator.into(posterView);
    }
}
