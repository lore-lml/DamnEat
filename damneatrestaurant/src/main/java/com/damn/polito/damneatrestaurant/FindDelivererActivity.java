package com.damn.polito.damneatrestaurant;

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
import com.damn.polito.commonresources.beans.Deliverer;
import com.damn.polito.commonresources.beans.Haversine;
import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.Welcome;
import com.damn.polito.damneatrestaurant.adapters.DelivererAdapter;
import com.damn.polito.commonresources.beans.Deliverer;
import com.damn.polito.damneatrestaurant.adapters.DelivererAdapter;
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

public class FindDelivererActivity extends AppCompatActivity {//extends Fragment {//implements HandleDismissDialog {

    public enum SortType{Alpha, Closer, Rating, TotDeliver}

    public static final int REQUEST_CODE = 9000;
    public static final String REDO = "REDO";

    private RecyclerView recyclerView;
    private DelivererAdapter adapter;
    private LinearLayout offline;
    private TextView registered_tv;
    private ImageView registered_im;
    private Button sort,filter;
    private Context ctx;

    private DatabaseReference dbRef;
    private ChildEventListener listener;

    private List<Deliverer> deliverers = new ArrayList<>();;

    private SortType sortType;
    private String categories;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_deliverer);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.deliverer_recycler);
        recyclerView.setHasFixedSize(true);
        adapter = new DelivererAdapter(ctx, deliverers);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        init();
        loadData();
    }
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        setHasOptionsMenu(true);
//        return inflater.inflate(R.layout.activity_find_deliverer, container, false);
//    }
//
//    @Override
//    public void onCreate(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        AppCompatActivity activity = ((AppCompatActivity)getActivity());
//        assert activity != null;
//        Objects.requireNonNull(activity.getSupportActionBar()).setTitle(R.string.app_name);
//
//        ctx = getContext();
//        assert ctx != null;
//
//        offline = view.findViewById(R.id.restaurant_offline);
//        registered_tv = view.findViewById(R.id.not_registered_tv);
//        registered_im = view.findViewById(R.id.not_registered_im);
//        recyclerView = view.findViewById(R.id.restaurant_recycler);
//        sort = view.findViewById(R.id.button_sort);
//        filter = view.findViewById(R.id.button_filter);
//
//        if(InternetConnection.haveInternetConnection(ctx)) {
//            init();
//            loadData();
//            offline.setVisibility(View.GONE);
//        }else{
//            offline.setVisibility(View.VISIBLE);
//        }
//        Log.d("not_registered reg", String.valueOf(userRegistered()));
//        if(!Welcome.accountExist){
//            registered_tv.setVisibility(View.VISIBLE);
//            registered_im.setVisibility(View.VISIBLE);
//            offline.setVisibility(View.GONE);
//            recyclerView.setVisibility(View.GONE);
//            sort.setVisibility(View.GONE);
//            filter.setVisibility(View.GONE);
//
//        }else {
//            registered_tv.setVisibility(View.GONE);
//            registered_im.setVisibility(View.GONE);
//            recyclerView.setVisibility(View.VISIBLE);
//            sort.setVisibility(View.VISIBLE);
//            filter.setVisibility(View.VISIBLE);
//        }
//    }

