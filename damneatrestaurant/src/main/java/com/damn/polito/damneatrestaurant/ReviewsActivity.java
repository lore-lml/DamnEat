package com.damn.polito.damneatrestaurant;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.damn.polito.commonresources.beans.RateObject;
import com.damn.polito.damneatrestaurant.adapters.ReviewsAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ReviewsActivity extends AppCompatActivity {
    private List<RateObject> reviews = new LinkedList<>();
    private RecyclerView recyclerView;
    private ReviewsAdapter adapter;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private Query reviewQuery;
    private ChildEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.review_title);

        setContentView(R.layout.activity_reviews);
        reviews.clear();
        initReyclerView();
        loadData();
    }

    private void loadData() {
        String dbKey = Welcome.getDbKey();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("/reviews/");
        reviewQuery = dbRef.orderByChild("restaurant/restaurantID").equalTo(dbKey);
        eventListener = reviewQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RateObject rate = dataSnapshot.getValue(RateObject.class);
                if(rate!=null) {
                    reviews.add(rate);
                    adapter.notifyItemChanged(reviews.size()-1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void initReyclerView(){
        recyclerView = findViewById(R.id.recyclerview_reviews);
        adapter = new ReviewsAdapter(reviews, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(reviewQuery!=null && eventListener!=null)
            reviewQuery.removeEventListener(eventListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
