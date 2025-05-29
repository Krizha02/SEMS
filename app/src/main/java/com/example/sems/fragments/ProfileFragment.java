package com.example.sems.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sems.database.DatabaseHelper;
import com.example.sems.R;
import com.example.sems.models.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private DatabaseHelper dbHelper;
    private User currentUser;

    // UI Elements
    private TextView profileNameTextView;
    private TextView profileEmailTextView;
    private TextInputEditText nameInput;
    private TextInputEditText phoneNumberInput;
    private TextInputEditText departmentInput;
    private TextInputLayout positionTextInputLayout;
    private AutoCompleteTextView positionAutoCompleteTextView;
    private Button saveProfileButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());
        String userEmail = getArguments() != null ? getArguments().getString("user_email") : null;
        if (userEmail != null) {
            currentUser = dbHelper.getUserByEmail(userEmail);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        profileNameTextView = view.findViewById(R.id.profileName);
        profileEmailTextView = view.findViewById(R.id.profileEmail);
        nameInput = view.findViewById(R.id.nameInput);
        phoneNumberInput = view.findViewById(R.id.phoneNumberInput);
        departmentInput = view.findViewById(R.id.departmentInput);
        positionTextInputLayout = view.findViewById(R.id.positionTextInputLayout);
        positionAutoCompleteTextView = view.findViewById(R.id.positionAutoCompleteTextView);
        saveProfileButton = view.findViewById(R.id.saveProfileButton);

        // Set up Position dropdown
        String[] roles = {"admin", "user"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, roles);
        positionAutoCompleteTextView.setAdapter(adapter);

        // Load user data if available
        if (currentUser != null) {
            profileNameTextView.setText(currentUser.getName());
            profileEmailTextView.setText(currentUser.getEmail());
            nameInput.setText(currentUser.getName());
            phoneNumberInput.setText(currentUser.getPhoneNumber());
            departmentInput.setText(currentUser.getDepartment());
            positionAutoCompleteTextView.setText(currentUser.getRole(), false);

            // Hide position field for non-admin users
            if (!"admin".equals(currentUser.getRole())) {
                positionTextInputLayout.setVisibility(View.GONE);
                // Also prevent saving the role if the field is hidden
            }
        }

        // Set up Save button click listener
        saveProfileButton.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        if (currentUser != null) {
            // Get updated data from UI
            String name = nameInput.getText().toString().trim();
            String phoneNumber = phoneNumberInput.getText().toString().trim();
            String department = departmentInput.getText().toString().trim();

            // Update user object
            currentUser.setName(name);
            currentUser.setPhoneNumber(phoneNumber);
            currentUser.setDepartment(department);

            // Only update position/role if the user is an admin (field was enabled)
            if ("admin".equals(currentUser.getRole())) {
                String position = positionAutoCompleteTextView.getText().toString().trim();
                currentUser.setPosition(position);
                currentUser.setRole(position); // Assuming position and role are the same here
            }

            // Save to database
            int result = dbHelper.updateUser(currentUser);

            if (result > 0) {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                // Optionally refresh UI after saving
                profileNameTextView.setText(currentUser.getName());
                profileEmailTextView.setText(currentUser.getEmail());
            } else {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
} 