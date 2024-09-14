package com.townlift.townlift_customer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.townlift.townlift_customer.R;
import com.townlift.townlift_customer.services.URLConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private List<JSONObject> shopList;
    private OnItemClickListener onItemClickListener;

    public ShopAdapter(List<JSONObject> shopList, OnItemClickListener onItemClickListener) {
        this.shopList = shopList;
        this.onItemClickListener = onItemClickListener;
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
            holder.deliveryCharges.setText(shop.getString("delivery_fee")+" Rs");
            holder.deliveryTime.setText(shop.getString("delivery_time")+" mins");
            holder.ratings.setText(shop.getString("average_rating"));
            String imageUrl = URLConstants.BASE_URL + shop.getString("image");

            Glide.with(holder.shopImageUrl.getContext())
                    .load(imageUrl)
                    .into(holder.shopImageUrl);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    public static class ShopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView shopImageUrl;
        TextView shopName, deliveryCharges, deliveryTime, ratings;
        OnItemClickListener onItemClickListener;

        public ShopViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            shopImageUrl = itemView.findViewById(R.id.shop_image);
            shopName = itemView.findViewById(R.id.txt_shop_name);
            deliveryCharges = itemView.findViewById(R.id.txt_delivery_charges);
            deliveryTime = itemView.findViewById(R.id.txt_delivery_time);
            ratings = itemView.findViewById(R.id.txt_ratings);
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
