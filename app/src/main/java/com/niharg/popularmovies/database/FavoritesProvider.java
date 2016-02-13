package com.niharg.popularmovies.database;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by niharg on 2/13/16 at 2:57 PM.
 */

@ContentProvider(authority = FavoritesProvider.AUTHORITY, database = FavoriteDatabase.class)
public class FavoritesProvider {

    public static final String AUTHORITY = "com.niharg.popularmovies";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String MOVIES = "movies";
    }

    @TableEndpoint(table = FavoriteDatabase.TABLE_NAME) public static class Movies {

        @ContentUri(
                path = Path.MOVIES,
                type = "vnd.android.cursor.dir/movie",
                defaultSort = DatabaseContract.MovieEntry._ID + " DESC")
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Path.MOVIES).build();

        @InexactContentUri(
                path = Path.MOVIES + "/#",
                name = "MOVIE_ID",
                type = "vnd.android.cursor.item/movie",
                whereColumn = DatabaseContract.MovieEntry._ID,
                pathSegment = 1)
        public static Uri withId(long id) {
            return BASE_CONTENT_URI.buildUpon().appendPath(Path.MOVIES).appendPath(String.valueOf(id)).build();
        }
    }

}
