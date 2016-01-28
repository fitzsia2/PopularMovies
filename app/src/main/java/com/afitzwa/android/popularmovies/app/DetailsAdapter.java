package com.afitzwa.android.popularmovies.app;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by AndrewF on 1/23/2016.
 */
public class DetailsAdapter extends CursorAdapter {

    public static class ViewHolder {

        public final TextView titleView;
        public final ImageView posterView;
        public final TextView yearView;
        public final TextView lengthView;
        public final TextView ratingView;
        public final TextView overviewView;
        public final TextView trailerDescriptionView;
        public final LinearLayout reviewView;

        public ViewHolder(View view) {
            titleView = (TextView) view.findViewById(R.id.detail_fragment_title);
            posterView = (ImageView) view.findViewById(R.id.detail_fragment_poster_image_view);
            yearView = (TextView) view.findViewById(R.id.detail_fragment_year_text_view);
            lengthView = (TextView) view.findViewById(R.id.detail_fragment_movie_length_text_view);
            ratingView = (TextView) view.findViewById(R.id.detail_fragment_rating_text_view);
            overviewView = (TextView) view.findViewById(R.id.detail_fragment_overview);
            trailerDescriptionView = (TextView) view.findViewById(R.id.detail_trailer_name);
            reviewView = (LinearLayout) view.findViewById(R.id.detail_fragment_reviews);
        }
    }

    public DetailsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layoutId = -1;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.titleView.setText(cursor.getString(DetailFragment.COL_MOVIE_TITLE));
    }
}
