package com.damn.polito.damneatrestaurant.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Deliverer;
import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneatrestaurant.FindDelivererActivity;
import com.damn.polito.damneatrestaurant.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class DelivererAdapter extends RecyclerView.Adapter<DelivererAdapter.DelivererViewHolder> {

    private Context ctx;
    private List<Deliverer> deliverers;
    private Order order;
    private OnItemClickListener mListener;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    public interface OnItemClickListener { void onItemClick(int position); }

    public void setOnItemClickListener (OnItemClickListener listener) { mListener = listener; }

    public DelivererAdapter(Context ctx, List<Deliverer> deliverers, Order order) {
        this.ctx = ctx;
        this.deliverers = deliverers;
        this.order = order;
    }

    @NonNull
    @Override
    public DelivererViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.deliverer_layout, viewGroup, false);
        return new DelivererViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DelivererViewHolder holder, int pos) {
        Deliverer current = deliverers.get(pos);
        Bitmap img = Utility.StringToBitMap(current.getBitmapProf());

        if(img != null)
            holder.delivererImage.setImageBitmap(img);
        else
            holder.delivererImage.setImageResource(R.drawable.profile_sample);

        holder.name.setText(current.getName());
        holder.phone.setText(current.getPhone());
        holder.description.setText(current.getDescription());
        holder.button.setOnClickListener(v -> {
            startTransaction(pos);
        });

        if (!current.Expanded()) {
            holder.button.setVisibility(View.GONE);
        }else{
            holder.button.setVisibility(View.VISIBLE);
        }

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

    private void startTransaction(int position) {

        Deliverer current = deliverers.get(position);
        /*    StringBuilder delivererKey = new StringBuilder();
            dbRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {

                    if (deliverers.isEmpty())
                        return Transaction.abort();

                    delivererKey.append(deliverers.get(0));


                    for (MutableData child : mutableData.getChildren()) {
                        if (child.getValue() != null) {
                            String s = child.getValue(String.class);
                            if (s!= null && s.equals(delivererKey.toString())) {
                                mutableData.child(s).setValue(null);
                                return Transaction.success(mutableData);
                            }
                        }
                    }
                    return Transaction.abort();
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                    if (b) {
                        if (delivererKey.toString().isEmpty()) {
                            DatabaseReference dbOrder = database.getReference("/ordini/" + order.getId() + "/state");
                            dbOrder.setValue("rejected");
                            Toast.makeText(ctx, R.string.no_availabity, Toast.LENGTH_LONG).show();
                            return;
                        }
                        refreshAvailabityAndAccept(position, delivererKey.toString());
                    } else {
                        DatabaseReference dbOrder = database.getReference("/ordini/" + order.getId() + "/state");
                        dbOrder.setValue("rejected");
                        Toast.makeText(ctx, R.string.no_free_deliverers, Toast.LENGTH_LONG).show();
                    }

                }
            });*/
        //AGGIORNO LE AVAILABILITY
        DatabaseReference ref = database.getReference("/ristoranti/" + order.getRestaurant().getRestaurantID() + "/piatti_del_giorno/");
        ref.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {

                for(MutableData child: mutableData.getChildren()){
                    Dish d = child.getValue(Dish.class);
                    if(d!=null) {
                        for (Dish d_ord : order.getDishes()) {
                            int new_quantity = d.getAvailability() - d_ord.getQuantity();
                            if (new_quantity < 0)
                                return Transaction.abort();
                            else {
                                d.setAvailability(new_quantity);
                                child.setValue(d);
                                DatabaseReference ref = database.getReference("/ristoranti/" + order.getRestaurant().getRestaurantID()
                                        + "/piatti_totali/" + d.getId());
                                ref.setValue(d);
                            }
                        }
                    }
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if(b){
                    pickDeliverer(current);

                }else {
                    DatabaseReference dbOrder = database.getReference("/ordini/" + order.getId() + "/state");
                    dbOrder.setValue("rejected");
                    Toast.makeText(ctx, "Insufficient quantity", Toast.LENGTH_SHORT).show();
                    ((Activity)ctx).finish();
                }
            }
        });
    }

    private void pickDeliverer(Deliverer current) {
        DatabaseReference dbRef = database.getReference("/deliverers_liberi/");
        dbRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData){
                if(mutableData.getValue() == null)
                    return Transaction.abort();


                for(MutableData child : mutableData.getChildren()){
                    if(child.getValue() == null) continue;
                    assert child.getValue() != null;
                    if(Objects.equals(child.getValue(String.class), current.getKey())){
                        database.getReference("deliverers_liberi/"+ child.getValue()).removeValue();
                        return Transaction.success(child);
                    }
                }
                return Transaction.abort();
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot){
                if(b){
                    DatabaseReference ref = database.getReference("deliverers/" + current.getKey() + "/current_order");
                    ref.setValue(order.getId());
                    DatabaseReference dbOrder = database.getReference("/ordini/" + order.getId() + "/state");
                    dbOrder.setValue("accepted");
                }
                else{
                    DatabaseReference dbOrder = database.getReference("/ordini/" + order.getId() + "/state");
                    dbOrder.setValue("rejected");
                }

                ((Activity)ctx).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return deliverers.size();
    }

    public class DelivererViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView delivererImage;
        private TextView name, phone, description;
        private CardView root;
        private Button button;

        public DelivererViewHolder(@NonNull View itemView, OnItemClickListener mListener) {
            super(itemView);

            delivererImage = itemView.findViewById(R.id.deliverer_img);
            name = itemView.findViewById(R.id.deliverer_name);
            phone = itemView.findViewById(R.id.deliverer_phone_number);
            description = itemView.findViewById(R.id.deliverer_description);
            root = itemView.findViewById(R.id.deliverer_root);
            button = itemView.findViewById(R.id.delivery_button);
            itemView.findViewById(R.id.divider).setVisibility(View.INVISIBLE);

            itemView.setOnClickListener(view -> {
                if (mListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(position);
                    }
                }
            });
        }
    }
}
