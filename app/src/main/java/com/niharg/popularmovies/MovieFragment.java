package com.niharg.popularmovies;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


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
        dataTask.execute(FetchDataTask.SORT_BY_POPULARITY);

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
                pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
                pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
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
        switch(item.getItemId()) {
            case R.id.sort_by_popularity:
                Log.d(this.toString(), "Sort by popularity");
                new FetchDataTask(getContext()).execute(FetchDataTask.SORT_BY_POPULARITY);
                return true;
            case R.id.sort_by_rating:
                Log.d(this.toString(), "Sort by rating");
                new FetchDataTask(getContext()).execute(FetchDataTask.SORT_BY_RATING);
                return true;
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

    public class FetchDataTask extends AsyncTask<String, Void, Movie[]> {

        public static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/discover/movie?";

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

        FetchDataTask(Context context) {
            mContext = context;
        }

        @Override
        protected Movie[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String responseJson;

            try {
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
            }

            return null;
        }

        private Movie[] parseJson(String jsonString) throws JSONException {
            JSONObject dataObj = new JSONObject(jsonString);
            JSONArray results = dataObj.getJSONArray("results");
            Movie[] data = new Movie[results.length()];

            for(int i = 0; i < results.length(); i++) {
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
        protected void onPostExecute(Movie[] movies) {
            super.onPostExecute(movies);
            mAdapter.clear();
            for(Movie m: movies) {
                mAdapter.add(m);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

}
