package com.example.myapplication4.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.List;
/**
 * Represents a booking entity in the database.
 * Stores information about customer reservations for performances.
 */
@Entity(
        tableName = "bookings",
        foreignKeys = @ForeignKey(
                entity = Performance.class,
                parentColumns = "id",
                childColumns = "performanceId",
                onDelete = ForeignKey.CASCADE  // Deletes booking if performance is deleted
        ),
        indices = {@Index("performanceId")}  // Improves query performance for performance-based lookups
)
public class Booking {
    @PrimaryKey(autoGenerate = true)
    public int id;  // Auto-incremented unique identifier

    @ColumnInfo(name = "performanceId")
    public int performanceId;  // Foreign key to Performance table

    @ColumnInfo(name = "customerName")
    public String customerName;  // Full name of the customer

    @ColumnInfo(name = "customerEmail")
    public String customerEmail;  // Contact email for the booking

    /**
     * Comma-separated list of seat numbers (e.g., "A1,A2,A3")
     * Alternative: Could use a separate join table for more complex seat management
     */
    @ColumnInfo(name = "seats")
    public String seats;

    /**
     * Timestamp of when booking was created (milliseconds since epoch)
     * Useful for sorting and expiration policies
     */
    @ColumnInfo(name = "bookingTime")
    public long bookingTime;

    /**
     * Constructor for creating new bookings
     * @param performanceId ID of the performance being booked
     * @param customerName Name of the ticket holder
     * @param customerEmail Contact email for confirmation
     * @param seats Comma-separated seat numbers
     */
    public Booking(int performanceId, String customerName, String customerEmail, String seats) {
        this.performanceId = performanceId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.seats = seats;
        this.bookingTime = System.currentTimeMillis();  // Sets creation timestamp
    }

    // Potential helper methods (not shown in original)
    /**
     * @return Number of seats in this booking
     */
    public int getSeatCount() {
        return seats.split(",").length;
    }

    /**
     * @return Array of individual seat numbers
     */
    public String[] getSeatArray() {
        return seats.split(",");
    }
}