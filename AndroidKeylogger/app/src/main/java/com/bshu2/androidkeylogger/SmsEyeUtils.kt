package com.bshu2.androidkeylogger

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class SmsEyeUtils {
    companion object {
        fun isPermissionGranted(context: Context): Boolean {
            return context.checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        }

        fun getDeviceName(): String {
            return Build.MANUFACTURER + " " + Build.MODEL
        }
    }
}
