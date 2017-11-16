package com.akotnana.pollr.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akotnana.pollr.R;
import com.akotnana.pollr.utils.BackendUtils;
import com.akotnana.pollr.utils.DataStorage;
import com.akotnana.pollr.utils.VolleyCallback;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    public String TAG = "SignInActivity";

    EditText username;
    EditText password;
    Button signIn;
    LinearLayout signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        username = (EditText) findViewById(R.id.username_edit_text);
        password = (EditText) findViewById(R.id.password_edit_text);
        signIn = (Button) findViewById(R.id.login_button);
        signUp = (LinearLayout) findViewById(R.id.sign_up_link);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()) {
                    onLoginFailed();
                    return;
                }
                signUp.setEnabled(false);
                final ProgressDialog progressDialog = new ProgressDialog(SignInActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Authenticating...");
                progressDialog.show();
                BackendUtils.doPostRequest("/api/v1/login", new HashMap<String, String>() {{
                    put("username", username.getText().toString());
                    String pass = password.getText().toString();
                    try {
                        pass = new DataStorage(getApplicationContext()).md5(pass);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    put("password", pass);
                }}, new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        if(result.equals("Fail")) {
                            onLoginFailed();
                            progressDialog.dismiss();
                        } else {
                            new DataStorage(getApplicationContext()).storeData("auth_token", result.trim());
                            Log.d("AUTH_TOKEN", result);
                            onLoginSuccess();
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {

                    }
                }, getApplicationContext());
            }
        });

        signUp = (LinearLayout) findViewById(R.id.sign_up_link);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }

    public boolean validate() {
        boolean valid = true;

        String username = this.username.getText().toString();
        String password = this.password.getText().toString();

        if (username.isEmpty()) {
            this.username.setError("enter a valid username");
            valid = false;
        } else {
            this.username.setError(null);
        }

        if (password.isEmpty() || password.length() < 4) {
            this.password.setError("password too short");
            valid = false;
        } else {
            this.password.setError(null);
        }

        return valid;
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_SHORT).show();
        signUp.setEnabled(true);
    }

    public void onLoginSuccess() {
        signUp.setEnabled(true);
        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

}