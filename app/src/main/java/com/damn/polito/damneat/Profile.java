package com.damn.polito.damneat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.damn.polito.commonresources.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class Profile extends AppCompatActivity {

    private String defaultValue;
    private ImageView profileImage;
    private TextView name, mail, description, address;
    private Bitmap profileBitmap;
    private boolean empty = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        defaultValue = getString(R.string.nullText);

        profileImage = findViewById(R.id.profile_image);
        name = findViewById(R.id.editText_name);
        mail = findViewById(R.id.editText_email);
        description = findViewById(R.id.editText_desc);
        address = findViewById(R.id.editText_address);

        loadData();
    }

    private void editProfile() {
        //Crea il corretto intent per l'apertura dell'activity EditProfile
        Intent intent = new Intent(this, EditProfile.class);

        //Se il profilo esisteva, passa le informazioni a EditProfile
        if(!empty){
            intent.putExtra("name", name.getText().toString().trim());
            intent.putExtra("mail", mail.getText().toString().trim());
            intent.putExtra("description", description.getText().toString().trim());
            intent.putExtra("address", address.getText().toString().trim());
            intent.putExtra("image", profileImage.getDrawable().toString());
            if(profileBitmap != null)
                intent.putExtra("profile", profileBitmap);

        }
        startActivityForResult(intent, 1);
    }

    private String stringOrDefault(String s){
        return (s == null || s.trim().isEmpty()) ? defaultValue : s;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK){
            storeData(data);
        }
    }

    private void storeData(Intent data) {
        String name = data.getStringExtra("name");
        String mail = data.getStringExtra("mail");
        String description = data.getStringExtra("description");
        String address = data.getStringExtra("address");
        profileBitmap = data.getParcelableExtra("profile");

        JSONObject values = new JSONObject();
        try {
            boolean hasProfile = false;
            values.put("name", name);
            values.put("mail", mail);
            values.put("description", description);
            values.put("address", address);
            if(profileBitmap != null) {
                String bts = Utility.BitMapToString(profileBitmap);
                values.put("profile", bts);
                hasProfile = true;
            }
            values.put("hasProfile", hasProfile);

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            pref.edit().putString("info", values.toString()).apply();

            this.name.setText(name);
            this.mail.setText(mail);
            this.description.setText(description);
            this.address.setText(address);
            if(profileBitmap != null) profileImage.setImageBitmap(profileBitmap);

            empty = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadData(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String s = pref.getString("info", null);
        if(s == null) return;

        try {
            JSONObject values = new JSONObject(s);
            name.setText(stringOrDefault(values.getString("name")));
            mail.setText(stringOrDefault(values.getString("mail")));
            description.setText(stringOrDefault(values.getString("description")));
            address.setText(stringOrDefault(values.getString("address")));
            if(values.getBoolean("hasProfile")){
                String encodedBitmap = values.getString("profile");
                profileBitmap = Utility.StringToBitMap(encodedBitmap);
                if(profileBitmap != null)
                    profileImage.setImageBitmap(profileBitmap);
            }

            empty = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.item_edit:
                editProfile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
