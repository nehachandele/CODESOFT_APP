package com.example.quoteoftheday;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LikedListActivity extends AppCompatActivity implements QuoteAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private QuoteAdapter adapter;
    private QuoteDatabaseHelper databaseHelper;
    ImageView home, liked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuoteAdapter(this, null); // Initialize with null cursor
        home = findViewById(R.id.home);

        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this); // Set item click listener

        databaseHelper = new QuoteDatabaseHelper(this);
        loadLikedQuotes();

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LikedListActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadLikedQuotes() {
        Cursor cursor = databaseHelper.getAllLikedQuotes();
        adapter.swapCursor(cursor); // Update the cursor in the adapter
    }

    @Override
    public void onItemClick(int position) {
        // Handle item click (optional)
        Toast.makeText(this, "Item clicked at position " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUnlikeClick(int position) {
        // Handle unlike action
        Cursor cursor = adapter.getCursor();
        if (cursor.moveToPosition(position)) {
            String quote = cursor.getString(cursor.getColumnIndex(QuoteDatabaseHelper.COLUMN_QUOTE));
            ContentValues values = new ContentValues();
            values.put(QuoteDatabaseHelper.COLUMN_LIKED, 0); // Set liked to false (0)
            databaseHelper.updateQuote(quote, values); // Update quote in database
            loadLikedQuotes(); // Reload the list
            Toast.makeText(this, "Quote unliked: " + quote, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onShareClick(int position) {
        // Handle share action
        Cursor cursor = adapter.getCursor();
        if (cursor.moveToPosition(position)) {
            String quote = cursor.getString(cursor.getColumnIndex(QuoteDatabaseHelper.COLUMN_QUOTE));
            // Implement share functionality (e.g., share intent)
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, quote);
            startActivity(Intent.createChooser(shareIntent, "Share Quote"));
        }
    }

    @Override
    public void onHomeClick(int position) {
        // Handle home click
        Toast.makeText(this, "Home clicked at position " + position, Toast.LENGTH_SHORT).show();
        // Implement your logic, e.g., navigate to home screen
    }

    @Override
    public void onLikedClick(int position) {
        // Handle liked click
        Toast.makeText(this, "Liked clicked at position " + position, Toast.LENGTH_SHORT).show();
        // Implement your logic, e.g., handle liking functionality
    }
}
