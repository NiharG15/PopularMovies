package com.niharg.popularmovies.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by niharg on 2/5/16 at 11:53 PM.
 */
public class VideoResults {

    @SerializedName("id")
    long id;

    @SerializedName("results")
    List<Video> videos;

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
