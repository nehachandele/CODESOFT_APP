package com.example.quoteoftheday;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder> {

    private Context context;
    private Cursor cursor;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onUnlikeClick(int position);
        void onShareClick(int position);
        void onHomeClick(int position); // Add method for home click
        void onLikedClick(int position); // Add method for liked click
    }

    public QuoteAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.quote_item, parent, false);
        return new QuoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuoteViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) {
            return;
        }

        String quote = cursor.getString(cursor.getColumnIndex(QuoteDatabaseHelper.COLUMN_QUOTE));
        String author = cursor.getString(cursor.getColumnIndex(QuoteDatabaseHelper.COLUMN_AUTHOR));
        String date = cursor.getString(cursor.getColumnIndex(QuoteDatabaseHelper.COLUMN_DATE));

        holder.quoteText.setText(quote);
        holder.authorText.setText(author);
        holder.dateText.setText(date); // Set the date

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });

        holder.unlikeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUnlikeClick(position);
            }
        });

        holder.shareButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShareClick(position);
            }
        });


    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }

    public Cursor getCursor() {
        return cursor;
    }

    static class QuoteViewHolder extends RecyclerView.ViewHolder {
        TextView quoteText;
        TextView authorText;
        TextView dateText; // New TextView for date
        ImageView unlikeButton;
        ImageView shareButton;

        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            quoteText = itemView.findViewById(R.id.quoteText);
            authorText = itemView.findViewById(R.id.authorText);
            dateText = itemView.findViewById(R.id.dateText); // Initialize date TextView
            unlikeButton = itemView.findViewById(R.id.unlikeButton); // Assuming unlike button ID
            shareButton = itemView.findViewById(R.id.shareButton); // Assuming share button ID

        }
    }
}
