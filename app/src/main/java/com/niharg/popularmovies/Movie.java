package com.niharg.popularmovies;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by niharg on 1/29/16 at 7:40 PM.
 */
public class Movie implements Serializable {

    @SerializedName("title")
    private String title;
    @SerializedName("overview")
    private String description;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("release_date")
    private String relDate;
    @SerializedName("id")
    private long id;
    @SerializedName("original_title")
    private String origTitle;
    @SerializedName("backdrop_path")
    private String backdropPath;
    @SerializedName("vote_average")
    private String voteAvg;

    public Movie() {}

    public Movie(String title, String description, String posterPath, String relDate, long id, String origTitle, String backdropPath, String voteAvg) {
        this.title = title;
        this.description = description;
        this.posterPath = posterPath;
        this.relDate = relDate;
        this.id = id;
        this.origTitle = origTitle;
        this.backdropPath = backdropPath;
        this.voteAvg = voteAvg;
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

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getVoteAvg() {
        return voteAvg;
    }

    public void setVoteAvg(String voteAvg) {
        this.voteAvg = voteAvg;
    }
}
