package com.damn.polito.damneatrestaurant;

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
import android.widget.Toast;

import com.damn.polito.commonresources.FirebaseLogin;
import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneatrestaurant.beans.Profile;
import com.damn.polito.damneatrestaurant.fragments.DishesFragment;
import com.damn.polito.damneatrestaurant.fragments.OrderFragment;
import com.damn.polito.damneatrestaurant.fragments.ProfileFragment;
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


    private FragmentManager fragmentManager;
    private DishesFragment dishesFragment;
    private ProfileFragment profileFragment;
    private OrderFragment orderFragment;
    private String dbKey;

    private FirebaseDatabase database;

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
        FirebaseLogin.init();
        FirebaseLogin.shownSignInOptions(this);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(navListener);
        fragmentManager = getSupportFragmentManager();
        navigation.setSelectedItemId(R.id.nav_dishes);
        database = FirebaseDatabase.getInstance();
        if(Utility.firstON) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Utility.firstON = false;
        }
        loadDataProfile(this);

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
                Toast.makeText(this, ""+user.getEmail().toString(), Toast.LENGTH_LONG).show();
                //set button signout
                //b.setEnabled(true);
                FirebaseLogin.storeData(user, this);

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
            }
        }
    }
    private void storeProfile(Profile profile){
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

    public void loadDataProfile(Context ctx) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);

        if(dbKey == null) {
            dbKey = pref.getString("dbkey", null);
            if (dbKey == null) return;
        }

        DatabaseReference myRef = database.getReference("ristoratori/" + dbKey);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Profile prof = dataSnapshot.getValue(Profile.class);
                storeProfile(prof);
                if(profileFragment!=null)
                    profileFragment.updateProfile();
  //              if (prof != null) {

                    //todo: da gestire tutto il resto
//                        if(prof.getPriceShip() != null && !prof.getPriceShip().equals(0.0))
//                            ProfileFragment.this.shipPrice.setText(getString(R.string.order_price, prof.getPriceShip()));
//                        else
//                            ProfileFragment.this.shipPrice.setText(String.valueOf(0.0));
//                        if (prof.getImage() != null) {
//                            String encodedBitmap = prof.getImage();
//                            profileBitmap = Utility.StringToBitMap(encodedBitmap);
//                            if (profileBitmap != null)
//                                profileImage.setImageBitmap(profileBitmap);
//                        }
                    //empty = false;

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ctx, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

