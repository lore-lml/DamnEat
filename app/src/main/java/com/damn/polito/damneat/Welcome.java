package com.damn.polito.damneat;

import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneat.fragments.OrderFragment;
import com.damn.polito.damneat.fragments.ProfileFragment;
import com.damn.polito.damneat.fragments.RestaurantFragment;
import com.google.firebase.database.FirebaseDatabase;

public class Welcome extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private RestaurantFragment restaurantFragment;

    private BottomNavigationView navigation;
    private Integer selectedId = null;

    private BottomNavigationView.OnNavigationItemSelectedListener navListener
            = item -> {
        Fragment selected = null;
        selectedId = item.getItemId();
        switch (selectedId) {
            case R.id.nav_restaurant:
                if(restaurantFragment == null)
                    restaurantFragment = new RestaurantFragment();
                selected = restaurantFragment;
                break;
            case R.id.nav_reservations:
                selected = new OrderFragment();
                break;
            case R.id.nav_profile:
                selected = new ProfileFragment();
                break;
        }
        if(selected != null)
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
        navigation.setSelectedItemId(R.id.nav_restaurant);

        if(Utility.firstON) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Utility.firstON = false;
        }
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