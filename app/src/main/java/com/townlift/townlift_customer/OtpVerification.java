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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpVerification extends AppCompatActivity {

    private static final String TAG = "OtpVerification";
    private FirebaseAuth mAuth;
    private String verificationId;
    private EditText edtOtp;
    private TextView headerTitle;
    private ImageView cmdNavBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otp_verification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        verificationId = getIntent().getStringExtra("verificationId");
        edtOtp = findViewById(R.id.edt_otp);
        headerTitle = findViewById(R.id.header_title);
        headerTitle.setText("OTP Verification");

        findViewById(R.id.cmd_verify_otp).setOnClickListener(v -> {
            String otp = edtOtp.getText().toString().trim();
            if (!otp.isEmpty() && verificationId != null) {
                verifyCode(otp);
            } else {
                Toast.makeText(OtpVerification.this, "Please enter the OTP.", Toast.LENGTH_SHORT).show();
            }
        });

        cmdNavBack = findViewById(R.id.cmd_nav_back);
        cmdNavBack.setOnClickListener(v -> finish());
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Navigate to ProfileSetup activity after successful verification
                        Intent intent = new Intent(OtpVerification.this, ProfileSetup.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(OtpVerification.this, "Verification failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
