package com.townlift.townlift_customer.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.townlift.townlift_customer.MainActivity;
import com.townlift.townlift_customer.R;
import com.townlift.townlift_customer.services.URLConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private List<JSONObject> shopList;
    private OnItemClickListener onItemClickListener;
    private MainActivity mainActivity; // Reference to the activity

    // Modify the constructor to accept MainActivity instance
    public ShopAdapter(List<JSONObject> shopList, OnItemClickListener onItemClickListener, MainActivity mainActivity) {
        this.shopList = shopList;
        this.onItemClickListener = onItemClickListener;
        this.mainActivity = mainActivity; // Assign activity instance
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_available_shops, parent, false);
        return new ShopViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        JSONObject shop = shopList.get(position);
        try {
            holder.shopName.setText(shop.getString("name"));
            holder.deliveryCharges.setText(shop.getString("delivery_fee") + " Rs");
            holder.deliveryTime.setText(shop.getString("delivery_time") + " mins");
            holder.ratings.setText(shop.getString("average_rating"));

            String imageUrl = URLConstants.BASE_URL + shop.getString("image");
            Glide.with(holder.shopImageUrl.getContext())
                    .load(imageUrl)
                    .into(holder.shopImageUrl);

            // Handle rating and star icon
            if (shop.getString("average_rating").equals("null")) {
                holder.ratings.setText("0.0");
                holder.ratingStar.setImageResource(R.drawable.star_disabled_24);
            } else {
                holder.ratingStar.setImageResource(R.drawable.star_enabled_24);
            }

            // Check if shop is a favorite and set the appropriate icon
            boolean isFavorite = shop.getBoolean("is_favorite");
            Log.d("ShopAdapter", "isFavorite: " + shop.toString());
            if (isFavorite) {
                holder.addToFav.setImageResource(R.drawable.heart_filled_24);
            } else {
                holder.addToFav.setImageResource(R.drawable.heart_outline_24);
            }

            // Handle Add to Favorites / Remove from Favorites action
            holder.addToFav.setOnClickListener(v -> {
                try {
                    int shopId = shop.getInt("id");  // Get shop ID
                    if (isFavorite) {
                        mainActivity.apiRemoveShopFromFavorites(shopId, () -> updateFavoriteStatus(shop, false, position));
                    } else {
                        mainActivity.apiAddShopToFavorites(shopId, () -> updateFavoriteStatus(shop, true, position));
                    }
                } catch (JSONException e) {
                    Log.e("ShopAdapter", "Error handling favorite action", e);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateFavoriteStatus(JSONObject shop, boolean isFavorite, int position) {
        try {
            shop.put("is_favorite", isFavorite);
            notifyItemChanged(position);
        } catch (JSONException e) {
            Log.e("ShopAdapter", "Error updating favorite status", e);
        }
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    public static class ShopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView shopImageUrl;
        ImageView ratingStar;
        TextView shopName, deliveryCharges, deliveryTime, ratings;
        OnItemClickListener onItemClickListener;
        ImageButton addToFav;

        public ShopViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            shopImageUrl = itemView.findViewById(R.id.shop_image);
            shopName = itemView.findViewById(R.id.txt_shop_name);
            deliveryCharges = itemView.findViewById(R.id.txt_delivery_charges);
            deliveryTime = itemView.findViewById(R.id.txt_delivery_time);
            ratings = itemView.findViewById(R.id.txt_ratings);
            ratingStar = itemView.findViewById(R.id.rating_star);
            addToFav = itemView.findViewById(R.id.cmd_add_to_fav);

            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(getAdapterPosition());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}

