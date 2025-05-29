package com.example.sems.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.sems.CreateEventActivity;
import com.example.sems.R;
import com.example.sems.adapters.CalendarPagerAdapter;
import com.example.sems.adapters.DayViewAdapter;
import com.example.sems.adapters.EventAdapter;
import com.example.sems.adapters.WeekViewAdapter;
import com.example.sems.database.DatabaseHelper;
import com.example.sems.interfaces.OnDateClickListener;
import com.example.sems.models.Event;
import com.example.sems.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends Fragment implements OnDateClickListener {
    private static final String TAG = "CalendarFragment";
    private static final int CREATE_EVENT_REQUEST = 1;
    private static final int VIEW_MODE_MONTH = 1;
    private static final int VIEW_MODE_WEEK = 2;
    private static final int VIEW_MODE_DAY = 3;
    private static final int VIEW_MODE_LIST = 4;
    
    private ViewPager2 calendarViewPager;
    private TextView monthYearText;
    private CalendarPagerAdapter adapter;
    private MaterialButtonToggleGroup viewToggleGroup;
    private MaterialButton addEventButton;
    private Map<String, Integer> eventCountMap;
    private final DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    private LocalDate selectedDate = LocalDate.now();
    private View monthView;
    private View weekView;
    private View dayView;
    private View listView;
    private ViewGroup calendarContainer;
    private DatabaseHelper dbHelper;
    private User currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventCountMap = new HashMap<>();
        dbHelper = new DatabaseHelper(requireContext());
        String userEmail = getArguments() != null ? getArguments().getString("user_email") : null;
        if (userEmail != null) {
            currentUser = dbHelper.getUserByEmail(userEmail);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        calendarViewPager = view.findViewById(R.id.calendarViewPager);
        monthYearText = view.findViewById(R.id.tvMonthYear);
        viewToggleGroup = view.findViewById(R.id.viewToggleGroup);
        addEventButton = view.findViewById(R.id.addEventButton);

        // Initialize view container
        calendarContainer = view.findViewById(R.id.calendarViewPager);

        // Set up calendar adapter
        adapter = new CalendarPagerAdapter(this);
        calendarViewPager.setAdapter(adapter);
        
        // Set initial position to current month
        int initialPosition = adapter.getInitialPosition();
        calendarViewPager.setCurrentItem(initialPosition, false);

        // Update month/year text when page changes
        calendarViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                LocalDate date = LocalDate.now().plusMonths(position - initialPosition);
                monthYearText.setText(date.format(monthYearFormatter));
            }
        });

        // Set up view toggle group
        viewToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                int viewMode = getViewMode(checkedId);
                switchView(viewMode);
            }
        });

        // Set initial view to month
        viewToggleGroup.check(R.id.btnMonth);

        // Set up add event button
        setupAddEventButton();
        controlAddEventButtonVisibility();
    }

    private int getViewMode(int checkedId) {
        if (checkedId == R.id.btnMonth) {
            return VIEW_MODE_MONTH;
        } else if (checkedId == R.id.btnWeek) {
            return VIEW_MODE_WEEK;
        } else if (checkedId == R.id.btnDay) {
            return VIEW_MODE_DAY;
        } else if (checkedId == R.id.btnList) {
            return VIEW_MODE_LIST;
        }
        return VIEW_MODE_MONTH; // Default to month view
    }

    private void setupAddEventButton() {
        addEventButton.setOnClickListener(v -> {
            try {
                if (getActivity() == null) {
                    Toast.makeText(requireContext(), "Cannot create event at this time", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userEmail = getUserEmail();
                if (userEmail == null || userEmail.isEmpty()) {
                    Toast.makeText(requireContext(), "Please log in again to create events", Toast.LENGTH_SHORT).show();
                    return;
                }

                launchCreateEventActivity(userEmail);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Error creating event", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void controlAddEventButtonVisibility() {
        if (currentUser != null && "admin".equals(currentUser.getRole())) {
            addEventButton.setVisibility(View.VISIBLE);
        } else {
            addEventButton.setVisibility(View.GONE);
        }
    }

    private String getUserEmail() {
        if (getActivity() != null && getActivity().getIntent() != null) {
            return getActivity().getIntent().getStringExtra("user_email");
        }
        return null;
    }

    private void launchCreateEventActivity(String userEmail) {
        Intent intent = new Intent(requireContext(), CreateEventActivity.class);
        // Convert LocalDate to Date
        Date eventDate = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        intent.putExtra("selected_date", eventDate.getTime());
        intent.putExtra("user_email", userEmail);
        startActivityForResult(intent, CREATE_EVENT_REQUEST);
    }

    @Override
    public void onDateClick(LocalDate date) {
        selectedDate = date;
        Toast.makeText(requireContext(), "Selected date: " + date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_EVENT_REQUEST) {
            // Refresh all calendar views
            refreshAllViews();
        }
    }

    private void refreshAllViews() {
        // Refresh month view
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // Refresh week view if visible
        if (weekView != null && weekView.getVisibility() == View.VISIBLE) {
            updateWeekView();
        }

        // Refresh day view if visible
        if (dayView != null && dayView.getVisibility() == View.VISIBLE) {
            updateDayView();
        }

        // Refresh list view if visible
        if (listView != null && listView.getVisibility() == View.VISIBLE) {
            updateListView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            refreshAllViews();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage());
        }
        // Refresh user data and button visibility in onResume
        String userEmail = getArguments() != null ? getArguments().getString("user_email") : null;
        if (userEmail != null) {
            currentUser = dbHelper.getUserByEmail(userEmail);
            controlAddEventButtonVisibility();
        }
    }

    public void refreshEvents() {
        try {
            refreshAllViews();
        } catch (Exception e) {
            Log.e(TAG, "Error in refreshEvents: " + e.getMessage());
        }
    }

    private void updateSelectedDateDisplay() {
        // Implementation of updateSelectedDateDisplay method
    }

    private void loadEventsForSelectedDate() {
        // Implementation of loadEventsForSelectedDate method
    }

    private void updateEventCountMap() {
        // Implementation of updateEventCountMap method
    }

    private void switchView(int viewMode) {
        // Hide all views first
        calendarViewPager.setVisibility(View.GONE);
        if (weekView != null) weekView.setVisibility(View.GONE);
        if (dayView != null) dayView.setVisibility(View.GONE);
        if (listView != null) listView.setVisibility(View.GONE);

        switch (viewMode) {
            case VIEW_MODE_MONTH:
                calendarViewPager.setVisibility(View.VISIBLE);
                break;
            case VIEW_MODE_WEEK:
                if (weekView == null) {
                    weekView = createWeekView();
                }
                weekView.setVisibility(View.VISIBLE);
                updateWeekView();
                break;
            case VIEW_MODE_DAY:
                if (dayView == null) {
                    dayView = createDayView();
                }
                dayView.setVisibility(View.VISIBLE);
                updateDayView();
                break;
            case VIEW_MODE_LIST:
                if (listView == null) {
                    listView = createListView();
                }
                listView.setVisibility(View.VISIBLE);
                updateListView();
                break;
        }
    }

    private View createWeekView() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.calendar_week_view, calendarContainer, false);
        calendarContainer.addView(view);
        view.setVisibility(View.GONE);
        return view;
    }

    private View createDayView() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.calendar_day_view, calendarContainer, false);
        calendarContainer.addView(view);
        view.setVisibility(View.GONE);
        return view;
    }

    private View createListView() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.calendar_list_view, calendarContainer, false);
        calendarContainer.addView(view);
        view.setVisibility(View.GONE);
        return view;
    }

    private void updateWeekView() {
        if (weekView == null) return;

        // Get the current week's dates
        LocalDate startOfWeek = selectedDate.with(java.time.DayOfWeek.MONDAY);
        List<LocalDate> weekDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekDays.add(startOfWeek.plusDays(i));
        }

        // Update week range text
        TextView weekRangeText = weekView.findViewById(R.id.weekRangeText);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
        String weekRange = weekDays.get(0).format(formatter) + " - " + 
                         weekDays.get(6).format(formatter) + ", " + 
                         weekDays.get(0).getYear();
        weekRangeText.setText(weekRange);

        // Set up the RecyclerView
        RecyclerView weekDaysGrid = weekView.findViewById(R.id.weekDaysGrid);
        weekDaysGrid.setLayoutManager(new GridLayoutManager(requireContext(), 1, GridLayoutManager.HORIZONTAL, false));
        WeekViewAdapter adapter = new WeekViewAdapter(weekDays, this, getDatabaseHelper());
        weekDaysGrid.setAdapter(adapter);
    }

    private void updateDayView() {
        if (dayView == null) return;

        // Update day header
        TextView dayHeaderText = dayView.findViewById(R.id.dayHeaderText);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM d");
        dayHeaderText.setText(selectedDate.format(formatter));

        // Set up the RecyclerView
        RecyclerView dayScheduleList = dayView.findViewById(R.id.dayScheduleList);
        dayScheduleList.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Convert LocalDate to Date for database query
        Date date = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Event> events = getDatabaseHelper().getEventsByDate(date);
        
        DayViewAdapter adapter = new DayViewAdapter(events);
        dayScheduleList.setAdapter(adapter);
    }

    private DatabaseHelper getDatabaseHelper() {
        return dbHelper;
    }

    private void updateListView() {
        if (listView == null) return;

        // Set up the RecyclerView
        RecyclerView eventsListView = listView.findViewById(R.id.eventsListView);
        eventsListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Get all upcoming events from today
        Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Event> events = getDatabaseHelper().getUpcomingEvents(today);
        
        EventAdapter adapter = new EventAdapter(requireContext());
        adapter.setEvents(events);
        eventsListView.setAdapter(adapter);

        // Set click listener for events
        adapter.setOnItemClickListener(event -> {
            // TODO: Handle event click (e.g., show event details)
            Toast.makeText(requireContext(), "Event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}