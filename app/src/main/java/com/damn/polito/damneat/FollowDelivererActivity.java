package com.damn.polito.damneat;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.damn.polito.commonresources.Haversine;
import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Deliverer;
import com.damn.polito.damneat.adapters.CustomInfoMarkerAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

public class FollowDelivererActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int LOCATION_PERMISSION_REQUESt_CODE = 1212;
    private static final int DEFAULT_ZOOM = 13;

    private boolean mLocGranted;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFLPC;
    private static LatLng latLng;
    private Location currLoc;
    private MarkerOptions place1, place2;
    private String key, name;
    private Bitmap photo;
    private ValueEventListener profListener;
    private DatabaseReference dbRef;
    private String customerAddress;
    private Marker markerBike;
    private Address deliveryAddress;
    private Deliverer chosen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_deliverer_map);

        initCustomerMarker();
        getLocationPermissions();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        key = getIntent().getStringExtra("key");
        startDelivererPosition();
        name = getIntent().getStringExtra("name");

        photo = Utility.StringToBitMap(getIntent().getStringExtra("photo"));
    }

    private void initCustomerMarker() {
        customerAddress = getIntent().getStringExtra("customer_address");
        try {
            BitmapDescriptor icon = getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.ic_home));
            place1 = new MarkerOptions().position(getCustomerPosition())
                    .title(getString(R.string.my_position)).icon(icon);
        } catch (IOException e) {
            e.printStackTrace();
            place1 = null;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unchecked")
    private void getDeviceLocation() {
        mFLPC = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocGranted) {
                Task location = mFLPC.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currLoc = (Location) task.getResult();
                        if(currLoc!=null)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currLoc.getLatitude(), currLoc.getLongitude()), DEFAULT_ZOOM));

                    } else {
                        makeText(FollowDelivererActivity.this, "Unable to get current Location", LENGTH_LONG).show();
                    }
                });
            }
        } catch (SecurityException ignored) {
        }
    }

    private LatLng getCustomerPosition() throws IOException {
        Geocoder coder = new Geocoder(this, Locale.ITALY);
        List<Address> pos = coder.getFromLocationName(customerAddress+", Torino",1);

        deliveryAddress = pos.get(0);
        double latitude = deliveryAddress.getLatitude();
        double longitude = deliveryAddress.getLongitude();
        return new LatLng(latitude, longitude);

    }

    private void setDelivererPosition(){

        if(mMap==null)
            return;
        if (latLng != null) {
            if(place2==null) {
                place2 = new MarkerOptions().position(latLng).title(name);
                   //     .icon(getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.ic_bike, null)));
                Bitmap biker = BitmapFactory.decodeResource(getResources(), R.drawable.bikerfinal);
                BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(biker);
                place2.icon(markerIcon);
                markerBike = mMap.addMarker(place2);
                markerBike.setPosition(latLng);
            }else markerBike.setPosition(latLng);
        }
    }

    private void moveCamera(LatLng latLng, float zoom, Deliverer deliverer) {

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
//                .title(title)
//                .icon(getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.ic_bike)))
                ;
        mMap.setInfoWindowAdapter(new CustomInfoMarkerAdapter(FollowDelivererActivity.this, name, photo, (Double)null));
//        mMap.addMarker(options);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
//        Toast.makeText(MapsActivity.this, "Map is ready", Toast.LENGTH_LONG).show();
        mMap = googleMap;
        mMap.clear();

        if (mLocGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            //POSSIBILITY TO ADD SOME FEATURES mMap.getUISettings().......(true);
        }
        mMap.setOnMarkerClickListener(marker -> {
            if (markerBike.equals(marker)){
                if(chosen!=null && deliveryAddress!=null) {
                    double distance = Haversine.distance(chosen.getLatitude(), chosen.getLongitude(), deliveryAddress.getLatitude(), deliveryAddress.getLongitude()) * 1000;
                    CustomInfoMarkerAdapter adapter = new CustomInfoMarkerAdapter(FollowDelivererActivity.this, name, photo, distance);
                    adapter.setDistance(distance);
                    mMap.setInfoWindowAdapter(adapter);
                }
                else Toast.makeText(this, R.string.no_internet, LENGTH_LONG).show();
            }
            else mMap.setInfoWindowAdapter(new CustomInfoMarkerAdapter(FollowDelivererActivity.this, Welcome.getProfile().getName(), Utility.StringToBitMap(Welcome.getProfile().getBitmapProf()), Welcome.getProfile().getAddress()));

            return false;
        });

        if(place1!=null)
            mMap.addMarker(place1);

        if(place2==null)
            return;

        if (place2 != null)
            mMap.addMarker(place2);

        animateCamera();

    }

    private void animateCamera(){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint(place1,place2), 13));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(place1.getPosition());
        builder.include(place2.getPosition());
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        mMap.animateCamera(cameraUpdate);
    }



    public LatLng midPoint(MarkerOptions m1,MarkerOptions m2){
        return new LatLng((m1.getPosition().latitude+m2.getPosition().latitude)/2,(m1.getPosition().longitude+m2.getPosition().longitude)/2);
    }

    private void getLocationPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUESt_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUESt_CODE);
        }
    }

    private  void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocGranted = false;

        if (requestCode == LOCATION_PERMISSION_REQUESt_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                mLocGranted = true;
                initMap();
            }
        }
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void startDelivererPosition() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("/deliverers/" + key + "/info");
        profListener = dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chosen = dataSnapshot.getValue(Deliverer.class);
                if (chosen != null) {
                    latLng = new LatLng(chosen.getLatitude(), chosen.getLongitude());
                    setDelivererPosition();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(dbRef!=null && profListener!=null)
            dbRef.removeEventListener(profListener);
    }
}

