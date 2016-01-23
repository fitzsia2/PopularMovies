package com.afitzwa.android.popularmovies.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.afitzwa.android.popularmovies.app.data.MovieContract.MovieEntry;
import com.afitzwa.android.popularmovies.app.data.MovieContract.TrailerEntry;
import com.afitzwa.android.popularmovies.app.data.MovieContract.ReviewEntry;

/**
 * Makes a local database for our movie data.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 6;
    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID +                " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieEntry.COLUMN_POSTER_URL +  " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_DB_ID + " BIGINT UNIQUE NOT NULL, " +
                MovieEntry.COLUMN_TITLE +       " TEXT NOT NULL, " +
                MovieEntry.COLUMN_YEAR +        " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_LENGTH +      " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_RATING +      " TEXT NOT NULL, " +
                MovieEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL" +
                " );";

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + " (" +
                TrailerEntry._ID                + " INTEGER PRIMARY KEY," +
                TrailerEntry.COLUMN_MOVIE_KEY   + " BIGINT NOT NULL, " +
                TrailerEntry.COLUMN_TRAILER_URL + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + TrailerEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + ")" +
                ");";

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID                + " INTEGER PRIMARY KEY," +
                ReviewEntry.COLUMN_MOVIE_KEY   + " BIGINT NOT NULL, " +
                ReviewEntry.COLUMN_USER        + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL" +
                ");";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_TRAILER_TABLE);
        db.execSQL(SQL_CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        onCreate(db);
    }
}
