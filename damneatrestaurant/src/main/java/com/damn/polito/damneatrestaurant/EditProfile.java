package com.damn.polito.damneatrestaurant;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneatrestaurant.dialogs.HandleDismissDialog;
import com.damn.polito.damneatrestaurant.dialogs.OpeningDialog;

import static com.damn.polito.commonresources.Utility.*;

import java.io.IOException;
import java.util.Objects;

public class EditProfile extends AppCompatActivity implements HandleDismissDialog {

    private ImageView profile;
    private ImageButton camera;
    private EditText name, mail, description, address, phone, opening;
    private Button save;
    private Bitmap profImg;

    // VARIABILI PER VERIFICARE SE SONO STATE EFFETTUATE MODIFICHE
    private String sName, sMail, sDesc, sAddress, sPhone, sOpening;
    private Bitmap profImgPrec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        profile = findViewById(R.id.profile_image);
        camera = findViewById(R.id.btn_camera);
        name = findViewById(R.id.edit_name);
        mail = findViewById(R.id.edit_mail);
        phone = findViewById(R.id.edit_phone);
        description = findViewById(R.id.edit_desc);
        address = findViewById(R.id.edit_address);
        opening = findViewById(R.id.edit_opening);
        save = findViewById(R.id.edit_save);

        init();
    }

    private void init() {
        //Recupera le informazioni passate da ProfileFragment
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Intent intent = getIntent();

        sName = intent.getStringExtra("name");
        sMail = intent.getStringExtra("mail");
        sPhone = intent.getStringExtra("phone");

        if(sPhone != null && !sPhone.isEmpty())
            sPhone = sPhone.substring(4);

        sDesc = intent.getStringExtra("description");
        sAddress = intent.getStringExtra("address");
        sOpening = intent.getStringExtra("opening");
        String bitmapString = pref.getString("profile", null);
        if(bitmapString != null) {
            profImg = StringToBitMap(bitmapString);
            profImgPrec = profImg;
            pref.edit().remove("profile").apply();
        }

        name.setText(sName);
        mail.setText(sMail);
        phone.setText(sPhone);
        description.setText(sDesc);
        address.setText(sAddress);
        opening.setText(sOpening);
        if(profImg != null){
            profile.setImageBitmap(profImg);
        }


        //Imposta la funzione del bottone "SALVA"
        save.setOnClickListener(v->{
            if(checkField()){
                setActivityResult();
                finish();
            }
        });

        camera.setOnClickListener(v->{
            PopupMenu pop = new PopupMenu(this, camera);
            pop.getMenuInflater().inflate(R.menu.context_camera_menu, pop.getMenu());
            pop.setOnMenuItemClickListener(item->{
                switch (item.getItemId()){
                    case R.id.item_snap:
                        itemCamera();
                        return true;
                    case R.id.item_gallery:
                        itemGallery();
                        return true;
                    default:
                        return super.onContextItemSelected(item);
                }
            });
            pop.show();
        });

        opening.setOnClickListener(v->{
            FragmentManager fm = getSupportFragmentManager();
            OpeningDialog opening = new OpeningDialog();
            opening.setDaysText(sOpening);
            opening.show(fm, "Opening Dialog");
        });
    }

    private void itemCamera() {
        if(!checkPermissionFromDevice(REQUEST_PERM_CAMERA))
            requestPermission(REQUEST_PERM_CAMERA, PERMISSION_CODE_CAMERA);
        else
            photoShot();
    }

    private void photoShot() {
        Intent intent = cameraIntent();
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void itemGallery(){
        if(!checkPermissionFromDevice(REQUEST_PERM_WRITE_EXTERNAL))
            requestPermission(REQUEST_PERM_WRITE_EXTERNAL, PERMISSION_CODE_WRITE_EXTERNAL);
        else
            pickFromGallery();
    }

    private void pickFromGallery() {
        Intent intent = galleryIntent();
        startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
    }

    private void setActivityResult() {
        setResult(RESULT_OK, getActivityResult());
    }

    private Intent getActivityResult() {
        Intent i = new Intent();
        i.putExtra("name", name.getText().toString().trim());
        i.putExtra("mail", mail.getText().toString().trim());
        i.putExtra("phone", getString(R.string.phone_prefix) + " " +phone.getText().toString().trim());
        i.putExtra("description", description.getText().toString().trim());
        i.putExtra("address", address.getText().toString().trim());
        i.putExtra("opening", opening.getText().toString().trim());
        if(profImg != null){
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            pref.edit().putString("profile", BitMapToString(profImg)).apply();
        }

        return i;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == IMAGE_GALLERY_REQUEST) {
            final Bundle extras = data.getExtras();
            if (extras != null) {
                profImg = extras.getParcelable("data");
                profile.setImageBitmap(profImg);
            }else{
                copyAndCrop(data.getData());
            }
        }
        if(requestCode == CROP_REQUEST){
            final Bundle extras = data.getExtras();
            if (extras != null) {
                profImg = extras.getParcelable("data");
                profile.setImageBitmap(profImg);
            }else{
                displayImage(data.getData());
            }
        }
    }

    private void displayImage(Uri data) {
        try {
            profImg = MediaStore.Images.Media.getBitmap(getContentResolver(), data);
            profile.setImageBitmap(profImg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyAndCrop(Uri uri) {
        assert uri != null;
        Uri newUri = getImageUrlWithAuthority(this, Objects.requireNonNull(uri));
        if(newUri != null) {
            cropImage(newUri);
        }
    }

    private void cropImage(Uri uri) {
        try{
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(uri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("scaleUpIfNeeded", true);
            cropIntent.putExtra("return-data", true);

            startActivityForResult(cropIntent,CROP_REQUEST);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
        }
    }

    private boolean checkField() {
        String name = this.name.getText().toString();

        //Controllo sui campi vuoti
        if(name.trim().isEmpty()){
            Toast.makeText(this, getString(R.string.empty_name), Toast.LENGTH_SHORT).show();
            this.name.requestFocus();
            return false;
        }

        //Controlla se la stringa inserita sia una mail valida
        String mail = this.mail.getText().toString();
        String regex = Utility.Regex.MAIL;
        if(mail.trim().toLowerCase().isEmpty()){
            Toast.makeText(this, getString(R.string.empty_mail), Toast.LENGTH_SHORT).show();
            this.mail.requestFocus();
            return false;
        }
        else if(!mail.matches(regex)){
            Toast.makeText(this, getString(R.string.invalid_mail), Toast.LENGTH_SHORT).show();
            this.mail.requestFocus();
            return false;
        }

        String phone = this.phone.getText().toString().trim();
        if(phone.isEmpty()){
            Toast.makeText(this, getString(R.string.empty_phone), Toast.LENGTH_SHORT).show();
            this.phone.requestFocus();
            return false;
        }else if(!phone.matches(Utility.Regex.MOBILE_PHONE) && !phone.matches(Utility.Regex.TELEPHONE)){
            Toast.makeText(this, getString(R.string.invalid_phone), Toast.LENGTH_SHORT).show();
            this.phone.requestFocus();
            return false;
        }

        String description = this.description.getText().toString();
        if(description.trim().isEmpty()){
            Toast.makeText(this, getString(R.string.empty_desc), Toast.LENGTH_SHORT).show();
            this.description.requestFocus();
            return false;
        }

        String address = this.address.getText().toString();
        if(address.trim().isEmpty()){
            this.address.requestFocus();
            Toast.makeText(this, getString(R.string.empty_address), Toast.LENGTH_SHORT).show();
            return false;
        }

        String opening = this.opening.getText().toString();
        if(opening.trim().isEmpty()){
            Toast.makeText(this, getString(R.string.empty_opening), Toast.LENGTH_SHORT).show();
            this.opening.requestFocus();
            return false;
        }

        return true;
    }

    private boolean checkChanges() {
        String name = this.name.getText().toString();
        if(!name.equals(sName))
            return true;

        String mail = this.mail.getText().toString();
        if(!(mail.equals(sMail)))
            return true;

        String phone = this.phone.getText().toString();
        if(!(phone.equals(sPhone)))
            return true;

        String description = this.description.getText().toString();
        if(!(description.equals(sDesc)))
            return true;

        String address = this.address.getText().toString();
        if(!(address.equals(sAddress)))
            return true;

        String opening = this.opening.getText().toString();
        if(!(opening.equals(sOpening)))
            return true;

        if(profImg == null) return false;

        return (!(profImg.equals(profImgPrec)));

    }


    private boolean checkPermissionFromDevice(String permission) {

        int result = ContextCompat.checkSelfPermission(this, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(final String permission, final int permission_code) {
        ActivityCompat.requestPermissions(this,new String[]{
                permission
        }, permission_code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case PERMISSION_CODE_CAMERA:
                if(!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    Toast.makeText(getApplicationContext(), getString(R.string.permission_denied),
                            Toast.LENGTH_SHORT).show();
                else
                    photoShot();

                break;

            case PERMISSION_CODE_WRITE_EXTERNAL:
                if(!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    Toast.makeText(getApplicationContext(), getString(R.string.permission_denied),
                            Toast.LENGTH_SHORT).show();
                else
                    pickFromGallery();
        }
    }

    @Override
    public void onBackPressed() {
        if(checkChanges())
            // Facciamo comparire il messagio solo se sono stati cambiati dei campi
            showWarning(this, checkField(), getActivityResult());
        else {
            setResult(RESULT_CANCELED);
            this.finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(checkChanges())
                    showWarning(this, checkField(), getActivityResult());
                else {
                    setResult(RESULT_CANCELED);
                    this.finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(profImg != null)
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("profile", BitMapToString(profImg)).apply();
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String bitmap = pref.getString("profile", null);
        if(bitmap != null) {
            profImg = StringToBitMap(bitmap);
            profile.setImageBitmap(profImg);
            pref.edit().remove("profile").apply();
        }
    }

    public boolean isEmailPresent(){



        return true;
    }

    @Override
    public void handleOnDismiss(String text) {
        opening.setText(text);
    }
}

