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

import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneatrestaurant.EditProfile;
import com.damn.polito.damneatrestaurant.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private String defaultValue;
    private ImageView profileImage;
    private TextView name, mail, description, address;
    private Bitmap profileBitmap;
    private boolean empty = true;
    private Context ctx;

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
        description = view.findViewById(R.id.editText_desc);
        address = view.findViewById(R.id.editText_address);

        loadData();
    }

    private void editProfile() {
        //Crea il corretto intent per l'apertura dell'activity EditProfile
        Intent intent = new Intent(ctx, EditProfile.class);

        //Se il profilo esisteva, passa le informazioni a EditProfile
        if (!empty) {
            intent.putExtra("name", name.getText().toString().trim());
            intent.putExtra("mail", mail.getText().toString().trim());
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
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean hasProfile = false;
        String name = data.getStringExtra("name");
        String mail = data.getStringExtra("mail");
        String description = data.getStringExtra("description");
        String address = data.getStringExtra("address");
        String bitmapProf = pref.getString("profile", null);
        if(bitmapProf!= null) {
            profileBitmap = Utility.StringToBitMap(bitmapProf);
            hasProfile = true;
            pref.edit().remove("profile").apply();
        }

        JSONObject values = new JSONObject();
        try {
            values.put("name", name);
            values.put("mail", mail);
            values.put("description", description);
            values.put("address", address);
            if (profileBitmap != null) {
                String bts = Utility.BitMapToString(profileBitmap);
                values.put("profile", bts);
                hasProfile = true;
            }
            values.put("hasProfile", hasProfile);


            pref.edit().putString("info", values.toString()).apply();

            this.name.setText(name);
            this.mail.setText(mail);
            this.description.setText(description);
            this.address.setText(address);
            if (profileBitmap != null) profileImage.setImageBitmap(profileBitmap);

            empty = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        String s = pref.getString("info", null);
        if (s == null) return;

        try {
            JSONObject values = new JSONObject(s);
            name.setText(stringOrDefault(values.getString("name")));
            mail.setText(stringOrDefault(values.getString("mail")));
            description.setText(stringOrDefault(values.getString("description")));
            address.setText(stringOrDefault(values.getString("address")));
            if (values.getBoolean("hasProfile")) {
                String encodedBitmap = values.getString("profile");
                profileBitmap = Utility.StringToBitMap(encodedBitmap);
                if (profileBitmap != null)
                    profileImage.setImageBitmap(profileBitmap);
            }

            empty = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
