package com.afitzwa.android.popularmovies.app;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Cursor adapter for populating favorites list view
 */
public class FavoritesPosterAdapter extends CursorAdapter {

    public FavoritesPosterAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.favorites_text_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view;
        textView.setText(cursor.getString(FavoriteMoviesFragment.MOVIE_COL_TITLE));
        textView.setPadding(0, 8, 0, 0);
        textView.setTextAppearance(context, R.style.ListText);
    }
}
