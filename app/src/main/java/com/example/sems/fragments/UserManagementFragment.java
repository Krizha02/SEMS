package com.example.sems.fragments;

import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sems.R;
import com.example.sems.adapters.UserAdapter;
import com.example.sems.database.DatabaseHelper;
import com.example.sems.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.List;
import java.util.stream.Collectors;

public class UserManagementFragment extends Fragment implements UserAdapter.OnUserActionListener {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private FloatingActionButton fabAddUser;
    private User currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_user_management, container, false);

        // Initialize views and database
        recyclerView = root.findViewById(R.id.recyclerView);
        fabAddUser = root.findViewById(R.id.fabAddUser);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Get current user
        String userEmail = requireActivity().getIntent().getStringExtra("user_email");
        if (userEmail != null) {
            currentUser = dbHelper.getUserByEmail(userEmail);
            updateUI();
        }

        // Set up FAB click listener
        fabAddUser.setOnClickListener(v -> {
            if (currentUser != null && "admin".equals(currentUser.getRole())) {
                showAddUserDialog();
            } else {
                Toast.makeText(requireContext(), "Only administrators can add users", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private void updateUI() {
        if (currentUser != null) {
            // Show/hide FAB based on user role
            if ("admin".equals(currentUser.getRole())) {
                fabAddUser.setVisibility(View.VISIBLE);
                loadUsers();
            } else {
                fabAddUser.setVisibility(View.GONE);
                // Show message for non-admin users
                Toast.makeText(requireContext(), "Access denied. Admin privileges required.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadUsers() {
        List<User> users = dbHelper.getAllUsers();
        adapter = new UserAdapter(users, this);
        recyclerView.setAdapter(adapter);
    }

    private void showAddUserDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_edit_user, null);

        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etPassword);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        TextInputEditText etDepartment = dialogView.findViewById(R.id.etDepartment);
        TextInputEditText etPosition = dialogView.findViewById(R.id.etPosition);
        AutoCompleteTextView spinnerRole = dialogView.findViewById(R.id.spinnerRole);

        // Set up role spinner
        String[] roles = {"admin", "user"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                roles
        );
        spinnerRole.setAdapter(roleAdapter);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add New User")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String department = etDepartment.getText().toString().trim();
                String position = etPosition.getText().toString().trim();
                String role = spinnerRole.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if email already exists
                if (dbHelper.getUserByEmail(email) != null) {
                    Toast.makeText(requireContext(), "Email already exists", Toast.LENGTH_SHORT).show();
                    return;
                }

                User newUser = new User(0, email, password, name, phone, department, position, role, true);
                long result = dbHelper.addUser(newUser);

                if (result != -1) {
                    Toast.makeText(requireContext(), "User added successfully", Toast.LENGTH_SHORT).show();
                    loadUsers();
                    dialog.dismiss();
                } else {
                    Toast.makeText(requireContext(), "Failed to add user", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showEditUserDialog(User user) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_edit_user, null);

        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etPassword);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        TextInputEditText etDepartment = dialogView.findViewById(R.id.etDepartment);
        TextInputEditText etPosition = dialogView.findViewById(R.id.etPosition);
        AutoCompleteTextView spinnerRole = dialogView.findViewById(R.id.spinnerRole);

        // Pre-fill the fields
        etEmail.setText(user.getEmail());
        etPassword.setText(user.getPassword());
        etName.setText(user.getName());
        etPhone.setText(user.getPhoneNumber());
        etDepartment.setText(user.getDepartment());
        etPosition.setText(user.getPosition());
        spinnerRole.setText(user.getRole());

        // Set up role spinner
        String[] roles = {"admin", "user"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                roles
        );
        spinnerRole.setAdapter(roleAdapter);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit User")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String department = etDepartment.getText().toString().trim();
                String position = etPosition.getText().toString().trim();
                String role = spinnerRole.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if email already exists (for different user)
                User existingUser = dbHelper.getUserByEmail(email);
                if (existingUser != null && existingUser.getId() != user.getId()) {
                    Toast.makeText(requireContext(), "Email already exists", Toast.LENGTH_SHORT).show();
                    return;
                }

                user.setEmail(email);
                user.setPassword(password);
                user.setName(name);
                user.setPhoneNumber(phone);
                user.setDepartment(department);
                user.setPosition(position);
                user.setRole(role);

                int result = dbHelper.updateUser(user);

                if (result > 0) {
                    Toast.makeText(requireContext(), "User updated successfully", Toast.LENGTH_SHORT).show();
                    loadUsers();
                    dialog.dismiss();
                } else {
                    Toast.makeText(requireContext(), "Failed to update user", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showDeleteUserDialog(User user) {
        // Prevent deleting the last admin
        if ("admin".equals(user.getRole())) {
            List<User> admins = dbHelper.getAllUsers().stream()
                    .filter(u -> "admin".equals(u.getRole()))
                    .collect(Collectors.toList());
            if (admins.size() <= 1) {
                Toast.makeText(requireContext(), "Cannot delete the last admin user", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteUser(user.getId());
                    Toast.makeText(requireContext(), "User deleted successfully", Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUser != null && "admin".equals(currentUser.getRole())) {
            loadUsers();
        }
    }

    @Override
    public void onEditUser(User user) {
        showEditUserDialog(user);
    }

    @Override
    public void onDeleteUser(User user) {
        showDeleteUserDialog(user);
    }
} 