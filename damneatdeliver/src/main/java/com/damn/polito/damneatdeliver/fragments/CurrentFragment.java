package com.damn.polito.damneatdeliver.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Deliverer;
import com.damn.polito.commonresources.beans.Haversine;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneatdeliver.R;
import com.damn.polito.damneatdeliver.Welcome;
import com.damn.polito.damneatdeliver.beans.Profile;
import com.damn.polito.damneatdeliver.fragments.maphelpers.FetchURL;
import com.damn.polito.damneatdeliver.fragments.maphelpers.TaskLoadedCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import static android.view.View.GONE;


public class CurrentFragment extends  Fragment implements OnMapReadyCallback,TaskLoadedCallback {

    private Context ctx;

    private TextView id,date, state_tv ,price,nDish, deliveryTime, name_big, address_big_text, phone_big_text, address_big, phone_big, waiting_confirm, accept_question;
    private TextView name_small, address_small, phone_small, note_small;
    private TextView name_small_text, address_small_text, phone_small_text, note_small_text;
    private ConstraintLayout id_shipped;

    private CardView root, card_order, card_avaible, card_small;
    private ImageView photo;
    private Button confirmButton, acceptButton, rejectButton, btnGetDirection;
    private Switch switch_available;
    private Bitmap bitmap, default_image;

    private SupportMapFragment map;
    private GoogleMap gmap;
    private MarkerOptions place1,place2;
    private Polyline currentPolyline;
    FragmentManager fm;
    //45.061511, 7.674472
    //45.057780, 7.682858
    private Order currentOrder;

    private boolean registered = false;

    private FirebaseDatabase database;


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

