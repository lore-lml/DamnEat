package com.damn.polito.damneat.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RatingBar;
import android.widget.TextView;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneat.ChooseDishes;
import com.damn.polito.damneat.R;
import com.damn.polito.damneat.beans.Restaurant;
import com.damn.polito.damneat.fragments.RestaurantFragment;

import static com.damn.polito.damneat.adapters.RestaurantAdapter.RestaurantViewHolder.*;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> implements Filterable {

    private Context ctx;
    private List<Restaurant> restaurants;
    private List<Restaurant> filtered;

    public RestaurantAdapter(Context ctx, List<Restaurant> restaurants) {
        this.ctx = ctx;
        this.filtered = restaurants;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.restaurant_layout, viewGroup, false);
        return new RestaurantAdapter.RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int pos) {
        Restaurant current = filtered.get(pos);
        Bitmap img = Utility.StringToBitMap(current.getImage());

        if(img != null)
            holder.restaurantImage.setImageBitmap(img);
        else
            holder.restaurantImage.setImageResource(R.drawable.dish_preview);

        holder.name.setText(current.getName());
        holder.categories.setText(current.getCategories());
        holder.reviews.setText(ctx.getString(R.string.restaurant_reviews, current.getReviews()));
        holder.ratingBar.setMax(MAX_PROGRESS);
        holder.ratingBar.setProgress(current.getReviews() == 0 ? 0 : current.rate());

        holder.priceShip.setText(current.getPriceShip() == 0 ?
                ctx.getString(R.string.price_free) : ctx.getString(R.string.order_price, current.getPriceShip()));

        holder.root.setOnClickListener(v->{
            //INFO RISTORANTE
            Intent intent = new Intent(ctx, ChooseDishes.class);
            intent.putExtra("rest_address", current.getAddress());
            intent.putExtra("rest_name", current.getName());
            intent.putExtra("rest_phone", current.getPhone());
            intent.putExtra("rest_key", current.getFbKey());
            intent.putExtra("rest_image", current.getImage());
            intent.putExtra("rest_description", current.getDescription());
            intent.putExtra("rest_priceship", current.getPriceShip());

            PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString("rest_opening", current.getOpening()).apply();
//            intent.putExtra("rest_rating", holder.ratingBar.getProgress());
//            intent.putExtra("rest_reviews", current.getReviews());
            ((Activity)ctx).startActivityForResult(intent, RestaurantFragment.REQUEST_CODE + pos);
        });
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter(){
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Restaurant> filteredList;

            if(constraint == null || constraint.length() == 0){
                filteredList = new ArrayList<>(restaurants);
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();
                filteredList = new ArrayList<>();
                for(Restaurant r : restaurants){
                    if(r.contains(filterPattern))
                        filteredList.add(r);
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            filtered.clear();
            if(filterResults.values instanceof List)
                filtered.addAll((List<Restaurant>)filterResults.values);
            notifyDataSetChanged();
        }
    };

    public void setFullList(@NonNull List<Restaurant> list){
        restaurants = new ArrayList<>(list);
    }

    public class RestaurantViewHolder extends RecyclerView.ViewHolder {

        public static final int MAX_PROGRESS = 500;

        private CircleImageView restaurantImage;
        private TextView name, categories, priceShip, reviews;
        private RatingBar ratingBar;
        private CardView root;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);

            restaurantImage = itemView.findViewById(R.id.restaurant_img);
            name = itemView.findViewById(R.id.restaurant_name);
            categories = itemView.findViewById(R.id.restaurant_category);
            priceShip = itemView.findViewById(R.id.restaurant_ship_price);
            reviews = itemView.findViewById(R.id.restaurant_reviews);
            ratingBar = itemView.findViewById(R.id.restaurant_ratingbar);
            root = itemView.findViewById(R.id.restaurant_root);
            itemView.findViewById(R.id.divider).setVisibility(View.INVISIBLE);
        }
    }
}
