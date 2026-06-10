package com.example.crumbsofcomfort.User.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.crumbsofcomfort.Delivery.Activity.SignUpDelivery;
import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.Vendor.Activity.VendorSignUp;
import com.example.crumbsofcomfort.databinding.ActivitySignupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.textHaveAccount.setOnClickListener(view -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        });

        binding.buttonSignUp.setOnClickListener(view -> {
            if (!isValidInput()) return;

            String name = binding.editName.getText().toString().trim();
            String email = binding.editEmail.getText().toString().trim();
            String password = binding.editPassword.getText().toString();

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                                user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                    if (profileTask.isSuccessful()) {
                                        // Save user data AFTER profile update completes
                                        HashMap<String, Object> userData = new HashMap<>();
                                        userData.put("name", name);
                                        userData.put("email", email);
                                        userData.put("role", "User");

                                        FirebaseDatabase.getInstance()
                                                .getReference("Users")
                                                .child(user.getUid())
                                                .setValue(userData)
                                                .addOnCompleteListener(dbTask -> {
                                                    if (dbTask.isSuccessful()) {
                                                        Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(this, "Database Error: " + dbTask.getException(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(this, "Profile update failed: " + profileTask.getException(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(SignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
        binding.textSignupVendor.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, VendorSignUp.class);
            startActivity(intent);
        });
        binding.textSignupDelivery.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, SignUpDelivery.class);
            startActivity(intent);
        });

    }
    private boolean isValidInput() {
        String name = binding.editName.getText().toString().trim();
        String email = binding.editEmail.getText().toString().trim();
        String password = binding.editPassword.getText().toString();
        String confirmPassword = binding.editConfirmPassword.getText().toString();

        if (name.isEmpty()) {
            binding.editName.setError("Name is required");
            binding.editName.requestFocus();
            return false;
        }

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

        // Regex for 1 uppercase, 1 special character, and 1 digit
        if (!password.matches("^(?=.*[A-Z])(?=.*[!@#$%^&*()_+=-])(?=.*\\d).+$")) {
            binding.editPassword.setError("Password must include 1 uppercase letter, 1 number, and 1 special character");
            binding.editPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            binding.editConfirmPassword.setError("Passwords do not match");
            binding.editConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

}
