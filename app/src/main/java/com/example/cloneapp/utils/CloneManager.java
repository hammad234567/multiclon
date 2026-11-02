package com.example.cloneapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import com.example.cloneapp.models.CloneModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    private CloneManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_CLONES_NAME, Context.MODE_PRIVATE);
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

    /**
     * Launches a clone instance in an independent Android task stack.
     * Uses custom taskAffinity via FLAG_ACTIVITY_NEW_TASK, FLAG_ACTIVITY_MULTIPLE_TASK,
     * FLAG_ACTIVITY_NEW_DOCUMENT and a distinct clone URI so it appears as its own task in Recents.
     */
    public void launchClone(Context ctx, CloneModel clone) {
        if (clone == null || clone.getTargetPackage() == null) {
            Toast.makeText(ctx, "Invalid clone configuration", Toast.LENGTH_SHORT).show();
            return;
        }

        PackageManager pm = ctx.getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(clone.getTargetPackage());

        if (launchIntent == null) {
            Toast.makeText(ctx, "Target application " + clone.getTargetPackage() + " is not installed.", Toast.LENGTH_LONG).show();
            return;
        }

        // Configure task isolation flags:
        // FLAG_ACTIVITY_NEW_TASK: starts activity in a new task
        // FLAG_ACTIVITY_MULTIPLE_TASK: generates a new task unconditionally
        // FLAG_ACTIVITY_NEW_DOCUMENT: opens activity as a distinct document in the Recents screen
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        // Distinguish the intent so Android creates an independent task entry in Recents
        launchIntent.setData(Uri.parse("cloneapp://" + clone.getTargetPackage() + "/" + clone.getId()));
        launchIntent.setAction(Intent.ACTION_MAIN);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        // Pass clone identifier extras
        launchIntent.putExtra("com.example.cloneapp.EXTRA_CLONE_ID", clone.getId());
        launchIntent.putExtra("com.example.cloneapp.EXTRA_CUSTOM_NAME", clone.getCustomName());
        launchIntent.putExtra("com.example.cloneapp.EXTRA_AFFINITY_SUFFIX", clone.getId());

        try {
            ctx.startActivity(launchIntent);
            Toast.makeText(ctx, "Launching " + clone.getCustomName() + "...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ctx, "Failed to launch clone: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Clear all clone records.
     */
    public synchronized void clearAllClones() {
        prefs.edit().remove(KEY_CLONES_LIST).apply();
    }
}
