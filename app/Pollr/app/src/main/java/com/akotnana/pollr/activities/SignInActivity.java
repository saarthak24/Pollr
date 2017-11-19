package com.akotnana.pollr.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akotnana.pollr.R;
import com.akotnana.pollr.utils.DataStorage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    public String TAG = "SignInActivity";

    private FirebaseAuth mAuth;

    ImageView backgroundView;

    EditText username;
    EditText password;
    TextView forgotPassword;
    Button signIn;
    LinearLayout signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        username = (EditText) findViewById(R.id.username_edit_text);
        password = (EditText) findViewById(R.id.password_edit_text);
        signIn = (Button) findViewById(R.id.login_button);
        signUp = (LinearLayout) findViewById(R.id.sign_up_link);

        forgotPassword = (TextView) findViewById(R.id.forgot_password);

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
                View mView = layoutInflaterAndroid.inflate(R.layout.email_input_dialog, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(new ContextThemeWrapper(SignInActivity.this, R.style.myDialog));
                alertDialogBuilderUserInput.setView(mView);

                final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.email_question);
                alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialogBox, int id) {
                                if(!userInputDialogEditText.getText().toString().equals("")) {
                                    mAuth.sendPasswordResetEmail(userInputDialogEditText.getText().toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "Email sent.");
                                                        Toast.makeText(getApplicationContext(), "Email sent.", Toast.LENGTH_SHORT).show();
                                                        dialogBox.cancel();
                                                    } else {
                                                        Log.d(TAG, "Email not sent.");
                                                        Toast.makeText(getApplicationContext(), "Incorrect email.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }
                        })

                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
            }
        });

        backgroundView = (ImageView) findViewById(R.id.hello_world);
        //Blurry.with(getApplicationContext()).radius(25).sampling(2).from(((BitmapDrawable) getResources().getDrawable(R.drawable.background1)).getBitmap()).into(backgroundView);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()) {
                    Toast.makeText(SignInActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                signIn.setEnabled(false);
                final ProgressDialog progressDialog = new ProgressDialog(SignInActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Authenticating...");
                progressDialog.show();
                mAuth.signInWithEmailAndPassword(username.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    String idToken = new DataStorage(getApplicationContext()).getAuthToken();
                                    //TODO: sned rahul jaunts
                                    progressDialog.dismiss();
                                    updateUI(mAuth.getCurrentUser());
                                } else {
                                    progressDialog.dismiss();
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(SignInActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    //updateUI(null);
                                }

                                // ...
                            }
                        });
            }
        });

        signUp = (LinearLayout) findViewById(R.id.sign_up_link);

        signUp.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String idToken = new DataStorage(getApplicationContext()).getAuthToken();
        //TODO: sned rahul jaunts
        updateUI(currentUser);
    }

    public void updateUI(FirebaseUser currUser) {
        signIn.setEnabled(true);
        if(currUser != null) {
            Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("displayName", currUser.getDisplayName());
            startActivity(intent);
            finish();
        }
    }

    public boolean validate() {
        boolean valid = true;

        String username = this.username.getText().toString();
        String password = this.password.getText().toString();

        if (username.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            this.username.setError("enter a valid email address");
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
}