package com.townlift.townlift_customer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.townlift.townlift_customer.helpers.AppHelper;
import com.townlift.townlift_customer.helpers.SnackbarUtils;
import com.townlift.townlift_customer.services.MySingleton;
import com.townlift.townlift_customer.services.URLConstants;
import com.townlift.townlift_customer.services.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;


public class ProfileSetup extends AppCompatActivity {

    // Constants and Variables
    String url = URLConstants.PROFILE_CREATE_URL;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int GALLERY_PERMISSION_CODE = 101;
    private Uri selectedImageUri;
    private boolean isImageSelected = false;

    // UI Elements
    private ImageView profileImageView;
    private EditText nameEditText, emailEditText, passwordEditText;
    private TextView headerTitle;
    private ImageView cmdNavBack;
    private CheckBox agreeCheckbox;
    private Button confirmButton;
    private LottieAnimationView lottieLoading, lottieSuccess;
    String profileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_setup);
        final View rootView = findViewById(android.R.id.content);
        final NestedScrollView scrollView = findViewById(R.id.profile_scroll_view);

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
        // Initialize Views
        profileImageView = findViewById(R.id.img_profile);
        nameEditText = findViewById(R.id.edt_name);
        emailEditText = findViewById(R.id.edt_email);
        passwordEditText = findViewById(R.id.edt_password);
        headerTitle = findViewById(R.id.header_title);
        cmdNavBack = findViewById(R.id.cmd_nav_back);
        agreeCheckbox = findViewById(R.id.cmd_agree);
        confirmButton = findViewById(R.id.cmd_confirm_profile);
        lottieLoading = findViewById(R.id.lottie_loading);
        lottieSuccess = findViewById(R.id.lottie_success);
        // Set up the Profile Information
        Intent intent = getIntent();
        profileImageUrl = intent.getStringExtra("profileImageUrl");
        String userName = intent.getStringExtra("userName");
        String userEmail = intent.getStringExtra("userEmail");

        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(this).load(profileImageUrl).centerCrop().circleCrop().into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.camera_icon);
        }
        nameEditText.setText(userName);
        emailEditText.setText(userEmail);
        headerTitle.setText("Complete Profile");

        // Set up Listeners
        profileImageView.setOnClickListener(v -> showImagePickerDialog());
        cmdNavBack.setOnClickListener(v -> finish());
        confirmButton.setOnClickListener(v -> apiCallCreateProfile());
    }

    private void apiCallCreateProfile() {

//        LoadingDialog.getInstance().show(this);

        confirmButton.setVisibility(View.INVISIBLE);
        lottieLoading.setVisibility(View.VISIBLE);
        lottieLoading.playAnimation();

        Map<String, String> mapParams = new HashMap<>();
        mapParams.put("name", nameEditText.getText().toString().trim());
        mapParams.put("email", emailEditText.getText().toString().trim());
        mapParams.put("password", passwordEditText.getText().toString().trim());
        if (selectedImageUri == null) {
            System.out.println("image======>>>>>" + profileImageUrl);
            mapParams.put("image_url", profileImageUrl);
        }


        Log.w("API", "Post Params ==== " + mapParams.toString());

        String requestUrl = URLConstants.PROFILE_CREATE_URL;

        VolleyMultipartRequest stringRequest = new VolleyMultipartRequest(Request.Method.POST, requestUrl, new com.android.volley.Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {

                String resultResponse = new String(response.data);
                Log.d("createActivityNew", "" + resultResponse);

                try {
                    JSONObject responseJsonObject = new JSONObject(resultResponse);
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (responseJsonObject.getBoolean("status")) {
                        JSONObject data = responseJsonObject.getJSONObject("data");
                        editor.putString("name", data.getString("name"));
                        editor.putString("profile_url", data.getString("profile_url"));
                        editor.putBoolean("isLoggedIn", true);
                        editor.putInt("id", data.getInt("id"));
                        editor.putString("token", data.getString("token"));
                        editor.apply();
                        showSuccessDialog();
                        lottieLoading.cancelAnimation();
                        lottieLoading.setVisibility(View.INVISIBLE);
                        confirmButton.setVisibility(View.VISIBLE);

                    } else {
                        lottieLoading.cancelAnimation();
                        lottieLoading.setVisibility(View.INVISIBLE);
                        confirmButton.setVisibility(View.VISIBLE);
                        SnackbarUtils.showErrorSnackbar(ProfileSetup.this, responseJsonObject.getString("message"));
                    }

                } catch (JSONException e) {
                    lottieLoading.cancelAnimation();
                    lottieLoading.setVisibility(View.INVISIBLE);
                    confirmButton.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                    SnackbarUtils.showErrorSnackbar(ProfileSetup.this, "Error in response.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                lottieLoading.cancelAnimation();
                lottieLoading.setVisibility(View.INVISIBLE);
                confirmButton.setVisibility(View.VISIBLE);
                Log.e("createActivityNew", "ERROR ===== " + error.toString());
                error.printStackTrace();
                SnackbarUtils.showErrorSnackbar(ProfileSetup.this, "Error in response.");

            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return mapParams;
            }

            @Override
            protected Map<String, VolleyMultipartRequest.DataPart> getByteData() {
                Map<String, VolleyMultipartRequest.DataPart> params = new HashMap<>();

                if (selectedImageUri == null) {
//                    params.put("image", new VolleyMultipartRequest.DataPart(
//                            null, profileImageUrl.getBytes(), "text/plain"));
                } else {
                    params.put("image", new VolleyMultipartRequest.DataPart(
                            "file.jpg", AppHelper.getFileDataFromUri(
                            getApplicationContext(),
                            selectedImageUri
                    ), "image/jpeg"));

                }
                return params;
            }

        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                500000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(ProfileSetup.this).addToRequestQueue(stringRequest);
    }

//    private void sendUserDataToBackend() {
//        String name = nameEditText.getText().toString().trim();
//        String email = emailEditText.getText().toString().trim();
//        String password = passwordEditText.getText().toString().trim();
//        String base64Image = "";
//
//        // If the image is selected from Google Login, load and convert it
//        Intent intent = getIntent();
//        String profileImageUrl = intent.getStringExtra("profileImageUrl");
//
//        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
//            base64Image = profileImageUrl;
//        }
//
//        if (selectedImageUri != null) {
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
//                base64Image = convertBitmapToBase64(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
//            SnackbarUtils.showErrorSnackbar(this, "Please fill in all required fields.");
//        } else {
//            if (agreeCheckbox.isChecked()) {
//                JSONObject postData = new JSONObject();
//                try {
//                    postData.put("name", name);
//                    postData.put("email", email);
//                    postData.put("password", password);
//                    postData.put("image", base64Image);
//
//                    confirmButton.setVisibility(View.INVISIBLE);
//                    lottieLoading.setVisibility(View.VISIBLE);
//                    lottieLoading.playAnimation();
//
//                    new Handler().postDelayed(() -> {
//                        ServiceProvider.getInstance(this).sendPostRequest(this, url, postData,
//                                response -> {
//                                    lottieLoading.cancelAnimation();
//                                    lottieLoading.setVisibility(View.INVISIBLE);
//                                    confirmButton.setVisibility(View.VISIBLE);
//
//                                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
//                                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                                    try {
//                                        JSONObject data = (JSONObject) response;
//                                        editor.putString("name", data.getString("name"));
//                                        editor.putString("profile_url", data.getString("profile_url"));
//                                        editor.putBoolean("isLoggedIn", true);
//                                        editor.putString("id", data.getString("id"));
//                                        editor.putString("token", data.getString("token"));
//                                        editor.apply();
//
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                    showSuccessDialog();
//                                },
//                                error -> {
//                                    lottieLoading.cancelAnimation();
//                                    lottieLoading.setVisibility(View.INVISIBLE);
//                                    confirmButton.setVisibility(View.VISIBLE);
//                                    SnackbarUtils.showErrorSnackbar(this, "Error in response.");
//                                }
//                        );
//                    }, 3000);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    SnackbarUtils.showErrorSnackbar(ProfileSetup.this, "Failed to parse response.");
//                    resetButtonState(true);
//                    confirmButton.setVisibility(View.VISIBLE);
//                }
//            } else {
//                new AlertDialog.Builder(ProfileSetup.this).setTitle("Terms and Conditions")
//                        .setMessage("Please agree to the terms and conditions to proceed.")
//                        .setPositiveButton("Agree", (dialog, which) -> agreeCheckbox.setChecked(true))
//                        .setNegativeButton("Cancel", null).show();
//                resetButtonState(true);
//                confirmButton.setVisibility(View.VISIBLE);
//            }
//        }
//    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void resetButtonState(boolean showButton) {
        confirmButton.setVisibility(View.VISIBLE);
        lottieLoading.setVisibility(View.INVISIBLE);
        lottieLoading.cancelAnimation();
    }

    private void showSuccessDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.success_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSetup.this);
        builder.setView(dialogView);
        AlertDialog successDialog = builder.create();

        successDialog.show();

        new Handler().postDelayed(() -> {
            successDialog.dismiss();
            Intent mainIntent = new Intent(ProfileSetup.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }, 2000);  // Delay for 2 seconds
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CAMERA);
        isImageSelected = true;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
        isImageSelected = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                profileImageView.setImageBitmap(bitmap);
                selectedImageUri = getImageUriFromBitmap(bitmap);
            } else if (requestCode == REQUEST_GALLERY) {
                selectedImageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    profileImageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "ProfileImage", null);
        return Uri.parse(path);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                SnackbarUtils.showErrorSnackbar(this, "Camera permission is required to take a photo.");
            }
        } else if (requestCode == GALLERY_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                SnackbarUtils.showErrorSnackbar(this, "Gallery permission is required to select an image.");
            }
        }
    }

    private Bitmap getBitmapFromURL(String urlString) {
        try {
            java.net.URL url = new java.net.URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                        } else {
                            openCamera();
                        }
                    } else if (which == 1) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_CODE);
                        } else {
                            openGallery();
                        }
                    }
                })
                .show();
    }
}

//    private void showPermissionDeniedDialog(String message) {
//        new AlertDialog.Builder(this)
//                .setTitle("Permission Required")
//                .setMessage(message)
//                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
//                .show();
//    }
