package com.example.crumbsofcomfort.User.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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
import com.example.crumbsofcomfort.Api.LocationIQClient;
import com.example.crumbsofcomfort.Api.LocationIQResponse;
import com.example.crumbsofcomfort.Api.LocationIQService;
import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.User.Helper.TinyDB;
import com.example.crumbsofcomfort.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
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
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
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
            //String address2 = snapshot.child("address2").getValue(String.class);
            String imageUrl = snapshot.child("imageUrl").getValue(String.class);

            if (name != null) {
                binding.tvName.setText(name);
                setInitials(name);
            }
            if (phone != null) binding.etPhone.setText(phone);
            if (dob != null) binding.etDob.setText(dob);
            if (address1 != null) binding.etAddress1.setText(address1);
            //if (address2 != null) binding.etAddress2.setText(address2);

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
            if (phone != null) tinyDB.putString("userPhone", phone);
            if (address1 != null) tinyDB.putString("userAddress", address1);
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
            //String address2 = binding.etAddress2.getText().toString().trim();

            if (phone.isEmpty() || address1.isEmpty()) {
                Toast.makeText(this, "Phone and Address 1 are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("phone", phone);
            updates.put("dob", dob);
            updates.put("address1", address1);
            //updates.put("address2", address2);

            userRef.updateChildren(updates).addOnSuccessListener(unused -> {
                TinyDB tinyDB = new TinyDB(this);
                tinyDB.putString("userPhone", phone);
                tinyDB.putString("userAddress", address1);
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                isEditMode = false;
                binding.editSection.setVisibility(View.GONE);

                getLatLngFromAddress(address1);
            });
        });


        binding.tvLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });
    }
    private void getLatLngFromAddress(String address) {
        LocationIQService service = LocationIQClient.getClient().create(LocationIQService.class);
        String apiKey = "pk.d9522a8c638ce1672dd6493a8d7dd380";

        retrofit2.Call<List<LocationIQResponse>> call = service.getCoordinates(apiKey, address, "json");

        call.enqueue(new retrofit2.Callback<List<LocationIQResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<List<LocationIQResponse>> call, retrofit2.Response<List<LocationIQResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    LocationIQResponse location = response.body().get(0);
                    String lat = location.lat;
                    String lon = location.lon;

                    Map<String, Object> geoUpdates = new HashMap<>();
                    geoUpdates.put("latitude", lat);
                    geoUpdates.put("longitude", lon);
                    userRef.updateChildren(geoUpdates);

                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Location updated!", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Failed to get location", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<LocationIQResponse>> call, Throwable t) {
                t.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Location fetch failed: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
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
