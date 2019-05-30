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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class RestaurantFragment extends Fragment implements HandleDismissDialog {
    public enum SortType{Alpha, PriceAsc, PriceDesc, MostRated}

    public static final int REQUEST_CODE = 9000;
    private Welcome parent;

    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;
    private LinearLayout offline;
    private TextView registered_tv;
    private ImageView registered_im;
    private Button sortButton, filterButton;
    private Context ctx;


    private List<Restaurant> restaurants;
    private List<Restaurant> filteredRestaurants;

    private SortType sortType = SortType.MostRated;
    private String categories;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        parent = (Welcome)getActivity();
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
        sortButton = view.findViewById(R.id.button_sort);
        filterButton = view.findViewById(R.id.button_filter);


        if(InternetConnection.haveInternetConnection(ctx)) {
            init();
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
            sortButton.setVisibility(View.GONE);
            filterButton.setVisibility(View.GONE);

        }else {
            registered_tv.setVisibility(View.GONE);
            registered_im.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            sortButton.setVisibility(View.VISIBLE);
            filterButton.setVisibility(View.VISIBLE);
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

    private void init(){
        restaurants = parent.getRestaurants();
        filteredRestaurants = new ArrayList<>(parent.getRestaurants());
        adapter = new RestaurantAdapter(getActivity(), filteredRestaurants, restaurants);
        orderMostRated();

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        sortButton.setOnClickListener(v->{
            assert getActivity() != null;
            FragmentManager fm = getActivity().getSupportFragmentManager();
            SortDialog sortDialog = new SortDialog();
            sortDialog.setListener(this);
            if(sortType != null)
                sortDialog.setSortType(sortType);
            sortDialog.show(fm, "Sort Dialog");
        });

        filterButton.setOnClickListener(v->{
            assert getActivity() != null;
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FilterDialog filterDialog = new FilterDialog();
            filterDialog.setListener(this);
            if(categories != null)
                filterDialog.setCategories(categories);
            filterDialog.show(fm, "Filter Dialog");
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
                return false;
            }

            @Override
            public boolean onQueryTextChange(String filterText) {
                String cat = (categories == null || categories.isEmpty()) ? "" : categories;
                adapter.getFilter().filter(cat + "\n" + filterText);
                return false;
            }
        });
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

    private void filterDismiss(String text){
        categories = text;
        adapter.getFilter().filter(((categories == null || categories.isEmpty()) ? "" : categories)+"\n");
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
        Collections.sort(filteredRestaurants);

        adapter.notifyDataSetChanged();
        sortType = SortType.MostRated;
    }

    private void orderPriceDesc() {
        Collections.sort(filteredRestaurants,
                (a,b)->{
                    if(a == b || a.equals(b))
                        return 0;

                    if(a.favorite() && !b.favorite())
                        return -1;
                    else if(!a.favorite() && b.favorite())
                        return 1;

                    Restaurant.PriceRange a1 = a.priceRange();
                    Restaurant.PriceRange b1 = b.priceRange();
                    int rate1 = a.rate();
                    int rate2 = b.rate();

                    return a1.equals(b1) ? rate2 - rate1 : b1.compareTo(a1);
                });

        adapter.notifyDataSetChanged();
        sortType = SortType.PriceDesc;
    }

    private void orderPriceAsc() {
        Collections.sort(filteredRestaurants,
                (a,b)->{
            if(a == b || a.equals(b))
                return 0;

            if(a.favorite() && !b.favorite())
                return -1;
            else if(!a.favorite() && b.favorite())
                return 1;

            Restaurant.PriceRange a1 = a.priceRange();
            Restaurant.PriceRange b1 = b.priceRange();
            int rate1 = a.rate();
            int rate2 = b.rate();

            return a1.equals(b1) ? rate2 - rate1 : a1.compareTo(b1);
        });

        adapter.notifyDataSetChanged();
        sortType = SortType.PriceAsc;
    }

    private void orderAlpha() {
        Collections.sort(filteredRestaurants,
                (a,b)->{
                    if(a == b || a.equals(b))
                        return 0;

                    if(a.favorite() && !b.favorite())
                        return -1;
                    else if(!a.favorite() && b.favorite())
                        return 1;

                    return a.getName().compareTo(b.getName());
                });

        adapter.notifyDataSetChanged();
        sortType = SortType.Alpha;
    }

    public void onChildAdded(Restaurant r){
        filteredRestaurants.add(r);
        Collections.sort(filteredRestaurants);
        adapter.notifyDataSetChanged();
    }

    public void onChildChanged(Restaurant r){
        int pos = filteredRestaurants.indexOf(r);
        if(pos == -1) return;

        filteredRestaurants.remove(r);
        filteredRestaurants.add(pos, r);
        Collections.sort(filteredRestaurants);
        adapter.notifyDataSetChanged();
    }

    public void onChildRemoved(Restaurant r) {
        int pos = filteredRestaurants.indexOf(r);
        filteredRestaurants.remove(r);
        if(pos!= -1)
            adapter.notifyItemRemoved(pos);
    }
}
