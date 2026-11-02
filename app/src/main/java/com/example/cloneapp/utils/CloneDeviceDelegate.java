package com.example.cloneapp.utils;

import android.content.Context;

import com.example.cloneapp.models.CloneModel;
import com.example.cloneapp.models.DeviceProfile;
import com.lody.virtual.client.hook.delegate.PhoneInfoDelegate;

/**
 * Feeds the per-clone {@link DeviceProfile} into the VirtualApp engine so each clone
 * sees its own device identifiers. Runs inside the virtual app process.
 */
public class CloneDeviceDelegate implements PhoneInfoDelegate {

    private final Context context;

    public CloneDeviceDelegate(Context context) {
        this.context = context.getApplicationContext() != null ? context.getApplicationContext() : context;
    }

    private DeviceProfile profileFor(int userId) {
        try {
            CloneModel clone = CloneManager.getInstance(context)
                    .getCloneByVirtualUser(userId, com.lody.virtual.client.VClientImpl.get().getCurrentPackage());
            if (clone == null) return null;
            android.content.SharedPreferences sp = context.getSharedPreferences(
                    "clone_privacy_" + clone.getId(), Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
            String json = sp.getString("device_profile_json", null);
            return json == null ? null : new com.google.gson.Gson().fromJson(json, DeviceProfile.class);
        } catch (Throwable t) {
            return null;
        }
    }

    private static boolean has(String s) {
        return s != null && !s.trim().isEmpty();
    }

    @Override
    public String getDeviceId(String oldDeviceId, int userId) {
        DeviceProfile p = profileFor(userId);
        return p != null && has(p.getImei()) ? p.getImei() : oldDeviceId;
    }

    @Override
    public String getBluetoothAddress(String oldBluetoothAddress, int userId) {
        return oldBluetoothAddress;
    }

    @Override
    public String getMacAddress(String oldMacAddress, int userId) {
        return oldMacAddress;
    }
}
