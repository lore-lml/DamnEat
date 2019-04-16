package com.damn.polito.damneatdeliver; //#7EE04A

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.damn.polito.damneatdeliver.R;
import com.damn.polito.damneatdeliver.fragments.OrderFragment;
import com.damn.polito.damneatdeliver.fragments.ProfileFragment;

public class Welcome extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private ProfileFragment profileFragment;
    private OrderFragment orderFragment;

    private BottomNavigationView navigation;
    private Integer selectedId = null;

    private BottomNavigationView.OnNavigationItemSelectedListener navListener
            = item -> {
        Fragment selected = null;
        selectedId = item.getItemId();
        switch (selectedId) {
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
        navigation.setSelectedItemId(R.id.nav_reservations);
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
