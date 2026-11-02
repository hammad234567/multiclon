package com.example.cloneapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.cloneapp.models.CloneModel;
import com.example.cloneapp.utils.PrefsHelper.GpsConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.ipc.VirtualLocationManager;
import com.lody.virtual.os.VUserInfo;
import com.lody.virtual.os.VUserManager;
import com.lody.virtual.remote.InstallResult;
import com.lody.virtual.remote.InstalledAppInfo;
import com.lody.virtual.remote.vloc.VLocation;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Singleton manager handling clone lifecycle, persistence to SharedPreferences via Gson,
 * and multi-task isolated activity launching.
 */
public class CloneManager {

    private static final String PREF_CLONES_NAME = "clone_app_storage";
    private static final String KEY_CLONES_LIST = "clones_list";

    private static CloneManager instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;
    private static final String TAG = "CloneManager";
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** Result callback for engine operations (always delivered on the main thread). */
    public interface Callback {
        void onSuccess();
        void onError(String message);
    }

    private CloneManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_CLONES_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        this.gson = new Gson();
    }

    public static synchronized CloneManager getInstance(Context context) {
        if (instance == null) {
            instance = new CloneManager(context);
        }
        return instance;
    }

    /**
     * Retrieves all clones stored in SharedPreferences.
     */
    public synchronized List<CloneModel> getAllClones() {
        String json = prefs.getString(KEY_CLONES_LIST, null);
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<ArrayList<CloneModel>>() {}.getType();
        try {
            List<CloneModel> list = gson.fromJson(json, listType);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Persists the clone list to SharedPreferences.
     */
    private synchronized void saveClones(List<CloneModel> clones) {
        String json = gson.toJson(clones);
        prefs.edit().putString(KEY_CLONES_LIST, json).apply();
    }

    /**
     * Add a new clone to the database.
     */
    public synchronized void addClone(CloneModel clone) {
        List<CloneModel> clones = getAllClones();
        clones.add(clone);
        saveClones(clones);
    }

    /**
     * Remove an existing clone by its unique ID.
     */
    public synchronized void removeClone(String id) {
        List<CloneModel> clones = getAllClones();
        Iterator<CloneModel> it = clones.iterator();
        while (it.hasNext()) {
            if (it.next().getId().equals(id)) {
                it.remove();
                break;
            }
        }
        saveClones(clones);
    }

    /**
     * Update existing clone attributes.
     */
    public synchronized void updateClone(CloneModel updated) {
        List<CloneModel> clones = getAllClones();
        for (int i = 0; i < clones.size(); i++) {
            if (clones.get(i).getId().equals(updated.getId())) {
                clones.set(i, updated);
                break;
            }
        }
        saveClones(clones);
    }

    /**
     * Get clone by its unique ID.
     */
    public synchronized CloneModel getCloneById(String id) {
        List<CloneModel> clones = getAllClones();
        for (CloneModel c : clones) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Get visible clones (not in Privacy Space).
     */
    public synchronized List<CloneModel> getVisibleClones() {
        List<CloneModel> all = getAllClones();
        List<CloneModel> visible = new ArrayList<>();
        for (CloneModel c : all) {
            if (!c.isHidden()) {
                visible.add(c);
            }
        }
        return visible;
    }

    /**
     * Get hidden clones (in Privacy Space).
     */
    public synchronized List<CloneModel> getHiddenClones() {
        List<CloneModel> all = getAllClones();
        List<CloneModel> hidden = new ArrayList<>();
        for (CloneModel c : all) {
            if (c.isHidden()) {
                hidden.add(c);
            }
        }
        return hidden;
    }

    /** Find the clone for a (VirtualApp user space, package) pair; user ids are shared across apps. */
    public synchronized CloneModel getCloneByVirtualUser(int userId, String pkg) {
        for (CloneModel c : getAllClones()) {
            if (c.getVirtualUserId() == userId && c.getTargetPackage() != null
                    && c.getTargetPackage().equals(pkg)) return c;
        }
        return null;
    }

    private void ensureEngine() {
        if (!VirtualCore.get().isEngineLaunched()) {
            VirtualCore.get().waitForEngine();
        }
    }

    /**
     * Installs the target app into the VirtualApp engine in its own user space (so it has its
     * own data/accounts), then stores the clone. Heavy work runs off the UI thread.
     */
    public void addCloneAsync(final CloneModel clone, final Callback cb) {
        worker.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ensureEngine();
                    String pkg = clone.getTargetPackage();
                    InstalledAppInfo info = VirtualCore.get().getInstalledAppInfo(pkg, 0);
                    int userId;
                    if (info == null) {
                        String apk = context.getPackageManager().getApplicationInfo(pkg, 0).sourceDir;
                        InstallResult res = VirtualCore.get().installPackage(apk,
                                InstallStrategy.COMPARE_VERSION | InstallStrategy.SKIP_DEX_OPT);
                        if (!res.isSuccess) {
                            throw new IllegalStateException("Engine install failed: " + res.error);
                        }
                        userId = 0;
                    } else {
                        // pick the lowest free user slot, creating the user space if needed
                        int[] used = info.getInstalledUsers();
                        userId = used.length;
                        for (int i = 0; i < used.length; i++) {
                            if (used[i] != i) {
                                userId = i;
                                break;
                            }
                        }
                        if (VUserManager.get().getUserInfo(userId) == null) {
                            VUserInfo created = VUserManager.get().createUser("Space " + (userId + 1), VUserInfo.FLAG_ADMIN);
                            if (created == null) throw new IllegalStateException("Could not create user space");
                        }
                        if (!VirtualCore.get().installPackageAsUser(userId, pkg)) {
                            throw new IllegalStateException("Engine install for user " + userId + " failed");
                        }
                    }
                    try {
                        VirtualCore.get().preOpt(pkg);
                    } catch (Exception e) {
                        Log.w(TAG, "preOpt failed", e);
                    }
                    clone.setVirtualUserId(userId);
                    addClone(clone);
                    postSuccess(cb);
                } catch (Throwable t) {
                    Log.e(TAG, "addCloneAsync failed", t);
                    postError(cb, String.valueOf(t.getMessage()));
                }
            }
        });
    }

    /** Removes the clone record and its virtual app data from the engine. */
    public void removeCloneAsync(final CloneModel clone, final Callback cb) {
        worker.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (clone.getVirtualUserId() >= 0) {
                        ensureEngine();
                        VirtualCore.get().uninstallPackageAsUser(clone.getTargetPackage(), clone.getVirtualUserId());
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "engine uninstall failed", t);
                }
                removeClone(clone.getId());
                postSuccess(cb);
            }
        });
    }

    /** Pushes the clone's fake-GPS settings into the engine. */
    private void applyGps(CloneModel clone) {
        try {
            GpsConfig gps = PrefsHelper.getInstance(context).getCloneGps(clone.getId());
            VirtualLocationManager vlm = VirtualLocationManager.get();
            if (gps != null && gps.enabled) {
                VLocation loc = new VLocation();
                loc.latitude = gps.latitude;
                loc.longitude = gps.longitude;
                loc.accuracy = 8f;
                vlm.setMode(clone.getVirtualUserId(), clone.getTargetPackage(), VirtualLocationManager.MODE_USE_SELF);
                vlm.setLocation(clone.getVirtualUserId(), clone.getTargetPackage(), loc);
            } else {
                vlm.setMode(clone.getVirtualUserId(), clone.getTargetPackage(), VirtualLocationManager.MODE_CLOSE);
            }
        } catch (Throwable t) {
            Log.w(TAG, "applyGps failed", t);
        }
    }

    /**
     * Launches a clone inside the VirtualApp engine, in its own user space.
     */
    public void launchClone(final Context ctx, final CloneModel clone) {
        if (clone == null || clone.getTargetPackage() == null) {
            Toast.makeText(ctx, "Invalid clone configuration", Toast.LENGTH_SHORT).show();
            return;
        }
        if (clone.getVirtualUserId() < 0) {
            Toast.makeText(ctx, "This clone was created before the engine was added. Please delete and re-create it.", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(ctx, "Launching " + clone.getCustomName() + "...", Toast.LENGTH_SHORT).show();
        worker.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ensureEngine();
                    applyGps(clone);
                    Intent intent = VirtualCore.get().getLaunchIntent(clone.getTargetPackage(), clone.getVirtualUserId());
                    if (intent == null) {
                        throw new IllegalStateException("Clone is not installed in the engine");
                    }
                    VActivityManager.get().startActivity(intent, clone.getVirtualUserId());
                } catch (final Throwable t) {
                    Log.e(TAG, "launchClone failed", t);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ctx, "Failed to launch clone: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void postSuccess(final Callback cb) {
        if (cb == null) return;
        mainHandler.post(new Runnable() { @Override public void run() { cb.onSuccess(); } });
    }

    private void postError(final Callback cb, final String msg) {
        if (cb == null) return;
        mainHandler.post(new Runnable() { @Override public void run() { cb.onError(msg); } });
    }

    /**
     * Clear all clone records.
     */
    public synchronized void clearAllClones() {
        prefs.edit().remove(KEY_CLONES_LIST).apply();
    }
}
