package com.niharg.popularmovies.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by niharg on 2/5/16 at 11:12 PM.
 */
public class DiscoverResults {

    @SerializedName("page")
    public String page;

    @SerializedName("results")
    private List<Movie> results;

    public List<Movie> getResults() {
        return results;
    }

    public void setResults(List<Movie> results) {
        this.results = results;
    }
}
