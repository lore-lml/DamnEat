package com.damn.polito.damneatrestaurant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.TextView;

import com.damn.polito.damneatrestaurant.adapters.RecyclerAdapter;
import com.damn.polito.damneatrestaurant.beans.Order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderActivity extends Activity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Order> orders;
    private RecyclerAdapter adapter;
    private TextView mTextMessage;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        recyclerView=findViewById(R.id.orders_recyclerview);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        List<String> tmp=new ArrayList<>();
        tmp.add("pizza");
        tmp.add("pasta");
        tmp.add("mandolino");
        tmp.add("pomodoro");
        orders= new ArrayList<>();
        orders.add(new Order(123121,tmp,new Date(1994,8,6,15,12),"via pastrengo 5","Osvaldo Osvaldi","IO",10.5));
        orders.add(new Order(456551,tmp,new Date(1998,7,6,15,12),"via pastrengo 180","Paperino","LEI",10.5));
        orders.add(new Order(454542,tmp,new Date(1995,7,6,15,12),"via pastrengo 8","Gigi","TU",10.5));
        orders.add(new Order(845663,tmp,new Date(1996,8,4,15,12),"via pastrengo 1","Steve","ESSI",10.5));
        orders.add(new Order(895241,tmp,new Date(1997,7,6,15,12),"via duca 9","Pippo","LUI",10.5));
        orders.add(new Order(123121,tmp,new Date(1994,8,6,15,12),"via pastrengo 5","Osvaldo Osvaldi","IO",10.5));
        orders.add(new Order(456551,tmp,new Date(1998,7,6,15,12),"via pastrengo 180","Paperino","LEI",10.5));
        orders.add(new Order(454542,tmp,new Date(1995,7,6,15,12),"via pastrengo 8","Gigi","TU",10.5));
        orders.add(new Order(845663,tmp,new Date(1996,8,4,15,12),"via pastrengo 1","Steve","ESSI",10.5));
        orders.add(new Order(895241,tmp,new Date(1997,7,6,15,12),"via duca 9","Pippo","LUI",10.5));

        adapter = new RecyclerAdapter(orders);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.nav_reservations);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_menu:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.nav_reservations:
                    /*mTextMessage.setText(R.string.title_dashboard);*/
                    Intent intent2 = new Intent(OrderActivity.this, OrderActivity.class);
                    startActivity(intent2);
                    return true;
                case R.id.nav_profile:
                    //mTextMessage.setText(R.string.title_notifications);
                    Intent intent3 = new Intent(OrderActivity.this, Profile.class);
                    startActivity(intent3);
                    return true;
            }
            return false;
        }
    };
}
