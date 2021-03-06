package com.damn.polito.damneatdeliver.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneatdeliver.R;
import com.damn.polito.damneatdeliver.Welcome;
import com.damn.polito.damneatdeliver.beans.Profile;
import com.damn.polito.damneatdeliver.fragments.maphelpers.FetchURL;
import com.damn.polito.damneatdeliver.fragments.maphelpers.TaskLoadedCallback;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;


public class CurrentFragment extends  Fragment implements OnMapReadyCallback,TaskLoadedCallback, GoogleApiClient.ConnectionCallbacks, ResultCallback<LocationSettingsResult>, GoogleApiClient.OnConnectionFailedListener {

    private Context ctx;

    private TextView id,date, state_tv ,price,nDish, deliveryTime, name_big, address_big_text, phone_big_text, address_big, phone_big, waiting_confirm, accept_question,distance;
    private TextView name_small, address_small, phone_small, note_small;
    private TextView name_small_text, address_small_text, phone_small_text, note_small_text;
    private ConstraintLayout id_shipped;

    private CardView root, card_order, card_avaible, card_small;
    private ImageView photo,bikerIcon;
    private Button confirmButton, acceptButton, rejectButton, btnGetDirection;
    private Switch switch_available;
    private Bitmap bitmap, default_image;

    List<Address> addresses;
    private SupportMapFragment map;
    private GoogleMap gmap;
    private MarkerOptions place1,place2;
    private Polyline currentPolyline;
    private FragmentManager fm;
    private float currentDistance, traveledDistance;
    //45.061511, 7.674472
    //45.057780, 7.682858
    private Order currentOrder;


    private boolean registered = false, switch_enabled;
    protected static boolean isVisible = false;

    private FirebaseDatabase database;

    //POSITIONING DIALOG
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest locationRequest;
    int REQUEST_CHECK_SETTINGS = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.current_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = ((AppCompatActivity)getActivity());
        assert activity != null;
        Objects.requireNonNull(activity.getSupportActionBar()).setTitle(R.string.nav_current);

        ctx = view.getContext();
        database = FirebaseDatabase.getInstance();

//        id= view.findViewById(R.id.order_id);
        date = view.findViewById(R.id.order_date_value);
        //map=view.findViewById(R.id.mapView);
        map = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView);
        currentOrder = Welcome.getCurrentOrder();
        distance = view.findViewById(R.id.distance);
        bikerIcon=view.findViewById(R.id.bikerIcon);
        /*map.getMapAsync(this);
        currentOrder = Welcome.getCurrentOrder();
        List<Address> addresses = getAddresses();
        if(addresses.size() == 2){
            place1 = new MarkerOptions().position(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()))
                    .title(currentOrder.getRestaurant().getRestaurantName());
            place2 = new MarkerOptions().position(new LatLng(addresses.get(1).getLatitude(), addresses.get(1).getLongitude()))
                    .title(currentOrder.getCustomer().getCustomerName());
            String url = getUrl(place1.getPosition(),place2.getPosition(),"driving");

            new FetchURL(CurrentFragment.this).execute(url,"driving");
        }*/
//        btnGetDirection=view.findViewById(R.id.start_navigation);

        //BIG TextView
        name_big = view.findViewById(R.id.name_big_tv);
        address_big_text = view.findViewById(R.id.address_big_text);
        phone_big_text = view.findViewById(R.id.phone_big_text);
