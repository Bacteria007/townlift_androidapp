package com.townlift.townlift_customer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.airbnb.lottie.LottieAnimationView;
import com.townlift.townlift_customer.helpers.SnackbarUtils;
import com.townlift.townlift_customer.services.ServiceProvider;
import com.townlift.townlift_customer.services.URLConstants;

import org.json.JSONException;
import org.json.JSONObject;

public class login extends AppCompatActivity {
    EditText edtEmail, edtPassword, edtName;
    Button cmdLogin;
    LottieAnimationView lottieLoading;
    String url = URLConstants.LOGIN_URL;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        final View rootView = findViewById(android.R.id.content);
        final NestedScrollView scrollView = findViewById(R.id.login_scroll_view);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    scrollView.setPadding(0, 0, 0, keypadHeight);
                } else {
                    scrollView.setPadding(0, 0, 0, 0);
                }
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.txt_goto_signin).setOnClickListener(v -> {
            Intent intent = new Intent(login.this, AuthOptions.class);
            startActivity(intent);
        });

        edtEmail = findViewById(R.id.edt_login_email);
        edtPassword = findViewById(R.id.edt_login_password);
        edtName = findViewById(R.id.edt_login_name);
        cmdLogin = findViewById(R.id.cmd_login);
        lottieLoading = findViewById(R.id.lottie_loading);

        cmdLogin.setOnClickListener(v -> loginUser());

        findViewById(R.id.main).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide keyboard when tapping outside of EditText fields
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    hideKeyboard();
                    return true; // Consume the event
                }
                return false; // Pass the event to other listeners
            }
        });
    }

    private void loginUser() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            SnackbarUtils.showErrorSnackbar(login.this, "Please fill all the fields");
        } else {
            cmdLogin.setVisibility(View.INVISIBLE);
            lottieLoading.setVisibility(View.VISIBLE);
            lottieLoading.playAnimation();

            JSONObject postData = new JSONObject();
            try {
                postData.put("name", name);
                postData.put("email", email);
                postData.put("password", password);

                new Handler().postDelayed(() -> {
                    ServiceProvider.getInstance(this).sendPostRequest(this, url, postData,
                            response -> {
                                lottieLoading.cancelAnimation();
                                lottieLoading.setVisibility(View.INVISIBLE);
                                cmdLogin.setVisibility(View.VISIBLE);

                                try {
                                    // Cast the response object to JSONObject
                                    JSONObject data = (JSONObject) response;

                                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("name", data.getString("name"));
                                    editor.putString("profile_url", data.getString("profile_url"));
                                    editor.putBoolean("isLoggedIn", true);
                                    editor.putInt("id", data.getInt("id"));
                                    editor.putString("token", data.getString("token"));
                                    editor.apply();

                                    SnackbarUtils.showSuccessSnackbar(this, "Login Successful");

                                    // Navigate to MainActivity
                                    Intent intent = new Intent(this, MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    SnackbarUtils.showErrorSnackbar(this, "Failed to parse response");
                                }
                            },
                            error -> {
                                cmdLogin.setVisibility(View.VISIBLE);
                                SnackbarUtils.showErrorSnackbar(this, "Login Failed");
                            }
                    );


                }, 3000);
            } catch (JSONException e) {
                e.printStackTrace();
                lottieLoading.cancelAnimation();
                lottieLoading.setVisibility(View.GONE);
                cmdLogin.setVisibility(View.VISIBLE);
                SnackbarUtils.showErrorSnackbar(this, "Login Failed");
            }
        }
    }


    // Method to hide the keyboard
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
