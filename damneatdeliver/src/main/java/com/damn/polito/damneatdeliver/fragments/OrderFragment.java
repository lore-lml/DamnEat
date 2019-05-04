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
import android.widget.TextView;

import com.damn.polito.damneatdeliver.R;
import com.damn.polito.damneatdeliver.adapters.OrdersAdapter;
import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class OrderFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Order> orders = new ArrayList<>();
    private OrdersAdapter adapter;

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

       // initExample();
        init();

        adapter = new OrdersAdapter(orders, ctx);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

//        if(orders.size()>0) {
//            if (!orders.get(0).Expanded())
//                orders.get(0).changeExpanded();
//        }
        adapter.setOnItemClickListener(position -> {
//            if (position != 0)
                orders.get(position).changeExpanded();
            adapter.notifyItemChanged(position);
        });
    }

    private void init(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("ordini");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key;
                Order order;
                orders.clear();
                for (DataSnapshot chidSnap : dataSnapshot.getChildren()) {
                    Log.d("tmz",""+ chidSnap.getKey()); //displays the key for the node
                    Log.d("tmz",""+ chidSnap.getValue());   //gives the value for given keyname
                    //DataPacket value = dataSnapshot.getValue(DataPacket.class);
                    key = chidSnap.getKey();
                    order = chidSnap.getValue(Order.class);
                    orders.add(order);
                    orders.get(orders.size()-1).sId(key);
                }
                adapter.notifyDataSetChanged();

                if(orders.size() == 0)  recyclerView.setVisibility(View.GONE);
                else {
//                    if(!orders.get(0).Expanded()) orders.get(0).changeExpanded();
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }

    private void initExample() {
        orders.clear();
        List<Dish> tmp = new ArrayList<>();
        tmp.add(new Dish("Gelato", "Un qualcosa ancora più buono", 3, 15));
        tmp.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50, 3));

        List<Dish> tmp2 = new ArrayList<>();
        tmp2.add(new Dish("Pizza", "Chi non conosce la pizza??", 6, 20));
        tmp2.add(new Dish("Carbonara", "Un piatto buonissimo", 7, 10));
        tmp2.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50, 3));

        List<Dish> tmp3 = new ArrayList<>();
        tmp3.add(new Dish("Pizza", "Chi non conosce la pizza??", 6, 20));
        tmp3.add(new Dish("Gelato", "Un qualcosa ancora più buono", 3, 15));
        tmp3.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50, 3));
        tmp3.add(new Dish("Gelato più grosso", "Un gelato ma più grosso", (float) 6.50, 10));

        List<Dish> tmp4 = new ArrayList<>();
        tmp4.add(new Dish("Pizza", "Chi non conosce la pizza??", 6, 20));
        tmp4.add(new Dish("Gelato", "Un qualcosa ancora più buono", 3, 15));
        tmp4.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50, 3));
        tmp4.add(new Dish("Gelato più grosso", "Un gelato ma più grosso", (float) 6.50, 10));
        tmp4.add(new Dish("Gelato", "Un qualcosa ancora più buono", 3, 15));
        tmp4.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50, 3));
        tmp4.add(new Dish("Gelato più grosso", "Un gelato ma più grosso", (float) 6.50, 10));
        tmp4.add(new Dish("Pizza", "Chi non conosce la pizza??", 6, 20));
        tmp4.add(new Dish("Gelato", "Un qualcosa ancora più buono", 3, 15));
        tmp4.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50, 3));
        tmp4.add(new Dish("Gelato più grosso", "Un gelato ma più grosso", (float) 6.50, 10));


        orders.add(new Order("123121", tmp, new Date(), "Via Pastrengo 5", "Osvaldo Osvaldi", "Mario Rossi", 10.5));
        orders.add(new Order("456551", tmp2, new Date(), "Via Pastrengo 180", "Paperino", "Luigi Bianchi", 10.5));
        orders.add(new Order("454542", tmp3, new Date(), "Via Pastrengo 8", "Gigi", "Marco Verdi", 10.5));
        orders.add(new Order("845663", tmp2, new Date(), "Via pastrengo 1", "Steve", "Francesco Gialli", 10.5));
        orders.add(new Order("895241", tmp, new Date(), "Corso Duca 9", "Pippo", "Stefano Arancioni", 10.5));
        orders.add(new Order("123121", tmp4, new Date(), "Via pastrengo 5", "Osvaldo Osvaldi", "Giuseppe Blu", 10.5));
        orders.add(new Order("456551", tmp2, new Date(), "Via pastrengo 180", "Paperino", "Gianfranco Neri", 10.5));
        orders.add(new Order("454542", tmp3, new Date(), "Via pastrengo 8", "Gigi", "Lorenzo Viola", 10.5));
        orders.add(new Order("845663", tmp, new Date(), "Via pastrengo 1", "Steve", "Matteo Azzurri", 10.5));
        orders.add(new Order("895241", tmp, new Date(), "Corso Duca 9", "Pippo", "Alessandro Rosa", 10.5));
    }
}
