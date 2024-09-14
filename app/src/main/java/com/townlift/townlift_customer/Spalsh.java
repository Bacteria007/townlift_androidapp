package com.townlift.townlift_customer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Spalsh extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spalsh);

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find the TextView and apply the fade-in translate animation
        TextView splashTitle = findViewById(R.id.splash_title);
        Animation fadeInTranslate = AnimationUtils.loadAnimation(this, R.anim.fade_in_translate);
        splashTitle.startAnimation(fadeInTranslate);

        // Delay for 2 seconds and then decide which activity to navigate to
        new Handler().postDelayed(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

            Intent intent;
            if (isLoggedIn) {
                // User is logged in
                intent = new Intent(Spalsh.this, MainActivity.class);
            } else {
                // User is not logged in
                intent = new Intent(Spalsh.this, AuthOptions.class);
            }

            startActivity(intent);
            finish();
        }, 3000);
    }
}
