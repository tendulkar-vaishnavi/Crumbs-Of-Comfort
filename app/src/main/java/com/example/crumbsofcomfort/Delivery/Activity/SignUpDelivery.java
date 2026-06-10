package com.example.crumbsofcomfort.Delivery.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.crumbsofcomfort.User.Activity.LoginActivity;
import com.example.crumbsofcomfort.databinding.ActivitySignUpDeliveryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpDelivery extends AppCompatActivity {
    ActivitySignUpDeliveryBinding binding;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpDeliveryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        View decor = window.getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        auth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.textHaveAccount.setOnClickListener(view -> {
            startActivity(new Intent(SignUpDelivery.this, LoginActivity.class));
        });

        binding.buttonSignUp.setOnClickListener(view -> {
            if (!isValidInput()) return;

            String name = binding.editName.getText().toString().trim();
            String email = binding.editEmail.getText().toString().trim();
            String password = binding.editPassword.getText().toString();
            String aadhaar = binding.editAadhaar.getText().toString().trim();
            String phone = binding.editPhoneNumber.getText().toString().trim();

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                user.updateProfile(profileUpdates);

                                HashMap<String, Object> userData = new HashMap<>();
                                userData.put("name", name);
                                userData.put("email", email);
                                userData.put("aadhaar", aadhaar);
                                userData.put("phone", phone);
                                userData.put("role", "Delivery Partner");

                                FirebaseDatabase.getInstance()
                                        .getReference("DeliveryRequests")
                                        .child(uid)
                                        .setValue(userData)
                                        .addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                Toast.makeText(this, "Access request sent to admin. Please wait for approval.", Toast.LENGTH_LONG).show();
                                                auth.signOut();
                                                startActivity(new Intent(this, LoginActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(this, "Error sending request: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

    }
    private boolean isValidInput() {
        String name = binding.editName.getText().toString().trim();
        String email = binding.editEmail.getText().toString().trim();
        String password = binding.editPassword.getText().toString();
        String confirmPassword = binding.editConfirmPassword.getText().toString();
        String aadhaar = binding.editAadhaar.getText().toString().trim();
        String phone = binding.editPhoneNumber.getText().toString().trim();

        if (name.isEmpty()) {
            binding.editName.setError("Name is required");
            return false;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.setError("Enter valid email");
            return false;
        }
        if (password.isEmpty()) {
            binding.editPassword.setError("Password is required");
            return false;
        }
        if (!password.matches("^(?=.*[A-Z])(?=.*[!@#$%^&*])(?=.*\\d).{6,}$")) {
            binding.editPassword.setError("Min 6 chars, 1 Uppercase, 1 special char, 1 number");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.editConfirmPassword.setError("Passwords do not match");
            return false;
        }
        if (aadhaar.length() != 12 || !aadhaar.matches("\\d{12}")) {
            binding.editAadhaar.setError("Enter valid 12-digit Aadhaar");
            return false;
        }
        if (phone.length() != 10 || !phone.matches("\\d{10}")) {
            binding.editPhoneNumber.setError("Enter valid 10-digit Number");
            return false;
        }
        return true;

    }
}