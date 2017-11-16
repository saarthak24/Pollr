package com.akotnana.pollr.utils;

import com.android.volley.VolleyError;

import org.json.JSONObject;

/**
 * Created by anees on 11/11/2017.
 */

public interface VolleyCallback{
    void onSuccess(String result);
    void onError(VolleyError error);
}
