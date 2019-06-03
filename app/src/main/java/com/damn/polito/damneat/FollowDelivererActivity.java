package com.damn.polito.damneat;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.damn.polito.commonresources.beans.Deliverer;
import com.damn.polito.damneat.adapters.CustomInfoMarkerAdapter;
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
import com.google.android.gms.tasks.Task;

import java.util.Objects;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

public class FollowDelivererActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int LOCATION_PERMISSION_REQUESt_CODE = 1212;
    private static final int DEFAULT_ZOOM = 13;

    private boolean mLocGranted;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFLPC;
    private Deliverer deliverer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_deliverer_map);
        getLocationPermissions();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

//        deliverer = Welcome.getDeliverer();
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

    @SuppressWarnings("unchecked")
    private void getDeviceLocation() {
        mFLPC = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocGranted) {
                Task location = mFLPC.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Location currLoc = (Location) task.getResult();

                        MarkerOptions marker = new MarkerOptions().position(new LatLng(currLoc.getLatitude(), currLoc.getLongitude())).title("Hello Maps");
//                        MarkerOptions marker = new MarkerOptions().position(new LatLng(deliverer.getLatitude(), deliverer.getLongitude())).title(deliverer.getName())
//                                .icon(getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.ic_bike, null)))
//                                ;
                        mMap.addMarker(marker);
                        moveCamera(new LatLng(deliverer.getLatitude(), deliverer.getLongitude()), DEFAULT_ZOOM, deliverer);

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currLoc.getLatitude(), currLoc.getLongitude()), DEFAULT_ZOOM));

                    } else {
                        makeText(FollowDelivererActivity.this, "Unable to get current Location", LENGTH_LONG).show();
                    }
                });
            }
        } catch (SecurityException e) {

        }
    }

    private void moveCamera(LatLng latLng, float zoom, Deliverer deliverer) {

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
//                .title(title)
//                .icon(getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.ic_bike)))
                ;
        mMap.setInfoWindowAdapter(new CustomInfoMarkerAdapter(FollowDelivererActivity.this, deliverer));
//        mMap.addMarker(options);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
//        Toast.makeText(MapsActivity.this, "Map is ready", Toast.LENGTH_LONG).show();
        mMap = googleMap;

        if (mLocGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            //POSSIBILITY TO ADD SOME FEATURES mMap.getUISettings().......(true);
        }
        mMap.setOnMarkerClickListener(marker -> {
            mMap.setInfoWindowAdapter(new CustomInfoMarkerAdapter(FollowDelivererActivity.this, deliverer));
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

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}

