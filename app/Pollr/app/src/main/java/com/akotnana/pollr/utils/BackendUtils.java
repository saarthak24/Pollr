package com.akotnana.pollr.utils;


import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by anees on 11/11/2017.
 */

public class BackendUtils {

    public static String TAG = "BackendUtils";

    //private static String IP = "http://10.199.25.174";
    private static String IP = "http://35.196.14.187";

    private static int PORT = 5000;
    private static String result = "";

    public static void doGetRequest(String address, Map<String, String> parameters, final VolleyCallback callback, Context context) {
        String request = IP + ":" + String.valueOf(PORT) + address + "?";
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            request += entry.getKey() + "=" + entry.getValue() + "&";
        }
        request = request.substring(0, request.length()-1);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, request,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "received callback");
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error == null || error.networkResponse == null) {
                            return;
                        }
                        String body = "";
                        //get status code here
                        final String statusCode = String.valueOf(error.networkResponse.statusCode);
                        //get response body and parse with appropriate encoding
                        try {
                            body = new String(error.networkResponse.data,"UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            // exception
                        }

                        Log.e(TAG, body + "\n");
                        callback.onError(error);
                    }
                });
        RequestQueueSingleton.getInstance(context)
                .getRequestQueue().add(stringRequest);
    }

    public static void doCustomGetRequest(String address, Map<String, String> parameters, final VolleyCallback callback, Context context) {
        String request = address + "?";
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            request += entry.getKey() + "=" + entry.getValue() + "&";
        }

        Log.d("location", request);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, request,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "received callback");
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error == null || error.networkResponse == null) {
                            return;
                        }
                        String body = "";
                        //get status code here
                        final String statusCode = String.valueOf(error.networkResponse.statusCode);
                        //get response body and parse with appropriate encoding
                        try {
                            body = new String(error.networkResponse.data,"UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            // exception
                        }

                        Log.e(TAG, body + "\n");
                        callback.onError(error);
                    }
                });
        RequestQueueSingleton.getInstance(context)
                .getRequestQueue().add(stringRequest);
    }

    public static void doPostRequest(String address, final Map<String, String> parameters, final VolleyCallback callback, Context context) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                IP + ":" + String.valueOf(PORT) + address,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "received callback");
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error == null || error.networkResponse == null) {
                    return;
                }
                String body = "";
                //get status code here
                final String statusCode = String.valueOf(error.networkResponse.statusCode);
                //get response body and parse with appropriate encoding
                try {
                    body = new String(error.networkResponse.data,"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // exception
                }

                Log.e(TAG, body + "\n");
                callback.onError(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return parameters;
            }

        };
        RequestQueueSingleton.getInstance(context)
                .getRequestQueue().add(stringRequest);
    }
}
