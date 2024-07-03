package com.example.quoteoftheday;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

public class addQuote extends AppCompatActivity {

    private static final String TAG = "addQuote";
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private QuoteDatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private QuoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quote);

        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);
        ImageView back = findViewById(R.id.back);
        dbHelper = new QuoteDatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuoteAdapter(this, getAllQuotes());
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> showAddQuoteDialog());

        back.setOnClickListener(v -> {
            // Clear user data (SharedPreferences in this example)
            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Navigate back to login activity
            Intent intent = new Intent(addQuote.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the activity stack
            startActivity(intent);
            finish(); // Finish the current activity to prevent returning to it
        });
    }

    private Cursor getAllQuotes() {
        return database.query(QuoteDatabaseHelper.TABLE_QUOTES,
                null, null, null, null, null, null);
    }

    private void showAddQuoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_quote, null);

        EditText quoteText = view.findViewById(R.id.quoteText);
        EditText authorText = view.findViewById(R.id.authorText);
        TextView dateText = view.findViewById(R.id.dateText);
        Button saveButton = view.findViewById(R.id.saveButton);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        dateText.setOnClickListener(v -> showDatePickerDialog(dateText));

        saveButton.setOnClickListener(v -> {
            String quote = quoteText.getText().toString();
            String author = authorText.getText().toString();
            String date = dateText.getText().toString();

            Log.d(TAG, "Inserting Quote: " + quote);
            Log.d(TAG, "Inserting Author: " + author);
            Log.d(TAG, "Inserting Date: " + date);

            if (isDateInPast(date)) {
                Toast.makeText(this, "Cannot add a quote with a past date.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Delete old quotes before inserting new one
            deleteOldQuotes();

            ContentValues values = new ContentValues();
            values.put(QuoteDatabaseHelper.COLUMN_QUOTE, quote);
            values.put(QuoteDatabaseHelper.COLUMN_AUTHOR, author);
            values.put(QuoteDatabaseHelper.COLUMN_LIKED, 0); // Set liked to false by default
            values.put(QuoteDatabaseHelper.COLUMN_DATE, date);

            database.insert(QuoteDatabaseHelper.TABLE_QUOTES, null, values);

            adapter.swapCursor(getAllQuotes());

            dialog.dismiss();
        });
    }

    private void showDatePickerDialog(final TextView dateText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            String date = String.format("%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
            dateText.setText(date);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void deleteOldQuotes() {
        String currentDate = getCurrentDate();
        String selection = QuoteDatabaseHelper.COLUMN_DATE + " < ?";
        String[] selectionArgs = { currentDate };

        int rowsDeleted = database.delete(QuoteDatabaseHelper.TABLE_QUOTES, selection, selectionArgs);
        Log.d(TAG, "Deleted old quotes: " + rowsDeleted + " rows.");
    }

    private boolean isDateInPast(String date) {
        String currentDate = getCurrentDate();
        return date.compareTo(currentDate) < 0;
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is zero-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return String.format("%04d-%02d-%02d", year, month, day);
    }
}
