package com.example.myapplication4.data;

import com.example.myapplication4.models.Performance;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for pre-populating the database with initial performance data.
 * Should be called once during app startup to ensure default data exists.
 */
public class DatabaseInitializer {

    /**
     * Asynchronously initializes the database with default performance data.
     * Uses the database write executor to avoid blocking the UI thread.
     *
     * @param db The TheaterDatabase instance to initialize
     */
    public static void initializeAsync(TheaterDatabase db) {
        // Default performances to be inserted
        List<Performance> performances = Arrays.asList(
                new Performance("Hamlet", "Auditorium 1", "14:00", 2),
                new Performance("Hamlet", "Auditorium 1", "19:00", 1),
                new Performance("Romeo and Juliet", "Auditorium 2", "14:00", 5),
                new Performance("Romeo and Juliet", "Auditorium 2", "19:00", 10)
        );

        // Execute on background thread
        TheaterDatabase.getDatabaseWriteExecutor().execute(() -> {
            // Individual inserts (consider using transaction if many items)
            db.bookingDao().insertPerformance(performances.get(0));
            db.bookingDao().insertPerformance(performances.get(1));
            db.bookingDao().insertPerformance(performances.get(2));
            db.bookingDao().insertPerformance(performances.get(3));


        });
    }
}
