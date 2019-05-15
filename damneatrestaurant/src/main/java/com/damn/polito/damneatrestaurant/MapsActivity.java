package com.damn.polito.damneatrestaurant;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final int LOCATION_PERMISSION_REQUESt_CODE = 1212;

    private boolean mLocGranted;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFLPC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        getLocationPermissions();
    }

    private void getDeviceLocation() {
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mFLPC = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocGranted) {
                Task location = mFLPC.getLastLocation();//setlocation with the variable;
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Location currLoc = (Location) task.getResult();

                        MarkerOptions marker = new MarkerOptions().position(new LatLng(currLoc.getLatitude(), currLoc.getLongitude())).title("Hello Maps");

                        moveCamera(new LatLng(currLoc.getLatitude(), currLoc.getLongitude()), 15, "MyLocation");

                    } else {
                        Toast.makeText(MapsActivity.this, "Unable to get current Location", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (SecurityException e) {

        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title)
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bike))
                ;
        mMap.addMarker(options);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(MapsActivity.this, "Map is ready", Toast.LENGTH_LONG).show();
        mMap = googleMap;

        if (mLocGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mMap.setMyLocationEnabled(true);
            //POSSIBILITY TO ADD SOME FEATURES mMap.getUISettings().......(true);
        }
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
        mapFragment.getMapAsync(this);
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
                    initMap();
                }
            }
        }
    }
}
