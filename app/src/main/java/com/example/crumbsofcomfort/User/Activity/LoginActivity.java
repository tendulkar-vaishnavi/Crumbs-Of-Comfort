package com.example.crumbsofcomfort.User.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.crumbsofcomfort.Admin.Activity.AdminDashboard;
import com.example.crumbsofcomfort.Delivery.Activity.DeliveryActivity;
import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.Vendor.Activity.VendorActivity;
import com.example.crumbsofcomfort.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        binding = ActivityLoginBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.textForgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        binding.textNoAccount.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        binding.buttonLogin.setOnClickListener(view -> {
            if (!isValidLoginInput()) return;

            String email = binding.editEmail.getText().toString().trim();
            String password = binding.editPassword.getText().toString();

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                fetchUserRoleAndRedirect(user.getUid());
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed:\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void fetchUserRoleAndRedirect(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("role");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.getValue(String.class);
                    Intent intent;

                    if ("Vendor".equalsIgnoreCase(role)) {
                        intent = new Intent(LoginActivity.this, VendorActivity.class);
                    }else if("Delivery Partner".equalsIgnoreCase(role)){
                        intent = new Intent(LoginActivity.this, DeliveryActivity.class);
                    }else if("admin".equalsIgnoreCase(role)){
                        intent = new Intent(LoginActivity.this, AdminDashboard.class);
                    }else {
                        intent = new Intent(LoginActivity.this, MainActivity.class);
                    }

                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Role not assigned. Please contact support.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Error fetching role: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isValidLoginInput() {
        String email = binding.editEmail.getText().toString().trim();
        String password = binding.editPassword.getText().toString();

        if (email.isEmpty()) {
            binding.editEmail.setError("Email is required");
            binding.editEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.setError("Enter a valid email");
            binding.editEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            binding.editPassword.setError("Password is required");
            binding.editPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            binding.editPassword.setError("Password must be at least 6 characters");
            binding.editPassword.requestFocus();
            return false;
        }

        return true;
    }
}
