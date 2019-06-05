package com.damn.polito.damneatdeliver;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneatdeliver.beans.Profile;
import com.damn.polito.damneatdeliver.fragments.CurrentFragment;
import com.damn.polito.damneatdeliver.fragments.OrderFragment;
import com.damn.polito.damneatdeliver.fragments.ProfileFragment;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Welcome extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult> {
    //STATIC VARIABLES
    public static boolean logged;
    private static final int ONE_MINUTE = 1000 * 60 ;
    private static final int TEN_MINUTES = 1000 * 60 * 10  ;
    private static Profile profile;
    private static Order currentOrder;
    private static String dbKey;
    private static boolean switchEnabled;

    private List<AuthUI.IdpConfig> providers;
    private final int REQUEST_CODE = 707;

    //FIREBASE VARIABLES
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private DatabaseReference orderRef;
    private ValueEventListener orderListener, profileListener;
    private ChildEventListener ordersChildListener;
    private DatabaseReference profileRef;
    private Query orderQuery;

    //FRAGMENT VARIABLES
    private FragmentManager fragmentManager;
    private ProfileFragment profileFragment;
    private OrderFragment orderFragment;
    private CurrentFragment currentFragment;

    //UI WIDGET
    private BottomNavigationView navigation;
    private ProgressBar buffer;
    private FrameLayout container;
    private Integer selectedId = null;
    private String orderKey;

    //LOCATION VARIABLES
    public static final int LOCATION_PERMISSION_REQUESt_CODE = 1212;
    private boolean mLocGranted;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location bestLocation;

    //CHECK LOCATION SETTINGS VARIABLES
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest locationRequest;
    int REQUEST_CHECK_SETTINGS = 100;

    //COLLECTIONS
    private static List<Order> orders = new LinkedList<>();

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
        if(selected != null)
            fragmentManager.beginTransaction().replace(R.id.fragment_container, selected).commitAllowingStateLoss();
        return true;
    };

    private String getKey() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        dbKey = pref.getString("dbkey", null);
        return dbKey;
    }

    public static Boolean getCurrentAvaibility() {
        if(profile == null)
            return false;
        return profile.getState();
    }

    public static boolean isSwitchEnabled() {
        return switchEnabled;
    }

    public static Profile getProfile() {
        return profile;
    }

    public static boolean registered() {
        if (profile != null && profile.getName()!=null && profile.getMail()!=null)
            logged = true;
        else
            logged = false;
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
        database = FirebaseDatabase.getInstance();
        if (getKey() != null)
            init();
        else
            shownSignInOptions();

        //initialize position DB, remove old position
//        DatabaseReference refTime = database.getReference("deliverers/" + dbKey + "/info/positionTime");
//        refTime.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Long time=dataSnapshot.getValue(Long.class);
//                Long ctime=System.currentTimeMillis();
//                if(time!=null){
//                    if((ctime-time)>=TEN_MINUTES){
////                        DatabaseReference RefLat = database.getReference("deliverers/" + dbKey + "/info/latitude");
////                        RefLat.setValue(null);
////                        DatabaseReference RefLong = database.getReference("deliverers/" + dbKey + "/info/longitude");
////                        RefLong.setValue(null);
////                        DatabaseReference Reftime = database.getReference("deliverers/" + dbKey + "/info/positionTime");
////                        Reftime.setValue(null);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

    }

    private void init() {
        currentOrder = new Order();
        currentOrder.setState("empty");
        navigation = findViewById(R.id.navigation);
        navigation.setVisibility(View.GONE);
        container = findViewById(R.id.fragment_container);
        container.setVisibility(View.GONE);
        buffer = findViewById(R.id.welcome_pb);
        navigation.setOnNavigationItemSelectedListener(navListener);
        fragmentManager = getSupportFragmentManager();
        navigation.setSelectedItemId(R.id.nav_current);

        //initialize the GoogleApiClient and LocationRequest
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        profile = new Profile();
        profile.setState(false);
        loadProfile();
        loadCurrentOrder();
        //loadCurrentState();

        if (profile != null) {
            Log.d("key", getKey());

            logged = true;
        } else {
            logged = false;
        }
        getLocationPermissions();
        //StartLocationManager();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //todo: fa crashare al primo avvio ho messo il controllo che sia diverso da null, ma non so cosa faccia questa funzione
        if(selectedId!=null)
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
                if (user != null) {
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putString("user_email", user.getEmail())
                            .putString("user_name", user.getDisplayName()).apply();
                    Toast.makeText(this, user.getEmail(), Toast.LENGTH_LONG).show();
                    //set button signout
                    //b.setEnabled(true);
                    storeData(user);
                    profile = null;

                    if (profileRef != null && profileListener != null)
                        profileRef.removeEventListener(profileListener);
                    if (getKey() != null)
                        init();
                }
                //if(selectedId == R.id.nav_current)
            }  else {
                String error = null;
                if(response != null && response.getError() != null)
                    error = response.getError().getMessage();

                if (error == null)
                    error = getString(R.string.login_error);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                finish();
            }
        }else if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                //StartLocationManager();
                Toast.makeText(getApplicationContext(), "GPS enabled", Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(getApplicationContext(), "GPS is not enabled", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void storeData(FirebaseUser user) {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(this).edit();
        pref.putString("dbkey", user.getUid());
        pref.apply();
    }

    private void loadProfile() {
        profileRef = database.getReference("/deliverers/" + dbKey + "/info");
        profileListener = profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                profile = dataSnapshot.getValue(Profile.class);
                if (profile == null)
                    logged = false;
                else{
                    logged = true;
                    if(Utility.firstON)
                        setListeners();
//                    if(profile.getLatitude()==null || profile.getLongitude()==null) {
//                        profile.setState(false);
//                    }
                    if (profile.getState() == null) {
                        database.getReference("/deliverers/" + dbKey + "/info/state/").setValue(false);
                        profile.setState(false);
                    }
                    changeGPSstate();
                }
                setDeliverFreeList();
                if (currentFragment != null)
                    if (selectedId == R.id.nav_current)
                        currentFragment.update();

                navigation.setVisibility(View.VISIBLE);
                container.setVisibility(View.VISIBLE);
                buffer.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("CustomerAppErrors", databaseError.getMessage());
            }
        });

    }

    private void setListeners() {
        Utility.firstON = false;
        setOrderListener();
        setTokenListener();
    }

    private void setTokenListener() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    // Get new Instance ID token
                    if(task.getResult() != null) {
                        String token = task.getResult().getToken();
                        if(!token.equals(profile.getNotificationId())){
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("deliverers/"+ Welcome.getDbKey() +"/info/notificationId");
                            ref.setValue(token);
                        }
                    }
                });
    }

    private void setOrderListener() {
        if(dbKey == null) return;
        DatabaseReference orderRef = database.getReference("ordini/");
        orderQuery = orderRef.orderByChild("delivererID").equalTo(dbKey);
        ordersChildListener = orderQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue() == null) return;
                String key = dataSnapshot.getKey();
                if(currentOrder.getId() != null && currentOrder.getId().equals(key)) return;
                Order o = dataSnapshot.getValue(Order.class);
                assert key != null;
                assert o != null;
                if(!o.getState().equals("confirmed") && !o.getState().equals("rejected") && !o.getState().equals("reassign"))
                    return;

                o.setId(key);
                ((LinkedList<Order>)orders).addFirst(o);

                if(selectedId == R.id.nav_reservations)
                    orderFragment.onChildAdded();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue() == null) return;
                String key = dataSnapshot.getKey();

                Order o = dataSnapshot.getValue(Order.class);
                assert key != null;
                assert o != null;
                if(!o.getState().equals("confirmed") && !o.getState().equals("rejected") && !o.getState().equals("reassign"))
                    return;
                o.setId(key);
                int pos = orders.indexOf(o);
                if(pos != -1)
                    orders.remove(pos);
                ((LinkedList<Order>)orders).addFirst(o);

                if(selectedId == R.id.nav_reservations)
                    orderFragment.onChildAdded();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public static List<Order> getOrders(){ return orders; }

    private void setDeliverFreeList(){
        if (profile!=null && profile.getState()) {
            DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + dbKey);
            freeDeliverersRef.setValue(dbKey);
        } else {
            DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + dbKey);
            freeDeliverersRef.removeValue();
        }
    }

    public void loadCurrentOrder() {
        myRef = database.getReference("deliverers/" + dbKey + "/current_order/");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderKey = dataSnapshot.getValue(String.class);
                orderRef = database.getReference("/ordini/" + orderKey);
                if (orderListener != null)
                    orderRef.removeEventListener(orderListener);
                if (currentFragment != null)
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
                                DatabaseReference orderRef = database.getReference("/deliverers/" + dbKey + "/info/state/");
                                orderRef.setValue(true);
                            } else {
                                DatabaseReference orderRef = database.getReference("/deliverers/" + dbKey + "/info/state/");
                                orderRef.setValue(false);
                            }

                        }
                        //Log.d("curren order", currentOrder.getId());
                        if (currentFragment != null)
                            if (selectedId == R.id.nav_current)
                            currentFragment.update();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("CustomerAppErrors", databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("CustomerAppErrors", databaseError.getMessage());
            }
        });
    }

    public static Order getCurrentOrder() {
        return currentOrder;
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
//            switchEnabled = false;
            return;
        }

        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                double latitude=location.getLatitude();
                double longitude=location.getLongitude();
                if(profile!=null) {
                    profile.setPosition(latitude, longitude);
                    //Toast.makeText(ctx,  "" + location.getLatitude() + location.getLongitude(), Toast.LENGTH_LONG).show();
                }
