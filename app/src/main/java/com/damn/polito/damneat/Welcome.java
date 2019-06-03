package com.damn.polito.damneat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.damn.polito.commonresources.FirebaseLogin;
import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.commonresources.notifications.NotificationListener;
import com.damn.polito.damneat.beans.Profile;
import com.damn.polito.damneat.beans.Restaurant;
import com.damn.polito.damneat.fragments.OrderFragment;
import com.damn.polito.damneat.fragments.ProfileFragment;
import com.damn.polito.damneat.fragments.RestaurantFragment;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Welcome extends AppCompatActivity implements NotificationListener {

    public static boolean accountExist = false;
    private static String dbKey;
    private static Profile profile;

    //Fragments
    private FragmentManager fragmentManager;
    private RestaurantFragment restaurantFragment;
    private ProfileFragment profileFragment;
    private OrderFragment orderFragment;

    //UI Widget
    private BottomNavigationView navigation;
    private View notificationBadge;
    private Integer selectedId = null;
    
    //FirebaseReferences
    private FirebaseDatabase database;
    private DatabaseReference profileRef, restaurantsRef;
    private Query orderQuery;
    //Listeners
    private ValueEventListener profileListener, orderListenerNotifier;
    private ChildEventListener restaurantsListener, orderListener;

    //Collections
    private List<Restaurant> restaurants = new ArrayList<>();
    private List<Order> orders = new LinkedList<>();



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
                if(orderFragment == null)
                    orderFragment = new OrderFragment();

                selected = orderFragment;
                refreshNotificationBadge(false);
                break;
            case R.id.nav_profile:
                profileFragment = new ProfileFragment();
                selected = profileFragment;
                break;
        }
        if(selected != null)
            fragmentManager.beginTransaction().replace(R.id.fragment_container, selected).commitAllowingStateLoss();
        return true;
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        FirebaseLogin.init();


        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(navListener);
        fragmentManager = getSupportFragmentManager();
        database = FirebaseDatabase.getInstance();

        if (getKey() == null)
            FirebaseLogin.shownSignInOptions(this);
        else
            loadProfileData();
        addNotificationBadge();
    }

    private String getKey() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        dbKey = pref.getString("dbkey", null);
        return dbKey;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == FirebaseLogin.REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode==RESULT_OK){
                //get user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //show email on toast
                if(user !=  null) {
                    Toast.makeText(this, "" + user.getEmail(), Toast.LENGTH_LONG).show();
                    //set button signout
                    //b.setEnabled(true);
                    FirebaseLogin.storeData(user, this);
                    loadProfileData();
                }
            }
            else{
                String error = null;
                
                if(response != null)
                    error = response.getError().getMessage();
                    
                if(error == null) {
                    error = getString(R.string.login_error);
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                }
                finish();
            }
        }
    }

    private void loadProfileData() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        if(dbKey == null) {
            dbKey = pref.getString("dbkey", null);
            if (dbKey == null) return;
        }

        profileRef = database.getReference("clienti/" + dbKey);
        profileListener = profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                profile = dataSnapshot.getValue(Profile.class);
                if(profile != null && Utility.firstON) {
                    setListeners();
                    Utility.firstON = false;
                }

                if(selectedId == null){
                    navigation.setSelectedItemId(R.id.nav_restaurant);
                }else if(selectedId == R.id.nav_profile)
                    profileFragment.updateProfile();

                if(profile != null){
                    for(String key : profile.favouriteRestaurantsSet()){
                        Restaurant r = new Restaurant();
                        r.setFbKey(key);
                        int pos = restaurants.indexOf(r);
                        if(pos == -1)
                            continue;
                        r = restaurants.get(pos);
                        r.sFavorite(true);
                        if(selectedId == R.id.nav_restaurant)
                            restaurantFragment.onChildChanged(r);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("CustomerAppErrors", "DataBase Error");
            }
        });

    }

    private void setListeners() {
        accountExist = true;
        setRestaurantListener();
        setOrderListener();
    }

    private void setRestaurantListener(){
        if(!accountExist) return;
        restaurantsRef = database.getReference("ristoratori/");

        restaurantsListener = restaurantsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                Restaurant r = dataSnapshot.getValue(Restaurant.class);
                assert key != null;
                assert r != null;
                r.setFbKey(key);

                if(profile.favouriteRestaurantsSet().contains(key))
                    r.sFavorite(true);

                restaurants.add(r);
                if(selectedId == R.id.nav_restaurant)
                    restaurantFragment.onChildAdded(r);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                Restaurant r = dataSnapshot.getValue(Restaurant.class);
                assert key != null;
                assert r != null;
                r.setFbKey(key);

                if(profile.favouriteRestaurantsSet().contains(key))
                    r.sFavorite(true);

                int pos = restaurants.indexOf(r);
                restaurants.remove(r);
                restaurants.add(pos, r);
                if(selectedId == R.id.nav_restaurant)
                    restaurantFragment.onChildChanged(r);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                Restaurant r = dataSnapshot.getValue(Restaurant.class);
                assert key != null;
                assert r != null;
                r.setFbKey(key);

                if(profile.favouriteRestaurantsSet().contains(key))
                    r.sFavorite(true);

                restaurants.remove(r);
                if(selectedId == R.id.nav_restaurant)
                    restaurantFragment.onChildRemoved(r);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("CustomerAppErrors", "DataBase Error");
            }
        });

    }

    public List<Restaurant> getRestaurants(){return restaurants;}

    @SuppressWarnings("unchecked")
    private void setOrderListener() {
        if(dbKey == null) return;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        DatabaseReference orderRef = database.getReference("ordini/");

        orderQuery = orderRef.orderByChild("customer/customerID").equalTo(dbKey);
        orderListenerNotifier = orderQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() == null) {
                    pref.edit().putLong("nOrder", 0).apply();
                    return;
                }

                long old = pref.getLong("nOrder", -1);
                long count = dataSnapshot.getChildrenCount();
                if(old != count){
                    pref.edit().putLong("nOrder", count).apply();
                    if(old != -1)
                        refreshNotificationBadge(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("CustomerAppErrors", "DataBase Error");
            }
        });

        orderListener = orderQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                Order o = dataSnapshot.getValue(Order.class);
                assert key != null;
                assert o != null;
                o.setId(key);
                orders.add(0, o);

                if(selectedId == R.id.nav_reservations)
                    orderFragment.onChildAdded();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                Order o = dataSnapshot.getValue(Order.class);
                assert key != null;
                assert o != null;
                o.setId(key);
                int pos = orders.indexOf(o);
                Order old = orders.remove(pos);
                boolean rateChanged = true;
                if(old.isRated() == o.isRated()) {
                    pos = 0;
                    rateChanged = false;
                }
                orders.add(pos, o);

                if(selectedId == R.id.nav_reservations)
                    orderFragment.onChildChanged(pos, rateChanged);
                else
                    refreshNotificationBadge(true);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                Order o = dataSnapshot.getValue(Order.class);
                assert key != null;
                assert o != null;
                o.setId(key);
                int pos = orders.indexOf(o);
                orders.remove(o);

                if(selectedId == R.id.nav_reservations)
                    orderFragment.onChildRemoved(pos);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("CustomerAppErrors", "DataBase Error");
            }
        });
    }

    public List<Order> getOrders(){return orders;}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.firstON = true;
        if (profileRef != null)
            profileRef.removeEventListener(profileListener);
        if(orderQuery != null) {
            orderQuery.removeEventListener(orderListenerNotifier);
            orderQuery.removeEventListener(orderListener);
        }
        if(restaurantsRef != null)
            restaurantsRef.removeEventListener(restaurantsListener);
    }

    @Override
    public void addNotificationBadge() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(0);

        notificationBadge = LayoutInflater.from(this).inflate(R.layout.icon_badge, menuView, false);

        itemView.addView(notificationBadge);
        refreshNotificationBadge(false);
    }

    @Override
    public void refreshNotificationBadge(boolean visible) {
        notificationBadge.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public static String getDbKey(){return dbKey;}

    public static Profile getProfile() {return profile;}
}
