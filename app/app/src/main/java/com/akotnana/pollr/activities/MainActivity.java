package com.akotnana.pollr.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.akotnana.pollr.R;
import com.akotnana.pollr.utils.BackendUtils;
import com.akotnana.pollr.utils.VolleyCallback;
import com.android.volley.VolleyError;

import java.util.HashMap;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

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
            public void onError(VolleyError error) {
                getRequest.setText("Bad request");
                getRequest.setVisibility(View.VISIBLE);
                get.setVisibility(GONE);
            }
        }, getApplicationContext());

        ///////////////////////////////////

        post.setIndeterminate(true);
        post.setVisibility(View.VISIBLE);
        postRequest.setVisibility(GONE);
        BackendUtils.doGetRequest("/api/v1/pokemon", new HashMap<String, String>() {{
            put("username", "2018akotnana");
            put("password", "password");
            put("firebase_id", "dwjiodjwaoidjwoaidjwaoidjwaidwa");
        }}, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                postRequest.setText(result);
                postRequest.setVisibility(View.VISIBLE);
                post.setVisibility(GONE);
            }

            @Override
            public void onError(VolleyError error) {
                postRequest.setText("Bad request");
                postRequest.setVisibility(View.VISIBLE);
                post.setVisibility(GONE);
            }
        }, getApplicationContext());

    }

}
