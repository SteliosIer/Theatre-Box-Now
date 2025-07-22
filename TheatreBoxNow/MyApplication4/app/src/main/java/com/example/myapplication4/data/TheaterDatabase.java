package com.example.myapplication4.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.myapplication4.models.Booking;
import com.example.myapplication4.models.Performance;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room Database class that serves as the main access point to the persisted data.
 * Configures the database connection and provides access to DAOs.
 */
@Database(entities = {Performance.class, Booking.class}, version = 1)
public abstract class TheaterDatabase extends RoomDatabase {

    // DAO Accessor
    public abstract BookingDao bookingDao();

    // Singleton instance
    private static volatile TheaterDatabase INSTANCE;

    // Thread pool configuration
    private static final int NUMBER_OF_THREADS = 4;
    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Gets the singleton instance of the database using double-checked locking pattern
     * @param context Application context
     * @return The singleton database instance
     */
    public static TheaterDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TheaterDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    TheaterDatabase.class, "theater_database")
                            .fallbackToDestructiveMigration() // Wipes DB on version mismatch
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Clears all data from both tables asynchronously
     * Useful for testing or complete data reset
     */
    public void clearAllTables() {
        if (isOpen()) {
            databaseWriteExecutor.execute(() -> {
                SupportSQLiteDatabase db = getOpenHelper().getWritableDatabase();
                db.execSQL("DELETE FROM performances");
                db.execSQL("DELETE FROM bookings");
            });
        }
    }

    /**
     * Provides access to the shared database write executor
     * @return The thread pool executor for database operations
     */
    public static ExecutorService getDatabaseWriteExecutor() {
        return databaseWriteExecutor;
    }
}
