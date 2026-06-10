package com.example.crumbsofcomfort.Vendor.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.User.Activity.LoginActivity;
import com.example.crumbsofcomfort.User.Activity.MainActivity;
import com.example.crumbsofcomfort.databinding.ActivityVendorSignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class VendorSignUp extends AppCompatActivity {
    ActivityVendorSignUpBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVendorSignUpBinding.inflate(getLayoutInflater());
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

       /* String[] vehicleTypes = {"Two Wheeler", "Three Wheeler", "Four Wheeler"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vehicleTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerVehicleType.setAdapter(adapter);

        // Upload proof click
        binding.btnUploadProof.setOnClickListener(view -> {
            Toast.makeText(this, "Upload proof clicked", Toast.LENGTH_SHORT).show();
            // TODO: Open file picker here
        });*/

        binding.textHaveAccount.setOnClickListener(view -> {
            startActivity(new Intent(VendorSignUp.this, LoginActivity.class));
        });

        binding.buttonSignUp.setOnClickListener(view -> {
            if (!isValidInput()) return;

            String name = binding.editName.getText().toString().trim();
            String email = binding.editEmail.getText().toString().trim();
            String password = binding.editPassword.getText().toString();
            String shopName = binding.editShopName.getText().toString().trim();
            String aadhaar = binding.editAadhaar.getText().toString().trim();
            String shopReg = binding.editShopReg.getText().toString().trim();
            String phoneNum = binding.editPhoneNumber.getText().toString().trim();

           // String vehicleType = binding.spinnerVehicleType.getSelectedItem().toString();

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
                                userData.put("shopName", shopName);
                                userData.put("aadhaar", aadhaar);
                                userData.put("shopReg", shopReg);
                                userData.put("phoneNum",phoneNum);
                                userData.put("role", "Vendor");

                                FirebaseDatabase.getInstance()
                                        .getReference("VendorRequests")
                                        .child(uid)
                                        .setValue(userData)
                                        .addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                Toast.makeText(this, "Access request sent to admin. Please wait for approval.", Toast.LENGTH_LONG).show();
                                                auth.signOut(); // sign out until approval
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
        String phoneNum = binding.editPhoneNumber.getText().toString().trim();


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
        if (phoneNum.length() != 10) {
            binding.editPassword.setError("PhoneNumber is required");
            return false;
        }
        return true;
    }
}