package com.akotnana.pollr.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.akotnana.pollr.R;
import com.akotnana.pollr.utils.BackendUtils;
import com.akotnana.pollr.utils.VolleyCallback;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";

    TextView getRequest;
    TextView postRequest;
    ProgressBar get;
    ProgressBar post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getRequest = (TextView) findViewById(R.id.get_request);
        postRequest = (TextView) findViewById(R.id.post_request);
        get = (ProgressBar) findViewById(R.id.indeterminateBar1);
        post = (ProgressBar) findViewById(R.id.indeterminateBar2);

        get.setIndeterminate(true);
        get.setVisibility(View.VISIBLE);
        getRequest.setVisibility(GONE);
        BackendUtils.doGetRequest("/api/v1/pokemon", new HashMap<String, String>() {{
            put("auth_token", "rahul_u_dumb_bitch");
        }}, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                getRequest.setText(result);
                getRequest.setVisibility(View.VISIBLE);
                get.setVisibility(GONE);
            }

            @Override
            public void onSuccess(JSONObject result) {

            }

            @Override
            public void onError(VolleyError error) {
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

                getRequest.setText("Bad request");
                getRequest.setVisibility(View.VISIBLE);
                get.setVisibility(GONE);
            }
        }, getApplicationContext());

        ///////////////////////////////////

        post.setIndeterminate(true);
        post.setVisibility(View.VISIBLE);
        postRequest.setVisibility(GONE);
        BackendUtils.doPostRequest("/api/v1/pokemon", new HashMap<String, String>() {{
            put("username", "2018akotnana");
            put("password", "password");
            put("firebase_id", "dwjiodjwaoidjwoaidjwaoidjwaidwa");
        }}, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                postRequest.setText(result.toString());
                postRequest.setVisibility(View.VISIBLE);
                post.setVisibility(GONE);
            }

            @Override
            public void onSuccess(JSONObject result) {

            }

            @Override
            public void onError(VolleyError error) {

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

                postRequest.setText("Bad request");
                postRequest.setVisibility(View.VISIBLE);
                post.setVisibility(GONE);
            }
        }, getApplicationContext());

    }

}
