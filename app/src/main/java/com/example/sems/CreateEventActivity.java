package com.example.sems;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sems.database.DatabaseHelper;
import com.example.sems.models.Event;
import com.example.sems.models.User;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Date;

public class CreateEventActivity extends AppCompatActivity {
    private static final String TAG = "CreateEventActivity";

    private TextInputEditText etTitle, etDescription, etLocation;
    private Button btnStartDate, btnEndDate, btnCreate;
    private DatabaseHelper dbHelper;
    private Date startDate, endDate;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_create_event);

            // Initialize database
            dbHelper = new DatabaseHelper(this);
            
            // Get and validate user email
            String userEmail = getIntent().getStringExtra("user_email");
            Log.d(TAG, "Received user email from intent: " + userEmail);
            
            if (userEmail == null || userEmail.isEmpty()) {
                Log.e(TAG, "No user email provided in intent");
                Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Get user from database
            currentUser = dbHelper.getUserByEmail(userEmail);
            Log.d(TAG, "Retrieved user from database: " + (currentUser != null ? currentUser.getEmail() : "null"));
            
            if (currentUser == null) {
                Log.e(TAG, "User not found in database for email: " + userEmail);
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize views and setup
            initializeViews();
            setupInitialDates();
            setupDatePickers();
            setupCreateButton();
            setupActionBar();
            
            Log.d(TAG, "Activity initialization completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing event creation", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            etTitle = findViewById(R.id.etTitle);
            etDescription = findViewById(R.id.etDescription);
            etLocation = findViewById(R.id.etLocation);
            btnStartDate = findViewById(R.id.btnStartDate);
            btnEndDate = findViewById(R.id.btnEndDate);
            btnCreate = findViewById(R.id.btnCreate);
        } catch (Exception e) {
            Log.e(TAG, "Error in initializeViews: " + e.getMessage());
            throw e;
        }
    }

    private void setupInitialDates() {
        try {
            long selectedDateMillis = getIntent().getLongExtra("selected_date", -1);
            if (selectedDateMillis != -1) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selectedDateMillis);
                calendar.set(Calendar.HOUR_OF_DAY, 9); // Default to 9 AM
                calendar.set(Calendar.MINUTE, 0);
                startDate = calendar.getTime();
                
                // Set end date to 1 hour after start date by default
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                endDate = calendar.getTime();
                
                updateDateTimeButtons();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in setupInitialDates: " + e.getMessage());
        }
    }

    private void setupDatePickers() {
        try {
            Calendar calendar = Calendar.getInstance();
            if (startDate != null) {
                calendar.setTime(startDate);
            }

            setupStartDatePicker(calendar);
            setupEndDatePicker(calendar);
        } catch (Exception e) {
            Log.e(TAG, "Error in setupDatePickers: " + e.getMessage());
        }
    }

    private void setupStartDatePicker(final Calendar calendar) {
        try {
            btnStartDate.setOnClickListener(v -> {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {
                            try {
                                calendar.set(year, month, dayOfMonth);
                                showTimePicker(calendar, true);
                            } catch (Exception e) {
                                Log.e(TAG, "Error setting start date: " + e.getMessage());
                                Toast.makeText(this, "Error setting date", Toast.LENGTH_SHORT).show();
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in setupStartDatePicker: " + e.getMessage());
        }
    }

    private void setupEndDatePicker(final Calendar calendar) {
        try {
            btnEndDate.setOnClickListener(v -> {
                if (startDate == null) {
                    Toast.makeText(this, "Please select start date first", Toast.LENGTH_SHORT).show();
                    return;
                }

                calendar.setTime(startDate);
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {
                            try {
                                calendar.set(year, month, dayOfMonth);
                                showTimePicker(calendar, false);
                            } catch (Exception e) {
                                Log.e(TAG, "Error setting end date: " + e.getMessage());
                                Toast.makeText(this, "Error setting date", Toast.LENGTH_SHORT).show();
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.getDatePicker().setMinDate(startDate.getTime());
                datePickerDialog.show();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in setupEndDatePicker: " + e.getMessage());
        }
    }

    private void showTimePicker(final Calendar calendar, final boolean isStartDate) {
        try {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view1, hourOfDay, minute) -> {
                        try {
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            calendar.set(Calendar.MINUTE, minute);
                            if (isStartDate) {
                                startDate = calendar.getTime();
                            } else {
                                endDate = calendar.getTime();
                            }
                            updateDateTimeButtons();
                        } catch (Exception e) {
                            Log.e(TAG, "Error setting time: " + e.getMessage());
                            Toast.makeText(this, "Error setting time", Toast.LENGTH_SHORT).show();
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error in showTimePicker: " + e.getMessage());
        }
    }

    private void setupCreateButton() {
        try {
            btnCreate.setOnClickListener(v -> createEvent());
        } catch (Exception e) {
            Log.e(TAG, "Error in setupCreateButton: " + e.getMessage());
        }
    }

    private void setupActionBar() {
        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Create New Event");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in setupActionBar: " + e.getMessage());
        }
    }

    private void updateDateTimeButtons() {
        try {
            if (startDate != null) {
                btnStartDate.setText(formatDateTime(startDate));
            }
            if (endDate != null) {
                btnEndDate.setText(formatDateTime(endDate));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in updateDateTimeButtons: " + e.getMessage());
        }
    }

    private void createEvent() {
        try {
            Log.d(TAG, "Starting event creation process");
            
            // Validate input fields
            if (etTitle == null || etDescription == null || etLocation == null) {
                Log.e(TAG, "Error: Input fields are null");
                Toast.makeText(this, "Error: Input fields not initialized", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get and trim input values
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";

            Log.d(TAG, "Input values - Title: " + title + ", Description: " + description + ", Location: " + location);

            // Clear any previous errors
            etTitle.setError(null);
            etDescription.setError(null);
            etLocation.setError(null);

            // Validate user session
            if (currentUser == null || currentUser.getEmail() == null) {
                Log.e(TAG, "Current user is null or has no email");
                Toast.makeText(this, "User session expired. Please log in again.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Log.d(TAG, "Creating event for user: " + currentUser.getEmail());

            // Validate required fields
            boolean hasError = false;
            if (title.isEmpty()) {
                etTitle.setError("Title is required");
                hasError = true;
                Log.e(TAG, "Title validation failed - empty title");
            }
            if (description.isEmpty()) {
                etDescription.setError("Description is required");
                hasError = true;
                Log.e(TAG, "Description validation failed - empty description");
            }
            if (location.isEmpty()) {
                etLocation.setError("Location is required");
                hasError = true;
                Log.e(TAG, "Location validation failed - empty location");
            }
            if (startDate == null) {
                Toast.makeText(this, "Please select start date and time", Toast.LENGTH_SHORT).show();
                hasError = true;
                Log.e(TAG, "Start date validation failed - null start date");
            }
            if (endDate == null) {
                Toast.makeText(this, "Please select end date and time", Toast.LENGTH_SHORT).show();
                hasError = true;
                Log.e(TAG, "End date validation failed - null end date");
            }
            if (hasError) {
                Log.e(TAG, "Validation failed - returning without creating event");
                return;
            }

            // Validate date order
            if (endDate != null && startDate != null && endDate.before(startDate)) {
                Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Date order validation failed - end date before start date");
                return;
            }

            // Create event object
            Event event = new Event(
                0,
                title,
                description,
                startDate,
                endDate,
                location,
                currentUser.getEmail(),
                true
            );

            Log.d(TAG, "Created event object: " + event.toString());

            // Validate event using the model's validation
            if (!event.isValid()) {
                Log.e(TAG, "Event validation failed in Event model");
                Toast.makeText(this, "Please check all fields and try again", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Event validation passed, attempting to save to database");

            // Attempt to save event
            long result = dbHelper.addEvent(event);

            if (result != -1) {
                Log.d(TAG, "Event created successfully with ID: " + result);
                setResult(RESULT_OK);
                Toast.makeText(this, "Event created successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e(TAG, "Failed to create event in database");
                Toast.makeText(this, "Failed to create event. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in createEvent: " + e.getMessage(), e);
            Toast.makeText(this, "Error creating event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDateTime(Date date) {
        try {
            if (date != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault());
                return sdf.format(date);
            }
            return "";
        } catch (Exception e) {
            Log.e(TAG, "Error in formatDateTime: " + e.getMessage());
            return "";
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        try {
            onBackPressed();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error in onSupportNavigateUp: " + e.getMessage());
            finish();
            return true;
        }
    }
}