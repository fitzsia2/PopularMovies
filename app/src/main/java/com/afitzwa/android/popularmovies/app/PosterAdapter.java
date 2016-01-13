package com.afitzwa.android.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.List;


public class PosterAdapter extends ArrayAdapter<Poster> {
    public static final String LOG_TAG = PosterAdapter.class.getSimpleName();
    private int mCurrentPos = -1;

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the List is the data we want
     * to populate into the lists
     *
     * @param context The current context. Used to inflate the layout file.
     * @param posters A List of poster objects to display in a list
     */
    public PosterAdapter(Activity context, List<Poster> posters) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, posters);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate.
     *                    (search online for "android view recycling" to learn more)
     * @param parent      The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = getContext();

        // Gets the url string from the ArrayAdapter at the appropriate position
        String urlString = getItem(position).posterUrl;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_poster, parent, false);
        }

        ImageView view = (ImageView) convertView.findViewById(R.id.list_item_poster_image_view);
        RequestCreator requestCreator = Picasso.with(context).load(urlString)/*.into(view)*/;
        requestCreator.into(view);

        return convertView;
    }
}
