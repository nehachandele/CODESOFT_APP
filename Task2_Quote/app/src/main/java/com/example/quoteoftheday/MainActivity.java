package com.example.quoteoftheday;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView quoteText;
    private TextView quoteAuthor;
    private ImageView likeButton, shareButton,home,liked;
    private QuoteDatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        TextView appName = findViewById(R.id.app_name);
        likeButton = findViewById(R.id.like_button);
        quoteText = findViewById(R.id.quote_text);
        quoteAuthor = findViewById(R.id.quote_author);
        shareButton = findViewById(R.id.share_button);
        home=findViewById(R.id.home);
        liked=findViewById(R.id.liked);
        liked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,LikedListActivity.class);
                startActivity(intent);
            }
        });
        home.setEnabled(false);

        // Initialize database helper and get writable database instance
        dbHelper = new QuoteDatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        // Set click listener for app name TextView to navigate to login activity
        appName.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, login.class);
            startActivity(i);
        });

        // Set click listener for like button to update liked state
        likeButton.setOnClickListener(v -> {
            updateLikeState();
        });

        // Set click listener for share button to share quote
        shareButton.setOnClickListener(v -> {
            shareToWhatsApp();
        });

        // Display today's quote and update like button drawable based on initial liked state
        displayTodaysQuote();
    }

    private void displayTodaysQuote() {
        // Get today's date in yyyy-MM-dd format
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = dateFormat.format(calendar.getTime());
        Log.d(TAG, "Today's date: " + todayDate);

        // Query the database for today's quote
        String selection = QuoteDatabaseHelper.COLUMN_DATE + " = ?";
        String[] selectionArgs = { todayDate };
        Cursor cursor = database.query(
                QuoteDatabaseHelper.TABLE_QUOTES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        // Check if cursor has data and move to first row
        if (cursor != null && cursor.moveToFirst()) {
            // Retrieve quote and author from cursor
            String quote = cursor.getString(cursor.getColumnIndex(QuoteDatabaseHelper.COLUMN_QUOTE));
            String author = cursor.getString(cursor.getColumnIndex(QuoteDatabaseHelper.COLUMN_AUTHOR));

            Log.d(TAG, "Quote: " + quote);
            Log.d(TAG, "Author: " + author);

            // Update quote and author TextViews with retrieved data
            quoteText.setText(quote);
            quoteAuthor.setText("- " + author);

            // Check initial liked state and update like button drawable
            int liked = cursor.getInt(cursor.getColumnIndex(QuoteDatabaseHelper.COLUMN_LIKED));
            updateLikeButtonDrawable(liked);

            cursor.close(); // Close cursor after use to free up resources
        } else {
            // If no quote found for today, display appropriate message
            Log.d(TAG, "No quote found for today.");
            quoteText.setText("No quote for today.");
            quoteAuthor.setText("");
        }
    }

    private void updateLikeState() {
        // Get today's date in yyyy-MM-dd format
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = dateFormat.format(calendar.getTime());

        // Query the database for today's quote
        String selection = QuoteDatabaseHelper.COLUMN_DATE + " = ?";
        String[] selectionArgs = { todayDate };
        Cursor cursor = database.query(
                QuoteDatabaseHelper.TABLE_QUOTES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        // Check if cursor has data and move to first row
        if (cursor != null && cursor.moveToFirst()) {
            // Retrieve quote ID and current liked state from cursor
            int id = cursor.getInt(cursor.getColumnIndex(QuoteDatabaseHelper.COLUMN_ID));
            int liked = cursor.getInt(cursor.getColumnIndex(QuoteDatabaseHelper.COLUMN_LIKED));
            int newLikedState = (liked == 1) ? 0 : 1; // Toggle liked state

            // Update database with new liked state
            ContentValues values = new ContentValues();
            values.put(QuoteDatabaseHelper.COLUMN_LIKED, newLikedState);
            database.update(QuoteDatabaseHelper.TABLE_QUOTES, values,
                    QuoteDatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

            // Update UI based on new liked state
            updateLikeButtonDrawable(newLikedState);

            // Show toast message indicating that quote liked state is updated
            Toast.makeText(this, "Quote liked state updated.", Toast.LENGTH_SHORT).show();

            cursor.close(); // Close cursor after use to free up resources
        }
    }

    private void updateLikeButtonDrawable(int likedState) {
        // Update like button drawable based on liked state
        Drawable drawable;
        if (likedState == 1) {
            drawable = ContextCompat.getDrawable(this, R.drawable.heart2);
        } else {
            drawable = ContextCompat.getDrawable(this, R.drawable.heart);
        }
        likeButton.setImageDrawable(drawable);
    }

    private void shareToWhatsApp() {
        // Find the root view of your activity
        View rootView = findViewById(R.id.content);

        // Create a Bitmap from the rootView (your entire layout)
        Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        rootView.draw(canvas);

        // Convert Bitmap to byte array
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "QuoteImage", null);

        // Create an Intent to share image with text
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
        shareIntent.putExtra(Intent.EXTRA_TEXT, quoteText.getText().toString() + "\n- " + quoteAuthor.getText().toString());

        // Create chooser to let user choose the app to share with
        Intent chooserIntent = Intent.createChooser(shareIntent, "Share Quote");

        // Verify the intent will resolve to at least one activity
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooserIntent);
        } else {
            Toast.makeText(this, "No app available to share.", Toast.LENGTH_SHORT).show();
        }
    }

}
