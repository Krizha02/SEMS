package com.example.sems.models;

import android.util.Log;
import java.util.Date;

public class Event {
    private static final String TAG = "Event";
    private int id;
    private String title;
    private String description;
    private Date startDate;
    private Date endDate;
    private String location;
    private String organizer;
    private boolean isActive;

    public Event(int id, String title, String description, Date startDate, Date endDate, 
                String location, String organizer, boolean isActive) {
        try {
            this.id = id;
            this.title = title != null ? title.trim() : "";
            this.description = description != null ? description.trim() : "";
            this.startDate = startDate != null ? startDate : new Date();
            this.endDate = endDate != null ? endDate : new Date();
            this.location = location != null ? location.trim() : "";
            this.organizer = organizer != null ? organizer.trim() : "";
            this.isActive = isActive;

            // Log event creation
            Log.d(TAG, "Creating event: " + toString());
        } catch (Exception e) {
            Log.e(TAG, "Error creating event: " + e.getMessage(), e);
            throw new IllegalArgumentException("Invalid event data: " + e.getMessage());
        }
    }

    // Getters and Setters with validation
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title != null ? title.trim() : "";
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) {
        this.startDate = startDate != null ? startDate : new Date();
    }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) {
        this.endDate = endDate != null ? endDate : new Date();
    }

    public String getLocation() { return location; }
    public void setLocation(String location) {
        this.location = location != null ? location.trim() : "";
    }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) {
        this.organizer = organizer != null ? organizer.trim() : "";
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", location='" + location + '\'' +
                ", organizer='" + organizer + '\'' +
                ", isActive=" + isActive +
                '}';
    }

    public boolean isValid() {
        try {
            // Log all validation checks
            Log.d(TAG, "Validating event: " + toString());
            
            // Check title
            if (title == null || title.trim().isEmpty()) {
                Log.e(TAG, "Invalid event: Title is empty");
                return false;
            }
            Log.d(TAG, "Title validation passed");

            // Check description
            if (description == null || description.trim().isEmpty()) {
                Log.e(TAG, "Invalid event: Description is empty");
                return false;
            }
            Log.d(TAG, "Description validation passed");

            // Check location
            if (location == null || location.trim().isEmpty()) {
                Log.e(TAG, "Invalid event: Location is empty");
                return false;
            }
            Log.d(TAG, "Location validation passed");

            // Check organizer
            if (organizer == null || organizer.trim().isEmpty()) {
                Log.e(TAG, "Invalid event: Organizer is empty");
                return false;
            }
            Log.d(TAG, "Organizer validation passed");

            // Check dates
            if (startDate == null) {
                Log.e(TAG, "Invalid event: Start date is null");
                return false;
            }
            if (endDate == null) {
                Log.e(TAG, "Invalid event: End date is null");
                return false;
            }
            Log.d(TAG, "Date null checks passed");

            // Check date order
            if (endDate.before(startDate)) {
                Log.e(TAG, "Invalid event: End date (" + endDate + ") is before start date (" + startDate + ")");
                return false;
            }
            Log.d(TAG, "Date order validation passed");

            Log.d(TAG, "Event validation successful");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error validating event: " + e.getMessage(), e);
            return false;
        }
    }
}