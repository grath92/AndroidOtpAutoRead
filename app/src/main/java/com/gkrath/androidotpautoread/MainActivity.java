package com.gkrath.androidotpautoread;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private SmsBroadcastReceiver mSmsBroadcastReceiver;


    @BindView(R.id.edt_mobile) EditText edtMobile;
    @BindView(R.id.edt_otp) OtpEditText edtOtp;
    @BindView(R.id.btn_login) Button btnLogin;
    @BindView(R.id.btn_verify_otp) Button btnVerifyOtp;

    String otpHashCode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 10);
            }
        }

        startSMSListener();
        ArrayList<String> signtures = new AppSignatureHelper(getApplicationContext()).getAppSignatures();
        if (signtures.size() > 0){
            otpHashCode = signtures.get(0);
        }
    }

    @OnClick(R.id.btn_login)
    public void onLoginClick(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 10);
            }
        }else {
            Random rand = new Random();
            String mobile = edtMobile.getText().toString().trim();

            String messageBody = "<#> Use " + rand.nextInt(9999) + " as your verification code " + otpHashCode;

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(mobile, null, messageBody, null, null);

            edtMobile.setVisibility(View.GONE);
            btnLogin.setVisibility(View.GONE);
            edtOtp.setVisibility(View.VISIBLE);
            btnVerifyOtp.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btn_verify_otp)
    public void onVerifyOtpClick(){
        Toast.makeText(getApplicationContext(), "OTP Verified", Toast.LENGTH_LONG).show();
        edtOtp.setVisibility(View.GONE);
        btnVerifyOtp.setVisibility(View.GONE);
        edtMobile.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.VISIBLE);
    }

    public void startSMSListener() {
        SmsRetrieverClient mClient = SmsRetriever.getClient(this);
        Task<Void> mTask = mClient.startSmsRetriever();
        mTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override public void onSuccess(Void aVoid) {

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
                mSmsBroadcastReceiver = new SmsBroadcastReceiver(){
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (intent.getAction() != null){
                            if (intent.getAction().equals(SmsRetriever.SMS_RETRIEVED_ACTION)) {
                                String otp = intent.getStringExtra("otp");
                                edtOtp.setText(otp);
                            }
                        }
                    }
                };

                LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(mSmsBroadcastReceiver, intentFilter);
            }
        });
        mTask.addOnFailureListener(new OnFailureListener() {
            @Override public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 10) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "SMS Permission Required", Toast.LENGTH_LONG).show();
            }
        }
    }
}
