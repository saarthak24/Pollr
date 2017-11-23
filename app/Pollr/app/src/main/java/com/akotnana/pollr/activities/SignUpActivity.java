package com.akotnana.pollr.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.akotnana.pollr.R;
import com.akotnana.pollr.utils.BackendUtils;
import com.akotnana.pollr.utils.DataStorage;
import com.akotnana.pollr.utils.VolleyCallback;
import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    public String TAG = "SignUpActivity";

    private FirebaseAuth mAuth;

    ImageView backgroundView;

    EditText username;
    EditText password;
    EditText email;
    Button signUp;
    LinearLayout signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        username = (EditText) findViewById(R.id.username_edit_text);
        password = (EditText) findViewById(R.id.password_edit_text);
        email = (EditText) findViewById(R.id.email_edit_text);
        signUp = (Button) findViewById(R.id.register_button);


        backgroundView = (ImageView) findViewById(R.id.hello_world);
        //Blurry.with(getApplicationContext()).radius(25).sampling(2).from(((BitmapDrawable) getResources().getDrawable(R.drawable.background1)).getBitmap()).into(backgroundView);

        final String token = FirebaseInstanceId.getInstance().getToken();
        String realToken = "";
        if(token.equals("")) {
            realToken = new DataStorage(getApplicationContext()).getData("firebaseID");
        } else {
            realToken = token;
        }

        final String finalRealToken = realToken;
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()) {
                    Toast.makeText(SignUpActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                signUp.setEnabled(false);
                final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Authenticating...");
                progressDialog.show();
                mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(username.getText().toString())
                                            .build();
                                    user.updateProfile(profileUpdates);
                                    FirebaseAuth.getInstance().getCurrentUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                                        @Override
                                        public void onSuccess(GetTokenResult result) {
                                            Log.d("DataStorage", result.getToken());
                                            final String idToken = result.getToken();
                                            BackendUtils.doPostRequest("/api/v1/register", new HashMap<String, String>() {{
                                                put("auth_token", idToken);
                                            }}, new VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    Log.d(TAG, result);
                                                    progressDialog.dismiss();
                                                }

                                                @Override
                                                public void onError(VolleyError error) {
                                                    progressDialog.dismiss();
                                                }
                                            }, getApplicationContext());
                                        }
                                    });
                                    signUp.setEnabled(true);
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    progressDialog.dismiss();
                                    signUp.setEnabled(true);
                                    Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }

                                // ...
                            }
                        });
            }
        });

        signIn = (LinearLayout) findViewById(R.id.sign_in_link);

        signIn.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
                return false;
            }
        });
    }

    public void updateUI(FirebaseUser currUser) {
        signUp.setEnabled(true);
        if(currUser != null) {
            Intent intent = new Intent(getApplicationContext(), ProfileEditActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    public boolean validate() {
        boolean valid = true;

        String username = this.username.getText().toString();
        String email = this.email.getText().toString();
        String password = this.password.getText().toString();

        if (username.isEmpty()) {
            this.username.setError("enter a valid username");
            valid = false;
        } else {
            this.username.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError("enter a valid email address");
            valid = false;
        } else {
            this.email.setError(null);
        }

        if (password.isEmpty() || password.length() < 4) {
            this.password.setError("password too short");
            valid = false;
        } else {
            this.password.setError(null);
        }

        return valid;
    }

    public void onSignUpFailed() {
        Toast.makeText(getBaseContext(), "Sign up failed", Toast.LENGTH_SHORT).show();
        signUp.setEnabled(true);
    }

    public void onSignUpSuccess() {
        signUp.setEnabled(true);
        Intent intent = new Intent(getApplicationContext(), ProfileEditActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0,0);
    }



}
