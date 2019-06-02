package com.damn.polito.damneat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.beans.Customer;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.commonresources.beans.RateObject;
import com.damn.polito.commonresources.beans.Restaurant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RateRestaurant extends AppCompatActivity {

    private TextView restaurantText, foodText;
    private EditText restaurantEdit, foodEdit;
    private RatingBar restaurantRt, foodRt;
    private Button send;

    private Order order;
    private boolean firstReview = false, secondReview = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_restaurant);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.rate_activity_title);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        restaurantText = findViewById(R.id.rate_restaurant_value);
        foodText = findViewById(R.id.rate_food_value);
        restaurantEdit = findViewById(R.id.rate_restaurant_note);
        foodEdit = findViewById(R.id.rate_food_note);
        restaurantRt = findViewById(R.id.ratingBar_reastaurant);
        foodRt = findViewById(R.id.ratingBar_food);
        send = findViewById(R.id.rate_send);

        init();
    }

    private void init(){
        order = (Order) getIntent().getSerializableExtra("order");
        restaurantRt.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            changeValueAction(ratingBar, restaurantText);
            firstReview = true;
        });
        foodRt.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            changeValueAction(ratingBar, foodText);
            secondReview = true;
        });

        send.setOnClickListener(v-> sendAction());
    }

    private void sendAction() {
        if(!firstReview || !secondReview){
            Toast.makeText(this, R.string.incomplete_review, Toast.LENGTH_SHORT).show();
            return;
        }
        int restaurantProgress = restaurantRt.getProgress();
        String restaurantNote = restaurantEdit.getText().toString().isEmpty() ? null : restaurantEdit.getText().toString();
        Customer customer = new Customer();
        customer.setCustomerName(Welcome.getProfile().getName());
        customer.setCustomerPhoto(Welcome.getProfile().getBitmapProf());
        customer.setCustomerID(Welcome.getDbKey());

        RateObject rateRestaurant = new RateObject(restaurantProgress, restaurantNote, RateObject.RateType.Restaurant, customer);
        rateRestaurant.setRestaurant(order.getRestaurant());

        int foodProgress = foodRt.getProgress();
        String foodNote = foodEdit.getText().toString().isEmpty() ? null : foodEdit.getText().toString();

        RateObject rateFood = new RateObject(foodProgress, foodNote, RateObject.RateType.Meal, customer);
        rateFood.setRestaurant(order.getRestaurant());


        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference restRef = db.getReference("reviews/").push();
        restRef.setValue(rateRestaurant);

        DatabaseReference foodRef = db.getReference("reviews/").push();
        foodRef.setValue(rateFood);

        DatabaseReference orderRef = db.getReference("ordini/"+order.getId());
        Map<String, Object> children = new HashMap<>();
        children.put("rated", true);
        orderRef.updateChildren(children);

        DatabaseReference updateRate = db.getReference("ristoratori/" + order.getRestaurant().getRestaurantID());
        updateRate.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                if(mutableData.getValue() == null) return Transaction.abort();
                MutableData reviewChild = mutableData.child("reviews");
                MutableData rateChild = mutableData.child("totalRate");
                Integer reviews = reviewChild.getValue(Integer.class);
                Integer totalRate = rateChild.getValue(Integer.class);

                if(reviews == null || reviews <= 0)
                    reviews = 0;

                if(totalRate == null || totalRate <= 0)
                    totalRate = 0;

                reviews += 2;
                totalRate += restaurantProgress + foodProgress;
                reviewChild.setValue(reviews);
                rateChild.setValue(totalRate);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                RateRestaurant.this.finish();
            }
        });
    }

    private void changeValueAction(RatingBar ratingBar, TextView scale_tv) {
        switch ((int) ratingBar.getRating()) {
            case 1:
                scale_tv.setText(R.string.rate_one);
                break;
            case 2:
                scale_tv.setText(R.string.rate_two);
                break;
            case 3:
                scale_tv.setText(R.string.rate_three);
                break;
            case 4:
                scale_tv.setText(R.string.rate_four);
                break;
            case 5:
                scale_tv.setText(R.string.rate_five);
                break;
            default:
                ratingBar.setRating(1);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
