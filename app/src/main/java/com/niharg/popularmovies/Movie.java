package com.niharg.popularmovies;

/**
 * Created by niharg on 1/29/16 at 7:40 PM.
 */
public class Movie {

    private String title;
    private String description;
    private String posterPath;
    private String relDate;
    private long id;
    private String origTitle;

    public Movie() {}

    public Movie(String title, String description, String posterPath, String relDate, long id, String origTitle) {
        this.title = title;
        this.description = description;
        this.posterPath = posterPath;
        this.relDate = relDate;
        this.id = id;
        this.origTitle = origTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getRelDate() {
        return relDate;
    }

    public void setRelDate(String relDate) {
        this.relDate = relDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOrigTitle() {
        return origTitle;
    }

    public void setOrigTitle(String origTitle) {
        this.origTitle = origTitle;
    }
}
