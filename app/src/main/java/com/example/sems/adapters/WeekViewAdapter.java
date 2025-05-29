package com.example.sems.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sems.R;
import com.example.sems.database.DatabaseHelper;
import com.example.sems.interfaces.OnDateClickListener;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class WeekViewAdapter extends RecyclerView.Adapter<WeekViewAdapter.WeekDayViewHolder> {
    private final List<LocalDate> weekDays;
    private final OnDateClickListener listener;
    private final DatabaseHelper databaseHelper;
    private LocalDate selectedDate = LocalDate.now();
    private static final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE\nd");

    public WeekViewAdapter(List<LocalDate> weekDays, OnDateClickListener listener, DatabaseHelper databaseHelper) {
        this.weekDays = weekDays;
        this.listener = listener;
        this.databaseHelper = databaseHelper;
    }

    @NonNull
    @Override
    public WeekDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.week_day_item, parent, false);
        return new WeekDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekDayViewHolder holder, int position) {
        LocalDate date = weekDays.get(position);
        holder.bind(date);
    }

    @Override
    public int getItemCount() {
        return weekDays.size();
    }

    class WeekDayViewHolder extends RecyclerView.ViewHolder {
        private final TextView dayText;
        private final TextView eventCount;
        private final View dayContainer;

        WeekDayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.dayText);
            eventCount = itemView.findViewById(R.id.eventCount);
            dayContainer = itemView.findViewById(R.id.dayContainer);
        }

        void bind(LocalDate date) {
            dayText.setText(date.format(dayFormatter));
            
            // Convert LocalDate to Date for database query
            Date queryDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            int count = databaseHelper.getEventCountForDate(queryDate);
            
            if (count > 0) {
                eventCount.setVisibility(View.VISIBLE);
                eventCount.setText(String.valueOf(count));
            } else {
                eventCount.setVisibility(View.GONE);
            }

            boolean isToday = date.equals(LocalDate.now());
            boolean isSelected = date.equals(selectedDate);
            
            if (isToday) {
                dayContainer.setBackgroundResource(R.drawable.bg_calendar_today);
                dayText.setTextColor(itemView.getContext().getColor(R.color.white));
                eventCount.setTextColor(itemView.getContext().getColor(R.color.white));
            } else if (isSelected) {
                dayContainer.setBackgroundResource(R.drawable.bg_calendar_selected);
                dayText.setTextColor(itemView.getContext().getColor(R.color.white));
                eventCount.setTextColor(itemView.getContext().getColor(R.color.white));
            } else {
                dayContainer.setBackgroundResource(0);
                dayText.setTextColor(itemView.getContext().getColor(R.color.black));
                eventCount.setTextColor(itemView.getContext().getColor(R.color.black));
            }

            itemView.setOnClickListener(v -> {
                selectedDate = date;
                listener.onDateClick(date);
                notifyDataSetChanged();
            });
        }
    }
} 