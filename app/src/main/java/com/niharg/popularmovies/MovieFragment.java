package com.niharg.popularmovies;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.niharg.popularmovies.database.DatabaseContract;
import com.niharg.popularmovies.database.FavoritesProvider;
import com.niharg.popularmovies.database.MovieDbHelper;
import com.niharg.popularmovies.model.DiscoverResults;
import com.niharg.popularmovies.model.Movie;
import com.niharg.popularmovies.webservice.MovieService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * A simple {@link Fragment} subclass.
 */
public class MovieFragment extends Fragment {

    MovieGridAdapter mAdapter;
    GridView mMovieGrid;
    public MovieFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_movie, container, false);

        FetchDataTask dataTask = new FetchDataTask(getContext());
        NetworkInfo networkInfo = ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            dataTask.execute(FetchDataTask.SORT_BY_POPULARITY);
        } else {
            Toast.makeText(getContext(), R.string.string_network_unavail, Toast.LENGTH_SHORT).show();
        }


        mMovieGrid = (GridView) v.findViewById(R.id.poster_grid);
        mAdapter = new MovieGridAdapter(getContext(), new ArrayList<Movie>());
        mMovieGrid.setAdapter(mAdapter);
        mMovieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getContext(), MovieDetailActivity.class);
                i.putExtra(MovieDetailActivity.ARG_MOVIE, (Movie) parent.getItemAtPosition(position));
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    View statusBar = getActivity().getWindow().getDecorView().findViewById(android.R.id.statusBarBackground);
                    View navigationBar = getActivity().getWindow().getDecorView().findViewById(android.R.id.navigationBarBackground);
                    View toolbar = getActivity().findViewById(R.id.toolbar);
                    List<Pair<View, String>> pairs = new ArrayList<Pair<View, String>>();
                    pairs.add(Pair.create(toolbar, "toolbar"));
                    if(statusBar != null) pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
                    if(navigationBar != null) pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
                    pairs.add(Pair.create(view, "poster"));
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), pairs.toArray(new Pair[pairs.size()]));
                    ActivityCompat.startActivity(getActivity(), i, optionsCompat.toBundle()); }
                else {
                    startActivity(i);
                }
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment_movie, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NetworkInfo networkInfo = ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        switch(item.getItemId()) {
            case R.id.sort_by_popularity:
                if(networkInfo != null && networkInfo.isConnected()) {
                    new FetchDataTask(getContext()).execute(FetchDataTask.SORT_BY_POPULARITY);
                } else {
                    Toast.makeText(getContext(), R.string.string_network_unavail, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.sort_by_rating:
                if(networkInfo != null && networkInfo.isConnected()) {
                    new FetchDataTask(getContext()).execute(FetchDataTask.SORT_BY_RATING);
                } else {
                    Toast.makeText(getContext(), R.string.string_network_unavail, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.favoritesOnly:
                new DbReader(getContext()).execute();
                return  true;
            default:
                return false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("scrollPos", mMovieGrid.getFirstVisiblePosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            /*
                Restore scroll position when grid layout is drawn.
             */
            mMovieGrid.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mMovieGrid.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mMovieGrid.setSelection(savedInstanceState.getInt("scrollPos"));
                }
            });
        }
    }

    public class FetchDataTask extends AsyncTask<String, Void, List<Movie>> {

        public static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/discover/movie?";
        public static final String TMDB_BASE_URL2 = "https://api.themoviedb.org/3/";

        public static final String PARAM_API_KEY = "api_key";
        public static final String PARAM_SORT_BY = "sort_by";

        public static final String SORT_BY_POPULARITY = "popularity.desc";
        public static final String SORT_BY_RATING = "vote_average.desc";

        public static final String KEY_TITLE = "title";
        public static final String KEY_OTITLE = "original_title";
        public static final String KEY_ID = "id";
        public static final String KEY_POSTER = "poster_path";
        public static final String KEY_DESC = "overview";
        public static final String KEY_REL_DATE = "release_date";
        public static final String KEY_BACKDROP = "backdrop_path";
        public static final String KEY_VOTE_AVG = "vote_average";


        private Context mContext;

        boolean networkError = false;

        FetchDataTask(Context context) {
            mContext = context;
        }

        @Override
        protected List<Movie> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String responseJson;


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(TMDB_BASE_URL2)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();


            try {
                MovieService movieService = retrofit.create(MovieService.class);
                Call<DiscoverResults> call = movieService.getPopularMovies(mContext.getString(R.string.tmdb_api_key), params[0], (params[0].equals(SORT_BY_RATING) ? "1000" : "0"));
                Response<DiscoverResults> response = call.execute();
                return response.body().getResults();
            } catch (IOException e) {
                e.printStackTrace();
                if(e instanceof UnknownHostException) {
                    networkError = true;
                }
            }
/*            try {
                Uri.Builder uriBuilder = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendQueryParameter(PARAM_SORT_BY, params[0])
                        .appendQueryParameter(PARAM_API_KEY, mContext.getString(R.string.tmdb_api_key));
                //Vote count condition to get relevant results
                if(params[0].equals(SORT_BY_RATING)) {
                    uriBuilder.appendQueryParameter("vote_count.gte", "500");
                }

                Uri uri = uriBuilder.build();
                URL url = new URL(uri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if(inputStream == null) {
                    //Nothing to do
                    responseJson = null;
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if(buffer.length() == 0) {
                    //Empty String
                    responseJson = null;
                }

                responseJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
                responseJson = null;
            } finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }

                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                return parseJson(responseJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/

            return null;
        }

        private Movie[] parseJson(String jsonString) throws JSONException {
            JSONObject dataObj = new JSONObject(jsonString);
            JSONArray results = dataObj.getJSONArray("results");
            Movie[] data = new Movie[results.length()];

            for (int i = 0; i < results.length(); i++) {
                JSONObject temp = results.getJSONObject(i);
                data[i] = new Movie(temp.getString(KEY_TITLE),
                        temp.getString(KEY_DESC),
                        temp.getString(KEY_POSTER),
                        temp.getString(KEY_REL_DATE),
                        temp.getLong(KEY_ID),
                        temp.getString(KEY_OTITLE),
                        temp.getString(KEY_BACKDROP),
                        temp.getString(KEY_VOTE_AVG));
            }

            return data;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            if (movies != null) {
                mAdapter.clear();
                mAdapter.addAll(movies);
                mAdapter.notifyDataSetChanged();
            } else {
                if(networkError) {
                    Toast.makeText(mContext, "Network error", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public class DbReader extends AsyncTask<Void, Void, List<Movie>> {

        private Context mContext;

        public DbReader(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected List<Movie> doInBackground(Void... params) {

            MovieDbHelper dbHelper = new MovieDbHelper(mContext);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            List<Movie> movieList = new ArrayList<>();

//            Cursor c = db.query(DatabaseContract.MovieEntry.TABLE_NAME, null, null, null, null, null, null);

            Cursor c = mContext.getContentResolver().query(FavoritesProvider.Movies.CONTENT_URI, null, null, null, null);

            if (c != null) {
                int colTmdbId = c.getColumnIndex(DatabaseContract.MovieEntry.COL_TMDB_ID);
                int colTitle = c.getColumnIndex(DatabaseContract.MovieEntry.COL_TITLE);
                int colDesc = c.getColumnIndex(DatabaseContract.MovieEntry.COL_DESC);
                int colPoster = c.getColumnIndex(DatabaseContract.MovieEntry.COL_POSTER_PATH);
                int colRelDate = c.getColumnIndex(DatabaseContract.MovieEntry.COL_REL_DATE);
                int colOgTitle = c.getColumnIndex(DatabaseContract.MovieEntry.COL_ORIGINAL_TITLE);
                int colBackdrop = c.getColumnIndex(DatabaseContract.MovieEntry.COL_BACKDROP_PATH);
                int colVoteAvg = c.getColumnIndex(DatabaseContract.MovieEntry.COL_VOTE_AVG);

                if(c.moveToFirst()) {
                    movieList.add(new Movie(c.getString(colTitle),
                            c.getString(colDesc),
                            c.getString(colPoster),
                            c.getString(colRelDate),
                            c.getLong(colTmdbId),
                            c.getString(colOgTitle),
                            c.getString(colBackdrop),
                            c.getString(colVoteAvg)));
                }

                while(c.moveToNext()) {
                    movieList.add(new Movie(c.getString(colTitle),
                            c.getString(colDesc),
                            c.getString(colPoster),
                            c.getString(colRelDate),
                            c.getLong(colTmdbId),
                            c.getString(colOgTitle),
                            c.getString(colBackdrop),
                            c.getString(colVoteAvg)));
                }

                c.close();
            }
            db.close();

            return movieList;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            if(movies != null && !movies.isEmpty()) {
                mAdapter.clear();
                mAdapter.addAll(movies);
                mAdapter.notifyDataSetChanged();
            } else {
                final Snackbar s = Snackbar.make(MovieFragment.this.mMovieGrid, R.string.string_no_favorites, Snackbar.LENGTH_INDEFINITE);
                    s.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        s.dismiss();
                    }
                });
                s.show();
            }
        }
    }

}
