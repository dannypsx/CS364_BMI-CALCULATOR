package com.example.bmicalculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "Settings";
    private static final String KEY_LANG = "My_Lang";
    private static final String KEY_SYSTEM_LANG = "System_Lang_At_Choice";
    private static final String KEY_FOLLOW_SYSTEM_FONT = "Follow_System_Font";
    private static final String STATE_BMI = "last_bmi";

    private DecimalFormat formatter = new DecimalFormat("#,##0.00");

    private float lastBmi = Float.NaN;
    private TextView tvBmiValue;
    private TextView tvBmiStatus;
    private MaterialCardView cardBmiStatus;

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean followSystem = prefs.getBoolean(KEY_FOLLOW_SYSTEM_FONT, true);

        Locale locale = new Locale(resolveLang(newBase));
        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.setLocale(locale);
        if (!followSystem) {
            config.fontScale = 1.0f; // คงขนาดตัวอักษรเดิมไว้ (Checklist #10)
        }
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

        // ---------------- Runtime Font Scale (Checklist #10) ----------------
        MaterialSwitch switchFontScale = findViewById(R.id.switchFontScale);
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        switchFontScale.setChecked(prefs.getBoolean(KEY_FOLLOW_SYSTEM_FONT, true));
        switchFontScale.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_FOLLOW_SYSTEM_FONT, isChecked).apply();
            recreate();
        });

        // ---------------- Language Settings (Checklist #9) ----------------
        MaterialButtonToggleGroup toggleLanguage = findViewById(R.id.toggleLanguage);
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

        // ---------------- Input & Controls ----------------
        TextInputEditText etWeight = findViewById(R.id.etWeight);
        TextInputEditText etHeight = findViewById(R.id.etHeight);

        // Input Filter (Checklist #3 & Handout 1.1): ตัวเลขไม่เกิน 8 หลัก ทศนิยมไม่เกิน 2 หลัก
        InputFilter decimalFilter = new DecimalDigitsInputFilter(8, 2);
        etWeight.setFilters(new InputFilter[]{decimalFilter});
        etHeight.setFilters(new InputFilter[]{decimalFilter});

        MaterialButton btnReset = findViewById(R.id.btnReset);
        MaterialButton btnCalculate = findViewById(R.id.btnCalculate);
        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvBmiStatus = findViewById(R.id.tvBmiStatus);
        cardBmiStatus = findViewById(R.id.cardBmiStatus);
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
                    float height = Float.parseFloat(heightStr) / 100f;
                    if (height <= 0 || weight <= 0) return;
                    lastBmi = weight / (height * height);
                    showResult();
                } catch (NumberFormatException ignored) {
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // เมื่อขนาดตัวอักษรของระบบเปลี่ยน (Runtime Font Scale) ให้ recreate activity ถ้าเลือกปรับตามระบบ
        boolean followSystem = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_FOLLOW_SYSTEM_FONT, true);
        if (followSystem) {
            recreate();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(STATE_BMI, lastBmi);
    }

    // Checklist #4 & #5: Formatting 2 decimals and dynamic Risk Level styling
    private void showResult() {
        if (Float.isNaN(lastBmi)) {
            tvBmiValue.setText(getString(R.string.default_bmi_value));
            tvBmiStatus.setText(getString(R.string.tv_bmi_status_default));
            tvBmiStatus.setTextColor(ContextCompat.getColor(this, R.color.status_default_text));
            cardBmiStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_default_bg));
            return;
        }

        // Checklist #4: Format BMI to 2 decimal places using DecimalFormat
        tvBmiValue.setText(formatter.format(lastBmi));

        // Checklist #5: Set risk level text and colors according to WHO criteria
        int statusTextRes;
        int statusColorRes;
        int statusBgColorRes;

        if (lastBmi < 18.5f) {
            statusTextRes = R.string.status_underweight;
            statusColorRes = R.color.bmi_underweight;
            statusBgColorRes = R.color.bmi_underweight_bg;
        } else if (lastBmi < 25f) {
            statusTextRes = R.string.status_normal;
            statusColorRes = R.color.bmi_normal;
            statusBgColorRes = R.color.bmi_normal_bg;
        } else if (lastBmi < 30f) {
            statusTextRes = R.string.status_overweight;
            statusColorRes = R.color.bmi_overweight;
            statusBgColorRes = R.color.bmi_overweight_bg;
        } else {
            statusTextRes = R.string.status_obese;
            statusColorRes = R.color.bmi_obese;
            statusBgColorRes = R.color.bmi_obese_bg;
        }

        tvBmiStatus.setText(getString(statusTextRes));
        tvBmiStatus.setTextColor(ContextCompat.getColor(this, statusColorRes));
        cardBmiStatus.setCardBackgroundColor(ContextCompat.getColor(this, statusBgColorRes));
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

// ---------------- Checklist #3 & Handout 1.2 ----------------
class DecimalDigitsInputFilter implements InputFilter {
    private Pattern mPattern;

    DecimalDigitsInputFilter(int digits, int digitsAfterZero) {
        mPattern = Pattern.compile("[0-9]{0," + (digits - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        Matcher matcher = mPattern.matcher(dest);
        if (!matcher.matches())
            return "";
        return null;
    }
}