//        address_big = view.findViewById(R.id.address_big_tv);
        phone_big = view.findViewById(R.id.phone_big_tv);
        id_shipped = view.findViewById(R.id.id_shipped);

        //SMALL TextView
        card_small = view.findViewById(R.id.card_small);
        name_small = view.findViewById(R.id.name_small);
        name_small_text= view.findViewById(R.id.name_small_text);
        address_small = view.findViewById(R.id.address_small);
        address_small_text = view.findViewById(R.id.address_small_text);
        phone_small = view.findViewById(R.id.phone_small);
        phone_small_text = view.findViewById(R.id.phone_small_text);
        note_small = view.findViewById(R.id.note_small);
        note_small_text = view.findViewById(R.id.note_small_text);

        state_tv = view.findViewById(R.id.state_tv);
        confirmButton = view.findViewById(R.id.confirmOrder);
        card_avaible = view.findViewById(R.id.card_available);
        deliveryTime = view.findViewById(R.id.delivery_time);
        waiting_confirm = view.findViewById(R.id.waiting_confirm_tv);
        accept_question = view.findViewById(R.id.accept_question);

        acceptButton = view.findViewById(R.id.acceptOrder);
        rejectButton = view.findViewById(R.id.rejectOrder);
        switch_available = view.findViewById(R.id.available_switch);
        card_order = view.findViewById(R.id.card_order);
        photo = view.findViewById(R.id.circleImageView);

        default_image = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.profile_sample);

        confirmButton.setOnClickListener(v ->{
            if(currentOrder!=null){
                DatabaseReference orderState = database.getReference("ordini/" + currentOrder.getId() + "/state/");
                orderState.setValue("delivered");
            }
        });

        acceptButton.setOnClickListener(v ->{
            if(currentOrder!=null){
                if(Welcome.getProfile()== null){
                    Log.d("button", "profile null");
                    Toast.makeText(ctx, R.string.not_registered, Toast.LENGTH_LONG).show();
                }else {
                    Profile prof = Welcome.getProfile();
                    DatabaseReference orderState = database.getReference("ordini/" + currentOrder.getId() + "/state/");
                    orderState.setValue("assigned");
                    DatabaseReference orderPhoto = database.getReference("ordini/" + currentOrder.getId() + "/delivererPhoto/");
                    orderPhoto.setValue(prof.getBitmapProf());
                    DatabaseReference orderName = database.getReference("ordini/" + currentOrder.getId() + "/delivererName/");
                    orderName.setValue(prof.getName());
                    DatabaseReference orderID = database.getReference("ordini/" + currentOrder.getId() + "/delivererID/");
                    orderID.setValue(Welcome.getDbKey());

                    DatabaseReference RefLat = database.getReference("ordini/" + currentOrder.getId() + "/latitude");
                    RefLat.setValue(prof.getLatitude());
                    DatabaseReference RefLong = database.getReference("ordini/" + currentOrder.getId() + "/longitude");
                    RefLong.setValue(prof.getLongitude());
//                    DatabaseReference orderRef = database.getReference("deliverers/" + Welcome.getDbKey() + "/orders_list/" + currentOrder.getId() );
//                    orderRef.setValue(currentOrder.getId());
                }
            }
        });
        rejectButton.setOnClickListener(v ->{
            if(currentOrder!=null){
                DatabaseReference orderState = database.getReference("ordini/" + currentOrder.getId() + "/state/");
                orderState.setValue("reassign");
                DatabaseReference orderRef = database.getReference("deliverers/" + Welcome.getDbKey() + "/current_order/");
                orderRef.setValue("0");
                setVisibility("empty");
//                switch_available.setChecked(false);
            }
        });

        switch_available.setChecked(Welcome.getCurrentAvaibility());

        switch_available.setOnCheckedChangeListener((compoundButton, b) -> {
            DatabaseReference orderRef = database.getReference("/deliverers/" + Welcome.getDbKey() + "/info/state/");
            if(!Welcome.getProfile().getState().equals(b))
                orderRef.setValue(b);

//            if(b){
//                DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + Welcome.getDbKey());
//                freeDeliverersRef.setValue(Welcome.getDbKey());
////                waiting_confirm.setVisibility(View.VISIBLE);
//                Log.d("key", Welcome.getDbKey());
//            } else{
//                DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + Welcome.getDbKey());
//                freeDeliverersRef.removeValue();
////                waiting_confirm.setVisibility(GONE);
//            }
////            if(Welcome.getProfile().getLongitude()==null || Welcome.getProfile().getLatitude()==null)
//                Toast.makeText(ctx, R.string.no_position, Toast.LENGTH_LONG).show();
            ShowGPSDialog();

        });
        map.getMapAsync(this);
        update();
        removeAllVisibility();
        setVisibility(currentOrder.getState());
    }


    private void ShowGPSDialog() {
        mGoogleApiClient = new GoogleApiClient.Builder(ctx.getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(30 * 1000);
//        locationRequest.setFastestInterval(5 * 1000);
    }


    private void notRegistered(){
        if(!registered) {
            date.setVisibility(GONE);
//            id.setVisibility(GONE);
            waiting_confirm.setText(getString(R.string.not_registered));
            waiting_confirm.setVisibility(View.VISIBLE);
            card_avaible.setVisibility(GONE);
            card_small.setVisibility(GONE);
            card_order.setVisibility(GONE);
        }
    }


    public void update() {
        if(!isVisible)
            return;
        registered = Welcome.registered();

        currentOrder = Welcome.getCurrentOrder();
        if (Welcome.isSwitchEnabled()) {
            switch_available.setChecked(Welcome.getCurrentAvaibility());
        } else {
            switch_available.setChecked(Welcome.isSwitchEnabled());
        }
        switch_available.setEnabled(Welcome.isSwitchEnabled());

        if(currentOrder==null){
            currentOrder = new Order();
            currentOrder.setState("empty");
        }
        setVisibility(currentOrder.getState());

        //RETRIVE MAP ROUTES AND DISTANCE
        if(currentOrder.getState().toLowerCase().equals("accepted")||
                currentOrder.getState().toLowerCase().equals("assigned")){

            addresses = getAddressesToRestaurant();
            if(addresses.size() == 2){
                place1 = new MarkerOptions().position(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()))
                        .title(currentOrder.getDelivererName());
                place2 = new MarkerOptions().position(new LatLng(addresses.get(1).getLatitude(), addresses.get(1).getLongitude()))
                        .title(currentOrder.getRestaurant().getRestaurantName());
                String url = getUrl(place1.getPosition(),place2.getPosition(),"driving");

                new FetchURL(CurrentFragment.this).execute(url,"driving");
            }
            map.getMapAsync(this);


        }else if(currentOrder.getState().toLowerCase().equals("shipped")){

            addresses = getAddressesToCustomer();
            if(addresses.size() == 2){
                place1 = new MarkerOptions().position(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()))
                        .title(currentOrder.getDelivererName());
                place2 = new MarkerOptions().position(new LatLng(addresses.get(1).getLatitude(), addresses.get(1).getLongitude()))
                        .title(currentOrder.getCustomer().getCustomerName());
                String url = getUrl(place1.getPosition(),place2.getPosition(),"driving");

                new FetchURL(CurrentFragment.this).execute(url,"driving");
            }

            map.getMapAsync(this);
        }
        else if(currentOrder.getState().toLowerCase().equals("delivered")){
            //mostro la mappa del tragitto percorso
            addresses = new ArrayList<>();
            addresses.add(0, getAddressesToCustomer().get(1));
            addresses.add(1, getAddressesToRestaurant().get(1));

            if(addresses.size() == 2) {
                place1 = new MarkerOptions().position(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()))
                        .title(currentOrder.getDelivererName());
                place2 = new MarkerOptions().position(new LatLng(addresses.get(1).getLatitude(), addresses.get(1).getLongitude()))
                        .title(currentOrder.getCustomer().getCustomerName());
                String url = getUrl(place1.getPosition(),place2.getPosition(),"driving");

                new FetchURL(CurrentFragment.this).execute(url, "driving");
            }
            map.getMapAsync(this);
        }
        if(currentOrder.getState().toLowerCase().equals("empty")||currentOrder==null||currentOrder.getState().equals("rejected")||currentOrder.getState().equals("confirmed")||currentOrder.getState().equals("reassign")){

            map.getMapAsync(this);
        }

        if(currentOrder.getState().toLowerCase().equals("empty") || currentOrder.getState().toLowerCase().equals("ordered")){
            waiting_confirm.setText(ctx.getString(R.string.waiting_order));
            notRegistered();
            return; //BE CAREFUL TO THIS!!!!!!!!
        }else {
            date.setText(Utility.dateString(currentOrder.getDate()));
//            id.setText(currentOrder.getId());
        }

        if(currentOrder.getState().toLowerCase().equals("accepted")){

            accept_question.setText(R.string.accept_question);
            name_big.setText(currentOrder.getRestaurant().getRestaurantName());
            address_big_text.setText(currentOrder.getRestaurant().getRestaurantAddress());
            phone_big_text.setText(currentOrder.getRestaurant().getRestaurantPhone());

            phone_big.setText(ctx.getString(R.string.restaurant_phone));
//            address_big.setText(ctx.getString(R.string.restaurant_address));

            if(currentOrder.getRestaurant().getPhoto().equals("NO_PHOTO"))
                photo.setImageBitmap(default_image);
            else
                photo.setImageBitmap(Utility.StringToBitMap(currentOrder.getRestaurant().getPhoto()));
                name_small.setText(ctx.getText(R.string.customer_name));
                name_small_text.setText(currentOrder.getCustomer().getCustomerName());

                address_small.setText(ctx.getText(R.string.customer_address));
                address_small_text.setText(currentOrder.getCustomer().getCustomerAddress());

                phone_small.setText(ctx.getText(R.string.customer_phone));
                phone_small_text.setText(currentOrder.getCustomer().getCustomerPhone());

                note_small.setText(ctx.getText(R.string.note));
                note_small_text.setText(currentOrder.getNote());

                String delivery_t = currentOrder.getDeliveryTime();
                if (delivery_t.equals("ASAP")) deliveryTime.setText(R.string.time_asap);
                else deliveryTime.setText(ctx.getString(R.string.delivery_time_tv, delivery_t));

        }else {
            date.setText(Utility.dateString(currentOrder.getDate()));
//            id.setText(currentOrder.getId());

        }




        if(currentOrder.getState().toLowerCase().equals("assigned")){

            name_big.setText(currentOrder.getRestaurant().getRestaurantName());
            address_big_text.setText(currentOrder.getRestaurant().getRestaurantAddress());
            phone_big_text.setText(currentOrder.getRestaurant().getRestaurantPhone());

            phone_big.setText(ctx.getString(R.string.restaurant_phone));
//            address_big.setText(ctx.getString(R.string.restaurant_address));

            if(currentOrder.getRestaurant().getPhoto().equals("NO_PHOTO"))
                photo.setImageBitmap(default_image);
            else
                photo.setImageBitmap(Utility.StringToBitMap(currentOrder.getRestaurant().getPhoto()));
            name_small.setText(ctx.getText(R.string.customer_name));
            name_small_text.setText(currentOrder.getCustomer().getCustomerName());

            address_small.setText(ctx.getText(R.string.customer_address));
            address_small_text.setText(currentOrder.getCustomer().getCustomerAddress());

            phone_small.setText(ctx.getText(R.string.customer_phone));
            phone_small_text.setText(currentOrder.getCustomer().getCustomerPhone());

            note_small.setText(ctx.getText(R.string.note));
            note_small_text.setText(currentOrder.getNote());

            state_tv.setText(ctx.getString(R.string.state, currentOrder.getState().toLowerCase()));
        }


        if(currentOrder.getState().toLowerCase().equals("shipped")){

            name_big.setText(currentOrder.getCustomer().getCustomerName());
            address_big_text.setText(currentOrder.getCustomer().getCustomerAddress());
            phone_big_text.setText(currentOrder.getCustomer().getCustomerPhone());

            phone_big.setText(ctx.getString(R.string.customer_phone));
//            address_big.setText(ctx.getString(R.string.customer_address));

            if(currentOrder.getCustomer().getCustomerPhoto().equals("NO_PHOTO") || currentOrder.getCustomer().getCustomerPhoto().equals("") )
                photo.setImageBitmap(default_image);
            else
                photo.setImageBitmap(Utility.StringToBitMap(currentOrder.getCustomer().getCustomerPhoto()));

            name_small.setText(ctx.getText(R.string.restaurant_name));
            name_small_text.setText(currentOrder.getRestaurant().getRestaurantName());

            address_small.setText(ctx.getText(R.string.restaurant_address));
            address_small_text.setText(currentOrder.getRestaurant().getRestaurantAddress());

            phone_small.setText(ctx.getText(R.string.restaurant_phone));
            phone_small_text.setText(currentOrder.getRestaurant().getRestaurantPhone());

            note_small.setText(ctx.getText(R.string.note));
            note_small_text.setText(currentOrder.getNote());

//            deliveryTime.setText(ctx.getString(R.string.delivery_time_tv, currentOrder.getDeliveryTime()));
            String delivery_t = currentOrder.getDeliveryTime();
            if (delivery_t.equals("ASAP")) deliveryTime.setText(R.string.time_asap);
            else deliveryTime.setText(ctx.getString(R.string.delivery_time_tv, delivery_t));

            state_tv.setText(ctx.getString(R.string.state, currentOrder.getState().toLowerCase()));
            confirmButton.setText(ctx.getString(R.string.confirm_delivery));
        }



        if(currentOrder.getState().toLowerCase().equals("delivered")){
            name_big.setText(currentOrder.getCustomer().getCustomerName());
            address_big_text.setText(currentOrder.getCustomer().getCustomerAddress());
            phone_big_text.setText(currentOrder.getCustomer().getCustomerPhone());

            phone_big.setText(ctx.getString(R.string.customer_phone));
//            address_big.setText(ctx.getString(R.string.customer_address));

            if(currentOrder.getCustomer().getCustomerPhoto().equals("NO_PHOTO"))
                photo.setImageBitmap(default_image);
            else
                photo.setImageBitmap(Utility.StringToBitMap(currentOrder.getCustomer().getCustomerPhoto()));
            name_small.setText(ctx.getText(R.string.restaurant_name));
            name_small_text.setText(currentOrder.getRestaurant().getRestaurantName());

            address_small.setText(ctx.getText(R.string.restaurant_address));
            address_small_text.setText(currentOrder.getRestaurant().getRestaurantAddress());

            phone_small.setText(ctx.getText(R.string.restaurant_phone));
            phone_small_text.setText(currentOrder.getRestaurant().getRestaurantPhone());

            note_small.setText(ctx.getText(R.string.note));
            note_small_text.setText(currentOrder.getNote());

            waiting_confirm.setText(ctx.getString(R.string.waiting_customer_confirm));


            state_tv.setText(ctx.getString(R.string.state, currentOrder.getState().toLowerCase()));
        }

        state_tv.setText(ctx.getString(R.string.state, currentOrder.getState().toLowerCase()));


        if(currentOrder.getState().equals("confirmed")){
            Toast.makeText(ctx, R.string.order_completed, Toast.LENGTH_LONG).show();
            DatabaseReference orderDistance = database.getReference("ordini/" + currentOrder.getId() + "/distance/");
            orderDistance.setValue(traveledDistance);

            DatabaseReference distanceState = database.getReference("deliverers/" + Welcome.getDbKey() + "/analytics/");
            Map<String, Object> map = new HashMap<>();
            map.put(String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())),traveledDistance);
            distanceState.updateChildren(map);

            DatabaseReference orderRef = database.getReference("deliverers/" + Welcome.getDbKey() + "/current_order/");
            orderRef.setValue("0");
            setVisibility("empty");
            database.getReference("/deliverers/" + Welcome.getDbKey() + "/info/state/").setValue(true);
        }

        if(currentOrder.getState().equals("rejected")){
            DatabaseReference orderRef = database.getReference("deliverers/" + Welcome.getDbKey() + "/current_order/");
            orderRef.setValue("0");
            setVisibility("empty");
            database.getReference("/deliverers/" + Welcome.getDbKey() + "/info/state/").setValue(true);
        }

        if(currentOrder.getState().equals("reassign")){
            Toast.makeText(ctx, R.string.order_rejected, Toast.LENGTH_LONG).show();
            DatabaseReference orderRef = database.getReference("deliverers/" + Welcome.getDbKey() + "/current_order/");
            orderRef.setValue("0");
            setVisibility("empty");

        }
        notRegistered();
    }


    private void setVisibility(String state){
        removeAllVisibility();
        Log.d("registered", String.valueOf(registered));
        if(!registered) {
            waiting_confirm.setText(getString(R.string.not_registered));
            waiting_confirm.setVisibility(View.VISIBLE);
            card_avaible.setVisibility(GONE);
            card_order.setVisibility(GONE);
            return;
        }
        card_order.setVisibility(View.VISIBLE);
        if(state.equals("empty") || state.equals("ordered")|| state.equals("reassign")){
            card_avaible.setVisibility(View.VISIBLE);
            if (switch_available.isChecked()) waiting_confirm.setVisibility(View.VISIBLE);
            else waiting_confirm.setVisibility(View.GONE);
            //card_order.setVisibility(GONE);
            card_small.setVisibility(GONE);
        } else {
            card_order.setVisibility(View.VISIBLE);
            card_small.setVisibility(View.VISIBLE);
        }

        if(state.equals("shipped")) {
            date.setVisibility(View.VISIBLE);
//            id.setVisibility(View.VISIBLE);

            confirmButton.setVisibility(View.VISIBLE);
            //map.setVisibility(View.VISIBLE);
            name_small.setVisibility(View.VISIBLE);
            name_small_text.setVisibility(View.VISIBLE);
            address_small.setVisibility(View.VISIBLE);
            address_small_text.setVisibility(View.VISIBLE);
            phone_small.setVisibility(View.VISIBLE);
            phone_small_text.setVisibility(View.VISIBLE);
            note_small.setVisibility(View.VISIBLE);
            note_small_text.setVisibility(View.VISIBLE);

            deliveryTime.setVisibility(View.VISIBLE);
//            address_big.setVisibility(View.VISIBLE);
            name_big.setVisibility(View.VISIBLE);
//            address_big.setVisibility(View.VISIBLE);
            address_big_text.setVisibility(View.VISIBLE);
            phone_big.setVisibility(View.VISIBLE);
            phone_big_text.setVisibility(View.VISIBLE);
            photo.setVisibility(View.VISIBLE);
        }

        if(state.equals("accepted")){
            acceptButton.setVisibility(View.VISIBLE);
            rejectButton.setVisibility(View.VISIBLE);
            accept_question.setVisibility(View.VISIBLE);
            //map.setVisibility(View.VISIBLE);

            date.setVisibility(View.VISIBLE);
//            id.setVisibility(View.VISIBLE);

            name_small.setVisibility(View.VISIBLE);
            name_small_text.setVisibility(View.VISIBLE);
            address_small.setVisibility(View.VISIBLE);
            address_small_text.setVisibility(View.VISIBLE);
            phone_small.setVisibility(View.VISIBLE);
            phone_small_text.setVisibility(View.VISIBLE);
            note_small.setVisibility(View.VISIBLE);
            note_small_text.setVisibility(View.VISIBLE);

            deliveryTime.setVisibility(View.VISIBLE);

//            address_big.setVisibility(View.VISIBLE);
            name_big.setVisibility(View.VISIBLE);
//            address_big.setVisibility(View.VISIBLE);
            address_big_text.setVisibility(View.VISIBLE);
            phone_big.setVisibility(View.VISIBLE);
            phone_big_text.setVisibility(View.VISIBLE);
            photo.setVisibility(View.VISIBLE);

        }
        if(state.equals("assigned")){
            date.setVisibility(View.VISIBLE);
//            id.setVisibility(View.VISIBLE);
            //map.setVisibility(View.VISIBLE);

            name_small.setVisibility(View.VISIBLE);
            name_small_text.setVisibility(View.VISIBLE);
            address_small.setVisibility(View.VISIBLE);
            address_small_text.setVisibility(View.VISIBLE);
            phone_small.setVisibility(View.VISIBLE);
            phone_small_text.setVisibility(View.VISIBLE);
            note_small.setVisibility(View.VISIBLE);
            note_small_text.setVisibility(View.VISIBLE);

            deliveryTime.setVisibility(View.VISIBLE);
            state_tv.setVisibility(View.VISIBLE);

//            address_big.setVisibility(View.VISIBLE);
            name_big.setVisibility(View.VISIBLE);
//            address_big.setVisibility(View.VISIBLE);
            address_big_text.setVisibility(View.VISIBLE);
            phone_big.setVisibility(View.VISIBLE);
            phone_big_text.setVisibility(View.VISIBLE);
            photo.setVisibility(View.VISIBLE);
        }

        if(state.equals("delivered")){
            date.setVisibility(View.VISIBLE);
//            id.setVisibility(View.VISIBLE);

            name_small.setVisibility(View.VISIBLE);
            name_small_text.setVisibility(View.VISIBLE);
            address_small.setVisibility(View.VISIBLE);
            address_small_text.setVisibility(View.VISIBLE);
            phone_small.setVisibility(View.VISIBLE);
            phone_small_text.setVisibility(View.VISIBLE);
            note_small.setVisibility(View.VISIBLE);
            note_small_text.setVisibility(View.VISIBLE);

            deliveryTime.setVisibility(View.VISIBLE);
            state_tv.setVisibility(View.VISIBLE);

//            address_big.setVisibility(View.VISIBLE);
            name_big.setVisibility(View.VISIBLE);
//            address_big.setVisibility(View.VISIBLE);
            address_big_text.setVisibility(View.VISIBLE);
            phone_big.setVisibility(View.VISIBLE);
            phone_big_text.setVisibility(View.VISIBLE);
            photo.setVisibility(View.VISIBLE);
        }
        //SHOW OR HIDE MAPS
        if(state.equals("accepted")|| state.equals("assigned")||state.equals("shipped")|| state.equals("delivered")){
            fm = getFragmentManager();
            fm.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .show(map)
                    .commit();
        }
        else{
            if(map!=null){
                fm = getFragmentManager();
                fm.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .show(map)
                        .commit();
            }

        }

        //}


    }

    private void resizeFragment(Fragment f, int newWidth, int newHeight) {
        if (f != null) {
            View view = f.getView();
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(newWidth, newHeight);
            view.setLayoutParams(p);
            view.requestLayout();


        }
    }

    private void removeAllVisibility(){
        card_avaible.setVisibility(GONE);

        name_big.setVisibility(GONE);
        //map.setVisibility(GONE);

//        btnGetDirection.setVisibility(GONE);
//        address_big.setVisibility(GONE);
        address_big_text.setVisibility(GONE);
        phone_big.setVisibility(GONE);
        phone_big_text.setVisibility(GONE);

        photo.setVisibility(GONE);

        name_small.setVisibility(GONE);
        name_small_text.setVisibility(GONE);
        address_small.setVisibility(GONE);
        address_small_text.setVisibility(GONE);
        phone_small.setVisibility(GONE);
        phone_small_text.setVisibility(GONE);
        note_small.setVisibility(GONE);
        note_small_text.setVisibility(GONE);

        deliveryTime.setVisibility(GONE);
        confirmButton.setVisibility(GONE);
        waiting_confirm.setVisibility(GONE);

        state_tv.setVisibility(GONE);

        acceptButton.setVisibility(GONE);
        rejectButton.setVisibility(GONE);
        accept_question.setVisibility(GONE);
        distance.setVisibility(GONE);
        bikerIcon.setVisibility(GONE);
        date.setVisibility(GONE);
//        id.setVisibility(GONE);
    }

    private void startGoogleMaps(String address){
        //address = address + ", Torino";
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + address));
        startActivity(intent);
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        Log.d("MAP",url);
        return url;
    }


    @Override
    public void onTaskDone(Object... values) {
        if(currentPolyline!=null){
            currentPolyline.remove();
        }
        currentPolyline=gmap.addPolyline((PolylineOptions)values[0]);

        List<LatLng> latlangs = currentPolyline.getPoints();
        int size = latlangs.size() - 1;
        float[] results = new float[1];
        float sum = 0;

        for(int i = 0; i < size; i++){
            Location.distanceBetween(
                    latlangs.get(i).latitude,
                    latlangs.get(i).longitude,
                    latlangs.get(i+1).latitude,
                    latlangs.get(i+1).longitude,
                    results);
            sum += results[0];
        }
        currentDistance=sum;
        if(currentOrder.getState().toLowerCase().equals("delivered"))
            traveledDistance = currentDistance;
        //SET DISTANCE TEXT AND VISIBILITY
        distance.setText(ctx.getString(R.string.distance_format,currentDistance/1000));
        distance.setVisibility(View.VISIBLE);
        bikerIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


        if(currentOrder.getState().toLowerCase().equals("empty")||
                currentOrder.getState().equals("rejected")||
                currentOrder.getState().equals("confirmed")||
                currentOrder.getState().equals("reassign")||
                currentOrder==null){
            if(gmap!=null)
                gmap.clear();
            gmap = googleMap;
            if(Welcome.getProfile()!= null && Welcome.getProfile().getLatitude()!=null){
                LatLng latLng = new LatLng(Welcome.getProfile().getLatitude(),Welcome.getProfile().getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(getString(R.string.current_position));
                Drawable person = getResources().getDrawable(R.drawable.ic_directions_bike_black_32dp, null);
                BitmapDescriptor markerIcon = getMarkerIconFromDrawable(person);
                Bitmap biker = BitmapFactory.decodeResource(getResources(), R.drawable.bikerfinal);
                markerIcon = BitmapDescriptorFactory.fromBitmap(biker);
                markerOptions.icon(markerIcon);
                gmap.addMarker(markerOptions);

                //move map camera
                gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
            }



        }else{
            LatLngBounds BOUNDS;
            if(gmap!=null)
                gmap.clear();
            gmap = googleMap;
            Drawable bike = getResources().getDrawable(R.drawable.bikerfinal, null);
            BitmapDescriptor markerIcon = getMarkerIconFromDrawable(bike);

            if(place1==null || place2==null)
                return;
            place1.icon(markerIcon);

            gmap.addMarker(place1);
            gmap.addMarker(place2);
            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint(place1,place2), 13));

            //if(place1.getPosition().latitude<=place2.getPosition().latitude) {
            //    BOUNDS = new LatLngBounds(place1.getPosition(), place2.getPosition());
            //}
            //else{
            //   BOUNDS = new LatLngBounds(place2.getPosition(), place1.getPosition());
            //}
            // Set the camera to the greatest possible zoom level that includes the
            // bounds
            //gmap.moveCamera(CameraUpdateFactory.newLatLngBounds(BOUNDS, 15));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(place1.getPosition());
            builder.include(place2.getPosition());
            LatLngBounds bounds = builder.build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
            gmap.animateCamera(cameraUpdate);

        }

        gmap.getUiSettings().setMapToolbarEnabled(true);

    }

    private void checkLocationPermission() {
    }

    private void buildGoogleApiClient() {
    }

    public LatLng midPoint(MarkerOptions m1,MarkerOptions m2){
        return new LatLng((m1.getPosition().latitude+m2.getPosition().latitude)/2,(m1.getPosition().longitude+m2.getPosition().longitude)/2);
    }

    private List<Address> getAddressesToRestaurant() {
        List<Address> addresses = new ArrayList<>();
        Geocoder coder = new Geocoder(ctx);
        try {
            addresses.addAll(coder.getFromLocation(Welcome.getProfile().getLatitude(),Welcome.getProfile().getLongitude(),1));
            addresses.addAll(coder.getFromLocationName(currentOrder.getRestaurant().getRestaurantAddress() +", Torino", 1));
        } catch (IOException e) {
            Toast.makeText(ctx, "Address Error!", Toast.LENGTH_SHORT).show();
        }
        return addresses;
    }
    private List<Address> getAddressesToCustomer() {
        List<Address> addresses = new ArrayList<>();
        Geocoder coder = new Geocoder(ctx);
        try {
            addresses.addAll(coder.getFromLocation(Welcome.getProfile().getLatitude(),Welcome.getProfile().getLongitude(),1));
            addresses.addAll(coder.getFromLocationName(currentOrder.getCustomer().getCustomerAddress() +", Torino", 1));
        } catch (IOException e) {
            Toast.makeText(ctx, R.string.address_error, Toast.LENGTH_SHORT).show();
        }
        return addresses;
    }
    @Override
    public void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isVisible = true;
        update();
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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

                    status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);

                } catch (IntentSender.SendIntentException e) {

                    //failed to show dialog
                }
                break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are unavailable so not possible to show any dialog now
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
