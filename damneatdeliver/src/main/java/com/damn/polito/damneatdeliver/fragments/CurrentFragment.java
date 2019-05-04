package com.damn.polito.damneatdeliver.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneatdeliver.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class CurrentFragment extends Fragment {

    private Context ctx;

    private TextView id,date,price,nDish, deliverer_name, dishes_list, dishes_list_2, restaurant_info, state, note, delivery_time;
    private CardView root;
    private ImageView deliverer_photo, restaurant_photo;
    private Button confirmButton;
    private Bitmap bitmap;

    private boolean empty = true;
    private Map orders;


    private FirebaseDatabase database;
    private String key, defaultValue = "- -";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.order_layout_first, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = ((AppCompatActivity)getActivity());
        assert activity != null;
        Objects.requireNonNull(activity.getSupportActionBar()).setTitle(R.string.alert_edit_profile_title);

        ctx = view.getContext();

        id= view.findViewById(R.id.order_id);
        date = view.findViewById(R.id.order_date_value);
        price = view.findViewById(R.id.order_price);
        nDish = view.findViewById(R.id.order_num_dishes);
        deliverer_name = view.findViewById(R.id.order_Customer_name_textview);
        deliverer_photo = view.findViewById(R.id.circleImageView);
        dishes_list = view.findViewById(R.id.dishes_list);
        restaurant_info =view.findViewById(R.id.order_customer_info);
        delivery_time =view.findViewById(R.id.order_delivery_time);
        note =view.findViewById(R.id.order_note);
        confirmButton =view.findViewById(R.id.confirmOrder);

        database = FirebaseDatabase.getInstance();

        load();
    }

    private String getDishesList(Order selected){
        String dish_list_str = "";
        List<Dish> dishes = selected.getDishes();
        Double price = 0.;
        for (Dish d:dishes) {
            String p = String.format("%.2f", d.getPrice());
            dish_list_str += d.getQuantity() +"\tx\t"+ d.getName()+"\t"+ p + "€\n";
            price += d.getQuantity()*d.getPrice();
        }
        if(selected.getRestaurant().getRestaurant_price_ship() != null && selected.getRestaurant().getRestaurant_price_ship() != 0.) {
            String p = String.format("%.2f", selected.getRestaurant().getRestaurant_price_ship());
            dish_list_str += ctx.getString(R.string.ship) + " " + p + "€";
            Log.d("test", selected.getRestaurant().getRestaurant_price_ship().toString());
            price += selected.getRestaurant().getRestaurant_price_ship();
        }
        return dish_list_str;
    }

    private void load() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);

        if (key == null) {
            key = pref.getString("key", null);
            if (key == null) return;
        }

        DatabaseReference ref = database.getReference("clienti/" + key);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DateFormat dateFormat = new SimpleDateFormat(ctx.getString(R.string.date_format), Locale.getDefault());

                Order order = dataSnapshot.getValue(Order.class);
                if (order != null) {
                    String dish_list_str = getDishesList(order);
                    dishes_list.setText(dish_list_str);
                    date.setText(dateFormat.format(order));
                    String i = ctx.getString(R.string.order_id_s, order.Id());
                    id.setText(i);
                    nDish.setText(ctx.getString(R.string.order_num_dishes, order.DishesNumber()));
                    price.setText(ctx.getString(R.string.order_price, order.getPrice()));
                    restaurant_info.setText(ctx.getString(R.string.restaurant, order.getRestaurant().getRestaurantName()));
                    if (order.getCustomer().getCustomerPhoto() != null) {
                        String encodedBitmap = order.getCustomer().getCustomerPhoto();
                        bitmap = Utility.StringToBitMap(encodedBitmap);
                        if (bitmap != null)
                            deliverer_photo.setImageBitmap(bitmap);
                    }
                    empty = false;
                } else {
                    dishes_list.setText(defaultValue);
                    date.setText(defaultValue);
                    nDish.setText(defaultValue);
                    price.setText(defaultValue);
                    restaurant_info.setText(defaultValue);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ctx, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference ordini = database.getReference("ordini/" + key);
        ordini.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orders = (Map) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ctx, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
