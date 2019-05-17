package com.damn.polito.damneatrestaurant;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.damn.polito.commonresources.FirebaseLogin;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.commonresources.notifications.NotificationListener;
import com.damn.polito.damneatrestaurant.beans.Profile;
import com.damn.polito.damneatrestaurant.fragments.DishesFragment;
import com.damn.polito.damneatrestaurant.fragments.OrderFragment;
import com.damn.polito.damneatrestaurant.fragments.ProfileFragment;
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

import java.util.LinkedList;
import java.util.List;

public class Welcome extends AppCompatActivity implements NotificationListener {

    //SYSTEM VARIABLES
    public static boolean accountExist = false;
    public static String dbKey;

    //FRAGMENTS VARIABLES
    private FragmentManager fragmentManager;
    private DishesFragment dishesFragment;
    private ProfileFragment profileFragment;
    private OrderFragment orderFragment;

    //FIREBASE VARIABLES
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private ValueEventListener listener, orderListenerNotifier;
    private ChildEventListener orderListener;
    private Query orderQuery;

    //COLLECTIONS
    private Profile profile;
    private List<Order> orders = new LinkedList<>();

    //UI WIDGET
    private BottomNavigationView navigation;
    private View notificationBadge;
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
                        refreshNotificationBadge(false);
                        selected = orderFragment;
                        break;
                    case R.id.nav_profile:
                        if(profileFragment == null)
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
            loadDataProfile();

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
                if(user != null) {
                    Toast.makeText(this, user.getEmail(), Toast.LENGTH_LONG).show();
                    //set button signout
                    //b.setEnabled(true);
                    dbKey = user.getUid();
                    FirebaseLogin.storeData(user, this);
                }
                loadDataProfile();
            }
            else{
                String error = null;
                if(response != null && response.getError() != null)
                    error = response.getError().getMessage();
                else
                    error = getString(R.string.login_error);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    private void storeProfile(Profile profile){
        accountExist = true;
        if(selectedId == null)
            navigation.setSelectedItemId(R.id.nav_dishes);
        setOrderListener();

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("address", profile.getAddress());
        editor.putString("name", profile.getName());
        editor.putString("phone", profile.getPhone());
        editor.putString("mail", profile.getMail());
        editor.putString("description",profile.getDescription());
        editor.putString("opening", profile.getOpening());
        editor.putString("categories", profile.getCategories());
        editor.putString("shipprice", String.valueOf(profile.getPriceShip()));
        editor.putString("profile", profile.getImage());
        editor.apply();
    }

    @SuppressWarnings("unchecked")
    private void setOrderListener() {
        if(dbKey == null) return;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        DatabaseReference orderRef = database.getReference("ordini/");

        orderQuery = orderRef.orderByChild("restaurant/restaurantID").equalTo(dbKey);
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
                Toast.makeText(Welcome.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                orders.remove(o);
                orders.add(0, o);

                if(selectedId == R.id.nav_reservations)
                    orderFragment.onChildChanged();
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
                Toast.makeText(Welcome.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }

    public void loadDataProfile() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        if(dbKey == null) {
            dbKey = pref.getString("dbkey", null);
            if (dbKey == null) return;
        }

        myRef = database.getReference("ristoratori/" + dbKey);

        listener = myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Profile prof = dataSnapshot.getValue(Profile.class);
                if (prof != null)
                    storeProfile(prof);
                if (selectedId != null) {
                    if (selectedId == R.id.nav_profile)
                        profileFragment.updateProfile();

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Welcome.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myRef!=null)
            myRef.removeEventListener(listener);
        if(orderQuery != null) {
            orderQuery.removeEventListener(orderListener);
            orderQuery.removeEventListener(orderListenerNotifier);
        }
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

    public List<Order> getOrders() {
        return orders;
    }

    public static String getDbKey() { return dbKey; }
}

