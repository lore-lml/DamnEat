package com.damn.polito.damneat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.InternetConnection;
import com.damn.polito.damneat.R;
import com.damn.polito.damneat.adapters.RestaurantAdapter;
import com.damn.polito.damneat.beans.Restaurant;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class RestaurantFragment extends Fragment {

    public static final int REQUEST_CODE = 9000;

    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;
    private LinearLayout offline;
    private Context ctx;

    private DatabaseReference dbRef;
    private ChildEventListener listener;

    private List<Restaurant> restaurants;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.restaurant_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = ((AppCompatActivity)getActivity());
        assert activity != null;
        Objects.requireNonNull(activity.getSupportActionBar()).setTitle(R.string.app_name);

        ctx = getContext();
        assert ctx != null;

        offline = view.findViewById(R.id.restaurant_offline);

        if(InternetConnection.haveInternetConnection(ctx)) {
            init(view);
            loadData();
            offline.setVisibility(View.GONE);
        }else{
            offline.setVisibility(View.VISIBLE);
        }
    }

    private void init(View view){
        restaurants = new ArrayList<>();
        recyclerView = view.findViewById(R.id.restaurant_recycler);
        adapter = new RestaurantAdapter(getActivity(), restaurants);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
    }

    private void loadData() {
        dbRef = FirebaseDatabase.getInstance().getReference("ristoratori/");

        listener = dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                Restaurant r = dataSnapshot.getValue(Restaurant.class);
                assert key != null;
                assert r != null;
                r.setFbKey(key);
                restaurants.add(r);
                adapter.setFullList(restaurants);
                adapter.notifyItemInserted(restaurants.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                Restaurant r = dataSnapshot.getValue(Restaurant.class);
                assert key != null;
                assert r != null;
                r.setFbKey(key);
                int pos = restaurants.indexOf(r);
                restaurants.remove(r);
                adapter.setFullList(restaurants);
                adapter.notifyItemRemoved(pos);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ctx, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)item.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //USELESS FOR US
                return false;
            }

            @Override
            public boolean onQueryTextChange(String filterText) {
                adapter.getFilter().filter(filterText);
                return false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(listener!=null)
            dbRef.removeEventListener(listener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            int pos = requestCode-REQUEST_CODE;
            if(pos < 0 || pos > restaurants.size()) return;

            /*TODO: gestire stato ordine effettuato*/
        }
    }
}
