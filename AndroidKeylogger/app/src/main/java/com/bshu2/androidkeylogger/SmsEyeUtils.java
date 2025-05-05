package com.bshu2.androidkeylogger;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

public class SmsEyeUtils {

    // Modify this method to use context passed from the caller
    public static boolean isPermissionGranted(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        String permissionStatus = Settings.Secure.getString(contentResolver, "sms_default_application");
        // Your logic for permission check
        return permissionStatus != null;
    }

    // Modify this method to use context passed from the caller
    public static String getDeviceName(Context context) {
        return android.os.Build.MODEL; // Example - Modify if necessary
    }
}

