package com.damn.polito.commonresources.notifications;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class NotificationSender {

    private static NotificationSender mInstance;
    private static Context mCtx;
    private RequestQueue requestQueue;

    private NotificationSender(Context context){
        mCtx = context;
        requestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue(){
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized NotificationSender getInstance(Context context){
        if(mInstance == null){
            mInstance = new NotificationSender(context);
        }
        return mInstance;
    }

    public<T> void addToRequestQueue(Request<T> request){
        getRequestQueue().add(request);
    }
}