        id= view.findViewById(R.id.order_id);
        date = view.findViewById(R.id.order_date_value);
        //map=view.findViewById(R.id.mapView);
        map = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView);
        map.getMapAsync(this);
        place1 = new MarkerOptions().position(new LatLng(45.061511, 7.674472)).title("Location 1");
        place2 = new MarkerOptions().position(new LatLng(45.057780, 7.682858)).title("Location 2");
        String url = getUrl(place1.getPosition(),place2.getPosition(),"driving");
        //new FetchURL(ctx).execute(url,"driving");

        btnGetDirection=view.findViewById(R.id.start_navigation);




        //BIG TextView
        name_big = view.findViewById(R.id.name_big_tv);
        address_big_text = view.findViewById(R.id.address_big_text);
        phone_big_text = view.findViewById(R.id.phone_big_text);
        address_big = view.findViewById(R.id.address_big_tv);
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

        btnGetDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGoogleMaps((String) address_big_text.getText());
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
                    DatabaseReference orderRef = database.getReference("deliverers/" + Welcome.getKey() + "/orders_list/" + currentOrder.getId() );
                    orderRef.setValue(currentOrder.getId());
                }
            }
        });
        rejectButton.setOnClickListener(v ->{
            if(currentOrder!=null){
                DatabaseReference orderState = database.getReference("ordini/" + currentOrder.getId() + "/state/");
                orderState.setValue("rejected");
                DatabaseReference orderRef = database.getReference("deliverers/" + Welcome.getKey() + "/current_order/");
                orderRef.setValue("0");
                setVisibility("empty");
            }
        });

        switch_available.setChecked(Welcome.getCurrentAvaibility());
        /*switch_available.setOnClickListener(v -> {
            Boolean available = Welcome.getCurrentAvaibility();
            DatabaseReference orderRef = database.getReference("/deliverers/" + Welcome.getKey() + "/state/");
            orderRef.setValue(!available);
            //switch_available.setChecked(!available);
            if(!available){
                DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + Welcome.getKey());
                freeDeliverersRef.setValue(Welcome.getKey());
                //Welcome.setCurrentAvaibility(true);
                Log.d("key", Welcome.getKey());
            } else{
                DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + Welcome.getKey());
                freeDeliverersRef.removeValue();
                //Welcome.setCurrentAvaibility(false);
            }
        });*/

        switch_available.setOnCheckedChangeListener((compoundButton, b) -> {
            DatabaseReference orderRef = database.getReference("/deliverers/" + Welcome.getKey() + "/info/state/");
            orderRef.setValue(b);
            if(b){
                DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + Welcome.getKey());
                freeDeliverersRef.setValue(Welcome.getKey());
                Log.d("key", Welcome.getKey());
            } else{
                DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + Welcome.getKey());
                freeDeliverersRef.removeValue();
            }
        });


        update();

    }

    private void notRegistered(){
        if(!registered) {
            date.setVisibility(GONE);
            id.setVisibility(GONE);
            waiting_confirm.setText(getString(R.string.not_registered));
            waiting_confirm.setVisibility(View.VISIBLE);
            card_avaible.setVisibility(GONE);
        }
    }


    public void update() {
        registered = Welcome.registered();

        currentOrder = Welcome.getCurrentOrder();
        switch_available.setChecked(Welcome.getCurrentAvaibility());

        if(currentOrder==null){
            currentOrder = new Order();
            currentOrder.setState("empty");
        }
        setVisibility(currentOrder.getState());

        if(currentOrder.getState().toLowerCase().equals("empty") || currentOrder.getState().toLowerCase().equals("ordered")){
            waiting_confirm.setText(ctx.getString(R.string.waiting_order));
            notRegistered();
            return;
        }else {
            date.setText(Utility.dateString(currentOrder.getDate()));
            id.setText(currentOrder.getId());
        }

        if(currentOrder.getState().toLowerCase().equals("accepted")){
            //map.setOnClickListener(v -> {});

            accept_question.setText(R.string.accept_question);
            name_big.setText(currentOrder.getRestaurant().getRestaurantName());
            address_big_text.setText(currentOrder.getRestaurant().getRestaurantAddress());
            phone_big_text.setText(currentOrder.getRestaurant().getRestaurantPhone());

            phone_big.setText(ctx.getString(R.string.restaurant_phone));
            address_big.setText(ctx.getString(R.string.restaurant_address));

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
                deliveryTime.setText(ctx.getString(R.string.delivery_time_tv, delivery_t));

        }else {
            date.setText(Utility.dateString(currentOrder.getDate()));
            id.setText(currentOrder.getId());

        }




        if(currentOrder.getState().toLowerCase().equals("assigned")){
            //map.setOnClickListener(v -> startGoogleMaps(currentOrder.getRestaurant().getRestaurantAddress()));

            name_big.setText(currentOrder.getRestaurant().getRestaurantName());
            address_big_text.setText(currentOrder.getRestaurant().getRestaurantAddress());
            phone_big_text.setText(currentOrder.getRestaurant().getRestaurantPhone());

            phone_big.setText(ctx.getString(R.string.restaurant_phone));
            address_big.setText(ctx.getString(R.string.restaurant_address));

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
            //map.setOnClickListener(v -> startGoogleMaps(currentOrder.getCustomer().getCustomerAddress()));

            name_big.setText(currentOrder.getCustomer().getCustomerName());
            address_big_text.setText(currentOrder.getCustomer().getCustomerAddress());
            phone_big_text.setText(currentOrder.getCustomer().getCustomerPhone());

            phone_big.setText(ctx.getString(R.string.customer_phone));
            address_big.setText(ctx.getString(R.string.customer_address));

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

            deliveryTime.setText(ctx.getString(R.string.delivery_time_tv, currentOrder.getDeliveryTime()));

            state_tv.setText(ctx.getString(R.string.state, currentOrder.getState().toLowerCase()));
            confirmButton.setText(ctx.getString(R.string.confirm_delivery));
        }



        if(currentOrder.getState().toLowerCase().equals("delivered")){
            //map.setOnClickListener(v -> {});
            name_big.setText(currentOrder.getCustomer().getCustomerName());
            address_big_text.setText(currentOrder.getCustomer().getCustomerAddress());
            phone_big_text.setText(currentOrder.getCustomer().getCustomerPhone());

            phone_big.setText(ctx.getString(R.string.customer_phone));
            address_big.setText(ctx.getString(R.string.customer_address));

            if(currentOrder.getCustomer().getCustomerPhoto().equals("NO_PHOTO"))
                photo.setImageBitmap(default_image);
            else
                photo.setImageBitmap(Utility.StringToBitMap(currentOrder.getCustomer().getCustomerPhoto()));


            waiting_confirm.setText(ctx.getString(R.string.waiting_customer_confirm));


            state_tv.setText(ctx.getString(R.string.state, currentOrder.getState().toLowerCase()));
        }

        state_tv.setText(ctx.getString(R.string.state, currentOrder.getState().toLowerCase()));


        if(currentOrder.getState().equals("confirmed")){
            //map.setOnClickListener(v -> {});
            Toast.makeText(ctx, R.string.order_completed, Toast.LENGTH_LONG).show();
            DatabaseReference orderRef = database.getReference("deliverers/" + Welcome.getKey() + "/current_order/");
            orderRef.setValue("0");
            setVisibility("empty");

        }

        if(currentOrder.getState().equals("rejected")){
            Toast.makeText(ctx, R.string.order_rejected, Toast.LENGTH_LONG).show();
            DatabaseReference orderRef = database.getReference("deliverers/" + Welcome.getKey() + "/current_order/");
            orderRef.setValue("0");
            setVisibility("empty");

        }

//        if(currentOrder.getState().equals("ordered")){
//            Toast.makeText(ctx, R.string.order_rejected, Toast.LENGTH_LONG).show();
//            DatabaseReference orderRef = database.getReference("deliverers/" + Welcome.getKey() + "/current_order/");
//            orderRef.removeValue();
//            DatabaseReference freeDeliverersRef = database.getReference("/deliverers_liberi/" + Welcome.getKey());
//            freeDeliverersRef.setValue(Welcome.getKey());
//        }

        notRegistered();
    }

