package com.damn.polito.damneatdeliver.fragments;

import android.content.Context;
import android.os.Bundle;
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

import com.damn.polito.commonresources.beans.Customer;
import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Restaurant;
import com.damn.polito.damneatdeliver.R;
import com.damn.polito.damneatdeliver.Welcome;
import com.damn.polito.damneatdeliver.adapters.OrdersAdapter;
import com.damn.polito.commonresources.beans.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;

public class OrderFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinkedList<Order> orderList = new LinkedList<>();

    private List<String> orderKeyList = new LinkedList<>();
    private OrdersAdapter adapter;
    private List<ValueEventListener> listeners = new ArrayList<>();
    private List<DatabaseReference> listeners_ref = new ArrayList<>();

    private FirebaseDatabase database;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.orders_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context ctx = view.getContext();

        AppCompatActivity activity = ((AppCompatActivity)getActivity());
        assert activity != null;
        Objects.requireNonNull(activity.getSupportActionBar()).setTitle(R.string.app_name);

        recyclerView = view.findViewById(R.id.orders_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ctx);
        recyclerView.setLayoutManager(layoutManager);
        database = FirebaseDatabase.getInstance();
       // initExample();
        init();
        recyclerView.setVisibility(View.VISIBLE);
        adapter = new OrdersAdapter(orderList, ctx);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

//        if(orders.size()>0) {
//            if (!orders.get(0).Expanded())
//                orders.get(0).changeExpanded();
//        }
        adapter.setOnItemClickListener(position -> {
//            if (position != 0)
                orderList.get(position).changeExpanded();
            adapter.notifyItemChanged(position);
        });
    }

    private void init(){
        DatabaseReference dbRef = database.getReference("deliverers/" + Welcome.getKey() + "/orders_list/");
        List<String> keyList = new LinkedList<>();
        ValueEventListener listener = dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderKeyList.clear();
                String orderKey;
                for (DataSnapshot chidSnap : dataSnapshot.getChildren()) {
                           //DataPacket value = dataSnapshot.getValue(DataPacket.class);
                    orderKey = chidSnap.getValue(String.class);
                    if(orderKey != null){
                        getOrderFirebase(orderKey);
                        orderKeyList.add(orderKey);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        listeners_ref.add(dbRef);
        listeners.add(listener);
    }

    private void getOrderFirebase(String key){
        database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("ordini/"+ key);
        ValueEventListener listener = dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Log.d("order", key);
               // Log.d("order", dataSnapshot.getValue().toString());
                Order order = dataSnapshot.getValue(Order.class);
                if(order!=null){
                    order.sId(key);
                    for(int i=0; i<orderList.size(); i++)
                        if(orderList.get(i).Id().equals(order.Id())){
                            orderList.remove(i);
                            break;
                        }
                }
                orderList.addFirst(order);
                List<Dish> tmp = new ArrayList<>();

                //Log.d("order", order.getCustomer().getCustomerName());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        listeners_ref.add(dbRef);
        listeners.add(listener);
    }


    @Override
    public void onDestroy() {
        for (int i=0; i<listeners.size(); i++){
            listeners_ref.get(i).removeEventListener(listeners.get(i));
        }
        super.onDestroy();
    }
}
