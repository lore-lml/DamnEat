package com.damn.polito.damneat.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.damn.polito.commonresources.beans.RateObject;
import com.damn.polito.damneat.R;
import com.damn.polito.damneat.Welcome;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewsAdapter adapter;
    private List<RateObject> reviews;

    private Query ref;
    private ChildEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.reviews_title);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        initRecycler();
        setListener();
    }

    private void initRecycler(){
        reviews = new ArrayList<>();
        recyclerView = findViewById(R.id.reviews_recycler);
        adapter = new ReviewsAdapter(reviews, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setListener() {
        ref = FirebaseDatabase.getInstance().getReference("reviews/").orderByChild("customer/customerID").equalTo(Welcome.getDbKey());
        listener = ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue() == null) return;
                RateObject review = dataSnapshot.getValue(RateObject.class);
                reviews.add(review);

                Collections.sort(reviews, (a, b) -> {
                    //Se sono di tipo Service allora ordinali in base al rate
                    if(a.getType() == RateObject.RateType.Service && a.getType() == b.getType())
                        return b.getRate() - a.getRate();
                    //Se solo uno è di tipo service metti sempre dopo, il tipo service
                    if(a.getType() == RateObject.RateType.Service)
                        return 1;
                    if(b.getType() == RateObject.RateType.Service)
                        return -1;

                    //Altrimenti ordina in base al nome del ristorante
                    if(a.getRestaurant().getRestaurantName().equals(b.getRestaurant().getRestaurantName()))
                        return b.getRate() - a.getRate();
                    return a.getRestaurant().getRestaurantName().compareTo(b.getRestaurant().getRestaurantName());
                });

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ref.removeEventListener(listener);
    }
}
