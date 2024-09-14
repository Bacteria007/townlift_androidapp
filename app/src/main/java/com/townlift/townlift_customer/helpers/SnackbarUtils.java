package com.townlift.townlift_customer.helpers;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.townlift.townlift_customer.R;

public class SnackbarUtils {

    public static void showSuccessSnackbar(Activity activity, String message) {
        showSnackbar(activity, message, Color.parseColor("#4CAF50"), R.drawable.ic_success);
    }

    public static void showErrorSnackbar(Activity activity, String message) {
        showSnackbar(activity, message, Color.parseColor("#F44336"), R.drawable.ic_error);
    }

    private static void showSnackbar(Activity activity, String message, int backgroundColor, int iconRes) {
        // Create a Snackbar
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);

        // Get the Snackbar's View
        View snackbarView = snackbar.getView();

        // Set Snackbar's Position (e.g., at the top with margins)
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL; // Position at the top and center horizontally
        params.setMargins(44, 74, 44, 0); // Set margins (left, top, right, bottom)
        snackbarView.setLayoutParams(params);

        // Create a rounded background drawable
        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setColor(backgroundColor); // Set the background color
        backgroundDrawable.setCornerRadius(25); // Set the corner radius for rounded corners

        // Set the custom background to the Snackbar view
        snackbarView.setBackground(backgroundDrawable);

        // Add an Icon to the Snackbar
        TextView snackbarTextView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        snackbarTextView.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0); // Set your icon drawable
        snackbarTextView.setCompoundDrawablePadding(16); // Adjust padding between text and icon
        snackbarTextView.setTextColor(Color.WHITE);
        snackbarTextView.setTextSize(14);
        snackbarTextView.setTypeface(snackbarTextView.getTypeface(), Typeface.BOLD);
               // Show the Snackbar
        snackbar.show();
    }
}
