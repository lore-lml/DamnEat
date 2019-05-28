package com.damn.polito.damneat.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.RateObject;
import com.damn.polito.damneat.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.OrderViewHolder> {
    private List<RateObject> reviews;
    private Context ctx;
    private Bitmap default_image;

    public ReviewsAdapter(List<RateObject> reviews, Context context){
        this.reviews= reviews;
        this.ctx = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.rate_element_layout, parent, false);
        default_image = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.profile_sample);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        RateObject selected = reviews.get(position);

        if(selected.getRestaurant().getPhoto().equals("NO_PHOTO"))
            holder.image.setImageBitmap(default_image);
        else
            holder.image.setImageBitmap(Utility.StringToBitMap(selected.getRestaurant().getPhoto()));

        holder.name.setText(selected.getRestaurant().getRestaurantName());
        holder.date.setText(selected.getDate());
        holder.note.setText(selected.getNote());
        holder.ratingBar.setRating(selected.getRate());

        if (selected.getType() == RateObject.RateType.Service) {
            holder.name.setText(R.string.service_review);
            holder.reviewType.setImageResource(R.drawable.ic_star);
        } else {
            holder.name.setText(selected.getRestaurant().getRestaurantName());
            holder.reviewType.setImageResource(selected.getType() == RateObject.RateType.Meal ?
                    R.drawable.ic_restaurant : R.drawable.ic_cutlery);
        }
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
