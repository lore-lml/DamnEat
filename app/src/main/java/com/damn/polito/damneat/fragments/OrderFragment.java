package com.damn.polito.damneat.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.damn.polito.commonresources.beans.Customer;
import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneat.adapters.DishesAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderFragment extends Fragment {
    private List<String> orderKeyList = new ArrayList<>();
    private List<Order> orderList = new ArrayList<>();
    private RecyclerView recyclerView;
    private DishesAdapter adapter;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private Customer customer = new Customer();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        customer.setCustomerID("-LdpRQn5RteW_n9G3TFn");

        init();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void init(){
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("clienti/"+ customer.getCustomerID() +"/lista_ordini/");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key;
                orderKeyList.clear();
                orderList.clear();
                for (DataSnapshot chidSnap : dataSnapshot.getChildren()) {
                    Log.d("tmz",""+ chidSnap.getKey()); //displays the key for the node
                    Log.d("tmz",""+ chidSnap.getValue());   //gives the value for given keyname
                    //DataPacket value = dataSnapshot.getValue(DataPacket.class);
                    key = chidSnap.getValue(String.class);
                    getOrderFirebase(key);
                    orderKeyList.add(key);
                }
                //adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getOrderFirebase(String key){
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ordini/"+ key);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("order", key);

                Log.d("order", dataSnapshot.getValue().toString());
                Order order = dataSnapshot.getValue(Order.class);
                orderList.add(order);
                Log.d("order", order.getCustomer().getCustomerName());
                //adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
