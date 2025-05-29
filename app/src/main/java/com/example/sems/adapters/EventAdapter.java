package com.example.sems.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sems.R;
import com.example.sems.models.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private final Context context;
    private List<Event> events;
    private OnEventClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private OnEventLongClickListener longClickListener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public interface OnEventLongClickListener {
        void onEventLongClick(Event event);
    }

    public EventAdapter(Context context) {
        this.context = context;
        this.events = new ArrayList<>();
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnEventLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false);
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

    // For compatibility with existing code
    public void updateEvents(List<Event> events) {
        setEvents(events);
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView dateText;
        private final TextView timeText;
        private final TextView locationText;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            locationText = itemView.findViewById(R.id.locationText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEventClick(events.get(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onEventLongClick(events.get(position));
                    return true; // Consume the long click
                }
                return false;
            });
        }

        void bind(Event event) {
            titleText.setText(event.getTitle());
            dateText.setText(dateFormat.format(event.getStartDate()));
            String timeRange = timeFormat.format(event.getStartDate()) + " - " + 
                             timeFormat.format(event.getEndDate());
            timeText.setText(timeRange);
            locationText.setText(event.getLocation());
        }
    }
}