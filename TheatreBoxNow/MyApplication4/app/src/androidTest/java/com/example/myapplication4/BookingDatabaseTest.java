package com.example.myapplication4;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import com.example.myapplication4.data.BookingDao;
import com.example.myapplication4.data.TheaterDatabase;
import com.example.myapplication4.models.Performance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.Executors;

import utils.LiveDataTestUtil;

public class BookingDatabaseTest {
    // Rule for handling LiveData in tests (executes tasks synchronously)
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    // Database and DAO instances under test
    private TheaterDatabase database;
    private BookingDao dao;
    /**
     * Setup method run before each test case
     * Creates an in-memory database for isolated testing
     */
    @Before
    public void createDb() {
        database = Room.inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        TheaterDatabase.class
                )
                .allowMainThreadQueries() // Simplifies testing but not for production
                .setQueryExecutor(Executors.newSingleThreadExecutor())
                .build();

        // Clear any existing data to ensure test isolation
        database.clearAllTables();
        dao = database.bookingDao();
    }
    /**
     * Tests basic CRUD operation for Performance entity:
     * 1. Inserts a test performance
     * 2. Verifies it can be retrieved
     * 3. Checks field values match
     */
    @Test
    public void insertAndGetPerformance() throws Exception {
        // 1. Clear previous data (redundant but ensures clean state)
        database.clearAllTables();

        // 2. Create and insert test data
        Performance testPerformance = new Performance("Hamlet", "Auditorium 1", "14:00", 100);
        dao.insertPerformance(testPerformance);

        // 3. Verify insertion by checking count
        List<Performance> all = LiveDataTestUtil.getValue(dao.getAllPerformances());
        assertEquals(1, all.size());

        // 4. Test specific query by ID
        Performance loaded = LiveDataTestUtil.getValue(
                dao.getPerformanceById(all.get(0).getId()) // Use generated ID
        );

        assertNotNull("Performance should not be null", loaded);
        assertEquals("Hamlet", loaded.getTitle());
        assertEquals(100, loaded.getAvailableSeats());
    }
    /**
     * Helper method for detailed performance validation
     * @param original The performance that was inserted
     * @param loaded The performance loaded from database
     * @param insertedId The generated ID from insertion
     */
    private void verifyPerformanceInsertion(Performance original,
                                            Performance loaded,
                                            long insertedId) {
        // Basic null checks
        assertNotNull("Inserted performance ID should not be 0", insertedId);
        assertNotNull("Loaded performance should not be null", loaded);

        // Field-by-field comparison
        assertEquals("ID mismatch", (int)insertedId, loaded.getId());
        assertEquals("Title mismatch", original.getTitle(), loaded.getTitle());
        assertEquals("Auditorium mismatch", original.getAuditorium(), loaded.getAuditorium());
        assertEquals("Time mismatch", original.getTime(), loaded.getTime());
        assertEquals("Seats mismatch", original.getAvailableSeats(), loaded.getAvailableSeats());

        // Additional business logic validation
        assertTrue("Available seats should be positive",
                loaded.getAvailableSeats() > 0);
    }

    /**
     * Tests error case for non-existent performance
     * @throws Exception Expected to throw when no data exists
     */
    @Test(expected = IllegalStateException.class)
    public void testNonExistentPerformance() throws Exception {
        // Should throw when no data exists
        LiveDataTestUtil.getValue(dao.getPerformanceById(999));
    }
    /**
     * Tests bulk insertion and counting:
     * 1. Clears existing data
     * 2. Inserts multiple performances
     * 3. Verifies correct count
     */
    @Test
    public void testPerformanceInsertAndCount() throws Exception {
        // 1. Clear existing data
        dao.deleteAllPerformances();

        // 2. Insert sample performances with all required parameters
        Performance p1 = new Performance(
                "Hamlet",       // title
                "Auditorium 1", // auditorium
                "14:00",        // time
                100             // availableSeats
        );

        Performance p2 = new Performance(
                "Romeo & Juliet",
                "Auditorium 2",
                "19:00",
                80
        );

        // 3. Insert into database
        dao.insertPerformance(p1);
        dao.insertPerformance(p2);

        // 4. Verify count
        List<Performance> all = LiveDataTestUtil.getValue(dao.getAllPerformances());
        assertEquals("Should have exactly 2 performances", 2, all.size());
    }
    /**
     * Cleanup method run after each test case
     * Closes database to release resources
     */
    @After
    public void closeDb() {
        database.close();
    }
}

