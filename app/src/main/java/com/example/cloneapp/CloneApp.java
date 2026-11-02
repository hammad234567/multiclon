package com.example.cloneapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.cloneapp.utils.CloneDeviceDelegate;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.stub.VASettings;

/**
 * Application entry point. Boots the VirtualApp engine in every process
 * (host UI, virtual app processes and the engine server process).
 */
public class CloneApp extends Application {

    private static final String TAG = "CloneApp";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        VASettings.ENABLE_IO_REDIRECT = true;
        VASettings.ENABLE_INNER_SHORTCUT = false;
        try {
            VirtualCore.get().startup(base);
        } catch (Throwable e) {
            Log.e(TAG, "VirtualApp engine startup failed", e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final VirtualCore core = VirtualCore.get();
        core.initialize(new VirtualCore.VirtualInitializer() {
            @Override
            public void onVirtualProcess() {
                // Runs inside every cloned app's process: fake IMEI / MAC / Bluetooth per clone.
                core.setPhoneInfoDelegate(new CloneDeviceDelegate(CloneApp.this));
            }
        });
    }
}
