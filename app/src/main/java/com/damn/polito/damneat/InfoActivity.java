package com.damn.polito.damneat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.damn.polito.commonresources.beans.QueryType;
import com.damn.polito.damneat.adapters.ReviewsAdapter;
import com.damn.polito.commonresources.beans.RateObject;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class InfoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewsAdapter adapter;
    private ProgressBar buffer;
    private List<RateObject> reviews;

    private Query reviewQuery;
    private ValueEventListener reviewListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(com.damn.polito.commonresources.R.string.info_service_review_title);

        init();
    }

    private void init() {
        recyclerView = findViewById(R.id.info_recycler);
        recyclerView.setVisibility(View.GONE);

        buffer = findViewById(R.id.info_buffer);
        reviews = new LinkedList<>();
        adapter = new ReviewsAdapter(reviews, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setListener();
    }

    private void setListener() {
        reviewQuery = FirebaseDatabase.getInstance().getReference("reviews/").orderByChild("type").equalTo(RateObject.RateType.Service.toString());
        reviewListener = reviewQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                for(DataSnapshot child : dataSnapshot.getChildren()){
                    if(child.getValue() == null)
                        continue;
                    RateObject rate = child.getValue(RateObject.class);
                    assert rate != null;
                    rate.sQueryType(QueryType.ServiceType);
                    if(!reviews.contains(rate))
                        reviews.add(rate);
                }

                Collections.sort(reviews);
                adapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
                buffer.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(InfoActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(reviewQuery != null)
            reviewQuery.removeEventListener(reviewListener);
    }
}
