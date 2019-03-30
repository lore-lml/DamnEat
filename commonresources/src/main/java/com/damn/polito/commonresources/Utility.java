package com.damn.polito.commonresources;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_CANCELED;

public class Utility {

    public static final int REQUEST_IMAGE_CAPTURE = 2;
    public static final int IMAGE_GALLERY_REQUEST = 10;

    public static Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public static void showWarning(AppCompatActivity app) {
        //Se l'utente cerca di tornare indietro allora chiede la conferma
        AlertDialog.Builder alert = new AlertDialog.Builder(app);
        alert.setTitle(R.string.profile_text).setMessage(R.string.alert_edit_profile)
                .setNegativeButton(R.string.alert_edit_profile_negative, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.alert_edit_profile_positive, (dialog, which) -> {
                    app.setResult(RESULT_CANCELED);
                    app.finish();
                }).show();
    }

    public static Intent galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI,"image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("outputX", 256);
        intent.putExtra("outputY", 256);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("return-data", true);
        return intent;
    }

    public static Intent cameraIntent(){
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    }
}
