package com.example.crumbsofcomfort.User.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.databinding.ActivitySplashScreenBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreenActivity extends AppCompatActivity {
    private ActivitySplashScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.scale_bounce);

        binding.logoImage.startAnimation(fadeIn);
        new Handler().postDelayed(() -> binding.sloganText.startAnimation(fadeIn), 600);

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("introPrefs", MODE_PRIVATE);
            boolean isFirstInstall = prefs.getBoolean("firstLaunchAfterInstall", true);

            if (isFirstInstall) {
                /*FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() != null) {
                    auth.signOut();
                }*/
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.putBoolean("firstLaunchAfterInstall", false);
                editor.apply();

                startActivity(new Intent(SplashScreenActivity.this, IntroActivity.class));
            } else {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            }

            finish();
        }, 5000);


    }
}