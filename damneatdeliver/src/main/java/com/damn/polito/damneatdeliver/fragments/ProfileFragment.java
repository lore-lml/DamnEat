package com.damn.polito.damneatdeliver.fragments;

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

import com.damn.polito.commonresources.FirebaseLogin;
import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneatdeliver.Analytics;
import com.damn.polito.damneatdeliver.EditProfile;
import com.damn.polito.damneatdeliver.Welcome;
import com.damn.polito.damneatdeliver.beans.Profile;
import com.damn.polito.damneatdeliver.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static java.lang.Thread.sleep;

public class ProfileFragment extends Fragment{

    private String defaultValue;
    private ImageView profileImage;
    private TextView name, mail, description, address, phone;
    private Bitmap profileBitmap;
    private boolean empty = true;
    private Context ctx;

    private FirebaseDatabase database;
    private String dbKey;

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
        database = FirebaseDatabase.getInstance();

        loadData(Welcome.getProfile());
    }

    private void editProfile() {
        //Crea il corretto intent per l'apertura dell'activity EditProfile
        Intent intent = new Intent(ctx, EditProfile.class);

        //Se il profilo esisteva, passa le informazioni a EditProfile
        if (!empty && !name.getText().toString().trim().equals(defaultValue)) {
            intent.putExtra("name", name.getText().toString().trim());
            intent.putExtra("mail", mail.getText().toString().trim());
            intent.putExtra("phone", phone.getText().toString().trim());
            intent.putExtra("description", description.getText().toString().trim());
            intent.putExtra("image", profileImage.getDrawable().toString());
            if (profileBitmap != null){
                PreferenceManager.getDefaultSharedPreferences(ctx)
                        .edit().putString("profile", Utility.BitMapToString(profileBitmap)).apply();
            }

        }
        startActivityForResult(intent, 1);
    }

    private void showAnalytics(){
        Intent intent = new Intent(ctx, Analytics.class);

        //Se il profilo esisteva, passa le informazioni a EditProfile
        if (!empty && !name.getText().toString().trim().equals(defaultValue)) {
            intent.putExtra("name", name.getText().toString().trim());
            intent.putExtra("mail", mail.getText().toString().trim());
            intent.putExtra("phone", phone.getText().toString().trim());
            intent.putExtra("description", description.getText().toString().trim());
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
        String name = data.getStringExtra("name");
        String mail = data.getStringExtra("mail");
        String phone = data.getStringExtra("phone");
        String description = data.getStringExtra("description");
        String bitmapProf = pref.getString("profile", null);
        if(bitmapProf!= null) {
            profileBitmap = Utility.StringToBitMap(bitmapProf);
            pref.edit().remove("profile").apply();
        }

        storeProfileOnFirebase(new Profile(name,mail,phone,description,bitmapProf));

        this.name.setText(name);
        this.mail.setText(mail);
        this.phone.setText(phone);
        this.description.setText(description);
        if (profileBitmap != null) profileImage.setImageBitmap(profileBitmap);
        empty = false;
    }

    private void loadData(Profile prof) {
        if (prof != null) {
            name.setText(prof.getName());
            mail.setText(prof.getMail());
            phone.setText(prof.getPhone());
            description.setText(prof.getDescription());
            if (prof.getBitmapProf() != null) {
                String encodedBitmap = prof.getBitmapProf();
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
        }
    }

    private void storeProfileOnFirebase(Profile profile){
        DatabaseReference ref;
        ref = database.getReference("deliverers/" + Welcome.getDbKey() + "/info");
        ref.runTransaction(new Transaction.Handler(){
            @NonNull
            @Override
            public Transaction.Result doTransaction (@NonNull MutableData currentData){
                currentData.setValue(profile);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot currentData){
                if(committed) {
                    loadData(profile);
                }
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

                DatabaseReference dbRef = database.getReference("/deliverers_liberi/" + Welcome.getDbKey());
                dbRef.removeValue();

                dbRef = database.getReference("/deliverers/" + Welcome.getDbKey() + "/info/state/");
                dbRef.setValue(false).addOnCompleteListener(t -> {
                    FirebaseLogin.logout((Activity) ctx);
                    ((Activity) ctx).finish();
                });

                return true;
            case R.id.item_statistics:
                showAnalytics();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}