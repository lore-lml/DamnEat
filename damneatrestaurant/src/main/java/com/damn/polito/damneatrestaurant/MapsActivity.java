package com.damn.polito.damneatrestaurant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.damn.polito.commonresources.beans.Deliverer;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Objects;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int LOCATION_PERMISSION_REQUESt_CODE = 1212;
    private static final int DEFAULT_ZOOM = 13;

    private boolean mLocGranted;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFLPC;
    private Button choose_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        getLocationPermissions();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        choose_button = findViewById(R.id.choose_button);
        choose_button.setVisibility(View.GONE);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getDeviceLocation() {
        mFLPC = LocationServices.getFusedLocationProviderClient(this);
        Intent intent = getIntent();

        try {

            if (mLocGranted) {
                Task location = mFLPC.getLastLocation();//setlocation with the variable;
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Location currLoc = (Location) task.getResult();

//                        MarkerOptions marker = new MarkerOptions().position(new LatLng(currLoc.getLatitude(), currLoc.getLongitude())).title("Hello Maps");
                        List<Deliverer> deliverers = (List<Deliverer>) intent.getSerializableExtra("deliverers");
                        for (Deliverer d : deliverers)  {
                            MarkerOptions marker = new MarkerOptions().position(new LatLng(d.getLatitude(), d.getLongitude())).title(d.getName())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                            mMap.addMarker(marker);
                        }

                        moveCamera(new LatLng(currLoc.getLatitude(), currLoc.getLongitude()), DEFAULT_ZOOM, "MyLocation");

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
//                .title(title)
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bike))
                ;
//        mMap.addMarker(options);
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
            mMap.getUiSettings().setMapToolbarEnabled(false);
            //POSSIBILITY TO ADD SOME FEATURES mMap.getUISettings().......(true);
        }

        mMap.setOnMarkerClickListener(marker -> {
            choose_button.setVisibility(View.VISIBLE);
            choose_button.setOnClickListener(v -> {
//                DelivererAdapter.pick();
            });
            return false;
        });
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
