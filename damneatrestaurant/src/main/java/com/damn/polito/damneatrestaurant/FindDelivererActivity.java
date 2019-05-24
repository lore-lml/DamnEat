package com.damn.polito.damneatrestaurant;

import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.damn.polito.commonresources.beans.Deliverer;
import com.damn.polito.commonresources.beans.Haversine;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneatrestaurant.adapters.DelivererAdapter;
import com.damn.polito.damneatrestaurant.dialogs.DialogType;
import com.damn.polito.damneatrestaurant.dialogs.HandleDismissDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.damn.polito.commonresources.Utility.showWarning;

public class    FindDelivererActivity extends AppCompatActivity {

    public enum SortType {Alpha, Closer, Rating, TotDeliver}

    private static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int REQUEST_CODE = 9000;

    private RecyclerView recyclerView;
    private DelivererAdapter adapter;
    private Button map;

    private DatabaseReference freeDelRef;
    private Query freeDeliverersQuery;
    private ValueEventListener freeDelKeyListener, freeDelivererListener;
    public static List<Deliverer> deliverers = new ArrayList<>();

    private SortType sortType;
    private int oldPosition = -1;
    private Order currentOrder;
    private Address restAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_deliverer);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.deliverer_recycler);
        recyclerView.setHasFixedSize(true);

        loadData();

        adapter = new DelivererAdapter(this, deliverers, currentOrder);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        map = findViewById(R.id.button_map);

        init();
    }

    public static List<Deliverer> getDeliverers() {
        return  deliverers;
    }

    //Menu click Listener
    private void init() {
        if(isServicesOk()) {
            map.setOnClickListener(v -> {
                Intent intent = new Intent(FindDelivererActivity.this, MapsActivity.class);
                intent.putExtra("deliverers", (Serializable) deliverers);
                startActivity(intent);
            });
        }else{
            //todo: tradurre
            map.setOnClickListener(v-> Toast.makeText(this, "Unable to open maps", Toast.LENGTH_SHORT).show());
        }
        adapter.setOnItemClickListener(position -> {
            if (oldPosition >= 0) {
                deliverers.get(oldPosition).changeExpanded();
                adapter.notifyItemChanged(oldPosition);
            }
            deliverers.get(position).changeExpanded();
            oldPosition = position;
            adapter.notifyItemChanged(position);
        });

    }

    private void loadData(){
        currentOrder = (Order) getIntent().getSerializableExtra("order");
        if(currentOrder == null)
            //todo: tradurre
            throw new IllegalStateException("You must pass an order in the intent with \"order\" key");

        Geocoder coder = new Geocoder(this);
        try {
            restAddress = coder.getFromLocationName(currentOrder.getRestaurant().getRestaurantAddress(), 1).get(0);
        } catch (IOException e) {
            //todo: tradurre
            Toast.makeText(this, "Network Error! Try again later", Toast.LENGTH_SHORT).show();
        }
        assert restAddress != null;

        freeDelRef = FirebaseDatabase.getInstance().getReference("deliverers_liberi/");
        freeDelKeyListener = freeDelRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    deliverers.clear();
                    return;
                }
                List<String> keys = new ArrayList<>();
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    if(child.getValue() == null) continue;
                    keys.add(child.getValue(String.class));
                }

                getDelivererFireBase(keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FindDelivererActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getDelivererFireBase(List<String> keys) {
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("deliverers/");
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;

                deliverers.clear();
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    if(child.getValue() == null) continue;
                    if(child.getKey() != null && keys.contains(child.getKey())){
                        Deliverer d = child.child("info").getValue(Deliverer.class);
                        assert d != null;
                        d.setKey(child.getKey());
                        if(d.getName()!=null && d.getLatitude()!=null && d.getLongitude()!=null && restAddress!=null){
                            d.setDistance((int)(Haversine.distance(restAddress.getLatitude(), restAddress.getLongitude(), d.getLatitude(), d.getLongitude())*1000));
                            deliverers.add(d);
                        }
                    }
                }
                Collections.sort(deliverers, (d1, d2) -> {
                    if(restAddress != null)
                        return d1.distance() - d2.distance();

                    return Integer.MAX_VALUE;
                });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FindDelivererActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        if(freeDeliverersQuery != null)
            freeDeliverersQuery.removeEventListener(freeDelivererListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            int pos = requestCode - REQUEST_CODE;
            if (pos < 0 || pos > deliverers.size()) return;

            /*TODO: gestire stato ordine effettuato*/
        }
    }

    public boolean isServicesOk() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(FindDelivererActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(FindDelivererActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            //todo: tradurre
            Toast.makeText(this, "Impossible to map request", Toast.LENGTH_LONG).show();
        }
        return false;
    }

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
