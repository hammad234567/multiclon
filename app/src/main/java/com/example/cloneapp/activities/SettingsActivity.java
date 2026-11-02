package com.example.cloneapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.cloneapp.R;
import com.example.cloneapp.databinding.ActivitySettingsBinding;
import com.example.cloneapp.utils.CloneManager;
import com.example.cloneapp.utils.PrefsHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Application settings managing theme mode, language preference, notification alerts,
 * PIN security updates, and storage reset.
 */
public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private PrefsHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefsHelper = PrefsHelper.getInstance(this);

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

        loadSettings();
        setupListeners();
    }

    private void loadSettings() {
        // Theme label
        int themeMode = prefsHelper.getThemeMode();
        if (themeMode == AppCompatDelegate.MODE_NIGHT_NO) {
            binding.tvCurrentTheme.setText(R.string.setting_theme_light);
        } else if (themeMode == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.tvCurrentTheme.setText(R.string.setting_theme_dark);
        } else {
            binding.tvCurrentTheme.setText(R.string.setting_theme_system);
        }

        // Language label
        String lang = prefsHelper.getLanguage();
        if ("zh".equals(lang)) {
            binding.tvCurrentLanguage.setText("简体中文");
        } else if ("es".equals(lang)) {
            binding.tvCurrentLanguage.setText("Español");
        } else {
            binding.tvCurrentLanguage.setText("English");
        }

        // Notifications switch
        binding.switchNotifications.setChecked(prefsHelper.isNotificationsEnabled());
    }

    private void setupListeners() {
        // Theme selector dialog
        binding.rowSettingTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThemeDialog();
            }
        });

        // Language selector dialog
        binding.rowSettingLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageDialog();
            }
        });

        // Notifications switch
        binding.switchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefsHelper.setNotificationsEnabled(isChecked);
                Toast.makeText(SettingsActivity.this, "Notification preferences updated", Toast.LENGTH_SHORT).show();
            }
        });

        // Change PIN
        binding.rowChangePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, PrivacySpaceAuthActivity.class);
                intent.putExtra(PrivacySpaceAuthActivity.EXTRA_CHANGE_PIN, true);
                startActivity(intent);
            }
        });

        // Clear All Data
        binding.rowClearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearDataDialog();
            }
        });
    }

    private void showThemeDialog() {
        final String[] themes = new String[]{
                getString(R.string.setting_theme_system),
                getString(R.string.setting_theme_light),
                getString(R.string.setting_theme_dark)
        };

        int currentMode = prefsHelper.getThemeMode();
        int selectedIndex = 0;
        if (currentMode == AppCompatDelegate.MODE_NIGHT_NO) selectedIndex = 1;
        else if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) selectedIndex = 2;

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.setting_theme_title)
                .setSingleChoiceItems(themes, selectedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                        if (which == 1) mode = AppCompatDelegate.MODE_NIGHT_NO;
                        else if (which == 2) mode = AppCompatDelegate.MODE_NIGHT_YES;

                        prefsHelper.setThemeMode(mode);
                        AppCompatDelegate.setDefaultNightMode(mode);
                        dialog.dismiss();
                        loadSettings();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showLanguageDialog() {
        final String[] languages = new String[]{"English", "简体中文 (Chinese)", "Español (Spanish)"};
        final String[] codes = new String[]{"en", "zh", "es"};

        String currentCode = prefsHelper.getLanguage();
        int selectedIndex = 0;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(currentCode)) {
                selectedIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.setting_language_title)
                .setSingleChoiceItems(languages, selectedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefsHelper.setLanguage(codes[which]);
                        dialog.dismiss();
                        loadSettings();
                        Toast.makeText(SettingsActivity.this, "Language set to " + languages[which], Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showClearDataDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_clear_title)
                .setMessage(R.string.dialog_clear_message)
                .setPositiveButton(R.string.dialog_delete_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CloneManager.getInstance(SettingsActivity.this).clearAllClones();
                        prefsHelper.clearAllData();
                        Toast.makeText(SettingsActivity.this, R.string.data_cleared, Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
