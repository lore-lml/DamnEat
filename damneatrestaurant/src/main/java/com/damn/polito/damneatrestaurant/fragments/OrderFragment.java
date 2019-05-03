package com.damn.polito.damneatrestaurant.fragments;

import android.content.Context;
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

import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.adapters.OrdersAdapter;
import com.damn.polito.commonresources.beans.Dish;
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
import java.util.Map;
import java.util.Objects;

public class OrderFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private LinkedList<Order> orders = new LinkedList<>();
    private List<String> orderKeyList = new ArrayList<>();
    private OrdersAdapter adapter;
    private TextView mTextMessage;
    private Context ctx;
    private String key;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.orders_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ctx = view.getContext();

        AppCompatActivity activity = ((AppCompatActivity)getActivity());
        assert activity != null;
        Objects.requireNonNull(activity.getSupportActionBar()).setTitle(R.string.app_name);

        recyclerView = view.findViewById(R.id.orders_recyclerview);
        layoutManager = new LinearLayoutManager(ctx);
        recyclerView.setLayoutManager(layoutManager);

        // OTTENGO L'ID DALLE SHARED PREF
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        String s = pref.getString("dbkey", null);
        if (s != null|| !s.isEmpty()) {

            key = stringOrDefault(s);
            if (initOrders()==true){
                adapter = new OrdersAdapter(orders, ctx);
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener(position -> {
                    orders.get(position).changeExpanded();
                    adapter.notifyItemChanged(position);
                });
            }


        }
        //initExample();


    }

    private void initExample(){
        orders.clear();
        List<Dish> tmp = new ArrayList<>();
        tmp.add(new Dish("Gelato", "Un qualcosa ancora più buono", 3,15));
        tmp.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50,3));

        List<Dish> tmp2 = new ArrayList<>();
        tmp2.add(new Dish("Pizza", "Chi non conosce la pizza??", 6,20));
        tmp2.add(new Dish("Carbonara", "Un piatto buonissimo", 7,10));
        tmp2.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50,3));

        List<Dish> tmp3 = new ArrayList<>();
        tmp3.add(new Dish("Pizza", "Chi non conosce la pizza??", 6,20));
        tmp3.add(new Dish("Gelato", "Un qualcosa ancora più buono", 3,15));
        tmp3.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50,3));
        tmp3.add(new Dish("Gelato più grosso", "Un gelato ma più grosso", (float) 6.50,10));

        List<Dish> tmp4 = new ArrayList<>();
        tmp4.add(new Dish("Pizza", "Chi non conosce la pizza??", 6,20));
        tmp4.add(new Dish("Gelato", "Un qualcosa ancora più buono", 3,15));
        tmp4.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50,3));
        tmp4.add(new Dish("Gelato più grosso", "Un gelato ma più grosso", (float) 6.50,10));
        tmp4.add(new Dish("Gelato", "Un qualcosa ancora più buono", 3,15));
        tmp4.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50,3));
        tmp4.add(new Dish("Gelato più grosso", "Un gelato ma più grosso", (float) 6.50,10));
        tmp4.add(new Dish("Pizza", "Chi non conosce la pizza??", 6,20));
        tmp4.add(new Dish("Gelato", "Un qualcosa ancora più buono", 3,15));
        tmp4.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50,3));
        tmp4.add(new Dish("Gelato più grosso", "Un gelato ma più grosso", (float) 6.50,10));

//
//        orders.add(new Order(123121, tmp, new Date(), "Via Pastrengo 5", "Osvaldo Osvaldi", "Mario Rossi", 10.5));
//        orders.add(new Order(456551, tmp2, new Date(), "Via Pastrengo 180", "Paperino", "Luigi Bianchi", 10.5));
//        orders.add(new Order(454542, tmp3, new Date(), "Via Pastrengo 8", "Gigi", "Marco Verdi", 10.5));
//        orders.add(new Order(845663, tmp2, new Date(), "Via pastrengo 1", "Steve", "Francesco Gialli", 10.5));
//        orders.add(new Order(895241, tmp, new Date(), "Corso Duca 9", "Pippo", "Stefano Arancioni", 10.5));
//        orders.add(new Order(123121, tmp4, new Date(), "Via pastrengo 5", "Osvaldo Osvaldi", "Giuseppe Blu", 10.5));
//        orders.add(new Order(456551, tmp2, new Date(), "Via pastrengo 180", "Paperino", "Gianfranco Neri", 10.5));
//        orders.add(new Order(454542, tmp3, new Date(), "Via pastrengo 8", "Gigi", "Lorenzo Viola", 10.5));
//        orders.add(new Order(845663, tmp, new Date(), "Via pastrengo 1", "Steve", "Matteo Azzurri", 10.5));
//        orders.add(new Order(895241, tmp, new Date(), "Corso Duca 9", "Pippo", "Alessandro Rosa", 10.5));

    }

    private boolean initOrders(){
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ristoranti/"+key+"/ordini_pendenti/");
        if (dbRef==null) return false;
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key;
                orderKeyList.clear();
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
        return true;
    }

    private void getOrderFirebase(String key){
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ordini/"+ key);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Log.d("order", key);

                //Log.d("order", dataSnapshot.getValue().toString());
                Order order = dataSnapshot.getValue(Order.class);
                if(order!=null){
                    order.sId(key);
                    for(int i=0; i<orders.size(); i++)
                        if(orders.get(i).Id().equals(order.Id())){
                            orders.remove(i);
                            break;
                        }
                }
                orders.addFirst(order);
                //Log.d("order", order.getCustomer().getCustomerName());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public String stringOrDefault(String s) {
        return (s == null || s.trim().isEmpty()) ? "" : s;
    }
}
