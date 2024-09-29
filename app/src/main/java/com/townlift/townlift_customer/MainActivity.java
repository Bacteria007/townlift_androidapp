package com.townlift.townlift_customer;

import static com.townlift.townlift_customer.services.URLConstants.ADD_SHOP_TO_FAVORITES_URL;
import static com.townlift.townlift_customer.services.URLConstants.REMOVE_SHOP_FROM_FAVORITES_URL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.townlift.townlift_customer.adapters.ShopAdapter;
import com.townlift.townlift_customer.helpers.SnackbarUtils;
import com.townlift.townlift_customer.services.ServiceProvider;
import com.townlift.townlift_customer.services.URLConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ShopAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ShopAdapter shopAdapter;
    private List<JSONObject> shopList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView txtGreeting, txtUserName;
    private ImageView cmdLogout;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtGreeting = findViewById(R.id.txt_greeting);
        txtUserName = findViewById(R.id.txt_user_name);
//        cmdLogout = findViewById(R.id.cmd_logout);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("name", "");
        txtUserName.setText(userName);
        userId = sharedPreferences.getInt("id", -1);


//        cmdLogout.setOnClickListener(v -> {
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.clear();
//            editor.apply();
//            Intent intent = new Intent(MainActivity.this, AuthOptions.class);
//            startActivity(intent);
//            finish();
//        });
        // Update greeting based on the time of day
        updateGreetingBasedOnTime();

        ImageView imageView = findViewById(R.id.dp_image);
        String profileUrl = getProfileUrl(); // Method to get profile URL from SharedPreferences or any other source

        // Check if profileUrl is available
        if (profileUrl != null && !profileUrl.isEmpty()) {
            String imageUrl;
            if (profileUrl.startsWith("http") || profileUrl.startsWith("https")) {
                imageUrl = profileUrl; // Use the URL as is
            } else {
                imageUrl = URLConstants.BASE_URL + profileUrl; // Append base URL
            }
            // Load image from the constructed URL
            Glide.with(this)
                    .load(imageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);

        } else {
            // Load default drawable image
            Glide.with(this)
                    .load(R.drawable.dpimage) // Replace with your default drawable resource
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Fetch data when pull-to-refresh is triggered
            fetchShopsFromServer();
        });
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.available_shops);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Adapter with the shopList
        shopAdapter = new ShopAdapter(shopList, this, this);
        recyclerView.setAdapter(shopAdapter);

        fetchShopsFromServer();
    }


    private void updateGreetingBasedOnTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            txtGreeting.setText("Good Morning");
        } else if (hour >= 12 && hour < 17) {
            txtGreeting.setText("Good Afternoon");
        } else if (hour >= 17 && hour < 21) {
            txtGreeting.setText("Good Evening");
        } else {
            txtGreeting.setText("Good Night");
        }
    }

    public void apiAddShopToFavorites(int shopId, Runnable onSuccess) {
        String url = ADD_SHOP_TO_FAVORITES_URL + userId + '/' + shopId;

        // Use ServiceProvider for API call
        ServiceProvider.getInstance(this).sendPostRequest(
                this,
                url,
                null,
                data -> {
                    // On success, run the provided callback
                    onSuccess.run();
                },
                error -> {
                    // Handle error (show an error message if needed)
                    SnackbarUtils.showErrorSnackbar(this, "Failed to add shop to favorites");
                    error.printStackTrace();
                }
        );
    }

    public void apiRemoveShopFromFavorites(int shopId, Runnable onSuccess) {
        String url = REMOVE_SHOP_FROM_FAVORITES_URL + userId + '/' + shopId;

        // Use ServiceProvider for API call
        ServiceProvider.getInstance(this).sendPostRequest(
                this,
                url,
                null,
                data -> {
                    // On success, run the provided callback
                    onSuccess.run();
                },
                error -> {
                    // Handle error (show an error message if needed)
                    SnackbarUtils.showErrorSnackbar(this, "Failed to remove shop from favorites");
                    error.printStackTrace();
                }
        );
    }


    private void fetchShopsFromServer() {
        // Show refresh animation
        swipeRefreshLayout.setRefreshing(true);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userid = sharedPreferences.getInt("id", -1);
        if (userid == -1) {
            SnackbarUtils.showErrorSnackbar(this, "Invalid User ID");
            return;
        }

        String url = URLConstants.GET_ALL_SHOPS_URL + userid;

        ServiceProvider.getInstance(this).sendPostRequest(
                this,
                url,
                null, // Assuming no post data is required for fetching shops
                data -> {
                    try {
                        if (data instanceof JSONArray) {
                            JSONArray jsonArrayResponse = (JSONArray) data;
                            Log.d("MainActivity", "All Shops Response: " + jsonArrayResponse.toString());
                            if (jsonArrayResponse.length() == 0) {
                                showNoShopsAvailableAnimation();
                                swipeRefreshLayout.setRefreshing(false);  // Stop the refresh animation
                                return;
                            }
                            shopList.clear();
                            for (int i = 0; i < jsonArrayResponse.length(); i++) {
                                JSONObject shopJson = jsonArrayResponse.getJSONObject(i);
                                if (shopJson.has("id") && shopJson.has("image") && shopJson.has("name") &&
                                        shopJson.has("delivery_fee") && shopJson.has("delivery_time")) {
                                    shopList.add(shopJson);
                                    System.out.println("Shop JSON Object: " + shopJson.toString());
                                } else {
                                    System.out.println("Missing fields in shop JSON object at index: " + i);
                                }
                            }

                            shopAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);  // Stop the refresh animation

                        } else if (data instanceof JSONObject) {
                            JSONObject jsonObjectResponse = (JSONObject) data;
                            System.out.println("Server Response: " + jsonObjectResponse.toString());
                            swipeRefreshLayout.setRefreshing(false);  // Stop the refresh animation
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        System.out.println("Parsing Error: " + e.getMessage());
                        SnackbarUtils.showErrorSnackbar(this, "Parsing Error: " + e.getMessage());
                        swipeRefreshLayout.setRefreshing(false);  // Stop the refresh animation
                    }
                },
                error -> {
                    error.printStackTrace();
                    SnackbarUtils.showErrorSnackbar(this, "Error" + error.getMessage());
                    System.out.println("Request Error: " + error.getMessage());
                    swipeRefreshLayout.setRefreshing(false);  // Stop the refresh animation
                }
        );
    }

    private void showNoShopsAvailableAnimation() {
        LottieAnimationView lottieAnimationView = findViewById(R.id.lottie_no_shops);
        lottieAnimationView.setVisibility(View.VISIBLE);
        lottieAnimationView.playAnimation();

        recyclerView.setVisibility(View.GONE);

        System.out.println("No shops available, showing Lottie animation.");
    }

    @Override
    public void onItemClick(int position) {
        JSONObject selectedShop = shopList.get(position);

        try {
            Intent intent = new Intent(MainActivity.this, ShopDetails.class);
            intent.putExtra("id", selectedShop.getInt("id"));
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getProfileUrl() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("profile_url", null); // Replace "profileUrl" with your actual key
    }

}