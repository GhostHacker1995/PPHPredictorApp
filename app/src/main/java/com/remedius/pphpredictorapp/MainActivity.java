package com.remedius.pphpredictorapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    TextInputEditText inputAge, inputParity, inputMode, inputHb, inputPrevPPH, inputProlonged;
    MaterialButton btnPredict;
    SwitchMaterial themeSwitch;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Dark mode toggle setup
        themeSwitch = findViewById(R.id.themeSwitch);
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

        // Input fields and button
        inputAge = findViewById(R.id.inputAge);
        inputParity = findViewById(R.id.inputParity);
        inputMode = findViewById(R.id.inputMode);
        inputHb = findViewById(R.id.inputHb);
        inputPrevPPH = findViewById(R.id.inputPrevPPH);
        inputProlonged = findViewById(R.id.inputProlonged);
        btnPredict = findViewById(R.id.btnPredict);

        btnPredict.setOnClickListener(v -> {

            try {

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

            } catch (Exception e)
            {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
            }

        });
    }
}


