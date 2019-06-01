package com.damn.polito.damneat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.damn.polito.commonresources.beans.QueryType;
import com.damn.polito.commonresources.beans.RateObject;
import com.damn.polito.damneat.adapters.ReviewsAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewsAdapter adapter;
    private ProgressBar buffer;

    private List<RateObject> reviews;
    private Query ref;
    private ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        String restaurantName = getIntent().getStringExtra("restaurant_name");
        if(restaurantName == null) restaurantName = "";

        Objects.requireNonNull(getSupportActionBar()).setTitle(getQueryType() == QueryType.SelfReview ?
                getString(R.string.reviews_title) : getString(R.string.specific_restaurant_review_title, restaurantName));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        buffer = findViewById(R.id.review_buffer);
        initRecycler();
        setListener();
    }

    private void initRecycler(){
        reviews = new LinkedList<>();
        recyclerView = findViewById(R.id.reviews_recycler);
        recyclerView.setVisibility(View.GONE);
        adapter = new ReviewsAdapter(reviews, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setListener() {
        QueryType qtype = getQueryType();
        String query = getQuery();
        String equalToParam = getQueryEqualToParam();
        ref = FirebaseDatabase.getInstance().getReference("reviews/").orderByChild(query).equalTo(equalToParam);
        listener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    RateObject review = child.getValue(RateObject.class);
                    if(review == null)
                        continue;
                    review.sQueryType(qtype);
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
    private QueryType getQueryType(){
        Intent i = getIntent();
        String type = i.getStringExtra("query_type");
        if(type == null)
            throw new IllegalStateException("You must set a query type before start the activity with key <query_type>");

        return QueryType.valueOf(type);
    }
    private String getQuery(){
        QueryType qtype = getQueryType();

        switch (qtype){
            case SelfReview:
                return "customer/customerID";
            case RestaurantReview:
        }       return "restaurant/restaurantID";

    }

    private String getQueryEqualToParam(){
        QueryType qtype = getQueryType();

        switch (qtype){
            case SelfReview:
                return Welcome.getDbKey();
            case RestaurantReview:
        }       return getIntent().getStringExtra("restaurant_id");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ref.removeEventListener(listener);
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
