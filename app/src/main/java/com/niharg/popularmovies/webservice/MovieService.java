package com.niharg.popularmovies.webservice;

import com.niharg.popularmovies.model.DiscoverResults;
import com.niharg.popularmovies.model.ReviewResults;
import com.niharg.popularmovies.model.VideoResults;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by niharg on 2/5/16 at 10:46 PM.
 */
public interface MovieService {

    @GET("discover/movie")
    Call<DiscoverResults> getPopularMovies(@Query("api_key") String apiKey, @Query("sort_by") String sortBy, @Query("vote_count.gte") String minVotes);

    @GET("movie/{id}/videos")
    Call<VideoResults> getVideosForMovie(@Path("id") long id, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<ReviewResults> getReviewsForMovie(@Path("id") long id, @Query("api_key") String apiKey);
}
