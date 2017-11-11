package com.akotnana.pollr.utils;

import com.android.volley.VolleyError;

/**
 * Created by anees on 11/11/2017.
 */

public interface VolleyCallback{
    void onSuccess(String result);
    void onError(VolleyError error);
}
