package com.example.todo_codsoft;

import android.database.Cursor;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;

public class dashboard extends AppCompatActivity {

    private LinearLayout taskContainer;
    private EditText searchTasks;
    private TextView pendingCount;
    private TextView progressText;
    private ProgressBar circularProgressBar; // Changed to CircularProgressIndicator
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        taskContainer = findViewById(R.id.ll1);
        searchTasks = findViewById(R.id.searchTasks);
        Button addTaskButton = findViewById(R.id.addTask);
        pendingCount = findViewById(R.id.pending);
        progressText = findViewById(R.id.progressText);
        circularProgressBar = findViewById(R.id.circularProgressBar);

        addTaskButton.setBackgroundTintList(null);

        dbHelper = new DatabaseHelper(this);

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });

        searchTasks.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTasks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        loadTasksFromDatabase();
        updatePendingCount();
        updateProgressBar();
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        final EditText editTextTaskTitle = dialogView.findViewById(R.id.editTextTaskTitle);
        final EditText editTextTaskDescription = dialogView.findViewById(R.id.editTextTaskDescription);
        final TimePicker editTextTaskTime = dialogView.findViewById(R.id.editTextTaskTime);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = editTextTaskTitle.getText().toString().trim();
            String description = editTextTaskDescription.getText().toString().trim();
            int hour = editTextTaskTime.getHour();
            int minute = editTextTaskTime.getMinute();
            String time = String.format("%02d:%02d", hour, minute);

            if (title.isEmpty() || description.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "All fields are required..!", Toast.LENGTH_SHORT).show();
            } else {
                long taskId = dbHelper.addTask(title, description, time, "pending");
                addTaskToLayout((int) taskId, title, description, time, "pending");
                Toast.makeText(this, "New Task Added", Toast.LENGTH_SHORT).show();
                playTaskAddedSound();
                updatePendingCount();
                updateProgressBar();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addTaskToLayout(int taskId, String title, String description, String time, String status) {
        View taskView = getLayoutInflater().inflate(R.layout.task_item, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 20); // Add bottom margin to create gap
        taskView.setLayoutParams(layoutParams);
        TextView taskTitle = taskView.findViewById(R.id.taskTitle);
        TextView taskDescription = taskView.findViewById(R.id.taskDescription);
        TextView taskTime = taskView.findViewById(R.id.taskTime);
        CheckBox completeTask = taskView.findViewById(R.id.completeTask);
        ImageView editTask = taskView.findViewById(R.id.editTask);
        ImageView deleteTask = taskView.findViewById(R.id.deleteTask);

        taskTitle.setText(title);
        taskDescription.setText(description);
        taskTime.setText(time);

        if (status.equals("completed")) {
            completeTask.setChecked(true);
            setTaskCompletedStyle(taskTitle, taskDescription, taskTime);
        }

        completeTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newStatus = completeTask.isChecked() ? "completed" : "pending";
                dbHelper.updateTask(taskId, title, description, time, newStatus);

                if (completeTask.isChecked()) {
                    playCompletionSound();
                    setTaskCompletedStyle(taskTitle, taskDescription, taskTime);
                    Toast.makeText(dashboard.this, "Marked As Completed", Toast.LENGTH_SHORT).show();
                } else {
                    setTaskPendingStyle(taskTitle, taskDescription, taskTime);
                }
                updatePendingCount();
                updateProgressBar();
            }
        });

        editTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditTaskDialog(taskId, taskView, taskTitle, taskDescription, taskTime);
            }
        });

        deleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.deleteTask(taskId);
                taskContainer.removeView(taskView);
                updatePendingCount();
                updateProgressBar();
            }
        });

        taskContainer.addView(taskView);
    }

    private void showEditTaskDialog(int taskId, View taskView, TextView taskTitle, TextView taskDescription, TextView taskTime) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        final EditText editTextTaskTitle = dialogView.findViewById(R.id.editTextTaskTitle);
        final EditText editTextTaskDescription = dialogView.findViewById(R.id.editTextTaskDescription);
        final TimePicker editTextTaskTime = dialogView.findViewById(R.id.editTextTaskTime);

        editTextTaskTitle.setText(taskTitle.getText().toString());
        editTextTaskDescription.setText(taskDescription.getText().toString());

        String[] timeParts = taskTime.getText().toString().split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        editTextTaskTime.setHour(hour);
        editTextTaskTime.setMinute(minute);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = editTextTaskTitle.getText().toString().trim();
            String description = editTextTaskDescription.getText().toString().trim();
            int fhour = editTextTaskTime.getHour();
            int fminute = editTextTaskTime.getMinute();
            String time = String.format("%02d:%02d", fhour, fminute);

            if (title.isEmpty() || description.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "All fields are required..!", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.updateTask(taskId, title, description, time, "pending");
                Toast.makeText(this, "Task Edited", Toast.LENGTH_SHORT).show();

                taskTitle.setText(title);
                taskDescription.setText(description);
                taskTime.setText(time);
                updatePendingCount();
                updateProgressBar();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loadTasksFromDatabase() {
        Cursor cursor = dbHelper.getAllTasks();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String description = cursor.getString(cursor.getColumnIndex("description"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            String status = cursor.getString(cursor.getColumnIndex("status"));

            addTaskToLayout(id, title, description, time, status);
        }
        cursor.close();
        updateProgressBar();
    }

    private void filterTasks(String query) {
        int childCount = taskContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View taskView = taskContainer.getChildAt(i);
            TextView taskTitle = taskView.findViewById(R.id.taskTitle);
            TextView taskDescription = taskView.findViewById(R.id.taskDescription);

            String title = taskTitle.getText().toString().toLowerCase();
            String description = taskDescription.getText().toString().toLowerCase();
            String searchQuery = query.toLowerCase();

            if (title.contains(searchQuery) || description.contains(searchQuery)) {
                taskView.setVisibility(View.VISIBLE);
            } else {
                taskView.setVisibility(View.GONE);
            }
        }
    }

    private void updatePendingCount() {
        int count = dbHelper.getPendingTasksCount();
        if (count == 0) {
            pendingCount.setText("No Pending Tasks");
        } else if (count == 1) {
            pendingCount.setText("Only 1 task is pending");
        } else {
            pendingCount.setText(count + " Tasks are pending");
        }
    }

    private void updateProgressBar() {
        int totalTasks = dbHelper.getTotalTasksCount();
        int completedTasks = dbHelper.getCompletedTasksCount();

        if (totalTasks == 0) {
            circularProgressBar.setProgress(0);
            progressText.setText("0%");
        } else {
            int progress = (int) ((completedTasks / (float) totalTasks) * 100);
            circularProgressBar.setProgress(progress);
            progressText.setText(progress + "%");
        }
    }

    private void setTaskCompletedStyle(TextView taskTitle, TextView taskDescription, TextView taskTime) {
        int completedColor = getResources().getColor(android.R.color.holo_red_dark);
        taskTitle.setTextColor(completedColor);
        taskTitle.setPaintFlags(taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        taskDescription.setTextColor(completedColor);
        taskDescription.setPaintFlags(taskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        taskTime.setTextColor(completedColor);
        taskTime.setPaintFlags(taskTime.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void setTaskPendingStyle(TextView taskTitle, TextView taskDescription, TextView taskTime) {
        int pendingColor = getResources().getColor(android.R.color.black);
        taskTitle.setTextColor(pendingColor);
        taskTitle.setPaintFlags(taskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        taskDescription.setTextColor(pendingColor);
        taskDescription.setPaintFlags(taskDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        taskTime.setTextColor(pendingColor);
        taskTime.setPaintFlags(taskTime.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }

    private void playCompletionSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(dashboard.this, R.raw.complete);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
    }
    private void   playTaskAddedSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(dashboard.this, R.raw.add);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
    }
}
