package com.damn.polito.damneatdeliver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.beans.Deliverer;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneatdeliver.R;
import com.damn.polito.damneatdeliver.beans.Profile;
import com.damn.polito.damneatdeliver.fragments.CurrentFragment;
import com.damn.polito.damneatdeliver.fragments.OrderFragment;
import com.damn.polito.damneatdeliver.fragments.ProfileFragment;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
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
    private ValueEventListener v;
    private DatabaseReference profileRef;
    private FragmentManager fragmentManager;
    private ProfileFragment profileFragment;
    private OrderFragment orderFragment;
    private CurrentFragment currentFragment;
    private static Profile profile;
    private static String hasSetName;

    private BottomNavigationView navigation;
    private Integer selectedId = null;
    private String orderKey;
    private static Context ctx;
    private static boolean logged;

    //LOCATION VARIABLES
    public static final int LOCATION_PERMISSION_REQUESt_CODE = 1212;
    private boolean mLocGranted;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private BottomNavigationView.OnNavigationItemSelectedListener navListener
            = item -> {
        Fragment selected = null;
        selectedId = item.getItemId();
        switch (selectedId) {
            case R.id.nav_reservations:
                if (orderFragment == null)
                    orderFragment = new OrderFragment();
                selected = orderFragment;
                break;
            case R.id.nav_current:
                if (currentFragment == null)
                    currentFragment = new CurrentFragment();
                selected = currentFragment;
                break;
            case R.id.nav_profile:
                if (profileFragment == null)
                    profileFragment = new ProfileFragment();
                selected = profileFragment;
                break;
        }
        fragmentManager.beginTransaction().replace(R.id.fragment_container, selected).commit();
        return true;
    };

    public static String getKey() {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString("dbkey", null);
    }

    public static Boolean getCurrentAvaibility() {
        if(profile == null)
            return false;
        return profile.getState();
    }

    public static Profile getProfile() {
        return profile;
    }

    public static boolean registered() {
        if (profile != null)
            logged = true;
        else
            logged = false;
        return logged;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.activity_welcome);
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
        database = FirebaseDatabase.getInstance();
        if (Welcome.getKey() != null)
            init();
        else
            shownSignInOptions();
        currentOrder = new Order();
        currentOrder.setState("empty");
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(navListener);
        fragmentManager = getSupportFragmentManager();
        navigation.setSelectedItemId(R.id.nav_current);


    }

    private void init() {
        profile = new Profile();
        profile.setState(false);
        loadProfile();
        loadCurrentOrder(this);
        loadCurrentState();
        ctx = this;

        if (profile != null) {
            Log.d("key", getKey());

//            DatabaseReference orderRef = database.getReference("/deliverers/" + Welcome.getKey() + "/state/");
//            orderRef.setValue(currentState);
            logged = true;
        } else {
            logged = false;
        }
        getLocationPermissions();
        StartLocationManager();
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
        if (selectedId != 0)
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
        if (requestCode == REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                //get user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //show email on toast
                Toast.makeText(this, user.getEmail().toString(), Toast.LENGTH_LONG).show();
                //set button signout
                //b.setEnabled(true);
                if (user != null) {
                    storeData(user);
                    profile = null;
//                    if (currentFragment != null)
//                        if (selectedId == R.id.nav_current)
//                            currentFragment.checkRegistered();
                    if (profileRef != null && v != null)
                        profileRef.removeEventListener(v);
                    if (getKey() != null)
                        init();
                }
                //if(selectedId == R.id.nav_current)
            } else {
                String error = null;
                try {
                    error = response.getError().getMessage();

                } catch (Exception e) {

                }
                if (error == null)
                    error = getString(R.string.login_error);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void storeData(FirebaseUser user) {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(this).edit();
        pref.putString("dbkey", user.getUid());
        pref.apply();
    }

    private void loadProfile() {
        profileRef = database.getReference("/deliverers/" + getKey() + "/info");
        v = profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                profile = dataSnapshot.getValue(Profile.class);
                if (profile == null)
                    logged = false;
                else
                    logged = true;
                if (currentFragment != null)
                    if (selectedId == R.id.nav_current)
                        currentFragment.update();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadCurrentState() {
        DatabaseReference stateRef = database.getReference("/deliverers/" + getKey() + "/info/state/");
        stateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) return;
                Boolean state = dataSnapshot.getValue(Boolean.class);
                if (state != null)
                    profile.setState(state);
                else {
                    DatabaseReference orderRef = database.getReference("/deliverers/" + Welcome.getKey() + "/info/state/");
                    orderRef.setValue(false);
                    profile.setState(false);
                }
                setDeliverFreeList();
                Log.d("state", String.valueOf(profile.getState()));
//
//                try {
//                }catch (Exception e){
//                    currentState = false;
//                }
                if (selectedId == R.id.nav_current)
                    currentFragment.update();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Welcome.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDeliverFreeList(){
        if (profile.getState()) {
            DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + getKey());
            freeDeliverersRef.setValue(Welcome.getKey());
        } else {
            DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + getKey());
            freeDeliverersRef.removeValue();
        }
//        if (!currentState) {
//            database.getReference("/deliverers_liberi/" + getKey()).removeValue();
//        }
    }

    public void loadCurrentOrder(Context ctx) {
        myRef = database.getReference("deliverers/" + getKey() + "/current_order/");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderKey = dataSnapshot.getValue(String.class);
                orderRef = database.getReference("/ordini/" + orderKey);
                if (orderListener != null)
                    orderRef.removeEventListener(orderListener);
                if (selectedId == R.id.nav_current)
                    currentFragment.update();

                orderListener = orderRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        currentOrder = dataSnapshot.getValue(Order.class);
                        if (currentOrder == null) {
                            currentOrder = new Order();
                            currentOrder.setState("empty");
                        } else {
                            if (currentOrder.getState().equals("empty")) {
                                DatabaseReference orderRef = database.getReference("/deliverers/" + Welcome.getKey() + "/info/state/");
                                orderRef.setValue(true);
                            } else {
                                DatabaseReference orderRef = database.getReference("/deliverers/" + Welcome.getKey() + "/info/state/");
                                orderRef.setValue(false);
                            }

                        }
                        //Log.d("curren order", currentOrder.getId());
                        if (selectedId == R.id.nav_current)
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

    public static Order getCurrentOrder() {
        return currentOrder;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(profile!=null) {
            //profile.setState(false);
            if (profile.getState()) {
                DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + getKey());
                freeDeliverersRef.setValue(Welcome.getKey());
                if (selectedId == R.id.nav_current)
                    currentFragment.update();
            }
        }
    }


    //LOCATION CODE
    private void StartLocationManager() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
            }, 1);
            return;
        }

        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                double latitude=location.getLatitude();
                double longitude=location.getLongitude();
                if(profile!=null) {
                    if (profile.getState()) {
                        profile.setPosition(latitude, longitude);
                        String msg = "New Latitude: " + latitude + "New Longitude: " + longitude;
                        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
                        //Toast.makeText(ctx,  "" + location.getLatitude() + location.getLongitude(), Toast.LENGTH_LONG).show();
                        DatabaseReference RefLat = database.getReference("deliverers/" + Welcome.getKey() + "/info/latitude");
                        RefLat.setValue(location.getLatitude());
                        DatabaseReference RefLong = database.getReference("deliverers/" + Welcome.getKey() + "/info/longitude");
                        RefLong.setValue(location.getLongitude());
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
//        locationListener = new LocationListener() {
//            @Override
//            public void onLocationChanged(Location location) {
//                Toast.makeText(ctx,  "" + location.getLatitude() + location.getLongitude(), Toast.LENGTH_LONG).show();
//                DatabaseReference RefLat = database.getReference("deliverers/" + Welcome.getKey() + "/latitude");
//                RefLat.setValue(location.getLatitude());
//                DatabaseReference RefLong = database.getReference("deliverers/" + Welcome.getKey() + "/longitude");
//                RefLong.setValue(location.getLongitude());
//            }
//
//            @Override
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//
//            }
//
//            @Override
//            public void onProviderEnabled(String provider) {
//
//            }
//
//            @Override
//            public void onProviderDisabled(String provider) {
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivity(intent);
//            }
//        };
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//            }
//        } else {
//            startManager();
//        }
    }



//    private void isLocationEnabled() {
//
//        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            AlertDialog.Builder alertDialog=new AlertDialog.Builder(mContext);
//            alertDialog.setTitle("Enable Location");
//            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
//            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener(){
//                public void onClick(DialogInterface dialog, int which){
//                    Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    startActivity(intent);
//                }
//            });
//            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
//                public void onClick(DialogInterface dialog, int which){
//                    dialog.cancel();
//                }
//            });
//            AlertDialog alert=alertDialog.create();
//            alert.show();
//        }
//        else{
//            AlertDialog.Builder alertDialog=new AlertDialog.Builder(mContext);
//            alertDialog.setTitle("Confirm Location");
//            alertDialog.setMessage("Your Location is enabled, please enjoy");
//            alertDialog.setNegativeButton("Back to interface",new DialogInterface.OnClickListener(){
//                public void onClick(DialogInterface dialog, int which){
//                    dialog.cancel();
//                }
//            });
//            AlertDialog alert=alertDialog.create();
//            alert.show();
//        }
//    }
    private void getLocationPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocGranted = true;
                StartLocationManager();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUESt_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUESt_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUESt_CODE: {

                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocGranted = false;
                            return;
                        }
                    }
                    mLocGranted = true;
                    StartLocationManager();
                }
            }
        }
    }

}
