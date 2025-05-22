package com.remedius.pphpredictorapp;

// Firebase for syncing prediction data to Firestore
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

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
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PPHPredictorApp";
    private static final String CUSTOM_BROADCAST = "com.remedius.pphpredictorapp.DATA_UPDATED";

    private FirebaseFirestore firestore;
    private PPHModelHelper modelHelper; // ML model helper

    TextInputEditText inputAge, inputParity, inputMode, inputHb, inputPrevPPH, inputProlonged;
    MaterialButton btnPredict, btnViewPredictions;
    SwitchMaterial themeSwitch;

    DatabaseHelper dbHelper;

    private final BroadcastReceiver dataUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CUSTOM_BROADCAST.equals(intent.getAction())) {
                Log.d(TAG, "Custom broadcast received.");
                Toast.makeText(context, "Health data updated!", Toast.LENGTH_SHORT).show();
            }
        }
    };

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

        inputAge = findViewById(R.id.inputAge);
        inputParity = findViewById(R.id.inputParity);
        inputMode = findViewById(R.id.inputMode);
        inputHb = findViewById(R.id.inputHb);
        inputPrevPPH = findViewById(R.id.inputPrevPPH);
        inputProlonged = findViewById(R.id.inputProlonged);
        btnPredict = findViewById(R.id.btnPredict);
        btnViewPredictions = findViewById(R.id.btnViewPredictions);
        themeSwitch = findViewById(R.id.themeSwitch);

        dbHelper = new DatabaseHelper(this);
        firestore = FirebaseFirestore.getInstance();
        modelHelper = new PPHModelHelper(getAssets());
        // Load the model

        // Theme settings
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

        // Broadcasts
        ContextCompat.registerReceiver(
                this,
                dataUpdateReceiver,
                new IntentFilter(CUSTOM_BROADCAST),
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
        ContextCompat.registerReceiver(
                this,
                batteryLowReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_LOW),
                ContextCompat.RECEIVER_EXPORTED
        );

        // Prediction logic with TFLite
        btnPredict.setOnClickListener(v -> {
            try {
                if (inputAge.getText().toString().isEmpty() ||
                        inputParity.getText().toString().isEmpty() ||
                        inputMode.getText().toString().isEmpty() ||
                        inputHb.getText().toString().isEmpty() ||
                        inputPrevPPH.getText().toString().isEmpty() ||
                        inputProlonged.getText().toString().isEmpty()) {
                    Toast.makeText(this, "⚠ Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int age = Integer.parseInt(inputAge.getText().toString());
                int parity = Integer.parseInt(inputParity.getText().toString());
                int mode = Integer.parseInt(inputMode.getText().toString());
                float hb = Float.parseFloat(inputHb.getText().toString());
                int prevPPH = Integer.parseInt(inputPrevPPH.getText().toString());
                int prolonged = Integer.parseInt(inputProlonged.getText().toString());

                //  Use TensorFlow Lite model for prediction
                float[] inputData = new float[]{age, parity, mode, hb, prevPPH, prolonged};
                float score = modelHelper.predict(inputData); // between 0 and 1
                String result = score >= 0.5 ? "High Risk of PPH" : "Low Risk of PPH";

                Toast.makeText(this, result + " (score: " + score + ")", Toast.LENGTH_LONG).show();

                // Save locally
                dbHelper.insertPrediction(age, parity, mode, hb, prevPPH, prolonged, result);
                Log.d(TAG, "Prediction saved locally.");

                // Save to Firestore
                Map<String, Object> prediction = new HashMap<>();
                prediction.put("age", age);
                prediction.put("parity", parity);
                prediction.put("mode", mode);
                prediction.put("haemoglobin", hb);
                prediction.put("previousPPH", prevPPH);
                prediction.put("prolongedLabor", prolonged);
                prediction.put("result", result);
                prediction.put("score", score);
                prediction.put("timestamp", System.currentTimeMillis());

                firestore.collection("pph_predictions")
                        .add(prediction)
                        .addOnSuccessListener(docRef -> {
                            Log.d(TAG, "Synced to Firebase: " + docRef.getId());
                            Toast.makeText(this, "Synced to Firebase", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Log.e(TAG, " Firebase sync failed", e));

                sendBroadcast(new Intent(CUSTOM_BROADCAST));

            } catch (NumberFormatException e) {
                Toast.makeText(this, "⚠ Enter valid numbers only", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Unexpected error occurred", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Prediction error", e);
            }
        });

        btnViewPredictions.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PredictionListActivity.class));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(dataUpdateReceiver); } catch (Exception ignored) {}
        try { unregisterReceiver(batteryLowReceiver); } catch (Exception ignored) {}
    }
}
