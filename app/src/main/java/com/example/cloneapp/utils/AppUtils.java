package com.example.cloneapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import com.example.cloneapp.models.DeviceProfile;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Utility functions for installed application discovery, image conversion,
 * and random virtual device identifier generation.
 */
public class AppUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static class AppInfo {
        private String appName;
        private String packageName;
        private Drawable icon;
        private boolean isSystemApp;

        public AppInfo(String appName, String packageName, Drawable icon, boolean isSystemApp) {
            this.appName = appName;
            this.packageName = packageName;
            this.icon = icon;
            this.isSystemApp = isSystemApp;
        }

        public String getAppName() {
            return appName;
        }

        public String getPackageName() {
            return packageName;
        }

        public Drawable getIcon() {
            return icon;
        }

        public boolean isSystemApp() {
            return isSystemApp;
        }
    }

    /**
     * Retrieve all launchable installed applications on the device.
     */
    public static List<AppInfo> getInstalledApps(Context context, boolean includeSystem) {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        String currentPackage = context.getPackageName();

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.packageName;
            if (pkgName.equals(currentPackage)) {
                continue;
            }

            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
                boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

                if (!includeSystem && isSystem) {
                    continue;
                }

                String appName = resolveInfo.loadLabel(pm).toString();
                Drawable icon = resolveInfo.loadIcon(pm);

                appList.add(new AppInfo(appName, pkgName, icon, isSystem));
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        Collections.sort(appList, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                return o1.getAppName().compareToIgnoreCase(o2.getAppName());
            }
        });

        return appList;
    }

    /**
     * Converts a Drawable into a compressed PNG Base64 string.
     */
    public static String drawableToBase64(Drawable drawable) {
        if (drawable == null) return "";
        try {
            Bitmap bitmap;
            if (drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            } else {
                int width = Math.max(1, drawable.getIntrinsicWidth());
                int height = Math.max(1, drawable.getIntrinsicHeight());
                if (width > 192) width = 192;
                if (height > 192) height = 192;
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Converts a Base64 string back into a Bitmap.
     */
    public static Bitmap base64ToBitmap(String base64Str) {
        if (base64Str == null || base64Str.trim().isEmpty()) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Converts a Base64 string into a Drawable.
     */
    public static Drawable base64ToDrawable(Context context, String base64Str) {
        Bitmap bitmap = base64ToBitmap(base64Str);
        if (bitmap != null) {
            return new BitmapDrawable(context.getResources(), bitmap);
        }
        return null;
    }

    /**
     * Generate random hex string (e.g. 16 chars for Android ID)
     */
    public static String generateRandomHex(int length) {
        StringBuilder sb = new StringBuilder();
        String chars = "0123456789abcdef";
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Generate random numeric string (e.g. 15 chars for IMEI)
     */
    public static String generateRandomNumeric(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                sb.append(RANDOM.nextInt(9) + 1);
            } else {
                sb.append(RANDOM.nextInt(10));
            }
        }
        return sb.toString();
    }

    public static String generateRandomDeviceModel() {
        String[] models = {
                "Pixel 8 Pro", "Pixel 7a", "Galaxy S24 Ultra", "Galaxy S23+",
                "Xiaomi 14 Pro", "OnePlus 12", "Redmi Note 13 Pro", "Oppo Find X7",
                "Vivo X100 Pro", "Honor Magic6 Pro", "Sony Xperia 1 V"
        };
        return models[RANDOM.nextInt(models.length)];
    }

    public static String generateRandomManufacturer() {
        String[] manufacturers = {
                "Google", "Samsung", "Xiaomi", "OnePlus",
                "Sony", "Oppo", "Vivo", "Honor", "Motorola"
        };
        return manufacturers[RANDOM.nextInt(manufacturers.length)];
    }

    public static String generateRandomWifiName() {
        String[] wifis = {
                "HomeNet_5G", "Linksys_Fast", "Starbucks_Guest", "ASUS_Gaming_AX",
                "NETGEAR_58", "Office_WiFi_High", "GoogleFiber_4G", "AndroidAP_Secure"
        };
        return wifis[RANDOM.nextInt(wifis.length)];
    }

    public static String generateRandomBluetoothName() {
        String[] btNames = {
                "Wireless-Headphones", "Galaxy Buds2 Pro", "WH-1000XM5",
                "Car_Audio_BT", "Pixel Buds Pro", "Bose QC45", "JBL Flip 6"
        };
        return btNames[RANDOM.nextInt(btNames.length)];
    }

    public static DeviceProfile createRandomProfile() {
        return new DeviceProfile(
                generateRandomHex(16),
                generateRandomNumeric(15),
                generateRandomDeviceModel(),
                generateRandomManufacturer(),
                generateRandomWifiName(),
                generateRandomBluetoothName()
        );
    }
}
