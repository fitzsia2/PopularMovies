package com.afitzwa.android.popularmovies.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.List;


public class PosterAdapter extends ArrayAdapter<MovieInfo> {
    private Context mContext;

    public PosterAdapter(Context context, List<MovieInfo> movieInfoList) {
        super(context, 0, movieInfoList);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String urlString = getItem(position).posterUrl;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_poster, parent, false);
        }

        ImageView view = (ImageView) convertView.findViewById(R.id.list_item_poster_image_view);
        RequestCreator requestCreator = Picasso.with(mContext).load(urlString);
        requestCreator.into(view);

        return convertView;
    }
}
