package com.example.bmicalculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialButton btnChangeLanguage = findViewById(R.id.btnChangeLanguage);
        TextInputEditText etWeight = findViewById(R.id.etWeight);
        TextInputEditText etHeight = findViewById(R.id.etHeight);
        MaterialButton btnReset = findViewById(R.id.btnReset);
        MaterialButton btnCalculate = findViewById(R.id.btnCalculate);
        TextView tvBmiValue = findViewById(R.id.tvBmiValue);
        TextView tvBmiStatus = findViewById(R.id.tvBmiStatus);
        TextView tvAdvice = findViewById(R.id.tvAdvice);

        btnChangeLanguage.setOnClickListener(v -> {
            // ดึงภาษาของเครื่องมาเป็น fallback ถ้ายังไม่เคยเซฟค่า
            String defaultDeviceLang = Locale.getDefault().getLanguage();
            String currentLang = getSharedPreferences("Settings", Context.MODE_PRIVATE)
                    .getString("My_Lang", defaultDeviceLang);

            if (currentLang.equals("th")) {
                setLocale("en");
            } else {
                setLocale("th");
            }
        });

        btnReset.setOnClickListener(v -> {
            etWeight.setText("");
            etHeight.setText("");
            tvBmiValue.setText(getString(R.string.default_bmi_value));
            tvBmiStatus.setText(getString(R.string.tv_bmi_status_default));
            tvAdvice.setText(getString(R.string.tv_advice_default));
            etWeight.clearFocus();
            etHeight.clearFocus();
        });

        btnCalculate.setOnClickListener(v -> {
            String weightStr = etWeight.getText() != null ? etWeight.getText().toString() : "";
            String heightStr = etHeight.getText() != null ? etHeight.getText().toString() : "";

            if (!weightStr.isEmpty() && !heightStr.isEmpty()) {
                float weight = Float.parseFloat(weightStr);
                float height = Float.parseFloat(heightStr) / 100;
                float bmi = weight / (height * height);

                tvBmiValue.setText(String.format(Locale.getDefault(), "%.1f", bmi));

                if (bmi < 18.5) {
                    tvBmiStatus.setText(getString(R.string.scale_underweight).replace("\n", " "));
                } else if (bmi <= 22.9) {
                    tvBmiStatus.setText(getString(R.string.scale_normal).replace("\n", " "));
                } else if (bmi <= 24.9) {
                    tvBmiStatus.setText(getString(R.string.scale_overweight).replace("\n", " "));
                } else {
                    tvBmiStatus.setText(getString(R.string.scale_obese).replace("\n", " "));
                }
            }
        });
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings", Context.MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();

        recreate();
    }

    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        // ดึงภาษาของเครื่องมาตรวจสอบก่อน
        String defaultDeviceLang = Locale.getDefault().getLanguage();
        String language = prefs.getString("My_Lang", defaultDeviceLang);

        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}