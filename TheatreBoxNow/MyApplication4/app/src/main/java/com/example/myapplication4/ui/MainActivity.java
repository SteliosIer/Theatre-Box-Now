package com.example.myapplication4.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication4.R;
import com.example.myapplication4.data.DatabaseInitializer;
import com.example.myapplication4.data.TheaterDatabase;
import com.example.myapplication4.databinding.ActivityMainBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // Initialize database with sample data when activity is created
        initializeDatabase();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    /**
     * Initializes the database by:
     * 1. Getting a database instance
     * 2. Clearing existing tables (if any)
     * 3. Populating with initial sample data
     *
     * Note: Runs on background thread to avoid blocking UI
     */
    private void initializeDatabase() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                TheaterDatabase database = TheaterDatabase.getInstance(getApplicationContext());
                database.clearAllTables();
                DatabaseInitializer.initializeAsync(database);
            } catch (Exception e) {
                Log.d("TAG", "initializeDatabase Initialization failed: ");
                Log.e("DatabaseInit", "Initialization failed", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Database init failed", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}