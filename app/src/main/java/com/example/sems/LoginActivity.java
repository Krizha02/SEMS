package com.example.sems;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.sems.database.DatabaseHelper;
import com.example.sems.models.User;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private DatabaseHelper dbHelper;
    private View rootView;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            // Enable hardware acceleration before super.onCreate
            getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            );
            
            super.onCreate(savedInstanceState);
            
            // Initialize database off the main thread
            executor.execute(() -> {
                try {
                    dbHelper = new DatabaseHelper(getApplicationContext());
                    Log.d(TAG, "Database initialized successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Error initializing database: " + e.getMessage(), e);
                    mainHandler.post(() -> {
                        Toast.makeText(LoginActivity.this, "Error initializing database", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });

            setContentView(R.layout.activity_login);
            initializeViews();
            setupClickListeners();
            
            Log.d(TAG, "LoginActivity initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing LoginActivity: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing login screen", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            rootView = findViewById(android.R.id.content);
            rootView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            
            tilEmail = findViewById(R.id.tilEmail);
            tilPassword = findViewById(R.id.tilPassword);
            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            btnLogin = findViewById(R.id.btnLogin);

            // Disable hardware acceleration for text input layouts
            tilEmail.setLayerType(View.LAYER_TYPE_NONE, null);
            tilPassword.setLayerType(View.LAYER_TYPE_NONE, null);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            throw e;
        }
    }

    private void setupClickListeners() {
        try {
            btnLogin.setOnClickListener(v -> attemptLogin());
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    private void attemptLogin() {
        try {
            // Clear previous errors
            tilEmail.setError(null);
            tilPassword.setError(null);

            // Get input values
            final String email = etEmail.getText().toString().trim();
            final String password = etPassword.getText().toString().trim();

            // Validate input
            if (email.isEmpty()) {
                tilEmail.setError("Email is required");
                etEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                tilPassword.setError("Password is required");
                etPassword.requestFocus();
                return;
            }

            // Show loading state
            btnLogin.setEnabled(false);
            
            // Perform login check in background
            executor.execute(() -> {
                try {
                    Log.d(TAG, "Attempting login for email: " + email);
                    
                    if (dbHelper == null) {
                        throw new IllegalStateException("Database not initialized");
                    }

                    final boolean isValid = dbHelper.checkUser(email, password);
                    final User user = isValid ? dbHelper.getUserByEmail(email) : null;

                    mainHandler.post(() -> {
                        try {
                            if (isValid && user != null) {
                                Log.d(TAG, "Login successful for user: " + email);
                                
                                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                intent.putExtra("user_email", email);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.w(TAG, "Login failed for email: " + email);
                                Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                                btnLogin.setEnabled(true);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error in login UI update: " + e.getMessage(), e);
                            Toast.makeText(LoginActivity.this, "Login error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            btnLogin.setEnabled(true);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error during login attempt: " + e.getMessage(), e);
                    mainHandler.post(() -> {
                        Toast.makeText(LoginActivity.this, "Login error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnLogin.setEnabled(true);
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in attemptLogin: " + e.getMessage(), e);
            Toast.makeText(this, "Login error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnLogin.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            executor.shutdown();
            if (dbHelper != null) {
                dbHelper.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage(), e);
        }
    }
} 