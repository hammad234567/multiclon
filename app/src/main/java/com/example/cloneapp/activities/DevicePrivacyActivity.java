package com.example.cloneapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cloneapp.R;
import com.example.cloneapp.databinding.ActivityDevicePrivacyBinding;
import com.example.cloneapp.models.DeviceProfile;
import com.example.cloneapp.utils.AppUtils;
import com.example.cloneapp.utils.PrefsHelper;

/**
 * Screen enabling users to configure virtual device profiles per clone.
 * Stores virtual identifiers in per-clone SharedPreferences namespace (clone_privacy_{cloneId}).
 */
public class DevicePrivacyActivity extends AppCompatActivity {

    public static final String EXTRA_CLONE_ID = "EXTRA_CLONE_ID";

    private ActivityDevicePrivacyBinding binding;
    private PrefsHelper prefsHelper;
    private String cloneId;
    private DeviceProfile currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDevicePrivacyBinding.inflate(getLayoutInflater());
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

        loadProfile();
        setupListeners();
    }

    private void loadProfile() {
        if (cloneId != null) {
            currentProfile = prefsHelper.getClonePrivacy(cloneId);
        } else {
            currentProfile = AppUtils.createRandomProfile();
        }

        binding.etAndroidId.setText(currentProfile.getAndroidId());
        binding.etImei.setText(currentProfile.getImei());
        binding.etDeviceModel.setText(currentProfile.getModel());
        binding.etManufacturer.setText(currentProfile.getManufacturer());
        binding.etWifiName.setText(currentProfile.getWifiName());
        binding.etBluetoothName.setText(currentProfile.getBluetoothName());
    }

    private void setupListeners() {
        // Randomize Android ID
        binding.btnRandAndroidId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.etAndroidId.setText(AppUtils.generateRandomHex(16));
            }
        });

        // Randomize IMEI
        binding.btnRandImei.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.etImei.setText(AppUtils.generateRandomNumeric(15));
            }
        });

        // Randomize Model
        binding.btnRandModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.etDeviceModel.setText(AppUtils.generateRandomDeviceModel());
            }
        });

        // Randomize Manufacturer
        binding.btnRandManufacturer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.etManufacturer.setText(AppUtils.generateRandomManufacturer());
            }
        });

        // Randomize Wi-Fi
        binding.btnRandWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.etWifiName.setText(AppUtils.generateRandomWifiName());
            }
        });

        // Randomize Bluetooth
        binding.btnRandBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.etBluetoothName.setText(AppUtils.generateRandomBluetoothName());
            }
        });

        // Randomize All
        binding.btnRandomizeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.etAndroidId.setText(AppUtils.generateRandomHex(16));
                binding.etImei.setText(AppUtils.generateRandomNumeric(15));
                binding.etDeviceModel.setText(AppUtils.generateRandomDeviceModel());
                binding.etManufacturer.setText(AppUtils.generateRandomManufacturer());
                binding.etWifiName.setText(AppUtils.generateRandomWifiName());
                binding.etBluetoothName.setText(AppUtils.generateRandomBluetoothName());
                Toast.makeText(DevicePrivacyActivity.this, "Generated new random device identity", Toast.LENGTH_SHORT).show();
            }
        });

        // Save
        binding.btnSavePrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cloneId == null) {
                    finish();
                    return;
                }

                String androidId = getText(binding.etAndroidId);
                String imei = getText(binding.etImei);
                String model = getText(binding.etDeviceModel);
                String manufacturer = getText(binding.etManufacturer);
                String wifi = getText(binding.etWifiName);
                String bluetooth = getText(binding.etBluetoothName);

                DeviceProfile profile = new DeviceProfile(androidId, imei, model, manufacturer, wifi, bluetooth);
                prefsHelper.saveClonePrivacy(cloneId, profile);

                Toast.makeText(DevicePrivacyActivity.this, R.string.privacy_saved, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private String getText(android.widget.EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
