package com.damn.polito.damneatrestaurant;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class Welcome extends AppCompatActivity {

    private TextView mTextMessage;
    Intent i;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.nav_dishes:
                        // a scopo di test
                        i = new Intent(this, TestDishes.class);
                        startActivity(i);
                        return true;
                    case R.id.nav_reservations:

                        return true;
                    case R.id.nav_profile:
                        // a scopo di test
                        i = new Intent(this, Profile.class);
                        startActivity(i);
                        return true;
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
