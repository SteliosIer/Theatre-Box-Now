package com.example.myapplication4.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * Represents a theatrical performance entity in the database.
 * Stores information about show times, locations, and seat availability.
 */
@Entity(tableName = "performances")
public class Performance {
    @PrimaryKey(autoGenerate = true)
    private int id;  // Auto-generated unique identifier

    @NonNull
    private String title;  // Name of the performance (e.g., "Hamlet")

    @NonNull
    private String auditorium;  // Venue location (e.g., "Main Hall")

    @NonNull
    private String time;  // Show time in HH:mm format (e.g., "19:30")

    private int availableSeats;  // Remaining seat count (updated when bookings are made)

    /**
     * Constructor for creating new performances
     * @param title Name of the performance (non-null)
     * @param auditorium Location of the performance (non-null)
     * @param time Show time in HH:mm format (non-null)
     * @param availableSeats Initial seat capacity
     */
    public Performance(@NonNull String title, @NonNull String auditorium,
                       @NonNull String time, int availableSeats) {
        this.title = title;
        this.auditorium = auditorium;
        this.time = time;
        this.availableSeats = availableSeats;
    }

    // Accessor methods ----------------------------------------------

    public int getId() { return id; }

    @NonNull
    public String getTitle() { return title; }

    @NonNull
    public String getAuditorium() { return auditorium; }

    @NonNull
    public String getTime() { return time; }

    public int getAvailableSeats() { return availableSeats; }

    // Mutator methods (required by Room) ----------------------------

    public void setId(int id) { this.id = id; }

    public void setTitle(@NonNull String title) { this.title = title; }

    public void setAuditorium(@NonNull String auditorium) { this.auditorium = auditorium; }

    public void setTime(@NonNull String time) { this.time = time; }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    // Potential business logic methods ------------------------------

    /**
     * @return Formatted display string (e.g., "Hamlet - Main Hall - 19:30")
     */
    public String getDisplayName() {
        return title + " - " + auditorium + " - " + time;
    }

    /**
     * Checks if seats are available
     * @param requestedSeats Number of seats desired
     * @return True if requested seats are available
     */
    public boolean canBookSeats(int requestedSeats) {
        return availableSeats >= requestedSeats;
    }
}
