package com.example.sems.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.fragment.app.Fragment;

import com.example.sems.fragments.CalendarFragment;
import com.example.sems.fragments.MonthViewFragment;

import java.time.LocalDate;

public class CalendarPagerAdapter extends FragmentStateAdapter {
    private static final int TOTAL_PAGES = 25; // Show 12 months before and after current month
    private final int initialPosition = TOTAL_PAGES / 2;

    public CalendarPagerAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        LocalDate date = LocalDate.now().plusMonths(position - initialPosition);
        return MonthViewFragment.newInstance(date);
    }

    @Override
    public int getItemCount() {
        return TOTAL_PAGES;
    }

    public int getInitialPosition() {
        return initialPosition;
    }
} 