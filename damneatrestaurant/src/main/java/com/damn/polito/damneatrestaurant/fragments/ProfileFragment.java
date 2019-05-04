package com.damn.polito.damneatrestaurant.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneatrestaurant.EditProfile;
import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.beans.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment{

    private String defaultValue, dbkey;
    private ImageView profileImage;
    private TextView name, mail, description, address, phone, opening, categories, shipPrice;
    private Bitmap profileBitmap;
    private boolean empty = true;
    private Context ctx;
    private String dbKey;
    private FirebaseDatabase database;

    //private Map<String, Object> orders;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.profile_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = ((AppCompatActivity)getActivity());
        assert activity != null;
        Objects.requireNonNull(activity.getSupportActionBar()).setTitle(R.string.alert_edit_profile_title);

        ctx = view.getContext();

        defaultValue = getString(R.string.nullText);

        profileImage = view.findViewById(R.id.profile_image);
        name = view.findViewById(R.id.editText_name);
        mail = view.findViewById(R.id.editText_email);
        phone = view.findViewById(R.id.editText_phone);
        description = view.findViewById(R.id.editText_desc);
        address = view.findViewById(R.id.editText_address);
        opening = view.findViewById(R.id.editText_opening);
        categories = view.findViewById(R.id.editText_category);
        shipPrice = view.findViewById(R.id.editText_shipprice);
        database = FirebaseDatabase.getInstance();
        loadData();
    }

    private void editProfile() {
        //Crea il corretto intent per l'apertura dell'activity EditProfile
        Intent intent = new Intent(ctx, EditProfile.class);

        //Se il profilo esisteva, passa le informazioni a EditProfile
        if (!empty) {
            intent.putExtra("name", name.getText().toString().trim());
            intent.putExtra("mail", mail.getText().toString().trim());
            intent.putExtra("phone", phone.getText().toString().trim());
            intent.putExtra("description", description.getText().toString().trim());
            intent.putExtra("address", address.getText().toString().trim());
            intent.putExtra("opening", opening.getText().toString().trim());
            intent.putExtra("categories", categories.getText().toString().trim());
            intent.putExtra("shipprice", shipPrice.getText().toString().trim());
            intent.putExtra("image", profileImage.getDrawable().toString());
            if (profileBitmap != null){
                PreferenceManager.getDefaultSharedPreferences(ctx)
                        .edit().putString("profile", Utility.BitMapToString(profileBitmap)).apply();
            }

        }
        startActivityForResult(intent, 1);
    }

    private String stringOrDefault(String s) {
        return (s == null || s.trim().isEmpty()) ? defaultValue : s;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            storeData(data);
        }
    }

    private void storeData(Intent data) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean hasProfile = false;
        String name = data.getStringExtra("name");
        String mail = data.getStringExtra("mail");
        String phone = data.getStringExtra("phone");
        String description = data.getStringExtra("description");
        String address = data.getStringExtra("address");
        String opening = data.getStringExtra("opening");
        String categories = data.getStringExtra("categories");
        String shipprice = data.getStringExtra("shipprice");
        String bitmapProf = pref.getString("profile", null);
        if(bitmapProf!= null) {
            profileBitmap = Utility.StringToBitMap(bitmapProf);
            hasProfile = true;
            pref.edit().remove("profile").apply();
        }
        double priceship = shipprice.equals(getString(R.string.price_free)) ? 0.0 : Double.valueOf(shipprice.replace(",","."));
        //
        //CARICO I DATI SU FIREBASE
        storeProfileOnFirebase(new Profile(name,mail,phone,description,address, opening, categories, priceship, bitmapProf));
        //code
        //

        this.name.setText(name);
        this.mail.setText(mail);
        this.phone.setText(phone);
        this.description.setText(description);
        this.address.setText(address);
        this.opening.setText(opening);
        this.categories.setText(categories);
        if(!shipprice.equals(getString(R.string.price_free)))
            this.shipPrice.setText(getString(R.string.order_price, Double.valueOf(shipprice.replace(",","."))));
        else
            this.shipPrice.setText(shipprice);
        if (profileBitmap != null) profileImage.setImageBitmap(profileBitmap);
        empty = false;

        /*
        JSONObject values = new JSONObject();
        try {

            values.put("name", name);
            values.put("mail", mail);
            values.put("phone", phone);
            values.put("description", description);
            values.put("address", address);
            values.put("opening", opening);
            values.put("categories", categories);
            values.put("shipprice", shipprice);
            if (profileBitmap != null) {
                String bts = Utility.BitMapToString(profileBitmap);
                values.put("profile", bts);
                hasProfile = true;
            }
            values.put("hasProfile", hasProfile);


            pref.edit().putString("info", values.toString()).apply();

            this.name.setText(name);
            this.mail.setText(mail);
            this.phone.setText(phone);
            this.description.setText(description);
            this.address.setText(address);
            this.opening.setText(opening);
            this.shipPrice.setText(shipprice);
            this.categories.setText(categories);
            if (profileBitmap != null) profileImage.setImageBitmap(profileBitmap);

            empty = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    private void storeProfileOnFirebase(Profile profile){

        //DA IMPLEMENTARE RETURN TRUE OR FALSE A SECONDA CHE LA TRANSAZIONE VADA A BUONFINE
        //
        //NON è IMPLEMENTATA LA POSSIBILITà DI MODIFICARE L'ACCOUNT, SI PUO' PER ADESSO SOLO
        //CREARNE DI NUOVI

        //SE LA MAIL ESISTE NON SI SALVA NULLA, SE NON ESISTE SI CREA UNA NUOVA KEY SU FIREBASE
        DatabaseReference myRef;
        //DatabaseReference ordini;

        myRef = database.getReference("ristoratori/" + dbKey);
            //ordini = database.getReference("ristoratori/" + dbKey + "/lista_ordini");

        myRef.runTransaction(new Transaction.Handler(){
            @NonNull
            @Override
            public Transaction.Result doTransaction (@NonNull MutableData currentData){
                currentData.setValue(profile);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot currentData){
                //this method will be called once with the result of the transaction
                if(committed) {
                    /*if(orders != null && orders.size() != 0)
                        ordini.updateChildren(orders);*/
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
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
            }
        });
    }

    private void loadData() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);

            if(dbKey == null) {
                dbKey = pref.getString("dbkey", null);
                if (dbKey == null) return;
            }

            DatabaseReference myRef = database.getReference("ristoratori/" + dbKey);

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Profile prof = dataSnapshot.getValue(Profile.class);
                    if (prof != null) {
                        name.setText(prof.getName());
                        mail.setText(prof.getMail());
                        phone.setText(prof.getPhone());
                        description.setText(prof.getDescription());
                        address.setText(prof.getAddress());
                        opening.setText(prof.getOpening());
                        categories.setText(prof.getCategories());
                        if(prof.getPriceShip() != null && !prof.getPriceShip().equals(0.0))
                            ProfileFragment.this.shipPrice.setText(getString(R.string.order_price, prof.getPriceShip()));
                        else
                            ProfileFragment.this.shipPrice.setText(String.valueOf(0.0));
                        if (prof.getImage() != null) {
                            String encodedBitmap = prof.getImage();
                            profileBitmap = Utility.StringToBitMap(encodedBitmap);
                            if (profileBitmap != null)
                                profileImage.setImageBitmap(profileBitmap);
                        }
                        empty = false;
                    }else{
                        name.setText(defaultValue);
                        mail.setText(defaultValue);
                        phone.setText(defaultValue);
                        description.setText(defaultValue);
                        address.setText(defaultValue);
                        opening.setText(defaultValue);
                        categories.setText(defaultValue);
                        shipPrice.setText(R.string.price_free);
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ctx, "Database Error", Toast.LENGTH_SHORT).show();
                }
            });

            /*DatabaseReference ordini = database.getReference("ristoratori/"+ dbKey +"/lista_ordini");
            ordini.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    orders = (Map)dataSnapshot.getValue();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ctx, "Database Error", Toast.LENGTH_SHORT).show();
                }
            });*/





            /* PARTE CHE CARICA DALLE SHARED
            name.setText(stringOrDefault(values.getString("name")));
            mail.setText(stringOrDefault(values.getString("mail")));
            phone.setText(stringOrDefault(values.getString("phone")));
            description.setText(stringOrDefault(values.getString("description")));
            address.setText(stringOrDefault(values.getString("address")));
            opening.setText(stringOrDefault(values.getString("opening")));
            if (values.getBoolean("hasProfile")) {
                String encodedBitmap = values.getString("profile");
                profileBitmap = Utility.StringToBitMap(encodedBitmap);
                if (profileBitmap != null)
                    profileImage.setImageBitmap(profileBitmap);
            }
            empty = false;
            */




    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_profile, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.item_edit:
                editProfile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
