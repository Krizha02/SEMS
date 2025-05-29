package com.example.sems.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sems.R;
import com.example.sems.models.Event;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DayViewAdapter extends RecyclerView.Adapter<DayViewAdapter.EventViewHolder> {
    private final List<Event> events;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public DayViewAdapter(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.day_event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeText;
        private final TextView titleText;
        private final TextView locationText;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.timeText);
            titleText = itemView.findViewById(R.id.titleText);
            locationText = itemView.findViewById(R.id.locationText);
        }

        void bind(Event event) {
            String timeRange = timeFormat.format(event.getStartDate()) + " - " + 
                             timeFormat.format(event.getEndDate());
            timeText.setText(timeRange);
            titleText.setText(event.getTitle());
            locationText.setText(event.getLocation());
        }
    }
} 