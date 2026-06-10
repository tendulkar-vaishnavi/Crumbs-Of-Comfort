package com.example.crumbsofcomfort.Delivery.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.User.Activity.LoginActivity;
import com.example.crumbsofcomfort.User.Helper.TinyDB;
import com.example.crumbsofcomfort.databinding.ActivityDprofileBinding;
import com.example.crumbsofcomfort.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DProfileActivity extends AppCompatActivity {

    private ActivityDprofileBinding binding;
    private DatabaseReference userRef;
    private String uid;
    private Uri selectedImageUri;
    private boolean isEditMode = false;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Glide.with(this).load(selectedImageUri).circleCrop().into(binding.profileImage);
                    binding.profileImage.setVisibility(View.VISIBLE);
                    binding.tvInitials.setVisibility(View.GONE);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDprofileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = systemBars.bottom + 3;
            v.setLayoutParams(params);
            return insets;
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        uid = user.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        binding.tvEmail.setText(user.getEmail());

        userRef.get().addOnSuccessListener(snapshot -> {
            String name = snapshot.child("name").getValue(String.class);
            String phone = snapshot.child("phone").getValue(String.class);
            String dob = snapshot.child("dob").getValue(String.class);
            String address1 = snapshot.child("address1").getValue(String.class);
            String imageUrl = snapshot.child("imageUrl").getValue(String.class);

            if (name != null) {
                binding.tvName.setText(name);
                setInitials(name);
            }
            if (phone != null) binding.etPhone.setText(phone);
            if (dob != null) binding.etDob.setText(dob);
            if (address1 != null) binding.etAddress1.setText(address1);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).circleCrop().into(binding.profileImage);
                binding.profileImage.setVisibility(View.VISIBLE);
                binding.tvInitials.setVisibility(View.GONE);
            } else {
                binding.profileImage.setImageDrawable(null);
                binding.tvInitials.setVisibility(View.VISIBLE);
                binding.tvInitials.bringToFront();
            }
            TinyDB tinyDB = new TinyDB(this);
            if (phone != null) tinyDB.putString("deliveryPhone", phone);
            if (address1 != null) tinyDB.putString("deliveryAddress", address1);
        });

        binding.editProfileFields.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            binding.editSection.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        });

        binding.editProfileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        binding.etDob.setOnClickListener(v -> {
            if (binding.etDob.isEnabled()) {
                Calendar cal = Calendar.getInstance();
                DatePickerDialog dp = new DatePickerDialog(this, (view, y, m, d) -> {
                    String dob = d + "/" + (m + 1) + "/" + y;
                    binding.etDob.setText(dob);
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                dp.show();
            }
        });

        binding.btnSave.setOnClickListener(v -> {
            String phone = binding.etPhone.getText().toString().trim();
            String dob = binding.etDob.getText().toString().trim();
            String address1 = binding.etAddress1.getText().toString().trim();

            if (phone.isEmpty() || address1.isEmpty()) {
                Toast.makeText(this, "Phone and Address 1 are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("phone", phone);
            updates.put("dob", dob);
            updates.put("address1", address1);

            userRef.updateChildren(updates).addOnSuccessListener(unused -> {
                TinyDB tinyDB = new TinyDB(this);
                tinyDB.putString("deliveryPhone", phone);
                tinyDB.putString("deliveryAddress", address1);

                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                isEditMode = false;
                binding.editSection.setVisibility(View.GONE);
            });
        });

        binding.tvLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(DProfileActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void setInitials(String fullName) {
        String[] parts = fullName.trim().split(" ");
        String initials = parts.length >= 2 ?
                parts[0].substring(0, 1).toUpperCase() + parts[1].substring(0, 1).toUpperCase() :
                parts[0].substring(0, 1).toUpperCase();
        binding.tvInitials.setText(initials);
        binding.tvInitials.bringToFront();
    }
}
