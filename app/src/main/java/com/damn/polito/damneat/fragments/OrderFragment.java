package com.damn.polito.damneat.fragments;

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

import com.damn.polito.commonresources.beans.Customer;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneat.R;
import com.damn.polito.damneat.Welcome;
import com.damn.polito.damneat.adapters.OrdersAdapter;
import com.damn.polito.damneat.dialogs.DialogType;
import com.damn.polito.damneat.dialogs.HandleDismissDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class OrderFragment extends Fragment {

    private Welcome parent;
    private List<Order> orderList;
    private RecyclerView recyclerView;
    private OrdersAdapter adapter;
    private Context ctx;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        parent = (Welcome)getActivity();
        return inflater.inflate(R.layout.orders_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = ((AppCompatActivity)getActivity());
        assert activity != null;
        Objects.requireNonNull(activity.getSupportActionBar()).setTitle(R.string.nav_reservations);

        ctx = getContext();
        assert ctx != null;
        orderList = parent.getOrders();
        initReyclerView(view);

    }

    private void initReyclerView(View view){
        recyclerView = view.findViewById(R.id.orders_recyclerview);
        adapter = new OrdersAdapter(orderList, ctx);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
    }

    public void onChildAdded() {
        adapter.notifyItemInserted(0);
    }

    public void onChildChanged(int pos, boolean rateChanged){
        if(rateChanged) {
            adapter.notifyItemChanged(pos);
            return;
        }

        adapter.notifyDataSetChanged();
    }

    public void onChildRemoved(int pos) {
        adapter.notifyItemRemoved(pos);
    }
}
