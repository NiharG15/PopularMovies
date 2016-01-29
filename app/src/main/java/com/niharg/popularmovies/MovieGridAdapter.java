package com.niharg.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by niharg on 1/29/16 at 7:55 PM.
 */
public class MovieGridAdapter extends ArrayAdapter<Movie> {

    public static final String TMDB_IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185";

    public MovieGridAdapter(Context context, ArrayList<Movie> data) {
        super(context, R.layout.grid_item_movie, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_movie, parent, false);
        }

        ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
        Uri posterUri = Uri.parse(TMDB_IMAGE_BASE_URL + movie.getPosterPath());
        Picasso.with(getContext()).load(posterUri).into(thumbnail);

        return convertView;
    }


}
