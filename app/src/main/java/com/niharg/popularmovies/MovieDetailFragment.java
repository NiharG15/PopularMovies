package com.niharg.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.niharg.popularmovies.database.DatabaseContract;
import com.niharg.popularmovies.database.FavoritesProvider;
import com.niharg.popularmovies.model.Movie;
import com.niharg.popularmovies.model.Review;
import com.niharg.popularmovies.model.ReviewResults;
import com.niharg.popularmovies.model.Video;
import com.niharg.popularmovies.model.VideoResults;
import com.niharg.popularmovies.webservice.MovieService;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MovieDetailFragment extends Fragment {

    private static final String ARG_MOVIE = "movie";
    public static final String TMDB_BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780";

    private MenuItem star;
    private MenuItem share;

    private ImageView poster;
    private ImageView backdrop;
    private TextView title;
    private TextView relDate;
    private TextView overview;
    private TextView rating;
    private TextView origTitle;
    private LinearLayout trailerView;
    private LinearLayout reviewView;

    private View mParent;

    private Movie mMovie;

    private boolean favorite = false;

    boolean trailerSharePrepared = false;
    private Uri trailerUri;

    boolean offlineMode = false;

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

        setHasOptionsMenu(true);
        trailerSharePrepared = false;
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        mParent = v;

        poster = (ImageView) v.findViewById(R.id.poster);
        backdrop = (ImageView) v.findViewById(R.id.backdrop);
        title = (TextView) v.findViewById(R.id.title);
        relDate = (TextView) v.findViewById(R.id.relDate);
        overview = (TextView) v.findViewById(R.id.overview);
        rating = (TextView) v.findViewById(R.id.rating);
        origTitle = (TextView) v.findViewById(R.id.originalTitle);
        trailerView = (LinearLayout) v.findViewById(R.id.trailerView);
        reviewView = (LinearLayout) v.findViewById(R.id.reviewView);

        Picasso.with(getContext()).load(MovieGridAdapter.TMDB_IMAGE_BASE_URL + mMovie.getPosterPath()).into(poster);
        Picasso.with(getContext()).load(TMDB_BACKDROP_BASE_URL + mMovie.getBackdropPath()).into(backdrop);

        backdrop.setColorFilter(Color.parseColor("#A6000000"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date date = sdf.parse(mMovie.getRelDate(), new ParsePosition(0));

        title.setText(String.format("%s (%s)", mMovie.getTitle(), DateFormat.format("yyyy", date)));
        relDate.setText(DateFormat.format("MMMM dd, yyyy", date));
        overview.setText(mMovie.getDescription());
        rating.setText(String.format(Locale.US, "%2.1f / 10", Double.parseDouble(mMovie.getVoteAvg())));
        origTitle.setText(mMovie.getOrigTitle());



        NetworkInfo networkInfo = ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected()) {


        /*
            Trailers
        */
            new FetchVideoTask(getContext()).execute(mMovie.getId());

        /*
            Reviews
        */

            new FetchReviewTask(getContext()).execute(mMovie.getId());

        } else {
            offlineMode = true;
            Snackbar.make(container, "Offline Mode", Snackbar.LENGTH_LONG).show();
            reviewView.setVisibility(View.GONE);
            trailerView.setVisibility(View.GONE);
            v.findViewById(R.id.trailerTitle).setVisibility(View.GONE);
            v.findViewById(R.id.reviewTitle).setVisibility(View.GONE);
        }


        Cursor c = getContext().getContentResolver().query(FavoritesProvider.Movies.CONTENT_URI, new String[]{DatabaseContract.MovieEntry.COL_TMDB_ID}, DatabaseContract.MovieEntry.COL_TMDB_ID + "=?", new String[]{Long.toString(mMovie.getId())}, null);

        if(c != null && c.moveToFirst()) {
            favorite = true;
        }

        if(c != null) c.close();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_movie_detail, menu);
        star = menu.findItem(R.id.favorite);
        if(favorite) {
            if(star != null) {
                star.setIcon(R.drawable.ic_star_white_24dp);
            }
        }

        share = menu.findItem(R.id.share);
        if(share != null && offlineMode) {
            share.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.favorite) {
            item.setIcon(R.drawable.ic_star_white_24dp);
           // MovieDbHelper dbHelper = new MovieDbHelper(getContext());
           // SQLiteDatabase db = dbHelper.getWritableDatabase();

            if(!favorite) {

                ContentValues contentValues = new ContentValues();
                contentValues.put(DatabaseContract.MovieEntry.COL_TMDB_ID, mMovie.getId());
                contentValues.put(DatabaseContract.MovieEntry.COL_TITLE, mMovie.getTitle());
                contentValues.put(DatabaseContract.MovieEntry.COL_DESC, mMovie.getDescription());
                contentValues.put(DatabaseContract.MovieEntry.COL_POSTER_PATH, mMovie.getPosterPath());
                contentValues.put(DatabaseContract.MovieEntry.COL_REL_DATE, mMovie.getRelDate());
                contentValues.put(DatabaseContract.MovieEntry.COL_ORIGINAL_TITLE, mMovie.getOrigTitle());
                contentValues.put(DatabaseContract.MovieEntry.COL_BACKDROP_PATH, mMovie.getBackdropPath());
                contentValues.put(DatabaseContract.MovieEntry.COL_VOTE_AVG, mMovie.getVoteAvg());

//                long res = db.insert(DatabaseContract.MovieEntry.TABLE_NAME, null, contentValues);

                Uri u = getContext().getContentResolver().insert(FavoritesProvider.Movies.CONTENT_URI, contentValues);

                favorite = true;
            } else {
                favorite = false;
                item.setIcon(R.drawable.ic_star_border_white_24dp);
                int n = getContext().getContentResolver().delete(FavoritesProvider.Movies.CONTENT_URI, DatabaseContract.MovieEntry.COL_TMDB_ID + "=?", new String[]{Long.toString(mMovie.getId())});
            }
            //db.close();
            return true;
        }

        if (item.getItemId() == R.id.share) {
            if (trailerSharePrepared) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, trailerUri.toString());
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, getString(R.string.string_share_trailer)));
            } else {
                Snackbar.make(mParent, getContext().getString(R.string.string_trailer_not_loaded), Snackbar.LENGTH_SHORT).show();
            }
        }


        return false;
    }

    private class FetchVideoTask extends AsyncTask<Long, Void, List<Video>> {

        // http://stackoverflow.com/a/8842839/2663152
        public static final String YT_THUMB_BASE = "http://img.youtube.com/vi/%s/0.jpg";
        public static final String YT_VIDEO_BASE = "http://www.youtube.com/watch?v=";

        private Context mContext;

        public FetchVideoTask(Context mContext) {
            this.mContext = mContext;
        }

        boolean networkError = false;

        @Override
        protected List<Video> doInBackground(Long... params) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(MovieFragment.FetchDataTask.TMDB_BASE_URL2)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            MovieService movieService = retrofit.create(MovieService.class);
            Call<VideoResults> call = movieService.getVideosForMovie(params[0], mContext.getString(R.string.tmdb_api_key));

            try {
                Response<VideoResults> response = call.execute();
                return response.body().getVideos();
            } catch (IOException e) {
                e.printStackTrace();
                networkError = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(final List<Video> videos) {
            super.onPostExecute(videos);

            if (videos != null && videos.size() != 0) {
                trailerUri = Uri.parse(YT_VIDEO_BASE + videos.get(0).getKey());
                trailerSharePrepared = true;
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(YT_VIDEO_BASE + videos.get((Integer) v.getTag()).getKey()));
                        startActivity(i);
                    }
                };

                for(int i = 0; i < videos.size(); i++) {
                    //If we have more than two trailers, add more image views.
                    if(i >= 2) {
                        ImageView imageView = new ImageView(mContext);
                        imageView.setLayoutParams(trailerView.getChildAt(0).getLayoutParams());
                        trailerView.addView(imageView);
                    }
                    Picasso.with(mContext).load(Uri.parse(String.format(YT_THUMB_BASE, videos.get(i).getKey()))).into((ImageView) trailerView.getChildAt(i));
                    trailerView.getChildAt(i).setTag(i);
                    trailerView.getChildAt(i).setOnClickListener(onClickListener);
                }

                //If we have only 1 trailer, disable the second ImageView.
                if(videos.size() < 2) {
                    trailerView.getChildAt(1).setVisibility(View.GONE);
                }
            } else {
                if(networkError) {
                    Snackbar.make(mParent, "Network Error", Snackbar.LENGTH_INDEFINITE).show();
                }
            }
        }
    }

    public class FetchReviewTask extends AsyncTask<Long, Void, List<Review>> {

        private Context mContext;
        private boolean networkError = false;

        public FetchReviewTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected List<Review> doInBackground(Long... params) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(MovieFragment.FetchDataTask.TMDB_BASE_URL2)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            MovieService movieService = retrofit.create(MovieService.class);

            Call<ReviewResults> call = movieService.getReviewsForMovie(mMovie.getId(), mContext.getString(R.string.tmdb_api_key));

            try {
                Response<ReviewResults> response = call.execute();
                return response.body().getResults();
            } catch (IOException e) {
                e.printStackTrace();
                networkError = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(final List<Review> reviews) {
            super.onPostExecute(reviews);

            if (reviews != null) {
                if(reviews.size() == 0) {
                    ((TextView) reviewView.getChildAt(0)).setText(R.string.string_no_reviews);
                    return;
                }

                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(reviews.get((Integer) v.getTag()).getUrl()));
                        startActivity(i);
                    }
                };

                for(int i = 0; i < reviews.size(); i++) {
                    if(i >= 1) {
                        TextView v = new TextView(mContext);
                        v.setLayoutParams(reviewView.getChildAt(0).getLayoutParams());
                        v.setTextColor(mContext.getResources().getColor(android.R.color.white));
                        v.setMaxLines(20);
                        v.setEllipsize(TextUtils.TruncateAt.END);
                        TypedValue outValue = new TypedValue();
                        mContext.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
                        v.setBackgroundResource(outValue.resourceId);
                        reviewView.addView(v);
                    }
                    String s1 = (reviews.get(i).getContent().length() > 400 ? reviews.get(i).getContent().substring(0, 400) : reviews.get(i).getContent());
                    ((TextView) reviewView.getChildAt(i)).setText(String.format(mContext.getString(R.string.string_review_format), reviews.get(i).getAuthor(), s1));
                    reviewView.getChildAt(i).setTag(i);
                    reviewView.getChildAt(i).setOnClickListener(onClickListener);
                }
            } else {
                if(networkError) {
                    ((TextView) reviewView.getChildAt(0)).setText(R.string.string_review_network_error);
                } else {
                    ((TextView) reviewView.getChildAt(0)).setText(R.string.string_no_reviews);
                }
            }
        }
    }

}
