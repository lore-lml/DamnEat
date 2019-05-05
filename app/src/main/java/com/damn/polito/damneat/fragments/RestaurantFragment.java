package com.damn.polito.damneat.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.InternetConnection;
import com.damn.polito.damneat.R;
import com.damn.polito.damneat.Welcome;
import com.damn.polito.damneat.adapters.RestaurantAdapter;
import com.damn.polito.damneat.beans.Restaurant;
import com.damn.polito.damneat.dialogs.DialogType;
import com.damn.polito.damneat.dialogs.FilterDialog;
import com.damn.polito.damneat.dialogs.HandleDismissDialog;
import com.damn.polito.damneat.dialogs.SortDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class RestaurantFragment extends Fragment implements HandleDismissDialog {

    public enum SortType{Alpha, PriceAsc, PriceDesc, MostRated}

    public static final int REQUEST_CODE = 9000;
    public static final String REDO = "REDO";

    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;
    private LinearLayout offline;
    private TextView registered_tv;
    private ImageView registered_im;
    private Button sort,filter;
    private Context ctx;

    private DatabaseReference dbRef;
    private ChildEventListener listener;

    private List<Restaurant> restaurants;

    private SortType sortType;
    private String categories;

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
        registered_tv = view.findViewById(R.id.not_registered_tv);
        registered_im = view.findViewById(R.id.not_registered_im);
        recyclerView = view.findViewById(R.id.restaurant_recycler);
        sort = view.findViewById(R.id.button_sort);
        filter = view.findViewById(R.id.button_filter);


        if(InternetConnection.haveInternetConnection(ctx)) {
            init();
            loadData();
            offline.setVisibility(View.GONE);
        }else{
            offline.setVisibility(View.VISIBLE);
        }
        Log.d("not_registered reg", String.valueOf(userRegistered()));
        if(!Welcome.accountExist){
            registered_tv.setVisibility(View.VISIBLE);
            registered_im.setVisibility(View.VISIBLE);
            offline.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            sort.setVisibility(View.GONE);
            filter.setVisibility(View.GONE);

        }else {
            registered_tv.setVisibility(View.GONE);
            registered_im.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            sort.setVisibility(View.VISIBLE);
            filter.setVisibility(View.VISIBLE);
        }
    }

    private boolean userRegistered() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        Log.d("not_registered name", pref.getString("clientname", ""));

        if(pref.getString("clientname", "").equals(""))
            return false;
        Log.d("not_registered address", pref.getString("clientaddress", ""));
        Log.d("not_registered equals", String.valueOf(pref.getString("clientname", "").equals("")));

        if(pref.getString("clientaddress", "").equals(""))
            return false;
        Log.d("not_registered mail", pref.getString("clientmail", ""));

        if(pref.getString("clientmail", "").equals(""))
            return false;
        Log.d("not_registered phone", pref.getString("clientphone", ""));

        return !pref.getString("clientphone", "").equals("");
    }

    private void getSharedData() {}

    private void init(){
        restaurants = new ArrayList<>();
        adapter = new RestaurantAdapter(getActivity(), restaurants);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        sort.setOnClickListener(v->{
            assert getActivity() != null;
            FragmentManager fm = getActivity().getSupportFragmentManager();
            SortDialog sortDialog = new SortDialog();
            sortDialog.setFragment(this);
            if(sortType != null)
                sortDialog.setSortType(sortType);
            sortDialog.show(fm, "Sort Dialog");
        });

        filter.setOnClickListener(v->{
            assert getActivity() != null;
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FilterDialog filterDialog = new FilterDialog();
            filterDialog.setListener(this);
            if(categories != null)
                filterDialog.setCategories(categories);
            filterDialog.show(fm, "Filter Dialog");
        });
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
                String key = dataSnapshot.getKey();
                Restaurant r = dataSnapshot.getValue(Restaurant.class);
                assert key != null;
                assert r != null;
                r.setFbKey(key);
                int pos = restaurants.indexOf(r);
                restaurants.remove(r);
                restaurants.add(pos, r);
                adapter.setFullList(restaurants);
                adapter.notifyItemChanged(pos);
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
                Toast.makeText(ctx, "CIAO", Toast.LENGTH_SHORT).show();
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

    @Override
    public void handleOnDismiss(DialogType type, String text) {
        if(type == DialogType.SortDialog)
            sortDismiss(text);
        else if(type == DialogType.FilterDialog)
            filterDismiss(text);
    }
    private void reDoFilter(String text){
        filterDismiss(text);
    }
    private void filterDismiss(String text){
        restaurants.clear();


        if(text == null || text.isEmpty()){
            restaurants.addAll(adapter.getFullList());
            adapter.notifyDataSetChanged();
            categories = null;
            return;
        }else if(text.equals(REDO) && categories != null) {
            text = categories;
        }

        String[] categories = text.split(",\\s?");
        List<Restaurant> fullList = new ArrayList<>(adapter.getFullList());
        for(Restaurant r : fullList){
            for(String cat : categories)
                if(r.contains(cat)) {
                    restaurants.add(r);
                    break;
                }
        }

        adapter.notifyDataSetChanged();
        this.categories = text;
    }

    private void sortDismiss(String text) {
        if(text == null || text.isEmpty()) return;
        SortType sort = SortType.valueOf(text);
        switch (sort){
            case Alpha:
                orderAlpha();
                break;
            case PriceAsc:
                orderPriceAsc();
                break;
            case PriceDesc:
                orderPriceDesc();
                break;
            case MostRated:
                orderMostRated();
                break;
        }
    }

    private void orderMostRated() {
        Collections.sort(restaurants,
                (a,b)->b.rate() - a.rate());

        adapter.setFullList(restaurants);
        adapter.notifyDataSetChanged();

        sortType = SortType.MostRated;
    }

    private void orderPriceDesc() {
        Collections.sort(restaurants,
                (a,b)->{
                    Restaurant.PriceRange a1 = a.priceRange();
                    Restaurant.PriceRange b1 = b.priceRange();
                    int rate1 = a.rate();
                    int rate2 = b.rate();

                    return a1.equals(b1) ? rate2 - rate1 : b1.compareTo(a1);
                });

        adapter.setFullList(restaurants);
        adapter.notifyDataSetChanged();

        sortType = SortType.PriceDesc;
    }

    private void orderPriceAsc() {
        Collections.sort(restaurants,
                (a,b)->{
            Restaurant.PriceRange a1 = a.priceRange();
            Restaurant.PriceRange b1 = b.priceRange();
            int rate1 = a.rate();
            int rate2 = b.rate();

            return a1.equals(b1) ? rate2 - rate1 : a1.compareTo(b1);
        });

        adapter.setFullList(restaurants);
        adapter.notifyDataSetChanged();

        sortType = SortType.PriceAsc;
    }

    private void orderAlpha() {
        Collections.sort(restaurants,
                (a,b)->a.getName().compareTo(b.getName()));

        adapter.setFullList(restaurants);
        adapter.notifyDataSetChanged();

        sortType = SortType.Alpha;
    }
}
