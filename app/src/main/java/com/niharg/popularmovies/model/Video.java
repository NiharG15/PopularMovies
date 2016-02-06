package com.niharg.popularmovies.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by niharg on 2/5/16 at 11:54 PM.
 */
public class Video {

    @SerializedName("id")
    String id;

    @SerializedName("key")
    String key;

    @SerializedName("name")
    String name;

    @SerializedName("site")
    String site;

    @SerializedName("size")
    int size;

    @SerializedName("type")
    String type;

    public Video() {
    }

    public Video(String id, String key, String name, String site, int size, String type) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.site = site;
        this.size = size;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
