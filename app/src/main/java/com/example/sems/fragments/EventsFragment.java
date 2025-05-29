package com.example.sems.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.sems.R;
import com.example.sems.adapters.EventAdapter;
import com.example.sems.database.DatabaseHelper;
import com.example.sems.models.Event;
import com.example.sems.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.sems.CreateEventActivity;
import android.app.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventsFragment extends Fragment {
    private static final String TAG = "EventsFragment";
    private static final int CREATE_EVENT_REQUEST = 1;

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private FloatingActionButton fabAddEvent;
    private TextView tvNoEvents;
    private User currentUser;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private List<Event> allEvents;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        try {
            dbHelper = new DatabaseHelper(requireContext());
            
            // Get user email from arguments
            String userEmail = getArguments() != null ? getArguments().getString("user_email") : null;
            if (userEmail != null) {
                currentUser = dbHelper.getUserByEmail(userEmail);
                Log.d(TAG, "User email from arguments: " + userEmail);
                Log.d(TAG, "Current user: " + (currentUser != null ? currentUser.getEmail() : "null"));
            } else {
                Log.e(TAG, "No user email in arguments");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_events, container, false);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            initializeViews(view);
            setupRecyclerView();
            setupSwipeRefresh();
            setupFabButton();
            controlFabVisibility();
            loadEvents();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage());
            Toast.makeText(requireContext(), "Error initializing events view", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews(View view) {
        try {
            recyclerView = view.findViewById(R.id.recyclerView);
            fabAddEvent = view.findViewById(R.id.fabAddEvent);
            tvNoEvents = view.findViewById(R.id.tvNoEvents);
            swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        } catch (Exception e) {
            Log.e(TAG, "Error in initializeViews: " + e.getMessage());
            throw e;
        }
    }

    private void setupRecyclerView() {
        try {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new EventAdapter(requireContext());
            adapter.setOnItemClickListener(event -> showEventDetails(event));
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error in setupRecyclerView: " + e.getMessage());
        }
    }

    private void setupSwipeRefresh() {
        try {
            swipeRefreshLayout.setOnRefreshListener(this::refreshEvents);
            swipeRefreshLayout.setColorSchemeResources(R.color.purple_500);
        } catch (Exception e) {
            Log.e(TAG, "Error in setupSwipeRefresh: " + e.getMessage());
        }
    }

    private void setupFabButton() {
        try {
            fabAddEvent.setOnClickListener(v -> {
                if (currentUser != null && "admin".equals(currentUser.getRole())) {
                    Log.d(TAG, "Creating event with user email: " + currentUser.getEmail());
                    Intent intent = new Intent(requireContext(), CreateEventActivity.class);
                    intent.putExtra("user_email", currentUser.getEmail());
                    startActivityForResult(intent, CREATE_EVENT_REQUEST);
                } else {
                    Log.e(TAG, "Cannot create event: Current user is null or not admin");
                    Toast.makeText(requireContext(), "Only admins can create events", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in setupFabButton: " + e.getMessage());
        }
    }

    private void controlFabVisibility() {
        if (currentUser != null && "admin".equals(currentUser.getRole())) {
            fabAddEvent.setVisibility(View.VISIBLE);
        } else {
            fabAddEvent.setVisibility(View.GONE);
        }
    }

    private void loadEvents() {
        try {
            if (dbHelper != null && currentUser != null) {
                Log.d(TAG, "Loading events from database for user: " + currentUser.getEmail());
                allEvents = dbHelper.getEventsByOrganizer(currentUser.getEmail());
                Log.d(TAG, "Loaded " + (allEvents != null ? allEvents.size() : 0) + " events for user: " + currentUser.getEmail());
                updateEventsDisplay(allEvents);
            } else if (currentUser == null) {
                Log.e(TAG, "Cannot load events: Current user is null.");
                // Optionally clear the event list and show a message if user is null
                allEvents = new ArrayList<>();
                updateEventsDisplay(allEvents);
            } else {
                Log.e(TAG, "Cannot load events: Database helper is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in loadEvents: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error loading events", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEventsDisplay(List<Event> events) {
        try {
            if (events == null) {
                Log.e(TAG, "Events list is null");
                events = new ArrayList<>();
            }

            Log.d(TAG, "Updating events display with " + events.size() + " events");
            
            if (events.isEmpty()) {
                tvNoEvents.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvNoEvents.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                // Sort events by date (most recent first)
                Collections.sort(events, (e1, e2) -> e2.getStartDate().compareTo(e1.getStartDate()));
                adapter.updateEvents(events);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in updateEventsDisplay: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error displaying events", Toast.LENGTH_SHORT).show();
        }
    }

    public void refreshEvents() {
        try {
            Log.d(TAG, "Refreshing events");
            loadEvents();
            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in refreshEvents: " + e.getMessage(), e);
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void showEventDetails(Event event) {
        try {
            // TODO: Implement event details view
            Toast.makeText(requireContext(), "Event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error in showEventDetails: " + e.getMessage());
        }
    }

    private void filterEvents(String query) {
        try {
            if (allEvents == null) return;

            List<Event> filteredList = new ArrayList<>();
            String lowercaseQuery = query.toLowerCase().trim();

            for (Event event : allEvents) {
                if (event.getTitle().toLowerCase().contains(lowercaseQuery) ||
                    event.getDescription().toLowerCase().contains(lowercaseQuery) ||
                    event.getLocation().toLowerCase().contains(lowercaseQuery)) {
                    filteredList.add(event);
                }
            }

            updateEventsDisplay(filteredList);
        } catch (Exception e) {
            Log.e(TAG, "Error in filterEvents: " + e.getMessage());
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        try {
            inflater.inflate(R.menu.events_menu, menu);
            MenuItem searchItem = menu.findItem(R.id.action_search);
            searchView = (SearchView) searchItem.getActionView();
            
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filterEvents(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterEvents(newText);
                    return true;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateOptionsMenu: " + e.getMessage());
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Log.d(TAG, "onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);
            if (requestCode == CREATE_EVENT_REQUEST) {
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "Event created successfully, refreshing events list");
                    refreshEvents();
                    Toast.makeText(requireContext(), "Event created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Event creation cancelled or failed");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onActivityResult: " + e.getMessage(), e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "Fragment resumed, refreshing events");
            refreshEvents();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
        }
    }
}