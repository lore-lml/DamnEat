package com.damn.polito.damneatrestaurant;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.damn.polito.damneatrestaurant.fragments.DishesFragment;

public class Welcome extends AppCompatActivity {


    private Intent i;
    private FragmentManager fragmentManager;
    private DishesFragment dishesFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener navListener
            = item -> {
        Fragment selected = null;
                switch (item.getItemId()) {
                    case R.id.nav_dishes:
//                        // a scopo di test
//                        i = new Intent(this, TestDishes.class);
//                        startActivity(i);
                        if(dishesFragment == null)
                            dishesFragment = new DishesFragment();
                        selected = dishesFragment;
                        break;
                    case R.id.nav_reservations:

                        break;
                    case R.id.nav_profile:
                        // a scopo di test
                        i = new Intent(this, Profile.class);
                        startActivity(i);
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.fragment_container, selected).commit();
                return true;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(navListener);
        fragmentManager = getSupportFragmentManager();
        navigation.setSelectedItemId(R.id.nav_dishes);
    }

}
