package com.townlift.townlift_customer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.concurrent.Executor;

public class AuthOptions extends AppCompatActivity {

    private View cmdGoogleSignin;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private CallbackManager callbackManager;
    private View fbLoginButton;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                            if (signInAccount != null) {
                                firebaseAuthWithGoogle(signInAccount.getIdToken());
                            }
                        } catch (ApiException e) {
                            Log.e("AuthOptions", "Google Sign-In failed", e);
                            Toast.makeText(AuthOptions.this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> facebookLoginLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    callbackManager.onActivityResult(result.getResultCode(), result.getResultCode(), result.getData());
                }
            }
    );

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_options);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set up Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        cmdGoogleSignin = findViewById(R.id.cmd_continue_google);
        cmdGoogleSignin.setOnClickListener(v -> {
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                signInLauncher.launch(signInIntent);
            });
        });

        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this.getApplication());
        callbackManager = CallbackManager.Factory.create();

        // Set up Facebook login button
        fbLoginButton = findViewById(R.id.cmd_continue_facebook);
        fbLoginButton.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(AuthOptions.this, Arrays.asList("email", "public_profile"));
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Toast.makeText(AuthOptions.this, "Facebook login canceled", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException error) {
                    Log.e("FacebookLoginError", "Error during Facebook login: ", error);
                    Toast.makeText(AuthOptions.this, "Error during Facebook login", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Set up Biometric authentication
        setupBiometricPrompt();
        findViewById(R.id.cmd_continue_biometric).setOnClickListener(v -> {
            biometricPrompt.authenticate(promptInfo);
        });

        // Phone login button
        findViewById(R.id.cmd_continue_phone).setOnClickListener(v -> {
            startActivity(new Intent(AuthOptions.this, PhoneAuth.class));
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(AuthOptions.this);
                if (account != null) {
                    Intent intent = new Intent(AuthOptions.this, ProfileSetup.class);
                    intent.putExtra("profileImageUrl", account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "");
                    intent.putExtra("userName", account.getDisplayName());
                    intent.putExtra("userEmail", account.getEmail());
                    startActivity(intent);
                }
                Toast.makeText(AuthOptions.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AuthOptions.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                Toast.makeText(AuthOptions.this, "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                navigateToHome();
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(AuthOptions.this, "Authentication failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                Toast.makeText(AuthOptions.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Use your biometric credential to login")
                .setNegativeButtonText("Cancel")
                .build();
    }

    private void navigateToHome() {
        Intent intent = new Intent(AuthOptions.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(AuthOptions.this, ProfileSetup.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(AuthOptions.this, "Facebook Authentication Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
