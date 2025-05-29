package com.example.sems.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sems.R;
import java.time.LocalDate;
import java.util.List;

public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.DayViewHolder> {
    private final List<LocalDate> days;
    private final OnDayClickListener listener;
    private LocalDate selectedDate = LocalDate.now();

    public interface OnDayClickListener {
        void onDayClick(LocalDate date);
    }

    public DaysAdapter(List<LocalDate> days, OnDayClickListener listener) {
        this.days = days;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.calendar_day_item, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        LocalDate date = days.get(position);
        holder.bind(date);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private final TextView dayText;
        private final View dayContainer;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.dayText);
            dayContainer = itemView.findViewById(R.id.dayContainer);
        }

        void bind(LocalDate date) {
            if (date == null) {
                dayText.setText("");
                dayContainer.setBackgroundResource(0);
                itemView.setOnClickListener(null);
                return;
            }

            dayText.setText(String.valueOf(date.getDayOfMonth()));
            
            // Highlight today
            boolean isToday = date.equals(LocalDate.now());
            // Highlight selected date
            boolean isSelected = date.equals(selectedDate);
            
            if (isToday) {
                dayContainer.setBackgroundResource(R.drawable.bg_calendar_today);
                dayText.setTextColor(itemView.getContext().getColor(R.color.white));
            } else if (isSelected) {
                dayContainer.setBackgroundResource(R.drawable.bg_calendar_selected);
                dayText.setTextColor(itemView.getContext().getColor(R.color.white));
            } else {
                dayContainer.setBackgroundResource(0);
                dayText.setTextColor(itemView.getContext().getColor(R.color.black));
            }

            itemView.setOnClickListener(v -> {
                selectedDate = date;
                listener.onDayClick(date);
                notifyDataSetChanged();
            });
        }
    }
} 