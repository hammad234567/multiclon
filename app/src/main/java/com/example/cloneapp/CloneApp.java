package com.example.cloneapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.configuration.AppLifecycleCallback;
import top.niunaijun.blackbox.app.configuration.ClientConfiguration;

/**
 * Application entry point. Boots the BlackBox virtualisation engine in every process
 * (host UI, cloned-app processes and the engine server process).
 */
public class CloneApp extends Application {

    private static final String TAG = "CloneApp";

    @Override
    protected void attachBaseContext(final Context base) {
        super.attachBaseContext(base);
        try {
            BlackBoxCore.get().doAttachBaseContext(base, new ClientConfiguration() {
                @Override
                public String getHostPackageName() {
                    return base.getPackageName();
                }

                @Override
                public boolean isEnableDaemonService() {
                    return false;
                }

                // Never upload logs to third-party servers.
                @Override
                public String getLogSenderChatId() {
                    return "";
                }
            });
            BlackBoxCore.get().addAppLifecycleCallback(new AppLifecycleCallback() {
            });
        } catch (Throwable t) {
            Log.e(TAG, "BlackBox attach failed", t);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            BlackBoxCore.get().doCreate();
        } catch (Throwable t) {
            Log.e(TAG, "BlackBox create failed", t);
        }
    }
}
