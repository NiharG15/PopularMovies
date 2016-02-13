package com.niharg.popularmovies.database;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by niharg on 2/13/16 at 2:49 PM.
 */

@Database(version = FavoriteDatabase.DATABASE_VERSION)
public final class FavoriteDatabase {
    public static final int DATABASE_VERSION = 1;

    @Table(DatabaseContract.MovieEntry.class) public static final String TABLE_NAME = "favmovies";

}
