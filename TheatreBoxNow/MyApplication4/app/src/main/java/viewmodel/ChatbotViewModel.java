package viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication4.data.NLPClient;
import com.example.myapplication4.data.TheaterDatabase;
import com.example.myapplication4.models.Performance;

import java.util.ArrayList;
import java.util.List;
/**
 * ViewModel for the Chatbot functionality that:
 * - Maintains performance data
 * - Survives configuration changes
 * - Provides data to ChatbotFragment
 */
public class ChatbotViewModel extends ViewModel {
    // Database reference for data access
    private final TheaterDatabase database;
    // LiveData list of performances (observable)
    private LiveData<List<Performance>> performances;
    //private final MutableLiveData<List<Performance>> performances = new MutableLiveData<>();
    /**
     * Constructor with database dependency
     * @param database TheaterDatabase instance
     */
    private List<Performance> performancesCache = new ArrayList<>();

    public ChatbotViewModel(TheaterDatabase database) {
        this.database = database;
        // Initialize with all performances (observes changes automatically)
        this.performances = database.bookingDao().getAllPerformances();
    }

    /**
     * Triggers performance loading (currently just activates LiveData observation)
     * Could be enhanced for network/database refresh
     */
    public List<Performance> getPerformancesCache() {
        return performancesCache;
    }


    public LiveData<List<Performance>> getPerformances() {
        Log.d("TAG","performances from getPerformances: "+ database.bookingDao().getAllPerformances().toString());
        return database.bookingDao().getAllPerformances();
    }
    public void setPerformancesCache(List<Performance> performances) {
        performancesCache = performances;
    }

    public void updatePerformance(Performance performance){
        database.bookingDao().updatePerformance(performance);
    }
    /**
     * Factory for creating ViewModel instances with constructor arguments
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final TheaterDatabase database;

        public Factory(TheaterDatabase database) {
            this.database = database;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            // Create new ViewModel instance with database dependency
            return (T) new ChatbotViewModel(database);
        }
    }
}