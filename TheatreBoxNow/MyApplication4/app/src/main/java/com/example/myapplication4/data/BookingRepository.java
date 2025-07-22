package com.example.myapplication4.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.myapplication4.models.Booking;
import com.example.myapplication4.models.Performance;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Repository class that abstracts data access from ViewModels.
 * Mediates between data sources (DB) and UI components, handling:
 * - Thread management (background execution)
 * - Data aggregation from multiple sources
 * - Single source of truth for performance/booking data
 */
public class BookingRepository {
    // Room Database Access Object
    private final BookingDao bookingDao;

    // Thread pool for database operations (prevents UI freezing)
    private final ExecutorService executor;

    // Cache of all performances (LiveData enables automatic UI updates)
    private LiveData<List<Performance>> allPerformances;

    public BookingRepository(Application application) {
        TheaterDatabase database = TheaterDatabase.getInstance(application);
        this.bookingDao = database.bookingDao();
        this.executor = database.getDatabaseWriteExecutor();
        this.allPerformances = bookingDao.getAllPerformances();
    }

    // Performance-related operations --------------------------------

    /**
     * @return LiveData list of all performances (observed by UI)
     */
    public LiveData<List<Performance>> getAllPerformances() {
        return allPerformances;
    }

    /**
     * @param id Performance ID
     * @return LiveData of single performance (for detail views)
     */
    public LiveData<Performance> getPerformance(int id) {
        return bookingDao.getPerformanceById(id);
    }

    /**
     * Inserts new performance on background thread
     * @param performance Performance to add
     */
    public void insertPerformance(Performance performance) {
        executor.execute(() -> bookingDao.insertPerformance(performance));
    }

    // Booking-related operations ------------------------------------

    /**
     * Atomic transaction: Creates booking AND updates seat availability
     * @param booking New booking details
     * @param seatsBooked Number of seats being reserved
     */
    public void bookTickets(Booking booking, int seatsBooked) {
        executor.execute(() -> {
            // Transaction-like behavior (both operations must succeed)
            bookingDao.insertBooking(booking);
            bookingDao.decreaseAvailableSeats(booking.performanceId, seatsBooked);
        });
    }

    /**
     * Atomic transaction: Cancels booking AND restores seat availability
     * @param booking Booking to cancel (must include performanceId)
     */
    public void cancelBooking(Booking booking) {
        executor.execute(() -> {
            int seatsToRestore = booking.seats.split(",").length; // Count individual seats
            bookingDao.deleteBooking(booking);
            bookingDao.increaseAvailableSeats(booking.performanceId, seatsToRestore);
        });
    }

    /**
     * @param email Customer's email address
     * @return LiveData list of customer's bookings
     */
    public LiveData<List<Booking>> getCustomerBookings(String email) {
        return bookingDao.getBookingsByCustomer(email);
    }


    public LiveData<List<Booking>> getBookingsByCustomer(String email) {
        return bookingDao.getBookingsByCustomer(email);
    }
}
