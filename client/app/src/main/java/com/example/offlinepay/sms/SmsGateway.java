package com.example.offlinepay.sms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import java.util.ArrayList;

public class SmsGateway {

    private static final String TAG = "SmsGateway";

    /**
     * Sends the encrypted payload via multi-part SMS to accommodate RSA length.
     */
    public static boolean sendPaymentSms(Context context, String phoneNumber, String message) {
        try {
            SmsManager smsManager;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                smsManager = context.getSystemService(SmsManager.class);
            } else {
                smsManager = SmsManager.getDefault();
            }

            ArrayList<String> parts = smsManager.divideMessage(message);
            
            // PendingIntents for delivery/sent status tracking (optional refinement)
            ArrayList<PendingIntent> sentIntents = new ArrayList<>();
            for (int i = 0; i < parts.size(); i++) {
                sentIntents.add(null);
            }

            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, null);
            Log.d(TAG, "Sent multi-part SMS to " + phoneNumber + " (" + parts.size() + " parts)");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS", e);
            return false;
        }
    }
}
