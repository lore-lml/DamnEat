package com.damn.polito.damneatrestaurant.adapters;

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
import com.damn.polito.commonresources.beans.Deliverer;
import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.commonresources.beans.Deliverer;
import com.damn.polito.damneatrestaurant.FindDelivererActivity;
import com.damn.polito.damneatrestaurant.R;

import static com.damn.polito.damneatrestaurant.adapters.DelivererAdapter.DelivererViewHolder.*;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DelivererAdapter extends RecyclerView.Adapter<DelivererAdapter.DelivererViewHolder> {

    private Context ctx;
    private List<Deliverer> deliverers;
    private List<Deliverer> filtered;

    public DelivererAdapter(Context ctx, List<Deliverer> deliverers) {
        this.ctx = ctx;
        this.filtered = deliverers;
    }

    @NonNull
    @Override
    public DelivererViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.deliverer_layout, viewGroup, false);
        return new DelivererAdapter.DelivererViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DelivererViewHolder holder, int pos) {
        Deliverer current = filtered.get(pos);
        Bitmap img = Utility.StringToBitMap(current.getBitmapProf());

        if(img != null)
            holder.delivererImage.setImageBitmap(img);
        else
            holder.delivererImage.setImageResource(R.drawable.profile_sample);

        holder.name.setText(current.getName());
        holder.phone.setText(current.getPhone());
        holder.description.setText(current.getDescription());

//        holder.root.setOnClickListener(v->{
//            //INFO RISTORANTE
//            Intent intent = new Intent(ctx, ChooseDeliver.class);
//            intent.putExtra("rest_name", current.getName());
//            intent.putExtra("rest_phone", current.getPhone());
//            intent.putExtra("rest_key", current.getKey());
//            intent.putExtra("rest_image", current.getBitmapProf());
//            intent.putExtra("rest_description", current.getDescription());
//
////            intent.putExtra("rest_rating", holder.ratingBar.getProgress());
////            intent.putExtra("rest_reviews", current.getReviews());
//            ((Activity)ctx).startActivityForResult(intent, FindDelivererActivity.REQUEST_CODE + pos);
//        });
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    public void setFullList(@NonNull List<Deliverer> list){
        deliverers = new ArrayList<>(list);
    }

    public List<Deliverer> getFullList(){
        return deliverers;
    }

    public class DelivererViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView delivererImage;
        private TextView name, phone, description;
        private CardView root;

        public DelivererViewHolder(@NonNull View itemView) {
            super(itemView);

            delivererImage = itemView.findViewById(R.id.deliverer_img);
            name = itemView.findViewById(R.id.deliverer_name);
            phone = itemView.findViewById(R.id.deliverer_phone_number);
            description = itemView.findViewById(R.id.deliverer_description);
            root = itemView.findViewById(R.id.deliverer_root);
            itemView.findViewById(R.id.divider).setVisibility(View.INVISIBLE);
        }
    }
}
