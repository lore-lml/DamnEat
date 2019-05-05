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
import android.widget.Toast;

import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.adapters.OrdersAdapter;
import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
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
            if (initOrders()){
                adapter = new OrdersAdapter(orders, ctx);
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener(position -> {
                    orders.get(position).changeExpanded();
                    adapter.notifyItemChanged(position);
                });


                //ABBINAMENTO DELIVERER ORDINE
                adapter.setOnButtonClickListener(position -> {

                    Log.d("tmz","pressed find deliverer");
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    List<String> deliverers_keys = new ArrayList<>();
                    DatabaseReference dbRef= database.getReference("/deliverers_liberi/");
                    dbRef.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            for (MutableData child : mutableData.getChildren()) {
                                if (child != null) {
                                    String s = child.getValue(String.class);
                                    deliverers_keys.add(s);
                                }
                            }
                            if(deliverers_keys.size()<1)
                                return Transaction.abort();

                            String delivererKey = deliverers_keys.get(0);


                            for (MutableData child : mutableData.getChildren()) {
                                if (child != null) {
                                    String s = child.getValue(String.class);
                                    if(s.equals(delivererKey)){
                                        mutableData.setValue(null);
                                        return Transaction.success(mutableData);
                                    }
                                }
                            }
                            return Transaction.abort();
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                            if(b){
                                String delivererKey = deliverers_keys.get(0);
                                if (delivererKey == null){
                                    DatabaseReference dbOrder = database.getReference("/ordini/" + orders.get(position).getId() + "/state");
                                    dbOrder.setValue("rejected");
                                    Toast.makeText(ctx, R.string.no_availabity, Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (!refreshAvailabity(position)){
                                    DatabaseReference dbOrder = database.getReference("/ordini/" + orders.get(position).getId() + "/state");
                                    dbOrder.setValue("rejected");
                                }

                                DatabaseReference dbDeliverer = database.getReference("/deliverers/" + delivererKey + "/current_order/");
                                dbDeliverer.setValue(orders.get(position).getId());


                                DatabaseReference dbOrder = database.getReference("/ordini/" + orders.get(position).getId() + "/state");
                                dbOrder.setValue("accepted");

                                adapter.notifyItemChanged(position);
                            } else {
                                String orderKey = orders.get(position).getId();
                                DatabaseReference dbOrder = database.getReference("/ordini/" + orders.get(position).getId() + "/state");
                                dbOrder.setValue("rejected");
                                Toast.makeText(ctx, R.string.no_free_deliverers, Toast.LENGTH_LONG).show();
                            }

                        }
                    });

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


        }


    }

    private boolean refreshAvailabity(int position) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        ArrayList<Dish> dishes = new ArrayList<>();
        //AGGIORNO LE AVAILABILITY
        for (Dish dish : orders.get(position).getDishes()) {
            DatabaseReference ref = db.getReference("/ristoranti/" + orders.get(position).getRestaurant().getRestaurantID() + "/piatti_del_giorno/" + dish.getId());
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Dish dsh = dataSnapshot.getValue(Dish.class);
                    if (dsh != null) {
                        dsh = new Dish();
                        dsh.setAvailability(0);
                    }
                    dishes.add(position, dsh);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
        for(int i=0; i<dishes.size(); i++){
            int new_availability = dishes.get(i).getAvailability() - orders.get(position).getDishes().get(i).getQuantity();
            if(new_availability < 0)
                return false;
            DatabaseReference dishRef = db.getReference("ristoranti/" + orders.get(position).getRestaurant().getRestaurantID() + "/piatti_totali/" + dishes.get(i).getId() + "/availability");
            dishRef.setValue(new_availability);
            dishRef = db.getReference("ristoranti/" + orders.get(position).getRestaurant().getRestaurantID() + "/piatti_del_giorno/" + dishes.get(i).getId() + "/availability");
            dishRef.setValue(new_availability);
        }
        return true;
    }

    private boolean initOrders(){
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ristoranti/"+key+"/ordini_pendenti/");
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
                orderKeyList.clear();
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
