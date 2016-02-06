package com.niharg.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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


    private ImageView poster;
    private ImageView backdrop;
    private TextView title;
    private TextView relDate;
    private TextView overview;
    private TextView rating;
    private TextView origTitle;
    private LinearLayout trailerView;
    private LinearLayout reviewView;

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

        /*
            Trailers
         */

        new FetchVideoTask(getContext()).execute(mMovie.getId());

        /*
            Reviews
         */

        new FetchReviewTask(getContext()).execute(mMovie.getId());

        return v;
    }

    private class FetchVideoTask extends AsyncTask<Long, Void, List<Video>> {

        // http://stackoverflow.com/a/8842839/2663152
        public static final String YT_THUMB_BASE = "http://img.youtube.com/vi/%s/0.jpg";
        public static final String YT_VIDEO_BASE = "http://www.youtube.com/watch?v=";

        private Context mContext;

        public FetchVideoTask(Context mContext) {
            this.mContext = mContext;
        }

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
            }

            return null;
        }

        @Override
        protected void onPostExecute(final List<Video> videos) {
            super.onPostExecute(videos);

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
        }
    }

    public class FetchReviewTask extends AsyncTask<Long, Void, List<Review>> {

        private Context mContext;

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
            }

            return null;
        }

        @Override
        protected void onPostExecute(final List<Review> reviews) {
            super.onPostExecute(reviews);

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
        }
    }

}
