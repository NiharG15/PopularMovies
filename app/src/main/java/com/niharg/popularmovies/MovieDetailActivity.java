package com.niharg.popularmovies;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Transition;
import android.view.MenuItem;

import com.niharg.popularmovies.model.Movie;

public class MovieDetailActivity extends AppCompatActivity {

    public static final String ARG_MOVIE = "movie";

    public static final String TAG_MD_FRAGMENT = "movie_detail_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            /*
                Code taken from: http://stackoverflow.com/questions/26600263/how-do-i-prevent-the-status-bar-and-navigation-bar-from-animating-during-an-acti

            */

            Transition fade = new Fade();
            fade.excludeTarget(android.R.id.statusBarBackground, true);
            fade.excludeTarget(android.R.id.navigationBarBackground, true);
            getWindow().setEnterTransition(fade);
            getWindow().setExitTransition(fade);
        }
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MovieDetailFragment movieDetailFragment = MovieDetailFragment.newInstance((Movie) getIntent().getExtras().getSerializable(ARG_MOVIE));
        getSupportFragmentManager().beginTransaction().replace(R.id.container_movie_detail, movieDetailFragment, TAG_MD_FRAGMENT).commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            ActivityCompat.finishAfterTransition(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
