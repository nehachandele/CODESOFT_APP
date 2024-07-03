package com.example.quoteoftheday;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class QuoteDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "quotes.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_QUOTES = "quotes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_QUOTE = "quote";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_LIKED = "liked";
    public static final String COLUMN_DATE = "date"; // Add date column

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_QUOTES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_QUOTE + " TEXT, " +
                    COLUMN_AUTHOR + " TEXT, " +
                    COLUMN_LIKED + " INTEGER, " +
                    COLUMN_DATE + " TEXT);";

    public QuoteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUOTES);
        onCreate(db);
    }

    // Method to retrieve all liked quotes from the database
    public Cursor getAllLikedQuotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                COLUMN_ID,
                COLUMN_QUOTE,
                COLUMN_AUTHOR,
                COLUMN_LIKED,
                COLUMN_DATE
        };
        String selection = COLUMN_LIKED + "=?";
        String[] selectionArgs = {"1"}; // Select only liked quotes
        return db.query(
                TABLE_QUOTES,    // The table to query
                projection,      // The array of columns to return (null to get all)
                selection,       // The columns for the WHERE clause
                selectionArgs,   // The values for the WHERE clause
                null,            // don't group the rows
                null,            // don't filter by row groups
                null             // The sort order
        );
    }

    // Method to update a quote in the database
    public void updateQuote(String quote, ContentValues values) {
        String selection = COLUMN_QUOTE + " = ?";
        String[] selectionArgs = { quote };
        getWritableDatabase().update(TABLE_QUOTES, values, selection, selectionArgs);
    }
}
