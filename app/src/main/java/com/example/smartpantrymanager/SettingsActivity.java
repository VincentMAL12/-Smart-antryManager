package com.example.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * Screen 5: Settings.
 * Satisfies the minimum-screens requirement (Section 3.1) with a toggle for
 * expiring-soon alerts and a units preference, persisted via SharedPreferences.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "pantry_settings";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";
    private static final String KEY_UNIT_SYSTEM = "unit_system"; // "metric" or "imperial"

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        Switch expirySwitch = findViewById(R.id.switchExpiryAlerts);
        RadioGroup unitGroup = findViewById(R.id.radioGroupUnits);

        expirySwitch.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        boolean isMetric = prefs.getString(KEY_UNIT_SYSTEM, "metric").equals("metric");
        unitGroup.check(isMetric ? R.id.radioMetric : R.id.radioImperial);

        expirySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, isChecked).apply();
            Toast.makeText(this, isChecked ? "Expiry alerts enabled" : "Expiry alerts disabled",
                    Toast.LENGTH_SHORT).show();
        });

        unitGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String value = checkedId == R.id.radioMetric ? "metric" : "imperial";
            prefs.edit().putString(KEY_UNIT_SYSTEM, value).apply();
        });
    }
}
