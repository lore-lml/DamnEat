package com.damn.polito.damneatrestaurant;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.damn.polito.damneatrestaurant.fragments.DishesFragment;
import com.damn.polito.damneatrestaurant.fragments.OrderFragment;
import com.damn.polito.damneatrestaurant.fragments.ProfileFragment;
import com.google.firebase.database.FirebaseDatabase;

public class Welcome extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private DishesFragment dishesFragment;
    private ProfileFragment profileFragment;
    private OrderFragment orderFragment;

    private BottomNavigationView navigation;
    private Integer selectedId = null;

    private BottomNavigationView.OnNavigationItemSelectedListener navListener
            = item -> {
            Fragment selected = null;
            selectedId = item.getItemId();
                switch (selectedId) {
                    case R.id.nav_dishes:
                        if(dishesFragment == null)
                            dishesFragment = new DishesFragment();
                        selected = dishesFragment;
                        break;
                    case R.id.nav_reservations:
                        if(orderFragment == null)
                            orderFragment = new OrderFragment();
                        selected = orderFragment;
                        break;
                    case R.id.nav_profile:
                        if(profileFragment == null)
                            profileFragment = new ProfileFragment();
                        selected = profileFragment;
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.fragment_container, selected).commit();
                return true;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(navListener);
        fragmentManager = getSupportFragmentManager();
        navigation.setSelectedItemId(R.id.nav_dishes);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("fragment_id", selectedId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedId = savedInstanceState.getInt("fragment_id");
        if(selectedId != 0)
            navigation.setSelectedItemId(selectedId);
    }
}
