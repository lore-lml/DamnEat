package com.damn.polito.damneat.fragments;

import android.app.Activity;
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

import com.damn.polito.commonresources.FirebaseLogin;
import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneat.EditProfile;
import com.damn.polito.damneat.R;
import com.damn.polito.damneat.Welcome;
import com.damn.polito.damneat.beans.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private String defaultValue;
    private ImageView profileImage;
    private TextView name, mail, description, address, phone;
    private Bitmap profileBitmap;
    //private boolean empty = true;
    private Context ctx;

    private FirebaseDatabase database;
    private String dbKey;

    private Profile prof;
    private Map<String, Object> orders;


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

        dbKey = PreferenceManager.getDefaultSharedPreferences(ctx).getString("dbkey", null);
        prof = new Profile();

        database = FirebaseDatabase.getInstance();

        updateProfile();
    }

    private void editProfile() {
        //Crea il corretto intent per l'apertura dell'activity EditProfile
        Intent intent = new Intent(ctx, EditProfile.class);

        //Se il profilo esisteva, passa le informazioni a EditProfile
        if (Welcome.accountExist && !name.getText().toString().trim().equals(defaultValue)) {
            intent.putExtra("name", name.getText().toString().trim());
            intent.putExtra("mail", mail.getText().toString().trim());
            intent.putExtra("phone", phone.getText().toString().trim());
            intent.putExtra("description", description.getText().toString().trim());
            intent.putExtra("address", address.getText().toString().trim());
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
        boolean hasChanged = data.getBooleanExtra("hasChanged", false);
        if(!hasChanged) return;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        //boolean hasProfile = false;
        String name = data.getStringExtra("name");
        String mail = data.getStringExtra("mail");
        String phone = data.getStringExtra("phone");
        String description = data.getStringExtra("description");
        String address = data.getStringExtra("address");
        String bitmapProf = pref.getString("profile", null);
        if(bitmapProf!= null) {
            profileBitmap = Utility.StringToBitMap(bitmapProf);
            //hasProfile = true;
            pref.edit().remove("profile").apply();
        }
        //
        //CARICO I DATI SU FIREBASE
        prof = new Profile(name,mail,phone,description,address,bitmapProf);
        storeProfileOnFirebase(prof);

        /*this.name.setText(name);
        this.mail.setText(mail);
        this.phone.setText(phone);
        this.description.setText(description);
        this.address.setText(address);
        if (profileBitmap != null) profileImage.setImageBitmap(profileBitmap);
        Welcome.accountExist = true;*/
    }

    private void storeProfileOnFirebase(Profile profile){
        if(dbKey == null) return;
        DatabaseReference myRef;
        DatabaseReference ordini;

        myRef = database.getReference("clienti/" + dbKey);
        ordini = database.getReference("clienti/" + dbKey + "/lista_ordini");


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
                    if(orders != null && orders.size() != 0)
                        ordini.updateChildren(orders);
                    /*SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();

                    editor.putString("clientaddress", profile.getAddress());
                    editor.putString("clientname", profile.getName());
                    editor.putString("clientphone", profile.getPhone());
                    editor.putString("clientmail", profile.getMail());
                    editor.apply();*/
                }
            }
        });
    }
    public void updateProfile(){
        loadData();
        if(name != null)
            name.setText(stringOrDefault(prof.getName()));
        if(mail != null)
            mail.setText(stringOrDefault(prof.getMail()));
        if(phone != null)
            phone.setText(stringOrDefault(prof.getPhone()));
        if(description != null)
            description.setText(stringOrDefault(prof.getDescription()));
        if(address != null)
            address.setText(stringOrDefault(prof.getAddress()));

        if(profileImage != null){
            profileBitmap = Utility.StringToBitMap(prof.getBitmapProf());
            if(profileBitmap != null)
                profileImage.setImageBitmap(profileBitmap);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        if(!Welcome.accountExist) return;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);

        prof.setAddress(pref.getString("address", ""));
        prof.setName(pref.getString("name", ""));
        prof.setMail(pref.getString("mail", ""));
        prof.setPhone(pref.getString("phone", ""));
        prof.setDescription(pref.getString("description", ""));
        prof.setBitmapProf(pref.getString("profile", ""));


        DatabaseReference ordini = database.getReference("clienti/"+ dbKey +"/lista_ordini");
        ordini.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!= null)
                    orders = (Map)dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ctx, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
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
            case R.id.item_disconnect:
                FirebaseLogin.logout((Activity) ctx);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
