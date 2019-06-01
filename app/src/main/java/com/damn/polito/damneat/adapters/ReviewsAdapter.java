package com.damn.polito.damneat.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.QueryType;
import com.damn.polito.commonresources.beans.RateObject;
import com.damn.polito.damneat.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.OrderViewHolder> {
    private List<RateObject> reviews;
    private Context ctx;

    public ReviewsAdapter(List<RateObject> reviews, Context context){
        this.reviews= reviews;
        this.ctx = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.rate_element_layout, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        RateObject selected = reviews.get(position);

        holder.date.setText(selected.getDate());
        holder.note.setText(selected.getNote());
        holder.ratingBar.setRating(selected.getRate());

        holder.name.setText(getProperName(selected));
        setImage(holder, selected);
        holder.reviewType.setImageResource(getProperIcon(selected));
    }

    private int getProperIcon(RateObject selected) {
        switch (selected.getType()){
            case Meal:
                return R.drawable.ic_restaurant;
            case Restaurant:
                return R.drawable.ic_cutlery;
            case Service:
                return R.drawable.ic_star;
            default:
                return -1;
        }
    }

    private void setImage(OrderViewHolder holder, RateObject selected) {
        if(selected.queryType() == QueryType.SelfReview  && selected.getType() == RateObject.RateType.Service)
            holder.image.setImageResource(R.mipmap.ic_launcher);
        else
            holder.image.setImageBitmap(Utility.StringToBitMap(getProperPhoto(selected)));
    }

    private String getProperName(RateObject selected){
        switch (selected.queryType()){
            case SelfReview:
                if(selected.getType() == RateObject.RateType.Service)
                    return ctx.getString(R.string.service_review);
                return selected.getRestaurant().getRestaurantName();
            case RestaurantReview:
                return selected.getCustomer().getCustomerName();
            case ServiceType:
                return selected.getCustomer().getCustomerName();
        }
        return null;
    }

    private String getProperPhoto(RateObject selected){
        switch (selected.queryType()){
            case SelfReview:
                return selected.getRestaurant().getPhoto();
            case RestaurantReview:
                return selected.getCustomer().getCustomerPhoto();
            case ServiceType:
                return selected.getCustomer().getCustomerPhoto();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView name, date, note;
        private CircleImageView image;
        private RatingBar ratingBar;
        private ImageView reviewType;

        public OrderViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.customer_name_tv);
            date = itemView.findViewById(R.id.date_tv);
            note = itemView.findViewById(R.id.note_tv);
            image = itemView.findViewById(R.id.image_customer);
            ratingBar = itemView.findViewById(R.id.ratingbar);
            reviewType = itemView.findViewById(R.id.review_type);
        }
    }

}
