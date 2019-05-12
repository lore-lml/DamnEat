package com.damn.polito.damneatrestaurant;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.damn.polito.commonresources.beans.Deliverer;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneatrestaurant.adapters.DelivererAdapter;
import com.damn.polito.damneatrestaurant.dialogs.DialogType;
import com.damn.polito.damneatrestaurant.dialogs.HandleDismissDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FindDelivererActivity extends AppCompatActivity implements HandleDismissDialog {

    public enum SortType{Alpha, Closer, Rating, TotDeliver}

    public static final int REQUEST_CODE = 9000;
    public static final String REDO = "REDO";

    private RecyclerView recyclerView;
    private DelivererAdapter adapter;
    private Button sort,map;
    private Context ctx;

    private DatabaseReference freeDelRef;
    private List<Deliverer> deliverers = new ArrayList<>();

    private SortType sortType;
    private String categories;

    private int oldPosition = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_deliverer);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.deliverer_recycler);
        recyclerView.setHasFixedSize(true);

        loadData();
        Order order = (Order) getIntent().getSerializableExtra("order");

        adapter = new DelivererAdapter(this, deliverers, order);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        sort = findViewById(R.id.button_sort);
        map = findViewById(R.id.button_map);

        init();

        adapter.setOnItemClickListener(position -> {
            if (oldPosition >= 0) {
                deliverers.get(oldPosition).changeExpanded();
                oldPosition = position;
                adapter.notifyItemChanged(oldPosition);
            }
            deliverers.get(position).changeExpanded();
            adapter.notifyItemChanged(position);
        });
    }

    //Menu click Listener
    private void init(){
        sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog d = new Dialog(FindDelivererActivity.this);
                d.setContentView(R.layout.dialog_sort);
                d.show();
            }
        });

        //ESEMPIO PER SETONCLICKLISTENER SOPRA
//        sort.setOnClickListener(v->{
//            assert getActivity() != null;
//            FragmentManager fm = getActivity().getSupportFragmentManager();
//            SortDialog sortDialog = new SortDialog();
//            sortDialog.setFragment(this);
//            if(sortType != null)
//                sortDialog.setSortType(sortType);
//            sortDialog.show(fm, "Sort Dialog");
//        });

        map.setOnClickListener(v->{
            Toast.makeText(this, "Open Map", Toast.LENGTH_LONG).show();
//           TODO: Start the map activity to show the deliverers positions
        });
    }

    private void loadData() {
        freeDelRef = FirebaseDatabase.getInstance().getReference("deliverers_liberi/");
        ValueEventListener listener = freeDelRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                deliverers.clear();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    String key = d.getValue(String.class);
                    if (key != null) {
                        getDelivererFireBase(key);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ctx, "Somethings is wrong", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getDelivererFireBase(String key) {
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("deliverers/" + key );
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Deliverer d = dataSnapshot.getValue(Deliverer.class);
                assert d != null;
                if (d.getName() != null) {
                    deliverers.add(d);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ctx, "Somethings is wrong", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
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

    @Override
    public void handleOnDismiss(DialogType type, String text) {
        if(type == DialogType.SortDialog)
            sortDismiss(text);
    }

    private void sortDismiss(String text) {
        if(text == null || text.isEmpty()) return;
        SortType sort = SortType.valueOf(text);
        switch (sort){
            case Alpha:
                sortAlpha();
                break;
            case Closer:
                sortClosest();
                break;
            case Rating:
                sortRating();
                break;
            case TotDeliver:
                sortTotDeliveries();
                break;
        }
    }

    private void sortAlpha() {
        Collections.sort(deliverers,
                (a,b)->a.getName().compareTo(b.getName()));

        adapter.notifyDataSetChanged();
        sortType = SortType.Alpha;
    }

    private void sortClosest() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Bundle extras = getIntent().getExtras();
        String resturant_key = extras.getString("resturant_key");
        DatabaseReference latitude = database.getReference("ristoranti/" + resturant_key + "/Coordinate/Latitude");
        DatabaseReference longitude = database.getReference("ristoranti/" + resturant_key + "/Coordinate/Longitude");

//        Collections.sort(deliverers,
//                (a,b)-> Haversine.distance(b.getLatitude(), b.getLongitude(), latitude.val, longitude.)
//                      - Haversine.distance(a.getLatitude(), a.getLongitude(), restauarant.getLatitude(), restaurant.getLongitude())
//        );

        adapter.notifyDataSetChanged();

        sortType = SortType.Closer;
    }

    private void sortRating() {
//        Collections.sort(deliverers,
//                (a,b)->b.rate() - a.rate());
//
//        adapter.setFullList(deliverers);
//        adapter.notifyDataSetChanged();
//
//        sortType = SortType.Rating;
    }

    private void sortTotDeliveries() {
//        Collections.sort(deliverers,
//                (a,b)->b.totDeliveries() - a.totDeliveries());
//
//        adapter.setFullList(deliverers);
//        adapter.notifyDataSetChanged();
//
//        sortType = SortType.TotDeliver;
    }
}
