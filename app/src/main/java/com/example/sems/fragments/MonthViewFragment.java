package com.example.sems.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sems.R;
import com.example.sems.adapters.DaysAdapter;
import com.example.sems.database.DatabaseHelper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class MonthViewFragment extends Fragment {
    private static final String ARG_MONTH_DATE = "month_date";
    private LocalDate monthDate;
    private RecyclerView daysGrid;
    private DatabaseHelper databaseHelper;

    public static MonthViewFragment newInstance(LocalDate date) {
        MonthViewFragment fragment = new MonthViewFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MONTH_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            monthDate = (LocalDate) getArguments().getSerializable(ARG_MONTH_DATE);
        }
        databaseHelper = new DatabaseHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_month_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        daysGrid = view.findViewById(R.id.daysGrid);
        daysGrid.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        
        setupMonthDays();
    }

    private void setupMonthDays() {
        YearMonth yearMonth = YearMonth.from(monthDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        int daysInMonth = yearMonth.lengthOfMonth();

        List<LocalDate> days = new ArrayList<>();
        
        // Add empty spaces for days before the first of the month
        for (int i = 1; i < dayOfWeek; i++) {
            days.add(null);
        }
        
        // Add all days of the month
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(firstOfMonth.plusDays(i - 1));
        }

        DaysAdapter adapter = new DaysAdapter(days, date -> {
            if (getParentFragment() instanceof CalendarFragment) {
                ((CalendarFragment) getParentFragment()).onDateClick(date);
            }
        });
        daysGrid.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
} 