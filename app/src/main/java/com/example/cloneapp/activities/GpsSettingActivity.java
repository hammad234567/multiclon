package com.example.cloneapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cloneapp.R;
import com.example.cloneapp.databinding.ActivityGpsSettingBinding;
import com.example.cloneapp.utils.PrefsHelper;

import java.util.Locale;

/**
 * Screen enabling users to configure simulated GPS geographical coordinates per clone.
 * Coordinates are persisted under the namespace clone_gps_{cloneId}.
 */
public class GpsSettingActivity extends AppCompatActivity {

    public static final String EXTRA_CLONE_ID = "EXTRA_CLONE_ID";

    private ActivityGpsSettingBinding binding;
    private PrefsHelper prefsHelper;
    private String cloneId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGpsSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefsHelper = PrefsHelper.getInstance(this);
        cloneId = getIntent().getStringExtra(EXTRA_CLONE_ID);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadGpsSettings();
        setupQuickLocations();
        setupSaveListener();
    }

    private void loadGpsSettings() {
        if (cloneId != null) {
            PrefsHelper.GpsConfig config = prefsHelper.getCloneGps(cloneId);
            binding.switchEnableGps.setChecked(config.enabled);
            binding.etLatitude.setText(String.format(Locale.US, "%.4f", config.latitude));
            binding.etLongitude.setText(String.format(Locale.US, "%.4f", config.longitude));
            updateMapPreview(config.latitude, config.longitude);
        }
    }

    private void updateMapPreview(double lat, double lng) {
        binding.tvMapCoordsPreview.setText(String.format(Locale.US, "%.4f, %.4f", lat, lng));
    }

    private void setupQuickLocations() {
        binding.chipNy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocation(40.7128, -74.0060);
            }
        });

        binding.chipLondon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocation(51.5074, -0.1278);
            }
        });

        binding.chipTokyo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocation(35.6762, 139.6503);
            }
        });

        binding.chipDubai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocation(25.2048, 55.2708);
            }
        });

        binding.chipParis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocation(48.8566, 2.3522);
            }
        });

        binding.chipSydney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocation(-33.8688, 151.2093);
            }
        });
    }

    private void setLocation(double lat, double lng) {
        binding.etLatitude.setText(String.format(Locale.US, "%.4f", lat));
        binding.etLongitude.setText(String.format(Locale.US, "%.4f", lng));
        updateMapPreview(lat, lng);
        Toast.makeText(this, "Coordinates updated to preset", Toast.LENGTH_SHORT).show();
    }

    private void setupSaveListener() {
        binding.btnSaveGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cloneId == null) {
                    finish();
                    return;
                }

                String latStr = binding.etLatitude.getText() != null ? binding.etLatitude.getText().toString().trim() : "";
                String lngStr = binding.etLongitude.getText() != null ? binding.etLongitude.getText().toString().trim() : "";

                double lat = 0.0;
                double lng = 0.0;
                try {
                    lat = Double.parseDouble(latStr);
                    lng = Double.parseDouble(lngStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(GpsSettingActivity.this, "Please enter valid decimal coordinates", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean enabled = binding.switchEnableGps.isChecked();
                prefsHelper.saveCloneGps(cloneId, lat, lng, enabled);
                Toast.makeText(GpsSettingActivity.this, R.string.gps_saved, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
