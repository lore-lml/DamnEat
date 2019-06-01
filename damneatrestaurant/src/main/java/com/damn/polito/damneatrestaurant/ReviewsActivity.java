package com.damn.polito.damneatrestaurant;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.damn.polito.commonresources.beans.RateObject;
import com.damn.polito.damneatrestaurant.adapters.ReviewsAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ReviewsActivity extends AppCompatActivity {
    private List<RateObject> reviews = new LinkedList<>();
    private RecyclerView recyclerView;
    private ReviewsAdapter adapter;
    private ProgressBar buffer;

    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private Query reviewQuery;
    private ValueEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.review_title);

        buffer = findViewById(R.id.review_buffer);
        initReyclerView();
        loadData();
    }

    private void loadData() {
        String dbKey = Welcome.getDbKey();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("/reviews/");
        reviewQuery = dbRef.orderByChild("restaurant/restaurantID").equalTo(dbKey);
        eventListener = reviewQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    if(child.getValue() == null)
                        continue;
                    RateObject review = child.getValue(RateObject.class);
                    if(!reviews.contains(review))
                        reviews.add(review);
                }

                Collections.sort(reviews);
                adapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
                buffer.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ReviewsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initReyclerView(){
        recyclerView = findViewById(R.id.recyclerview_reviews);
        recyclerView.setVisibility(View.GONE);
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
