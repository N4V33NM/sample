package com.bshu2.androidkeylogger;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

public class SmsEyeUtils {

    public static boolean isPermissionGranted(Context context) {
        return context.checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    public static String getDeviceName() {
        return Build.MANUFACTURER + " " + Build.MODEL + "\nandroid_id : " + getAndroidId();
    }

    public static String getAndroidId() {
        try {
            return Settings.Secure.getString(
                    android.content.ContextWrapper.getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