//                String msg = "New Latitude: " + latitude + "New Longitude: " + longitude;
//                Toast.makeText(Welcome.this, msg, Toast.LENGTH_LONG).show();
                if(isBetterLocation(location,bestLocation)){
                    DatabaseReference RefLat = database.getReference("deliverers/" + dbKey + "/info/latitude");
                    RefLat.setValue(location.getLatitude());
                    DatabaseReference RefLong = database.getReference("deliverers/" + dbKey + "/info/longitude");
                    RefLong.setValue(location.getLongitude());
                    DatabaseReference Reftime = database.getReference("deliverers/" + dbKey + "/info/positionTime");
                    Reftime.setValue(location.getTime());
                    bestLocation=location;

                    if(currentOrder!=null && !currentOrder.getState().equals("empty")){
                        RefLat = database.getReference("ordini/" + currentOrder.getId() + "/latitude");
                        RefLat.setValue(location.getLatitude());
                        RefLong = database.getReference("ordini/" + currentOrder.getId() + "/longitude");
                        RefLong.setValue(location.getLongitude());
                    }

                }
                changeGPSstate();

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

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Log.d("LOCATION","gps");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 800, 10, locationListener);
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //Log.d("LOCATION","network");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 800, 10, locationListener);
        }


        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
    }

    private void getLocationPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocGranted = true;
                switchEnabled = true;
                //StartLocationManager();
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
        switchEnabled = false;

        if (requestCode == LOCATION_PERMISSION_REQUESt_CODE) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        mLocGranted = false;
                        switchEnabled = false;
                        return;
                    }
                }
                mLocGranted = true;
                switchEnabled = true;
                //StartLocationManager();
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        //setVisible(true);
        if(profile!=null) {
            //profile.setState(false);
            if (profile.getState()) {
//                DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + dbKey);
//                freeDeliverersRef.setValue(dbKey);
                if (currentFragment != null)
                    if (selectedId == R.id.nav_current)
                    currentFragment.update();
            }
        }
    }

    public static String getDbKey() {
        return dbKey;
    }



    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
        boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        builder.build()
                );
        result.setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // NO need to show the dialog;

                break;

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                //  GPS disabled show the user a dialog to turn it on
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().

                    status.startResolutionForResult(Welcome.this, REQUEST_CHECK_SETTINGS);

                } catch (IntentSender.SendIntentException e) {

                    //failed to show dialog
                }


                break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are unavailable so not possible to show any dialog now
                break;
        }
    }

    private boolean isActive(){
        if(profile==null || profile.getState()==null)
            return false;
        return profile.getState() || !currentOrder.getState().equals("empty");
    }

    private void changeGPSstate(){
        Log.d("GPS State", String.valueOf(isActive()));
        if(isActive()){
            if(locationListener==null){
                Log.d("GPS State", "Abilito il gps");
                StartLocationManager();
            }
        } else{
            if(locationManager==null || locationListener == null)
                return;
            Log.d("GPS State", "Disabilito il gps");
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.firstON = true;
        if (mGoogleApiClient!=null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        orderQuery.removeEventListener(ordersChildListener);
        orderRef.removeEventListener(orderListener);
        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (IOException ignored) {
        }
    }

}
