package com.damn.polito.damneatrestaurant.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.adapters.OrdersAdapter;
import com.damn.polito.damneatrestaurant.beans.Order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class OrderFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Order> orders;
    private OrdersAdapter adapter;
    private TextView mTextMessage;
    private Context ctx;

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
        List<String> tmp = new ArrayList<>();
        tmp.add("pizza");
        tmp.add("pasta");
        tmp.add("mandolino");
        tmp.add("pomodoro");
        orders = new ArrayList<>();
        orders.add(new Order(123121, tmp, new Date(1994, 8, 6, 15, 12), "via pastrengo 5", "Osvaldo Osvaldi", "IO", 10.5));
        orders.add(new Order(456551, tmp, new Date(1998, 7, 6, 15, 12), "via pastrengo 180", "Paperino", "LEI", 10.5));
        orders.add(new Order(454542, tmp, new Date(1995, 7, 6, 15, 12), "via pastrengo 8", "Gigi", "TU", 10.5));
        orders.add(new Order(845663, tmp, new Date(1996, 8, 4, 15, 12), "via pastrengo 1", "Steve", "ESSI", 10.5));
        orders.add(new Order(895241, tmp, new Date(1997, 7, 6, 15, 12), "via duca 9", "Pippo", "LUI", 10.5));
        orders.add(new Order(123121, tmp, new Date(1994, 8, 6, 15, 12), "via pastrengo 5", "Osvaldo Osvaldi", "IO", 10.5));
        orders.add(new Order(456551, tmp, new Date(1998, 7, 6, 15, 12), "via pastrengo 180", "Paperino", "LEI", 10.5));
        orders.add(new Order(454542, tmp, new Date(1995, 7, 6, 15, 12), "via pastrengo 8", "Gigi", "TU", 10.5));
        orders.add(new Order(845663, tmp, new Date(1996, 8, 4, 15, 12), "via pastrengo 1", "Steve", "ESSI", 10.5));
        orders.add(new Order(895241, tmp, new Date(1997, 7, 6, 15, 12), "via duca 9", "Pippo", "LUI", 10.5));

        adapter = new OrdersAdapter(orders, ctx);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }
}
