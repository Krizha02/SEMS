package com.example.sems.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sems.R;
import com.example.sems.adapters.EventAdapter;
import com.example.sems.database.DatabaseHelper;
import com.example.sems.models.Event;
import com.example.sems.models.User;

import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class HomeFragment extends Fragment implements EventAdapter.OnEventLongClickListener {
    private static final String TAG = "HomeFragment";
    private DatabaseHelper dbHelper;
    private User currentUser;
    
    // UI Components
    private TextView tvTotalEvents;
    private TextView tvTotalUsers;
    private TextView tvUpcomingEvents;
    private RecyclerView rvRecentEvents;
    private TextView tvNoEvents;
    private EventAdapter eventAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());
        
        // Get user email from arguments
        String userEmail = getArguments() != null ? getArguments().getString("user_email") : null;
        if (userEmail != null) {
            currentUser = dbHelper.getUserByEmail(userEmail);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvTotalEvents = view.findViewById(R.id.tvTotalEvents);
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvUpcomingEvents = view.findViewById(R.id.tvUpcomingEvents);
        rvRecentEvents = view.findViewById(R.id.rvRecentEvents);
        tvNoEvents = view.findViewById(R.id.tvNoEvents);

        // Setup RecyclerView
        rvRecentEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventAdapter = new EventAdapter(requireContext());
        rvRecentEvents.setAdapter(eventAdapter);

        // Set long click listener for events
        eventAdapter.setOnItemLongClickListener(this);

        // Initial UI update
        updateUI();
    }

    private void updateUI() {
        if (currentUser != null) {
            // Update statistics
            int totalEvents = dbHelper.getTotalEventsByOrganizer(currentUser.getEmail());
            int totalUsers = dbHelper.getTotalUsers();
            int upcomingEvents = dbHelper.getUpcomingEventsCountByOrganizer(currentUser.getEmail());
            
            tvTotalEvents.setText(String.valueOf(totalEvents));
            tvTotalUsers.setText(String.valueOf(totalUsers));
            tvUpcomingEvents.setText(String.valueOf(upcomingEvents));

            // Update recent events
            List<Event> recentEvents = dbHelper.getRecentEventsByOrganizer(currentUser.getEmail(), 5); // Get 5 most recent events for the current user
            if (recentEvents != null && !recentEvents.isEmpty()) {
                eventAdapter.setEvents(recentEvents);
                rvRecentEvents.setVisibility(View.VISIBLE);
                tvNoEvents.setVisibility(View.GONE);
            } else {
                rvRecentEvents.setVisibility(View.GONE);
                tvNoEvents.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onEventLongClick(Event event) {
        if (currentUser != null && "admin".equals(currentUser.getRole())) {
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete the event: \"" + event.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Perform deletion
                    dbHelper.deleteEvent(event.getId());
                    Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                    // Refresh the list
                    updateUI();
                })
                .setNegativeButton("Cancel", null)
                .show();
        } else {
            Toast.makeText(requireContext(), "Only administrators can delete events", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        String userEmail = getArguments() != null ? getArguments().getString("user_email") : null;
        if (userEmail != null) {
            currentUser = dbHelper.getUserByEmail(userEmail);
            updateUI();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
} 