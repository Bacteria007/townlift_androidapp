package com.townlift.townlift_customer.services;

import android.app.Activity;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.townlift.townlift_customer.helpers.SnackbarUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class ServiceProvider {
    private static ServiceProvider instance;
    private RequestQueue requestQueue;
    private Context ctx;

    private ServiceProvider(Context context) {
        this.ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized ServiceProvider getInstance(Context context) {
        if (instance == null) {
            instance = new ServiceProvider(context.getApplicationContext());
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public void addToRequestQueue(Request<?> request) {
        getRequestQueue().add(request);
    }

    public void sendPostRequest(Activity activity, String url, JSONObject postData,
                                Response.Listener<Object> responseListener,
                                Response.ErrorListener errorListener) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    try {
                        boolean status = response.getBoolean("status");
                        if (status) {
                            if (response.has("data")) {
                                Object data = response.get("data");
                                responseListener.onResponse(data);
                            } else {
                                throw new Exception("Response does not contain 'data' field.");
                            }
                        } else {
                            // Handle error if status is false
                            String errorMessage = response.optString("message", "An error occurred.");
                            SnackbarUtils.showErrorSnackbar(activity, errorMessage);
                            errorListener.onErrorResponse(new VolleyError(errorMessage));
                        }
                    } catch (Exception e) {
                        // Handle parsing errors
                        e.printStackTrace();
                        String errorMessage = "Parsing error occurred: " + e.getMessage();
                        SnackbarUtils.showErrorSnackbar(activity, errorMessage);
                        errorListener.onErrorResponse(new VolleyError(errorMessage));
                    }
                },
                error -> {
                    // Handle VolleyError
                    error.printStackTrace();
                    String errorMessage = "Network error occurred: " + error.getMessage();
                    SnackbarUtils.showErrorSnackbar(activity, errorMessage);
                    errorListener.onErrorResponse(new VolleyError(errorMessage));
                }
        );
        addToRequestQueue(jsonObjectRequest);
    }
}