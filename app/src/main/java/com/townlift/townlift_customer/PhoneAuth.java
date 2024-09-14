package com.townlift.townlift_customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneAuth extends AppCompatActivity {

    private static final String TAG = "PhoneAuth";
    private FirebaseAuth mAuth;
    private String verificationId;
    private EditText edtPhoneNumber;
    private TextView headerTitle;
    private ImageView cmdNavBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_phone_auth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        headerTitle = findViewById(R.id.header_title);
        headerTitle.setText("Sign In");
        edtPhoneNumber = findViewById(R.id.edt_phone);

        cmdNavBack = findViewById(R.id.cmd_nav_back);
        cmdNavBack.setOnClickListener(v -> finish());

        findViewById(R.id.cmd_send_otp).setOnClickListener(v -> {
            String phoneNumber = edtPhoneNumber.getText().toString().trim();
            if (!phoneNumber.isEmpty()) {
                sendVerificationCode(phoneNumber);
            } else {
                Toast.makeText(PhoneAuth.this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    // Auto-retrieval or instant verification
                    Log.d(TAG, "onVerificationCompleted:" + credential);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Log.w(TAG, "onVerificationFailed", e);
                    Toast.makeText(PhoneAuth.this, "Verification failed. Please try again.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                    Log.d(TAG, "onCodeSent:" + verificationId);
                    PhoneAuth.this.verificationId = verificationId;

                    // Move to OTP verification activity
                    Intent intent = new Intent(PhoneAuth.this, OtpVerification.class);
                    intent.putExtra("verificationId", verificationId);
                    startActivity(intent);
                }
            };
}
