package com.damn.polito.commonresources.notifications;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HTTPRequestBuilder {

    public interface JSONRequestCallback{
        void onPostSuccess();
        void onPostFailure();
    }

    private static final String URL = "https://fcm.googleapis.com/fcm/send";
    public static final String SERVER_KEY =  "AAAAavIkH0U:APA91bG9zP0_PXMv2VyIJZbOKUlDC1SwmFDODlDSQcWHX0dAZFx18QHTuT2_8yw3Y8Fr-nbdN37aVgg39CC8puGUj0keSYu9LbSQ5RN7T5yu1Rg6V29WRuF-moqn9f3Qbc29-cWGVxr7";
    private String notificationId, title, body;
    private JSONRequestCallback callback;
    private Context ctx;


    public HTTPRequestBuilder(Context ctx, String notificationId, String title, String body) {
        this.notificationId = notificationId;
        this.title = title;
        this.body = body;
        this.ctx = ctx;
    }

    public HTTPRequestBuilder(Context ctx, String notificationId, String title, String body, JSONRequestCallback callback){
        this(ctx, notificationId, title, body);
        this.callback = callback;
    }

    public void sendRequest(){
        if(notificationId == null) return;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to", notificationId);
            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", body);
            notification.put("sound", "default");
            jsonObject.put("notification", notification);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                URL,
                jsonObject,
                response -> {
                    if(callback != null)
                        callback.onPostSuccess();
                },
                error ->{
                    Toast.makeText(ctx, "Error", Toast.LENGTH_SHORT).show();
                    if(callback != null)
                        callback.onPostFailure();
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers  = new HashMap<>();
                headers.put("Authorization", "key=" + SERVER_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        NotificationSender.getInstance(ctx).addToRequestQueue(request);
    }
}
