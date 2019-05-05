package com.damn.polito.damneatdeliver; //#7EE04A

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneatdeliver.R;
import com.damn.polito.damneatdeliver.fragments.CurrentFragment;
import com.damn.polito.damneatdeliver.fragments.OrderFragment;
import com.damn.polito.damneatdeliver.fragments.ProfileFragment;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class Welcome extends AppCompatActivity {
    List<AuthUI.IdpConfig> providers;
    private final int REQUEST_CODE = 707;
    private static Order currentOrder;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private DatabaseReference orderRef;
    private ValueEventListener orderListener;

    private FragmentManager fragmentManager;
    private ProfileFragment profileFragment;
    private OrderFragment orderFragment;
    private CurrentFragment currentFragment;
    private static boolean currentState;

    private BottomNavigationView navigation;
    private Integer selectedId = null;
    private static String dbKey;
    private String orderKey;

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
            case R.id.nav_current:
                if(currentFragment == null)
                    currentFragment = new CurrentFragment();
                selected = currentFragment;
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

    public static String getKey() {
        return dbKey;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
        shownSignInOptions();
        currentOrder = new Order();
        currentOrder.setState("empty");
        database = FirebaseDatabase.getInstance();
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(navListener);
        fragmentManager = getSupportFragmentManager();
        navigation.setSelectedItemId(R.id.nav_current);

        loadCurrentOrder(this);
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
    private void shownSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.Background)
                        .build(), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode==RESULT_OK){
                //get user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //show email on toast
                Toast.makeText(this, user.getEmail().toString(), Toast.LENGTH_LONG).show();
                //set button signout
                //b.setEnabled(true);
                storeData(user);

            }
            else{
                Toast.makeText(this, response.getError().getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void storeData(FirebaseUser user){
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(this).edit();
        pref.putString("dbkey", user.getUid());
        pref.apply();
    }

    public void loadCurrentOrder(Context ctx) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);

        if(dbKey == null) {
            dbKey = pref.getString("dbkey", null);
            if (dbKey == null) return;
        }

        myRef = database.getReference("deliverers/" + dbKey + "/current_order/");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderKey = dataSnapshot.getValue(String.class);
                orderRef = database.getReference("/ordini/" + orderKey);
                if(orderListener != null)
                    orderRef.removeEventListener(orderListener);
                orderListener = orderRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        currentOrder = dataSnapshot.getValue(Order.class);
                        if(currentOrder == null){
                            currentOrder = new Order();
                            currentOrder.setState("empty");
                        }
                        //Log.d("curren order", currentOrder.getId());
                        if(selectedId == R.id.nav_current)
                            currentFragment.update();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Toast.makeText(ctx, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static Order getCurrentOrder(){
        return currentOrder;
    }


}
