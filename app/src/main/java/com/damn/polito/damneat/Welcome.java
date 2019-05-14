package com.damn.polito.damneat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.damn.polito.commonresources.FirebaseLogin;
import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.notifications.NotificationListener;
import com.damn.polito.damneat.beans.Profile;
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
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class Welcome extends AppCompatActivity implements NotificationListener {

    public static boolean accountExist = false;

    private FragmentManager fragmentManager;
    private RestaurantFragment restaurantFragment;
    private ProfileFragment profileFragment;
    private OrderFragment orderFragment;

    private BottomNavigationView navigation;
    private View notificationBadge;
    private Integer selectedId = null;
    
    private String dbKey;
    private FirebaseDatabase database;
    private DatabaseReference myRef, orderRef;
    private ValueEventListener listener, orderListener;
    private Map<String, ChildEventListener> children = new HashMap<>();


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
        //navigation.setSelectedItemId(R.id.nav_restaurant);
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
                if(user !=  null && user.getEmail() != null)
                    Toast.makeText(this, ""+user.getEmail(), Toast.LENGTH_LONG).show();
                //set button signout
                //b.setEnabled(true);
                FirebaseLogin.storeData(user, this);
                loadProfileData();

                if(!accountExist){
                    //navigation.setSelectedItemId(R.id.nav_profile);
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

        myRef = database.getReference("clienti/" + dbKey);
        listener = myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Profile prof = dataSnapshot.getValue(Profile.class);
                if(prof != null) {
                    storeProfile(prof);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Welcome.this).edit();

                    editor.putString("clientaddress", prof.getAddress());
                    editor.putString("clientname", prof.getName());
                    editor.putString("clientphone", prof.getPhone());
                    editor.putString("clientmail", prof.getMail());
                    editor.putString("clientphoto", prof.getBitmapProf());
                    editor.apply();
                }else if(selectedId == null){
                    navigation.setSelectedItemId(R.id.nav_restaurant);
                }

                if(selectedId != null && selectedId == R.id.nav_profile)
                    profileFragment.updateProfile();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Welcome.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });

        setOrderListener();
    }

    private void storeProfile(Profile profile){
        accountExist = true;
        if(selectedId == null)
            navigation.setSelectedItemId(R.id.nav_restaurant);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();

        editor.putString("address", profile.getAddress());
        editor.putString("name", profile.getName());
        editor.putString("phone", profile.getPhone());
        editor.putString("mail", profile.getMail());
        editor.putString("description",profile.getDescription());
        editor.putString("profile", profile.getBitmapProf());
        editor.apply();
    }

    @SuppressWarnings("unchecked")
    private void setOrderListener() {
        if(dbKey == null) return;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Map<String, Object> map = new HashMap<>();
        orderRef = database.getReference("clienti/" + dbKey + "/lista_ordini");
        orderListener = orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null)
                    map.putAll((Map) dataSnapshot.getValue());
                else {
                    pref.edit().putInt("nOrder", 0).apply();
                    return;
                }

                int old = pref.getInt("nOrder", -1);
                if(old != map.size()){
                    pref.edit().putInt("nOrder", map.size()).apply();
                    if(old != -1)
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
                Toast.makeText(Welcome.this, "Database Error", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myRef != null)
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
