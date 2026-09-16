package com.example.bmicalculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String defaultDeviceLang = Locale.getDefault().getLanguage();
        String savedLang = prefs.getString("My_Lang", defaultDeviceLang);
        String currentLang = "th".equalsIgnoreCase(savedLang) ? "th" : "en";

        Locale locale = new Locale(currentLang);
        Locale.setDefault(locale);
        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.setLocale(locale);
        super.attachBaseContext(newBase.createConfigurationContext(config));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialButtonToggleGroup toggleLanguage = findViewById(R.id.toggleLanguage);
        String currentLang = getCurrentLang();
        toggleLanguage.check(currentLang.equals("th") ? R.id.btnLangTh : R.id.btnLangEn);

        toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            String target = checkedId == R.id.btnLangTh ? "th" : "en";
            if (!target.equals(getCurrentLang())) {
                setLocale(target);
            }
        });

        TextInputEditText etWeight = findViewById(R.id.etWeight);
        TextInputEditText etHeight = findViewById(R.id.etHeight);
        MaterialButton btnReset = findViewById(R.id.btnReset);
        MaterialButton btnCalculate = findViewById(R.id.btnCalculate);
        TextView tvBmiValue = findViewById(R.id.tvBmiValue);
        TextView tvBmiStatus = findViewById(R.id.tvBmiStatus);
        TextView tvAdvice = findViewById(R.id.tvAdvice);

        findViewById(R.id.main).setOnClickListener(v -> {
            etWeight.clearFocus();
            etHeight.clearFocus();
            hideKeyboard(v);
        });

        etHeight.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                btnCalculate.performClick();
                return true;
            }
            return false;
        });

        btnReset.setOnClickListener(v -> {
            hideKeyboard(v);
            etWeight.setText("");
            etHeight.setText("");
            tvBmiValue.setText(getString(R.string.default_bmi_value));
            tvBmiStatus.setText(getString(R.string.tv_bmi_status_default));
            tvAdvice.setText(getString(R.string.tv_advice_default));
            etWeight.clearFocus();
            etHeight.clearFocus();
        });

        btnCalculate.setOnClickListener(v -> {
            hideKeyboard(v);
            String rawWeight = etWeight.getText() != null ? etWeight.getText().toString() : "";
            String rawHeight = etHeight.getText() != null ? etHeight.getText().toString() : "";

            String weightStr = normalizeNumbers(rawWeight);
            String heightStr = normalizeNumbers(rawHeight);

            if (!weightStr.isEmpty() && !heightStr.isEmpty()) {
                try {
                    float weight = Float.parseFloat(weightStr);
                    float height = Float.parseFloat(heightStr) / 100;
                    if (height <= 0 || weight <= 0) return;
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
                } catch (NumberFormatException ignored) {
                }
            }
        });
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(view);
            if (controller != null) {
                controller.hide(WindowInsetsCompat.Type.ime());
            }
        }
    }

    private String normalizeNumbers(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= '๐' && c <= '๙') {
                sb.append((char) ('0' + (c - '๐')));
            } else if (c >= '\u0660' && c <= '\u0669') {
                sb.append((char) ('0' + (c - '\u0660')));
            } else if (c == ',') {
                sb.append('.');
            } else {
                sb.append(c);
            }
        }
        return sb.toString().trim();
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

    private String getCurrentLang() {
        SharedPreferences prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String defaultDeviceLang = Locale.getDefault().getLanguage();
        String currentLang = prefs.getString("My_Lang", defaultDeviceLang);
        return "th".equalsIgnoreCase(currentLang) ? "th" : "en";
    }

    private void loadLocale() {
        String language = getCurrentLang();
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}