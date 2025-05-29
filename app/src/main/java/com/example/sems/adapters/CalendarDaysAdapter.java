package com.example.sems.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sems.R;
import com.example.sems.interfaces.OnDateClickListener;
import java.time.LocalDate;
import java.util.List;

public class CalendarDaysAdapter extends RecyclerView.Adapter<CalendarDaysAdapter.DayViewHolder> {
    private final List<LocalDate> days;
    private final OnDateClickListener listener;
    private final LocalDate today = LocalDate.now();

    public CalendarDaysAdapter(List<LocalDate> days, OnDateClickListener listener) {
        this.days = days;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.calendar_day_item, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() / 6f); // 6 rows max in a month
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
        private final View container;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.dayText);
            container = itemView.findViewById(R.id.dayContainer);
        }

        void bind(LocalDate date) {
            if (date == null) {
                dayText.setText("");
                container.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                container.setOnClickListener(null);
                return;
            }

            dayText.setText(String.valueOf(date.getDayOfMonth()));
            
            // Highlight today
            if (date.equals(today)) {
                container.setBackgroundResource(R.drawable.today_background);
                dayText.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            } else {
                container.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                dayText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
            }

            container.setOnClickListener(v -> listener.onDateClick(date));
        }
    }
} 