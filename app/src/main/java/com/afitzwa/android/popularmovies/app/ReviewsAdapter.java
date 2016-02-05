package com.afitzwa.android.popularmovies.app;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by AndrewF on 1/31/2016.
 */
public class ReviewsAdapter extends CursorAdapter {

    public static class ViewHolder {
        public final TextView authorTextView;
        public final TextView reviewTextView;

        public ViewHolder(View view) {
            authorTextView = (TextView) view.findViewById(R.id.detail_fragment_review_author_text_view);
            reviewTextView = (TextView) view.findViewById(R.id.detail_fragment_review_text_view);
        }
    }

    public ReviewsAdapter(Context context) {
        super(context, null, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.review_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.authorTextView.setText(cursor.getString(DetailFragment.COL_REVIEW_USER));
        viewHolder.reviewTextView.setText(cursor.getString(DetailFragment.COL_REVIEW_DESCRIPTION));
    }
}