//    private boolean userRegistered() {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
//        Log.d("not_registered name", pref.getString("clientname", ""));
//
//        if(pref.getString("clientname", "").equals(""))
//            return false;
//        Log.d("not_registered address", pref.getString("clientaddress", ""));
//        Log.d("not_registered equals", String.valueOf(pref.getString("clientname", "").equals("")));
//
//        if(pref.getString("clientaddress", "").equals(""))
//            return false;
//        Log.d("not_registered mail", pref.getString("clientmail", ""));
//
//        if(pref.getString("clientmail", "").equals(""))
//            return false;
//        Log.d("not_registered phone", pref.getString("clientphone", ""));
//
//        return !pref.getString("clientphone", "").equals("");
//    }

    private void getSharedData() {}

    private void init(){
//        sort.setOnClickListener(v->{
//            assert getActivity() != null;
//            FragmentManager fm = getActivity().getSupportFragmentManager();
//            SortDialog sortDialog = new SortDialog();
//            sortDialog.setFragment(this);
//            if(sortType != null)
//                sortDialog.setSortType(sortType);
//            sortDialog.show(fm, "Sort Dialog");
//        });
//
//        filter.setOnClickListener(v->{
//            assert getActivity() != null;
//            FragmentManager fm = getActivity().getSupportFragmentManager();
//            FilterDialog filterDialog = new FilterDialog();
//            filterDialog.setListener(this);
//            if(categories != null)
//                filterDialog.setCategories(categories);
//            filterDialog.show(fm, "Filter Dialog");
//        });
    }

    private void loadData() {
        dbRef = FirebaseDatabase.getInstance().getReference("deliverers_liberi/");

        listener = dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                Deliverer d = dataSnapshot.getValue(Deliverer.class);
                assert key != null;
                assert d != null;
                d.setKey(key);
                deliverers.add(d);
                adapter.setFullList(deliverers);
                adapter.notifyItemInserted(deliverers.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                Deliverer d = dataSnapshot.getValue(Deliverer.class);
                assert key != null;
                assert d != null;
                d.setKey(key);
                int pos = deliverers.indexOf(d);
                deliverers.remove(d);
                deliverers.add(pos, d);
                adapter.setFullList(deliverers);
                adapter.notifyItemChanged(pos);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                Deliverer d = dataSnapshot.getValue(Deliverer.class);
                assert key != null;
                assert d != null;
                d.setKey(key);
                int pos = deliverers.indexOf(d);
                deliverers.remove(d);
                adapter.setFullList(deliverers);
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

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)item.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String s) {
//                Toast.makeText(ctx, "CIAO", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String filterText) {
//                adapter.getFilter().filter(filterText);
//                return false;
//            }
//
//        });
    }

    public void onDestroy() {
        super.onDestroy();
        if(listener!=null)
            dbRef.removeEventListener(listener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            int pos = requestCode-REQUEST_CODE;
            if(pos < 0 || pos > deliverers.size()) return;

            /*TODO: gestire stato ordine effettuato*/
        }
    }

//    @Override
//    public void handleOnDismiss(DialogType type, String text) {
//        if(type == DialogType.SortDialog)
//            sortDismiss(text);
//        else if(type == DialogType.FilterDialog)
//            filterDismiss(text);
//    }
//    private void reDoFilter(String text){
//        filterDismiss(text);
//    }
//    private void filterDismiss(String text){
//        deliverers.clear();
//
//
//        if(text == null || text.isEmpty()){
//            deliverers.addAll(adapter.getFullList());
//            adapter.notifyDataSetChanged();
//            categories = null;
//            return;
//        }else if(text.equals(REDO) && categories != null) {
//            text = categories;
//        }
//
//        String[] categories = text.split(",\\s?");
//        List<Deliverer> fullList = new ArrayList<>(adapter.getFullList());
//        for(Deliverer d : fullList){
//            for(String cat : categories)
//                if(d.contains(cat)) {
//                    deliverers.add(d);
//                    break;
//                }
//        }
//
//        adapter.notifyDataSetChanged();
//        this.categories = text;
//    }

    private void sortDismiss(String text) {
        if(text == null || text.isEmpty()) return;
        SortType sort = SortType.valueOf(text);
        switch (sort){
            case Alpha:
                orderAlpha();
                break;
            case Closer:
                orderClosest();
                break;
            case Rating:
                orderRating();
                break;
            case TotDeliver:
                orderTotDeliveries();
                break;
        }
    }

    private void orderAlpha() {
        Collections.sort(deliverers,
                (a,b)->a.getName().compareTo(b.getName()));

        adapter.setFullList(deliverers);
        adapter.notifyDataSetChanged();

        sortType = SortType.Alpha;
    }

    private void orderClosest() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Bundle extras = getIntent().getExtras();
        String resturant_key = extras.getString("resturant_key");
        DatabaseReference latitude = database.getReference("ristoranti/" + resturant_key + "/Coordinate/Latitude");
        DatabaseReference longitude = database.getReference("ristoranti/" + resturant_key + "/Coordinate/Longitude");

//        Collections.sort(deliverers,
//                (a,b)-> Haversine.distance(b.getLatitude(), b.getLongitude(), latitude.val, longitude.)
//                      - Haversine.distance(a.getLatitude(), a.getLongitude(), restauarant.getLatitude(), restaurant.getLongitude())
//        );

        adapter.setFullList(deliverers);
        adapter.notifyDataSetChanged();

        sortType = SortType.Closer;
    }

    private void orderRating() {
//        Collections.sort(deliverers,
//                (a,b)->b.rate() - a.rate());
//
//        adapter.setFullList(deliverers);
//        adapter.notifyDataSetChanged();
//
//        sortType = SortType.Rating;
    }

    private void orderTotDeliveries() {
//        Collections.sort(deliverers,
//                (a,b)->b.totDeliveries() - a.totDeliveries());
//
//        adapter.setFullList(deliverers);
//        adapter.notifyDataSetChanged();
//
//        sortType = SortType.TotDeliver;
    }
}