//    public void checkRegistered(){
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
//
//        String dbKey = pref.getString("dbkey", null);
//
//        FirebaseDatabase database= FirebaseDatabase.getInstance();
//        DatabaseReference profileRef = database.getReference("/deliverers/" + dbKey );
//        profileRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Deliverer del = dataSnapshot.getValue(Deliverer.class);
//                if(del==null){
//                    registered = false;
//                }
//                else registered = true;
//                update();
//                Log.d("registered", String.valueOf(registered));
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }


    private void setVisibility(String state){
        removeAllVisibility();
        Log.d("registered", String.valueOf(registered));
        if(!registered) {
            waiting_confirm.setText(getString(R.string.not_registered));
            waiting_confirm.setVisibility(View.VISIBLE);
            return;
        }

        if(state.equals("empty") || state.equals("ordered")){
            card_avaible.setVisibility(View.VISIBLE);
            waiting_confirm.setVisibility(View.VISIBLE);
        }
        if(state.equals("shipped")) {
            date.setVisibility(View.VISIBLE);
            id.setVisibility(View.VISIBLE);

            confirmButton.setVisibility(View.VISIBLE);
            //map.setVisibility(View.VISIBLE);
            fm = getFragmentManager();
            fm.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .show(map)
                    .commit();
            name_small.setVisibility(View.VISIBLE);
            name_small_text.setVisibility(View.VISIBLE);
            address_small.setVisibility(View.VISIBLE);
            address_small_text.setVisibility(View.VISIBLE);
            phone_small.setVisibility(View.VISIBLE);
            phone_small_text.setVisibility(View.VISIBLE);
            note_small.setVisibility(View.VISIBLE);
            note_small_text.setVisibility(View.VISIBLE);

            deliveryTime.setVisibility(View.VISIBLE);
            address_big.setVisibility(View.VISIBLE);
            name_big.setVisibility(View.VISIBLE);
            address_big.setVisibility(View.VISIBLE);
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
            fm = getFragmentManager();
            fm.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .show(map)
                    .commit();
            btnGetDirection.setVisibility(View.VISIBLE);
            date.setVisibility(View.VISIBLE);
            id.setVisibility(View.VISIBLE);

            name_small.setVisibility(View.VISIBLE);
            name_small_text.setVisibility(View.VISIBLE);
            address_small.setVisibility(View.VISIBLE);
            address_small_text.setVisibility(View.VISIBLE);
            phone_small.setVisibility(View.VISIBLE);
            phone_small_text.setVisibility(View.VISIBLE);
            note_small.setVisibility(View.VISIBLE);
            note_small_text.setVisibility(View.VISIBLE);

            deliveryTime.setVisibility(View.VISIBLE);

            address_big.setVisibility(View.VISIBLE);
            name_big.setVisibility(View.VISIBLE);
            address_big.setVisibility(View.VISIBLE);
            address_big_text.setVisibility(View.VISIBLE);
            phone_big.setVisibility(View.VISIBLE);
            phone_big_text.setVisibility(View.VISIBLE);
            photo.setVisibility(View.VISIBLE);

        }
        if(state.equals("assigned")){
            date.setVisibility(View.VISIBLE);
            id.setVisibility(View.VISIBLE);
            //map.setVisibility(View.VISIBLE);
            fm = getFragmentManager();
            fm.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .show(map)
                    .commit();
            btnGetDirection.setVisibility(View.VISIBLE);
            btnGetDirection.setVisibility(View.VISIBLE);
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

            address_big.setVisibility(View.VISIBLE);
            name_big.setVisibility(View.VISIBLE);
            address_big.setVisibility(View.VISIBLE);
            address_big_text.setVisibility(View.VISIBLE);
            phone_big.setVisibility(View.VISIBLE);
            phone_big_text.setVisibility(View.VISIBLE);
            photo.setVisibility(View.VISIBLE);
        }

        if(state.equals("delivered")){
            date.setVisibility(View.VISIBLE);
            id.setVisibility(View.VISIBLE);

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

            address_big.setVisibility(View.VISIBLE);
            name_big.setVisibility(View.VISIBLE);
            address_big.setVisibility(View.VISIBLE);
            address_big_text.setVisibility(View.VISIBLE);
            phone_big.setVisibility(View.VISIBLE);
            phone_big_text.setVisibility(View.VISIBLE);
            photo.setVisibility(View.VISIBLE);
        }

        //}


    }

    private void removeAllVisibility(){
        card_avaible.setVisibility(GONE);

        name_big.setVisibility(GONE);
        //map.setVisibility(GONE);
        fm = getFragmentManager();
        fm.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .hide(map)
                .commit();
        btnGetDirection.setVisibility(GONE);
        address_big.setVisibility(GONE);
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

        date.setVisibility(GONE);
        id.setVisibility(GONE);
    }

    private void startGoogleMaps(String address){
        address = address + ", Torino";
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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap=googleMap;
        gmap.addMarker(place1);
        gmap.addMarker(place2);

        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint(place1,place2), 13));
    }

    public LatLng midPoint(MarkerOptions m1,MarkerOptions m2){
        return new LatLng((m1.getPosition().latitude+m2.getPosition().latitude)/2,(m1.getPosition().longitude+m2.getPosition().longitude)/2);
    }
}
