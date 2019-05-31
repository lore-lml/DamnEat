package com.damn.polito.damneat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import com.damn.polito.commonresources.beans.RateObject;
import com.damn.polito.damneat.R;
import com.damn.polito.damneat.Welcome;
import com.damn.polito.damneat.adapters.ReviewsAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class ReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewsAdapter adapter;
    private List<RateObject> reviews;
    private Query ref;
    private ValueEventListener listener;

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
        listener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    RateObject review = child.getValue(RateObject.class);
                    reviews.add(review);
                }

                Collections.sort(reviews);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ReviewsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
