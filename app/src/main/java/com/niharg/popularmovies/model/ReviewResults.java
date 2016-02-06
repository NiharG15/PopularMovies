package com.niharg.popularmovies.model;

import java.util.List;

/**
 * Created by niharg on 2/6/16 at 12:50 PM.
 */
public class ReviewResults {

    int id;
    int page;
    List<Review> results;

    public ReviewResults() {}

    public ReviewResults(int id, int page, List<Review> results) {
        this.id = id;
        this.page = page;
        this.results = results;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<Review> getResults() {
        return results;
    }

    public void setResults(List<Review> results) {
        this.results = results;
    }
}
