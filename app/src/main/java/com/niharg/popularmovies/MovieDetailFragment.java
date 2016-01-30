package com.niharg.popularmovies;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MovieDetailFragment extends Fragment {

    private static final String ARG_MOVIE = "movie";
    public static final String TMDB_BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780";


    private Movie mMovie;


    public MovieDetailFragment() {
        // Required empty public constructor
    }

    public static MovieDetailFragment newInstance(Movie param1) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MOVIE, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovie = (Movie) getArguments().getSerializable(ARG_MOVIE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        ImageView poster = (ImageView) v.findViewById(R.id.poster);
        ImageView backdrop = (ImageView) v.findViewById(R.id.backdrop);
        TextView title = (TextView) v.findViewById(R.id.title);
        TextView relDate = (TextView) v.findViewById(R.id.relDate);
        TextView overview = (TextView) v.findViewById(R.id.overview);
        TextView rating = (TextView) v.findViewById(R.id.rating);

        Picasso.with(getContext()).load(MovieGridAdapter.TMDB_IMAGE_BASE_URL + mMovie.getPosterPath()).into(poster);
        Picasso.with(getContext()).load(TMDB_BACKDROP_BASE_URL + mMovie.getBackdropPath()).into(backdrop);

        backdrop.setColorFilter(Color.parseColor("#A6000000"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date date = sdf.parse(mMovie.getRelDate(), new ParsePosition(0));

        title.setText(String.format("%s (%s)", mMovie.getTitle(), DateFormat.format("yyyy", date)));
        relDate.setText(DateFormat.format("MMMM dd, yyyy", date));
        overview.setText(mMovie.getDescription());
        rating.setText(String.format("%2.1f / 10", Double.parseDouble(mMovie.getVoteAvg())));

        return v;
    }

}
