package com.example.crumbsofcomfort.Admin.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.crumbsofcomfort.Admin.Adapter.UsersAdapter;
import com.example.crumbsofcomfort.Admin.Model.UserModel;
import com.example.crumbsofcomfort.databinding.ActivityAllUsersBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AllUsersActivity extends AppCompatActivity {

    private ActivityAllUsersBinding binding;
    private UsersAdapter adapter;
    private List<UserModel> allUsersList = new ArrayList<>();
    private List<UserModel> filteredList = new ArrayList<>();
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Firebase reference
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Setup RecyclerView
        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsersAdapter(this, filteredList);
        binding.recyclerViewUsers.setAdapter(adapter);

        // Setup Spinner
        String[] roles = {"All", "User", "Vendor", "Delivery Partner"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRoleFilter.setAdapter(spinnerAdapter);

        binding.spinnerRoleFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterUsers(roles[position]);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Back button click
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        // Fetch data
        fetchAllUsers();
    }

    private void fetchAllUsers() {
        binding.textNoUsers.setVisibility(View.GONE);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsersList.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String uid = userSnap.getKey();
                    String name = userSnap.child("name").getValue(String.class);
                    String email = userSnap.child("email").getValue(String.class);
                    String role = userSnap.child("role").getValue(String.class);
                    String phone = userSnap.child("phone").getValue(String.class);

                    if (name != null && role != null) {
                        allUsersList.add(new UserModel(uid, name, email, phone, role));
                    }
                }

                // Apply current spinner filter
                String selectedRole = binding.spinnerRoleFilter.getSelectedItem().toString();
                filterUsers(selectedRole);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AllUsersActivity.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String role) {
        filteredList.clear();
        if (role.equals("All")) {
            filteredList.addAll(allUsersList);
        } else {
            for (UserModel user : allUsersList) {
                if (user.getRole().equalsIgnoreCase(role)) {
                    filteredList.add(user);
                }
            }
        }

        adapter.notifyDataSetChanged();
        binding.textNoUsers.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
