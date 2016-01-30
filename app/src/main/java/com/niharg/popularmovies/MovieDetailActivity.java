package com.niharg.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MovieDetailActivity extends AppCompatActivity {

    public static final String ARG_MOVIE = "movie";

    public static final String TAG_MD_FRAGMENT = "movie_detail_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        MovieDetailFragment movieDetailFragment = MovieDetailFragment.newInstance((Movie) getIntent().getExtras().getSerializable(ARG_MOVIE));
        getSupportFragmentManager().beginTransaction().replace(R.id.container_movie_detail, movieDetailFragment, TAG_MD_FRAGMENT).commit();

    }
}
