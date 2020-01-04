package com.gkrath.androidotpautoread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SmsBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsBroadcastReceiver";

    private boolean isDebugMode = true;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (isDebugMode) Log.d(TAG, "onReceive: ");

        try {
            if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                if (extras != null){
                    Status mStatus = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
                    if (isDebugMode) Log.d(TAG, "mStatus: " +mStatus);

                    if (mStatus != null){
                        if (isDebugMode) Log.d(TAG, "mStatusCode: " + mStatus.getStatusCode());

                        switch (mStatus.getStatusCode()) {

                            case CommonStatusCodes.SUCCESS:
                                String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                                if (isDebugMode) Log.d(TAG, "onReceive Message: "+message);

                                if (message != null) {
                                    String otp = getOtpNumberFromString(message);
                                    if (isDebugMode) Log.d(TAG, "OTP: " +otp);
                                    Intent otpIntent = new Intent(SmsRetriever.SMS_RETRIEVED_ACTION);
                                    otpIntent.putExtra("otp", otp);
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(otpIntent);
                                }
                                break;

                            case CommonStatusCodes.TIMEOUT:
                                if (isDebugMode) Log.d(TAG, "onReceive: failure");
                                break;
                        }
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public String getOtpNumberFromString(String data) {
        Pattern pattern = Pattern.compile("(\\d{4})");
        Matcher matcher = pattern.matcher(data);
        String val = "";
        if (matcher.find()) {
            val = matcher.group(0);
            return val;
        }
        return "";
    }
}
