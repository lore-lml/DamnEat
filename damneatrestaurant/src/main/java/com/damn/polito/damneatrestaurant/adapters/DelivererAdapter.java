package com.damn.polito.damneatrestaurant.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.damn.polito.damneatrestaurant.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Currency;
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

        double distance = (double)current.distance()/1000;
        if(distance > 0)
            holder.distance.setText(ctx.getString(R.string.distance_km, distance));
        else
            holder.distance.setText(ctx.getString(R.string.distance_meter, current.distance()));


        holder.button.setOnClickListener(v -> {
            if(checkCustomerInfo())
                pickDeliverer(pos);
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

    private boolean checkCustomerInfo() {
        if (order.getCustomerName().equals("") || order.getCustomerAddress().equals("")) {
            DatabaseReference dbOrder = database.getReference("/ordini/" + order.getId() + "/state");
            dbOrder.setValue("rejected");
            Toast.makeText(ctx, R.string.no_customer_info, Toast.LENGTH_LONG).show();
            return false;
        }
        else {
            return true;
        }
    }

    private void updateAvailabity(int position) {
//        Deliverer current = deliverers.get(position);
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
                            if(d_ord.getId().equals(d.getId())){
                                int new_quantity = d.getAvailability() - d_ord.getQuantity();
                                if (new_quantity < 0)
                                    return Transaction.abort();
                                else {
                                    d.setAvailability(new_quantity);
                                    child.setValue(d);
                                }
                            }
                        }
                    }
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if(b){
                    //updateTotalDishes();
                    //updateTotalDishes();
                }else {
                    DatabaseReference dbOrder = database.getReference("/ordini/" + order.getId() + "/state");
                    dbOrder.setValue("rejected");
                    Toast.makeText(ctx, "Insufficient quantity", Toast.LENGTH_SHORT).show();
                    ((Activity)ctx).finish();
                }
            }
        });
    }

    private void updateTotalDishes(Order current){
        //Deliverer current = deliverers.get(position);
        //AGGIORNO LE AVAILABILITY
        DatabaseReference ref = database.getReference("/ristoranti/" + order.getRestaurant().getRestaurantID() + "/piatti_totali/");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        ref.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {

                for(MutableData child: mutableData.getChildren()){
                    Dish d = child.getValue(Dish.class);
                    if(d!=null) {
                        for (Dish d_ord : order.getDishes()) {
                            if(d_ord.getId().equals(d.getId())){
                                int new_quantity = d.getAvailability() - d_ord.getQuantity();
                                if (new_quantity < 0)
                                    return Transaction.abort();
                                else {
                                    d.setAvailability(new_quantity);
                                    child.setValue(d);
                                }
                            }
                        }
                    }
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
            }
        });
    }

    private void pickDeliverer(int position) {
        Deliverer current = deliverers.get(position);
        DatabaseReference dbRef = database.getReference("/deliverers_liberi/");
        dbRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData){
                if(mutableData.getValue() == null)
                    return Transaction.abort();


                for (MutableData child : mutableData.getChildren()) {
                    if (child != null) {
                        String s = child.getValue(String.class);
                        if (s!=null && s.equals(current.getKey())) {
                            mutableData.child(s).setValue(null);
                            Log.d("tmz", "eliminate"+s);
                            return Transaction.success(mutableData);
                        }
                    }
                }
                return Transaction.abort();
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot){
                if(b){

//                    if (current.getKey().isEmpty()) {
//                        DatabaseReference dbOrder = database.getReference("/ordini/" + order.getId() + "/state");
//                        dbOrder.setValue("rejected");
//                        Toast.makeText(ctx, R.string.no_availabity, Toast.LENGTH_LONG).show();
//                        return;
//                    }
                    DatabaseReference ref = database.getReference("deliverers/" + current.getKey() + "/current_order");
                    ref.setValue(order.getId());
                    DatabaseReference dbOrder = database.getReference("/ordini/" + order.getId() + "/state");
                    dbOrder.setValue("accepted");
                    updateAvailabity(position);
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
        private TextView name, phone, description, distance;
        private CardView root;
        private Button button;

        public DelivererViewHolder(@NonNull View itemView, OnItemClickListener mListener) {
            super(itemView);

            delivererImage = itemView.findViewById(R.id.deliverer_img);
            name = itemView.findViewById(R.id.deliverer_name);
            phone = itemView.findViewById(R.id.deliverer_phone_number);
            description = itemView.findViewById(R.id.deliverer_description);
            distance = itemView.findViewById(R.id.deliverer_distance);
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

    public void setFullList(@NonNull List<Deliverer> list){
        deliverers = new ArrayList<>(list);
    }
}
