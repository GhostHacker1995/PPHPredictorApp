package com.remedius.pphpredictorapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PPHPredictorApp";
    private static final String CUSTOM_BROADCAST = "com.remedius.pphpredictorapp.DATA_UPDATED";

    TextInputEditText inputAge, inputParity, inputMode, inputHb, inputPrevPPH, inputProlonged;
    MaterialButton btnPredict;
    SwitchMaterial themeSwitch;

    DatabaseHelper dbHelper; // SQLite helper

    // Custom BroadcastReceiver
    private final BroadcastReceiver dataUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CUSTOM_BROADCAST.equals(intent.getAction())) {
                Log.d(TAG, "Custom broadcast received.");
                Toast.makeText(context, "Health data updated!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Unknown broadcast: " + intent.getAction());
            }
        }
    };

    // System BroadcastReceiver for battery low
    private final BroadcastReceiver batteryLowReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
                Toast.makeText(context, "Battery is low! Please charge your device.", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Battery low warning received.");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI initialization
        inputAge = findViewById(R.id.inputAge);
        inputParity = findViewById(R.id.inputParity);
        inputMode = findViewById(R.id.inputMode);
        inputHb = findViewById(R.id.inputHb);
        inputPrevPPH = findViewById(R.id.inputPrevPPH);
        inputProlonged = findViewById(R.id.inputProlonged);
        btnPredict = findViewById(R.id.btnPredict);
        themeSwitch = findViewById(R.id.themeSwitch);

        // Initialize SQLite database helper
        dbHelper = new DatabaseHelper(this);

        // Dark mode setup
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        themeSwitch.setChecked(isDarkMode);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        // Register receivers
        try {
            unregisterReceiver(dataUpdateReceiver);
        } catch (Exception ignored) {}

        try {
            registerReceiver(dataUpdateReceiver, new IntentFilter(CUSTOM_BROADCAST));
            Log.d(TAG, "Custom broadcast receiver registered.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to register custom receiver: " + e.getMessage(), e);
        }

        try {
            registerReceiver(batteryLowReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
            Log.d(TAG, "Battery receiver registered.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to register battery receiver.", e);
        }

        // Predict button logic
        btnPredict.setOnClickListener(v -> {
            try {
                if (inputAge.getText().toString().isEmpty() ||
                        inputParity.getText().toString().isEmpty() ||
                        inputMode.getText().toString().isEmpty() ||
                        inputHb.getText().toString().isEmpty() ||
                        inputPrevPPH.getText().toString().isEmpty() ||
                        inputProlonged.getText().toString().isEmpty()) {
                    Toast.makeText(this, "⚠️ Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int age = Integer.parseInt(inputAge.getText().toString());
                int parity = Integer.parseInt(inputParity.getText().toString());
                int mode = Integer.parseInt(inputMode.getText().toString());
                float hb = Float.parseFloat(inputHb.getText().toString());
                int prevPPH = Integer.parseInt(inputPrevPPH.getText().toString());
                int prolonged = Integer.parseInt(inputProlonged.getText().toString());

                double riskScore = (age * 0.2) + (parity * 0.3) + (mode * 1.5) - (hb * 0.4)
                        + (prevPPH * 2.0) + (prolonged * 1.5);

                String result = riskScore >= 5.0 ? "High Risk of PPH" : "Low Risk of PPH";
                Toast.makeText(this, result, Toast.LENGTH_LONG).show();

                // Save to SQLite DB
                dbHelper.insertPrediction(age, parity, mode, hb, prevPPH, prolonged, result);
                Log.d(TAG, "Prediction saved to SQLite database.");

                // Send custom broadcast
                sendBroadcast(new Intent(CUSTOM_BROADCAST));
                Log.d(TAG, "Custom broadcast sent.");

            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid number format", e);
                Toast.makeText(this, "⚠ Enter valid numbers only", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error", e);
                Toast.makeText(this, "Unexpected error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(dataUpdateReceiver);
            Log.d(TAG, "Custom receiver unregistered.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to unregister custom receiver.", e);
        }

        try {
            unregisterReceiver(batteryLowReceiver);
            Log.d(TAG, "Battery receiver unregistered.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to unregister battery receiver.", e);
        }
    }
}
