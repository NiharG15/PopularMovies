package com.niharg.popularmovies.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by niharg on 2/6/16 at 11:18 PM.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FavMovies.db";

    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context mContext) {
        super(mContext, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_MOVIE_TABLE = "CREATE TABLE " + DatabaseContract.MovieEntry.TABLE_NAME + " (" +
                DatabaseContract.MovieEntry._ID + " INTEGER PRIMARY KEY," +
                DatabaseContract.MovieEntry.COL_TMDB_ID + " INTEGER UNIQUE NOT NULL, " +
                DatabaseContract.MovieEntry.COL_TITLE + " TEXT NOT NULL, " +
                DatabaseContract.MovieEntry.COL_DESC + " TEXT NOT NULL, " +
                DatabaseContract.MovieEntry.COL_POSTER_PATH + " TEXT NOT NULL, " +
                DatabaseContract.MovieEntry.COL_REL_DATE + " TEXT NOT NULL, " +
                DatabaseContract.MovieEntry.COL_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                DatabaseContract.MovieEntry.COL_BACKDROP_PATH + " TEXT NOT NULL, " +
                DatabaseContract.MovieEntry.COL_VOTE_AVG + " TEXT NOT NULL" +
                " );";

        db.execSQL(CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: 2/6/16 Implement this if necessary!
    }
}
