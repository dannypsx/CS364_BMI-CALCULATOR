package com.example.bmicalculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

    private static final String PREFS = "Settings";
    private static final String KEY_LANG = "My_Lang";
    private static final String KEY_SYSTEM_LANG = "System_Lang_At_Choice";
    private static final String STATE_BMI = "last_bmi";

    private float lastBmi = Float.NaN;
    private TextView tvBmiValue;
    private TextView tvBmiStatus;

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale locale = new Locale(resolveLang(newBase));
        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.setLocale(locale);
        super.attachBaseContext(newBase.createConfigurationContext(config));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialButtonToggleGroup toggleLanguage = findViewById(R.id.toggleLanguage);
        // The language comes from resolveLang(); a restored checked state would fire the
        // listener after recreation and re-save a stale choice, so don't let it restore.
        toggleLanguage.setSaveEnabled(false);
        findViewById(R.id.btnLangTh).setSaveEnabled(false);
        findViewById(R.id.btnLangEn).setSaveEnabled(false);
        String currentLang = resolveLang(this);
        toggleLanguage.check(currentLang.equals("th") ? R.id.btnLangTh : R.id.btnLangEn);

        toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            String target = checkedId == R.id.btnLangTh ? "th" : "en";
            if (!target.equals(resolveLang(this))) {
                setLocale(target);
            }
        });

        TextInputEditText etWeight = findViewById(R.id.etWeight);
        TextInputEditText etHeight = findViewById(R.id.etHeight);
        MaterialButton btnReset = findViewById(R.id.btnReset);
        MaterialButton btnCalculate = findViewById(R.id.btnCalculate);
        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvBmiStatus = findViewById(R.id.tvBmiStatus);
        TextView tvAdvice = findViewById(R.id.tvAdvice);

        if (savedInstanceState != null) {
            lastBmi = savedInstanceState.getFloat(STATE_BMI, Float.NaN);
            showResult();
        }

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
            lastBmi = Float.NaN;
            showResult();
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
                    // Round to the displayed precision so the number and category always agree
                    lastBmi = Math.round(weight / (height * height) * 100) / 100f;
                    showResult();
                } catch (NumberFormatException ignored) {
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(STATE_BMI, lastBmi);
    }

    // WHO classification, as used by calculator.net
    private void showResult() {
        if (Float.isNaN(lastBmi)) {
            tvBmiValue.setText(getString(R.string.default_bmi_value));
            tvBmiStatus.setText(getString(R.string.tv_bmi_status_default));
            return;
        }
        Locale locale = getResources().getConfiguration().getLocales().get(0);
        tvBmiValue.setText(String.format(locale, "%.2f", lastBmi));

        int status;
        if (lastBmi < 18.5f) {
            status = R.string.scale_underweight;
        } else if (lastBmi < 25f) {
            status = R.string.scale_normal;
        } else if (lastBmi < 30f) {
            status = R.string.scale_overweight;
        } else {
            status = R.string.scale_obese;
        }
        tvBmiStatus.setText(getString(status).replace("\n", " "));
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
            } else if (c >= '٠' && c <= '٩') {
                sb.append((char) ('0' + (c - '٠')));
            } else if (c == ',') {
                sb.append('.');
            } else {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }

    private void setLocale(String lang) {
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_LANG, lang)
                .putString(KEY_SYSTEM_LANG, systemLang())
                .apply();
        recreate();
    }

    /** Device language, unaffected by the in-app override. */
    private static String systemLang() {
        return Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
    }

    /**
     * The most recent choice wins: an in-app TH/EN choice holds until the device
     * language is changed, after which the app follows the device again.
     */
    private static String resolveLang(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String system = systemLang();
        String chosen = prefs.getString(KEY_LANG, null);
        if (chosen != null && !system.equals(prefs.getString(KEY_SYSTEM_LANG, null))) {
            prefs.edit().remove(KEY_LANG).remove(KEY_SYSTEM_LANG).apply();
            chosen = null;
        }
        String lang = chosen != null ? chosen : system;
        return "th".equalsIgnoreCase(lang) ? "th" : "en";
    }
}
