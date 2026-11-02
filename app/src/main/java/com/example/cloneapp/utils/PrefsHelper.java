package com.example.cloneapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.cloneapp.models.DeviceProfile;
import com.google.gson.Gson;

/**
 * SharedPreferences helper managing application settings, encrypted PIN credentials,
 * and per-clone privacy and GPS configuration namespaces.
 */
public class PrefsHelper {

    private static final String PREFS_NAME = "clone_app_general_prefs";
    private static final String SECURE_PREFS_NAME = "clone_app_secure_prefs";

    private static final String KEY_PIN = "security_space_pin";
    private static final String KEY_THEME = "app_theme_mode";
    private static final String KEY_LANGUAGE = "app_language";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_IS_VIP = "is_vip_member";

    private final SharedPreferences generalPrefs;
    private SharedPreferences securePrefs;
    private final Context context;
    private final Gson gson;

    private static PrefsHelper instance;

    public static synchronized PrefsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PrefsHelper(context.getApplicationContext());
        }
        return instance;
    }

    private PrefsHelper(Context context) {
        this.context = context;
        this.gson = new Gson();
        this.generalPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Try initializing EncryptedSharedPreferences; fallback to normal prefs on failure
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            this.securePrefs = EncryptedSharedPreferences.create(
                    context,
                    SECURE_PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            this.securePrefs = context.getSharedPreferences(SECURE_PREFS_NAME + "_fallback", Context.MODE_PRIVATE);
        }
    }

    // ----------------------------------------------------
    // PIN and Privacy Space
    // ----------------------------------------------------

    public void setPin(String pin) {
        securePrefs.edit().putString(KEY_PIN, pin).apply();
    }

    public String getPin() {
        return securePrefs.getString(KEY_PIN, null);
    }

    public boolean hasPin() {
        String pin = getPin();
        return pin != null && !pin.trim().isEmpty();
    }

    public void clearPin() {
        securePrefs.edit().remove(KEY_PIN).apply();
    }

    // ----------------------------------------------------
    // VIP Membership
    // ----------------------------------------------------

    public boolean isVip() {
        return generalPrefs.getBoolean(KEY_IS_VIP, false);
    }

    public void setVip(boolean isVip) {
        generalPrefs.edit().putBoolean(KEY_IS_VIP, isVip).apply();
    }

    // ----------------------------------------------------
    // General Settings
    // ----------------------------------------------------

    public int getThemeMode() {
        // -1: Follow System, 1: Light, 2: Dark
        return generalPrefs.getInt(KEY_THEME, -1);
    }

    public void setThemeMode(int mode) {
        generalPrefs.edit().putInt(KEY_THEME, mode).apply();
    }

    public String getLanguage() {
        return generalPrefs.getString(KEY_LANGUAGE, "en");
    }

    public void setLanguage(String lang) {
        generalPrefs.edit().putString(KEY_LANGUAGE, lang).apply();
    }

    public boolean isNotificationsEnabled() {
        return generalPrefs.getBoolean(KEY_NOTIFICATIONS, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        generalPrefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    // ----------------------------------------------------
    // Per-Clone Device Privacy Namespace: clone_privacy_{cloneId}
    // ----------------------------------------------------

    public void saveClonePrivacy(String cloneId, DeviceProfile profile) {
        SharedPreferences prefs = context.getSharedPreferences("clone_privacy_" + cloneId, Context.MODE_PRIVATE);
        String json = gson.toJson(profile);
        prefs.edit().putString("device_profile_json", json).apply();
    }

    public DeviceProfile getClonePrivacy(String cloneId) {
        SharedPreferences prefs = context.getSharedPreferences("clone_privacy_" + cloneId, Context.MODE_PRIVATE);
        String json = prefs.getString("device_profile_json", null);
        if (json != null && !json.isEmpty()) {
            try {
                return gson.fromJson(json, DeviceProfile.class);
            } catch (Exception ignored) {
            }
        }
        return AppUtils.createRandomProfile();
    }

    // ----------------------------------------------------
    // Per-Clone Fake GPS Namespace: clone_gps_{cloneId}
    // ----------------------------------------------------

    public static class GpsConfig {
        public double latitude;
        public double longitude;
        public boolean enabled;

        public GpsConfig(double latitude, double longitude, boolean enabled) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.enabled = enabled;
        }
    }

    public void saveCloneGps(String cloneId, double lat, double lng, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences("clone_gps_" + cloneId, Context.MODE_PRIVATE);
        prefs.edit()
                .putString("latitude", String.valueOf(lat))
                .putString("longitude", String.valueOf(lng))
                .putBoolean("enabled", enabled)
                .apply();
    }

    public GpsConfig getCloneGps(String cloneId) {
        SharedPreferences prefs = context.getSharedPreferences("clone_gps_" + cloneId, Context.MODE_PRIVATE);
        String latStr = prefs.getString("latitude", "40.7128"); // default New York
        String lngStr = prefs.getString("longitude", "-74.0060");
        boolean enabled = prefs.getBoolean("enabled", false);

        double lat = 40.7128;
        double lng = -74.0060;
        try {
            lat = Double.parseDouble(latStr);
            lng = Double.parseDouble(lngStr);
        } catch (NumberFormatException ignored) {
        }

        return new GpsConfig(lat, lng, enabled);
    }

    // ----------------------------------------------------
    // Clear All App Data
    // ----------------------------------------------------

    public void clearAllData() {
        generalPrefs.edit().clear().apply();
        securePrefs.edit().clear().apply();
    }
}
