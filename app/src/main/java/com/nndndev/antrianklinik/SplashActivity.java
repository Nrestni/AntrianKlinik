package com.nndndev.antrianklinik;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * SplashActivity — Layar pembuka aplikasi.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // terapkan tema dari prefs
        boolean isDark = getSharedPreferences("ThemePrefs", MODE_PRIVATE).getBoolean("isDark", false);
        AppCompatDelegate.setDefaultNightMode(isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);

        new Handler().postDelayed(() -> {
            // cek status login sebelum masuk aplikasi
            if (sessionManager.isLoggedIn()) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, SPLASH_DURATION);
    }
}
