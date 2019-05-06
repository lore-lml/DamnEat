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
import com.damn.polito.damneatdeliver.beans.Profile;
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
    private static Profile profile;
    private FirebaseUser user;

    private BottomNavigationView navigation;
    private Integer selectedId = null;
    private static String dbKey;
    private String orderKey;
    private static Context ctx;
    private static boolean logged;

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
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        if(dbKey == null) {
            dbKey = pref.getString("dbkey", null);
        }

        return dbKey;
    }

    public static Boolean getCurrentAvaibility() {
        return currentState;
    }

    public static Profile getProfile() {
        return profile;
    }

    public static boolean registered() {
        return logged;
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
        loadProfile();
        loadCurrentOrder(this);
        ctx = this;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        if(dbKey == null) {
            dbKey = pref.getString("dbkey", null);
            if (dbKey == null) return;
        }
        if(profile != null) {
            DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + dbKey);
            freeDeliverersRef.setValue(Welcome.getKey());
            Log.d("key", dbKey);
            DatabaseReference orderRef = database.getReference("/deliverers/" + Welcome.getKey() + "/state/");
            orderRef.setValue(true);
            logged = true;
        }else{
            DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + dbKey);
            freeDeliverersRef.removeValue();
            DatabaseReference orderRef = database.getReference("/deliverers/" + Welcome.getKey() + "/state/");
            orderRef.setValue(false);
            logged = false;
        }
        loadCurrentState();


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
                user = FirebaseAuth.getInstance().getCurrentUser();
                //show email on toast
                Toast.makeText(this, user.getEmail().toString(), Toast.LENGTH_LONG).show();
                //set button signout
                //b.setEnabled(true);
                storeData(user);

            }
            else{
                String error = null;
                try {
                    error = response.getError().getMessage();

                }catch (Exception e){

                }
                if(error == null)
                    error = getString(R.string.login_error);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void storeData(FirebaseUser user){
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(this).edit();
        pref.putString("dbkey", user.getUid());
        pref.apply();
    }

    private void loadProfile(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        if(dbKey == null) {
            dbKey = pref.getString("dbkey", null);
            if (dbKey == null) return;
        }
        DatabaseReference profileRef = database.getReference("/deliverers/" + dbKey + "/info/");
        profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                profile = dataSnapshot.getValue(Profile.class);
                if(profile!=null)
                    logged = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        }

    private void loadCurrentState(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if(dbKey == null) {
            dbKey = pref.getString("dbkey", null);
            if (dbKey == null) return;
        }
        DatabaseReference stateRef = database.getReference("/deliverers/" + dbKey + "/state/");
        stateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;
                Boolean state = dataSnapshot.getValue(Boolean.class);
                if(state != null)
                    currentState = state;
                if(!currentState) {
                    database.getReference("/deliverers_liberi/" + dbKey).removeValue();
                }
                Log.d("state", String.valueOf(currentState));
//
//                try {
//                }catch (Exception e){
//                    currentState = false;
//                }
                currentFragment.update();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Welcome.this, "Database error" , Toast.LENGTH_SHORT).show();
            }
        });
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
                currentFragment.update();

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

    @Override
    protected void onResume() {
        super.onResume();
        if(currentState){
            DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + dbKey);
            freeDeliverersRef.setValue(Welcome.getKey());
            currentFragment.update();
        }
    }
}
