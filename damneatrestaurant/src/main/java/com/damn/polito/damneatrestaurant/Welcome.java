package com.damn.polito.damneatrestaurant;

import android.content.Context;
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
import com.damn.polito.commonresources.Utility;
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
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class Welcome extends AppCompatActivity implements NotificationListener {

    public static boolean accountExist = false;

    private FragmentManager fragmentManager;
    private DishesFragment dishesFragment;
    private ProfileFragment profileFragment;
    private OrderFragment orderFragment;
    private String dbKey;

    private FirebaseDatabase database;
    private DatabaseReference myRef, orderRef;
    private ValueEventListener listener, orderListener;
    private Map<String, ChildEventListener> children = new HashMap<>();

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
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, selected).commit();
                return true;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        FirebaseLogin.init();
        FirebaseLogin.shownSignInOptions(this);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(navListener);
        fragmentManager = getSupportFragmentManager();
        //navigation.setSelectedItemId(R.id.nav_dishes);
        database = FirebaseDatabase.getInstance();
        /*if(Utility.firstON) {
            database.setPersistenceEnabled(true);
        }*/

        addNotificationBadge();
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
                Toast.makeText(this, ""+user.getEmail(), Toast.LENGTH_LONG).show();
                //set button signout
                //b.setEnabled(true);
                dbKey = user.getUid();
                FirebaseLogin.storeData(user, this);
                loadDataProfile(this);
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
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        //editor.putString("dbkey", myRef.getKey());
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
        Map<String, Object> map = new HashMap<>();
        orderRef = database.getReference("ristoranti/" + dbKey + "/ordini_pendenti");
        orderListener = orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null)
                    map.putAll((Map) dataSnapshot.getValue());
                else{
                    pref.edit().putInt("nOrder", 0).apply();
                    return;
                }

                int old = pref.getInt("nOrder", -1);
                if(old != map.size()){
                    pref.edit().putInt("nOrder", map.size()).apply();
                    if(old != -1 && selectedId != R.id.nav_reservations)
                        refreshNotificationBadge(true);
                }

                for(Map.Entry entry : map.entrySet()){
                    if(!children.containsKey(entry.getValue().toString())){
                        ChildEventListener child = newChildEvent();
                        children.put(entry.getValue().toString(), child);

                        database.getReference("/ordini/" + entry.getValue())
                                .addChildEventListener(child);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Welcome.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        if(Utility.firstON) {
            Utility.firstON = false;
        }



    }

    private ChildEventListener newChildEvent(){
        return new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue() != null && selectedId != R.id.nav_reservations)
                    refreshNotificationBadge(true);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
    }

    public void loadDataProfile(Context ctx) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);

        if(dbKey == null) {
            dbKey = pref.getString("dbkey", null);
            if (dbKey == null) return;
        }

        myRef = database.getReference("ristoratori/" + dbKey);

        listener = myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Profile prof = dataSnapshot.getValue(Profile.class);
                if(prof != null)
                    storeProfile(prof);

                if(selectedId == R.id.nav_profile)
                    profileFragment.updateProfile();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ctx, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
        setOrderListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myRef==null || orderRef==null)
            return;
        myRef.removeEventListener(listener);
        orderRef.removeEventListener(orderListener);
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
}

