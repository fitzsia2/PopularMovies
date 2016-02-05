package com.afitzwa.android.popularmovies.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.afitzwa.android.popularmovies.app.utils.PollingCheck;

import java.util.Map;
import java.util.Set;


/**
 * Created by AndrewF on 1/22/2016.
 */
public class TestUtilities extends AndroidTestCase {
    static final long TEST_MOVIE = 65432187;
    static final long TEST_TRAILER = 65432188;
    static final long TEST_REVIEW = 65432189;

    /*----------------------------------------------
        Taken from Udacity's Sunshine
     ---------------------------------------------*/
    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }
    /*----------------------------------------------
        // END Taken from Udacity's Sunshine //
     ---------------------------------------------*/

    static ContentValues createMovieValues1() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID, TEST_MOVIE);
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "My favorite Movie");
        movieValues.put(MovieContract.MovieEntry.COLUMN_YEAR, 2001);
        movieValues.put(MovieContract.MovieEntry.COLUMN_LENGTH, 81);
        movieValues.put(MovieContract.MovieEntry.COLUMN_DESCRIPTION, "She talks to someone. Someone meets somebody.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_URL, "www.google.com");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RATING, "2");
        return movieValues;
    }

    static ContentValues createMovieValues2() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID, TEST_MOVIE + 5);
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "My 2nd favorite Movie");
        movieValues.put(MovieContract.MovieEntry.COLUMN_YEAR, 2005);
        movieValues.put(MovieContract.MovieEntry.COLUMN_LENGTH, 90);
        movieValues.put(MovieContract.MovieEntry.COLUMN_DESCRIPTION, "He talks to someone. Somebody meets someone.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_URL, "www.bing.com");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RATING, "3");
        return movieValues;
    }

    static ContentValues createTrailerValues(long movieKey) {
        ContentValues trailerValues = new ContentValues();
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_KEY, movieKey);
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_DESCRIPTION, "Trailer description for movie " + movieKey);
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_URL, "www.bing.com");
        return trailerValues;
    }

    static ContentValues createReviewValues(long movieKey) {
        ContentValues reviewValues = new ContentValues();
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, movieKey);
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_DESCRIPTION, "Review description for movie " + movieKey);
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_USER, "Review user for movie " + movieKey);
        return reviewValues;
    }

    static long insertTestMovieValues(Context context) {
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = createMovieValues1();

        long movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);
        assertTrue("Error: Failed to insert test movie data", movieRowId != -1);

        return movieRowId;
    }

    /*----------------------------------------------
        Taken from Udacity's Sunshine
     ---------------------------------------------*/
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }
    /*----------------------------------------------
        // END Taken from Udacity's Sunshine //
     ---------------------------------------------*/
}
