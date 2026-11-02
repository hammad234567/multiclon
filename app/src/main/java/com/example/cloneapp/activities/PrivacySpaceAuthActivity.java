package com.example.cloneapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cloneapp.R;
import com.example.cloneapp.databinding.ActivityPrivacySpaceAuthBinding;
import com.example.cloneapp.utils.PrefsHelper;

/**
 * PIN-protected authentication gateway for Privacy Space.
 * Supports PIN creation, confirmation, and unlocking with a custom numeric keypad.
 */
public class PrivacySpaceAuthActivity extends AppCompatActivity {

    public static final String EXTRA_CHANGE_PIN = "EXTRA_CHANGE_PIN";

    private ActivityPrivacySpaceAuthBinding binding;
    private PrefsHelper prefsHelper;
    private final StringBuilder currentInput = new StringBuilder();

    private boolean isSettingPin = false;
    private boolean isConfirmingPin = false;
    private String tempNewPin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacySpaceAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefsHelper = PrefsHelper.getInstance(this);

        boolean forceChange = getIntent().getBooleanExtra(EXTRA_CHANGE_PIN, false);
        if (forceChange || !prefsHelper.hasPin()) {
            isSettingPin = true;
            isConfirmingPin = false;
            binding.tvAuthTitle.setText(R.string.pin_prompt_create);
            binding.tvAuthSubtitle.setText("Create a new 4-digit PIN to secure hidden clones");
        } else {
            isSettingPin = false;
            binding.tvAuthTitle.setText(R.string.pin_prompt_enter);
            binding.tvAuthSubtitle.setText("Enter your secret PIN to access the privacy vault");
        }

        binding.btnAuthBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupKeypad();
    }

    private void setupKeypad() {
        View.OnClickListener numListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentInput.length() < 4) {
                    com.google.android.material.button.MaterialButton btn = (com.google.android.material.button.MaterialButton) v;
                    currentInput.append(btn.getText().toString());
                    updatePinDots();

                    if (currentInput.length() == 4) {
                        handlePinComplete(currentInput.toString());
                    }
                }
            }
        };

        binding.btnKey0.setOnClickListener(numListener);
        binding.btnKey1.setOnClickListener(numListener);
        binding.btnKey2.setOnClickListener(numListener);
        binding.btnKey3.setOnClickListener(numListener);
        binding.btnKey4.setOnClickListener(numListener);
        binding.btnKey5.setOnClickListener(numListener);
        binding.btnKey6.setOnClickListener(numListener);
        binding.btnKey7.setOnClickListener(numListener);
        binding.btnKey8.setOnClickListener(numListener);
        binding.btnKey9.setOnClickListener(numListener);

        binding.btnKeyClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentInput.setLength(0);
                updatePinDots();
            }
        });

        binding.btnKeyBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentInput.length() > 0) {
                    currentInput.deleteCharAt(currentInput.length() - 1);
                    updatePinDots();
                }
            }
        });
    }

    private void updatePinDots() {
        int len = currentInput.length();
        binding.dot1.setBackgroundResource(len >= 1 ? R.drawable.bg_pin_dot_filled : R.drawable.bg_pin_dot);
        binding.dot2.setBackgroundResource(len >= 2 ? R.drawable.bg_pin_dot_filled : R.drawable.bg_pin_dot);
        binding.dot3.setBackgroundResource(len >= 3 ? R.drawable.bg_pin_dot_filled : R.drawable.bg_pin_dot);
        binding.dot4.setBackgroundResource(len >= 4 ? R.drawable.bg_pin_dot_filled : R.drawable.bg_pin_dot);
    }

    private void handlePinComplete(String enteredPin) {
        if (isSettingPin) {
            if (!isConfirmingPin) {
                // First step of setting PIN: store temp and prompt to confirm
                tempNewPin = enteredPin;
                isConfirmingPin = true;
                currentInput.setLength(0);
                updatePinDots();
                binding.tvAuthTitle.setText(R.string.pin_prompt_confirm);
                binding.tvAuthSubtitle.setText("Re-enter the 4 digits to confirm");
            } else {
                // Second step: check if matches
                if (enteredPin.equals(tempNewPin)) {
                    prefsHelper.setPin(enteredPin);
                    Toast.makeText(this, R.string.pin_set_success, Toast.LENGTH_SHORT).show();
                    openPrivacySpace();
                } else {
                    vibrateError();
                    Toast.makeText(this, R.string.pin_mismatch, Toast.LENGTH_SHORT).show();
                    // Reset to first step
                    isConfirmingPin = false;
                    currentInput.setLength(0);
                    updatePinDots();
                    binding.tvAuthTitle.setText(R.string.pin_prompt_create);
                    binding.tvAuthSubtitle.setText("PINs did not match. Please enter new 4-digit PIN");
                }
            }
        } else {
            // Normal authentication
            String savedPin = prefsHelper.getPin();
            if (savedPin != null && savedPin.equals(enteredPin)) {
                openPrivacySpace();
            } else {
                vibrateError();
                Toast.makeText(this, R.string.pin_incorrect, Toast.LENGTH_SHORT).show();
                currentInput.setLength(0);
                updatePinDots();
            }
        }
    }

    private void openPrivacySpace() {
        Intent intent = new Intent(this, PrivacySpaceActivity.class);
        startActivity(intent);
        finish();
    }

    private void vibrateError() {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                v.vibrate(200);
            }
        } catch (Exception ignored) {
        }
    }
}
