package com.damn.polito.damneatrestaurant.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneatrestaurant.FindDelivererActivity;
import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.Welcome;
import com.damn.polito.damneatrestaurant.adapters.OrdersAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class OrderFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Order> orders;
    private List<String> orderKeyList = new ArrayList<>();
    private OrdersAdapter adapter;
    private Context ctx;
    private String key;
    private FirebaseDatabase database;
    private DatabaseReference dbRef, delFreeRef;
    private List<String> deliverersFree = new ArrayList<>();
    private ChildEventListener delFreeListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.orders_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ctx = getActivity();

        AppCompatActivity activity = ((AppCompatActivity)getActivity());
        assert activity != null;
        Objects.requireNonNull(activity.getSupportActionBar()).setTitle(R.string.nav_reservations);

        recyclerView = view.findViewById(R.id.orders_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        initFreeDeliveresListener();
        
        key = Welcome.getDbKey();
        if (key != null && !key.isEmpty()) {
            orders = ((Welcome)ctx).getOrders();
            adapter = new OrdersAdapter(orders, ctx);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);

            adapter.setOnItemClickListener(position -> {
                orders.get(position).changeExpanded();
                adapter.notifyItemChanged(position);
            });

            setOrdersStateBehaviour();
        }
    }

    private void setOrdersStateBehaviour() {
        //ABBINAMENTO DELIVERER ORDINE
        adapter.setOnButtonClickListener(position -> {

            //Controlla che il cliente abbia dei dati
            if(orders.get(position).getCustomer().getCustomerName().equals("") || orders.get(position).getCustomer().getCustomerAddress().equals("")){
                DatabaseReference dbOrder = database.getReference("/ordini/" + orders.get(position).getId() + "/state");
                dbOrder.setValue("rejected");
                Toast.makeText(ctx, R.string.no_customer_info, Toast.LENGTH_LONG).show();
            }else {
                Log.d("tmz", "pressed find deliverer");
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                Intent intent = new Intent(ctx, FindDelivererActivity.class);
                intent.putExtra("order", orders.get(position));
                startActivity(intent);
            }
        });

        //SET BUTTON AS SHIPPED
        adapter.setOnButtonShippedClickListener(position -> {

            Log.d("tmz","pressed set as shipped");
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference dbRef= database.getReference("/ordini/"+orders.get(position).getId()+"/state/");
            dbRef.setValue("shipped");

            adapter.notifyItemChanged(position);
        });
        //END SET BUTTON AS SHIPPED

        //SET BUTTON AS REJECTED

        adapter.setOnButtonRejectedClickListener(position -> {

            Log.d("tmz","pressed set as rejected");
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference dbRef= database.getReference("/ordini/"+orders.get(position).getId()+"/state/");
            dbRef.setValue("rejected");

            adapter.notifyItemChanged(position);
        });
        //END SET BUTTON AS REJECTED
    }

    private void initFreeDeliveresListener() {
        delFreeRef = FirebaseDatabase.getInstance().getReference("/deliverers_liberi/");
        delFreeListener = delFreeRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue() != null)
                    deliverersFree.add(dataSnapshot.getValue(String.class));
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null)
                    deliverersFree.remove(dataSnapshot.getValue(String.class));
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    public void onChildAdded() {
        adapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);
    }

    public void onChildChanged(){
        adapter.notifyItemChanged(0);
        recyclerView.smoothScrollToPosition(0);
    }

    public void onChildRemoved(int pos) {
        adapter.notifyItemRemoved(pos);
    }
}
