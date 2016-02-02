package com.afitzwa.android.popularmovies.app;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by AndrewF on 1/31/2016.
 */
public class ReviewsAdapter extends CursorAdapter {

    public ReviewsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.review_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        LinearLayout ll = (LinearLayout) view.findViewById(R.id.movie_review_layout);
        TextView author = (TextView) ll.findViewById(R.id.detail_view_author_text_view);
        author.setText(cursor.getString(DetailFragment.COL_REVIEW_USER));
        TextView movieDescription = (TextView) ll.findViewById(R.id.movie_review_text_view);
        movieDescription.setText(cursor.getString(DetailFragment.COL_REVIEW_DESCRIPTION));
    }
}
