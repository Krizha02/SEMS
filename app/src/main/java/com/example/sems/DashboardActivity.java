package com.example.sems;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.sems.database.DatabaseHelper;
import com.example.sems.fragments.CalendarFragment;
import com.example.sems.fragments.EventsFragment;
import com.example.sems.fragments.HomeFragment;
import com.example.sems.fragments.ProfileFragment;
import com.example.sems.fragments.SettingsFragment;
import com.example.sems.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class DashboardActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private static final String TAG = "DashboardActivity";
    private BottomNavigationView bottomNavigationView;
    private NavController navController;
    private MaterialToolbar toolbar;
    private DatabaseHelper dbHelper;
    private User currentUser;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_dashboard);

            // Initialize database helper
            dbHelper = new DatabaseHelper(this);

            // Initialize views
            bottomNavigationView = findViewById(R.id.bottom_navigation);
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            // Get user email from intent
            userEmail = getIntent().getStringExtra("user_email");
            Log.d(TAG, "User email from intent: " + userEmail);

            if (userEmail == null || userEmail.isEmpty()) {
                Log.e(TAG, "No user email provided");
                Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Get current user
            currentUser = dbHelper.getUserByEmail(userEmail);
            if (currentUser == null) {
                Log.e(TAG, "User not found for email: " + userEmail);
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            Log.d(TAG, "Current user loaded: " + currentUser.getEmail());

            // Set up Navigation
            setupNavigation(savedInstanceState);

            // Set up bottom navigation listener
            bottomNavigationView.setOnItemSelectedListener(this);

        } catch (Exception e) {
            Log.e(TAG, "Error initializing dashboard: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupNavigation(Bundle savedInstanceState) {
        try {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                navController = navHostFragment.getNavController();
                NavigationUI.setupWithNavController(bottomNavigationView, navController);

                // Create bundle with user email
                Bundle args = new Bundle();
                args.putString("user_email", userEmail);

                // Set initial navigation graph with arguments
                navController.setGraph(R.navigation.dashboard_nav_graph, args);

                // Add navigation listener to pass user email to fragments
                navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                    try {
                        Log.d(TAG, "Navigation to destination: " + destination.getLabel());
                        if (arguments == null || !arguments.containsKey("user_email")) {
                            arguments = new Bundle(args);
                            controller.setGraph(R.navigation.dashboard_nav_graph, arguments);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in navigation listener: " + e.getMessage(), e);
                    }
                });
            } else {
                Log.e(TAG, "NavHostFragment is null");
                throw new RuntimeException("NavHostFragment not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.dashboard_menu, menu);
            // Only show user management for admin users
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error creating options menu: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            return NavigationUI.onNavDestinationSelected(item, navController)
                    || super.onOptionsItemSelected(item);
        } catch (Exception e) {
            Log.e(TAG, "Error handling options item selected: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        try {
            Log.d(TAG, "Navigation item selected: " + item.getTitle());
            Bundle args = new Bundle();
            args.putString("user_email", userEmail);
            navController.navigate(item.getItemId(), args);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error in navigation item selection: " + e.getMessage(), e);
            Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        try {
            return navController.navigateUp() || super.onSupportNavigateUp();
        } catch (Exception e) {
            Log.e(TAG, "Error in navigate up: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            if (dbHelper != null) {
                dbHelper.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage(), e);
        }
    }
}