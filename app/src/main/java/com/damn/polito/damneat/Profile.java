package com.damn.polito.damneat;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class Profile extends AppCompatActivity {

    private ImageView profileImage, edit;
    private EditText name, mail, description, address;
    private TextView profAddress;
    private int type=1;
    private boolean editable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profile_image);
        edit = findViewById(R.id.profile_edit);
        name = findViewById(R.id.editText_name);
        mail = findViewById(R.id.editText_email);
        description = findViewById(R.id.editText_desc);
        address = findViewById(R.id.editText_address);
        profAddress = findViewById(R.id.profile_address);

        type = getIntent().getIntExtra("type", 1);

        initActivity();

        edit.setOnClickListener(v->{
            if(!editable){
                editable = true;
                edit.setImageResource(R.drawable.ic_check);
                enableEditTexts();
            }else{
                if(checkField()){
                    editable = false;
                    edit.setImageResource(R.drawable.ic_edit);
                    disableEditTexts();
                }
            }
        });
    }

    private boolean checkField() {
        String name = this.name.getText().toString();
        if(name.trim().isEmpty()) return false;

        String mail = this.mail.getText().toString();
        if(mail.trim().isEmpty()) return false;

        String description = this.description.getText().toString();
        if(description.trim().isEmpty()) return false;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String address;
        if(type == 1 || type == 2){
            address = this.address.getText().toString();
            if(address.trim().isEmpty()) return false;
            pref.edit().putString("address", address).apply();
        }

        pref.edit().putString("name", name).apply();
        pref.edit().putString("mail", mail).apply();
        pref.edit().putString("description", description).apply();
        return true;
    }

    private void initActivity() {

        switch (type){
            case 2:
                profAddress.setText(getString(R.string.profile_restaurant_address));
                break;
            case 3:
                profAddress.setVisibility(View.GONE);
                address.setVisibility(View.GONE);
                break;
            default:
                profAddress.setText(getString(R.string.profile_address));
                break;
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        name.setText(pref.getString("name", ""));
        mail.setText(pref.getString("mail", ""));
        description.setText(pref.getString("description", ""));
        address.setText(pref.getString("address", ""));
    }

    private void enableEditTexts(){
        name.setEnabled(true);
        mail.setEnabled(true);
        description.setEnabled(true);
        address.setEnabled(true);
    }

    private void disableEditTexts(){
        name.setEnabled(false);
        mail.setEnabled(false);
        description.setEnabled(false);
        address.setEnabled(false);
    }
}
