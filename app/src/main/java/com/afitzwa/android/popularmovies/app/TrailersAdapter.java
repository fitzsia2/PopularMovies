package com.afitzwa.android.popularmovies.app;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by AndrewF on 1/23/2016.
 */
public class TrailersAdapter extends CursorAdapter {
    Context mContext;

    public TrailersAdapter(Context context) {
        super(context, null, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.trailer_link_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        TextView trailerTextView = (TextView) view;
        trailerTextView.setText(cursor.getString(DetailFragment.COL_TRAILER_DESCRIPTION));

        // Set the onClick Listener to pull up the Youtube links
        trailerTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri builtUri = Uri.parse(cursor.getString(DetailFragment.COL_TRAILER_URL))
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
    }
}
