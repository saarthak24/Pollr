package com.akotnana.pollr.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.akotnana.pollr.R;
import com.akotnana.pollr.utils.BackendUtils;
import com.akotnana.pollr.utils.DataStorage;
import com.akotnana.pollr.utils.RoundRectCornerImageView;
import com.akotnana.pollr.utils.VolleyCallback;
import com.android.volley.VolleyError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.blurry.Blurry;

import static android.view.View.GONE;
import static com.akotnana.pollr.utils.DataStorage.decodeSampledBitmapFromFile;

public class ProfileEditActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    public String TAG = "ProfileEditActivity";

    private static final int CAMERA_REQUEST = 1888;
    private static final String CAPTURE_IMAGE_FILE_PROVIDER = "com.akotnana.pollr.fileprovider";

    private StorageReference mStorageRef;

    public Location myLocation;

    EditText name;
    EditText dob;
    EditText gender;
    EditText race;
    EditText income;
    EditText zip_code;
    RoundRectCornerImageView imageView;
    ImageView backgroundView;
    FloatingActionButton fab;

    int age = 0;
    Uri imageUri;

    Button join;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        join = (Button) findViewById(R.id.join_button);

        name = (EditText) findViewById(R.id.name_edit_text);
        dob = (EditText) findViewById(R.id.dob_edit_text);
        gender = (EditText) findViewById(R.id.gender_edit_text);
        race = (EditText) findViewById(R.id.race_edit_text);
        income = (EditText) findViewById(R.id.income_edit_text);
        backgroundView = (ImageView) findViewById(R.id.hello_world);
        zip_code = (EditText) findViewById(R.id.location_edit_text);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        imageView = (RoundRectCornerImageView) findViewById(R.id.profile_picture);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        //Blurry.with(getApplicationContext()).radius(25).sampling(2).from(((BitmapDrawable) getResources().getDrawable(R.drawable.background1)).getBitmap()).into(backgroundView);

        setupGenderSpinner();
        setupIncomeSpinner();
        setupRaceSpinner();

        zip_code.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    try {
                        PlacePicker.IntentBuilder intentBuilder =
                                new PlacePicker.IntentBuilder();
                        Intent intent = intentBuilder.build(ProfileEditActivity.this);
                        startActivityForResult(intent, 1337);

                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        dob.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    Calendar now = Calendar.getInstance();
                    DatePickerDialog dpd = DatePickerDialog.newInstance(
                            ProfileEditActivity.this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                    dpd.setAccentColor(getResources().getColor(R.color.colorAccent));
                    dpd.setOkColor(getResources().getColor(R.color.colorPrimary));
                    dpd.show(getFragmentManager(), "Datepickerdialog");
                }
            }
        });

        dob.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    Calendar now = Calendar.getInstance();
                    DatePickerDialog dpd = DatePickerDialog.newInstance(
                            ProfileEditActivity.this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                    dpd.setAccentColor(getResources().getColor(R.color.colorAccent));
                    dpd.setOkColor(getResources().getColor(R.color.colorPrimary));
                    dpd.show(getFragmentManager(), "Datepickerdialog");
                }
                return false;
            }
        });

        zip_code.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                {
                    try {
                    PlacePicker.IntentBuilder intentBuilder =
                            new PlacePicker.IntentBuilder();
                    Intent intent = intentBuilder.build(ProfileEditActivity.this);
                    startActivityForResult(intent, 1337);

                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                }

                return true;
            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()) {
                    onSignUpFailed();
                    return;
                }
                join.setEnabled(false);
                final ProgressDialog progressDialog = new ProgressDialog(ProfileEditActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Joining...");
                progressDialog.show();
                //new DataStorage(getApplicationContext()).storeData("dob", dob.getText().toString());
                BackendUtils.doPostRequest("/api/v1/user_profile", new HashMap<String, String>() {{
                    put("name", name.getText().toString());
                    put("race", race.getText().toString());
                    put("auth_token", new DataStorage(getApplicationContext()).getData("auth_token"));
                    put("district", zip_code.getText().toString());
                    put("age", String.valueOf(age) + "*" + dob.getText().toString());
                    put("income", income.getText().toString());
                    put("gender", gender.getText().toString());
                }}, new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        //new DataStorage(getApplicationContext()).storeData("auth_token", result.trim());
                        if(imageUri == null) {
                            if(gender.getText().toString().equals("Male"))
                                imageUri = Uri.parse("android.resource://your.package.name/" + R.drawable.male);
                            else
                                imageUri = Uri.parse("android.resource://your.package.name/" + R.drawable.female);
                        }

                        new DataStorage(getApplicationContext()).storeData("imageURI", imageUri.toString());

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(imageUri)
                                .build();
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User profile updated.");
                                        }
                                    }
                                });
                        onSignUpSuccess();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(VolleyError error) {

                    }
                }, getApplicationContext());
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeImageFromCamera(view);
            }
        });


    }

    public void takeImageFromCamera(View view) {
        int MY_CAMERA_REQUEST_CODE = 100;

        if (getApplicationContext().checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_REQUEST_CODE);
        } else {
            File path = new File(getApplicationContext().getFilesDir(), "pollr_images/");
            if (!path.exists()) path.mkdirs();
            File image = new File(path, "image.jpg");
            image.delete();
            imageUri = FileProvider.getUriForFile(ProfileEditActivity.this, CAPTURE_IMAGE_FILE_PROVIDER, image);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, CAMERA_REQUEST);
        }
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                takeImageFromCamera(fab);

            }
        }
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode, Intent data) {
        Log.d("requestCode", String.valueOf(requestCode));
        Log.d("RESULT_OK", String.valueOf(resultCode== Activity.RESULT_OK));
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {

                Log.d("TAG","refreshing the image!!!!");

                File path = new File(getApplicationContext().getFilesDir(), "pollr_images/");
                if (!path.exists()) path.mkdirs();
                File imageFile = new File(path, "image.jpg");
                Uri file = Uri.fromFile(imageFile);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference = storage.getReferenceFromUrl("gs://pollr-89a97.appspot.com").child(user.getUid() + ".jpg");
                storageReference.putFile(file)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                Log.d("ProfileEditActivity", "" + downloadUrl.toString());
                                Toast.makeText(getApplicationContext(), "Image saved!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.d("ProfileEditActivity", "image NOT sent");
                                Toast.makeText(getApplicationContext(), "Image not saved!", Toast.LENGTH_SHORT).show();
                            }
                        });

        /*View headerView = navigationView.inflateHeaderView(R.layout.nav_header);
        CircleImageView imageView = headerView.findViewById(R.id.materialup_profile_image);*/


                // use imageFile to open your image
                Log.d("ProfileEditActivity", "" + imageFile.exists());
                if(imageFile.exists()) {
                    Bitmap bitmap = decodeSampledBitmapFromFile(imageFile.getAbsolutePath(), 1000, 700);
                    Drawable d = new BitmapDrawable(getResources(), bitmap);
                    imageView.setImageDrawable(d);
                } else if(!new DataStorage(getApplicationContext()).getData("imageURI").equals("")){
                    Uri imageURI1 = Uri.parse(new DataStorage(getApplicationContext()).getData("imageURI"));
                    //imageView.setImageURI(null);
                    imageView.setImageURI(imageURI1);
                }


            }
        } else if (requestCode == 1337 && resultCode == Activity.RESULT_OK){
            if(data != null) {
                try {
                    final Place place = PlacePicker.getPlace(ProfileEditActivity.this, data);
                    Log.d("location", place.getAddress().toString());
                    String address = "";
                    try {
                        address = URLEncoder.encode(place.getAddress().toString(), "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    final String finalAddress = address;
                    BackendUtils.doCustomGetRequest("http://api.geocod.io/v1/geocode", new HashMap<String, String>() {{
                        put("q", finalAddress);
                        put("fields", "cd");
                        put("api_key", "eef7a5fdc404d4fb45f5efa47cd4570fc70f8a4");

                    }}, new VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            Log.d("location", result);
                            int i = result.indexOf("district_number");
                            Log.d("goat", result.substring(i + 17, i + 20).split(",")[0]);
                            String districtNumber = result.substring(i + 17, i + 20).split(",")[0];
                            i = result.indexOf("state");
                            Log.d("goat", result.substring(i + 7, i + 11).split(",")[0]);
                            String state = result.substring(i + 7, i + 11).split(",")[0].replace("\"", "");
                            zip_code.setText(state + districtNumber);

                        }

                        @Override
                        public void onError(VolleyError error) {

                        }
                    }, getApplicationContext());
                } catch (Exception e) {
                    FirebaseCrash.report(e);
                }
            }
        }
    }

    public boolean validate() {
        boolean valid = true;

        String nam = this.name.getText().toString();
        String gen = this.gender.getText().toString();
        String rac = this.race.getText().toString();
        String date = this.dob.getText().toString();
        String inc = this.income.getText().toString();

        if (nam.isEmpty()) {
            this.name.setError("enter a valid name");
            valid = false;
        } else {
            this.name.setError(null);
        }

        if (date.isEmpty()) {
            this.dob.setError("enter a valid date of birth");
            valid = false;
        } else {
            this.dob.setError(null);
        }

        if (gen.isEmpty()) {
            this.gender.setError("enter a valid gender");
            valid = false;
        } else {
            this.gender.setError(null);
        }

        if (rac.isEmpty()) {
            this.race.setError("enter a valid race");
            valid = false;
        } else {
            this.race.setError(null);
        }

        if (inc.isEmpty()) {
            this.income.setError("enter a valid income bracket");
            valid = false;
        } else {
            this.income.setError(null);
        }

        return valid;
    }

    public int getAge(Calendar dob) {
        int age = 0;
        Calendar now = Calendar.getInstance();
        if (dob.after(now)) {
            return -1;
        }
        int year1 = now.get(Calendar.YEAR);
        int year2 = dob.get(Calendar.YEAR);
        age = year1 - year2;
        int month1 = now.get(Calendar.MONTH);
        int month2 = dob.get(Calendar.MONTH);
        if (month2 > month1) {
            age--;
        } else if (month1 == month2) {
            int day1 = now.get(Calendar.DAY_OF_MONTH);
            int day2 = dob.get(Calendar.DAY_OF_MONTH);
            if (day2 > day1) {
                age--;
            }
        }
        return age ;
    }

    public void onSignUpFailed() {
        Toast.makeText(getBaseContext(), "Request failed", Toast.LENGTH_SHORT).show();
        join.setEnabled(true);
    }

    public void onSignUpSuccess() {
        join.setEnabled(true);
        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = "You picked the following date: "+dayOfMonth+"/"+(monthOfYear+1)+"/"+year;

        Calendar now = Calendar.getInstance();
        Calendar chosenDate = Calendar.getInstance();
        chosenDate.set(Calendar.YEAR, year);
        chosenDate.set(Calendar.MONTH, monthOfYear);
        chosenDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        DateFormat dateFormat = new SimpleDateFormat("MM-dd-YYYY");
        String result = dateFormat.format(chosenDate.getTime());
        dob.setText(result);

        age = getAge(chosenDate);
        if(age < 0) {
            dob.setText("");
        } else {
            dob.setText(result);
        }
    }
    
    public void setupGenderSpinner() {
        final Spinner spinnerSubject = (Spinner) findViewById(R.id.gender_spinner);
        final EditText gender_input = (EditText) findViewById(R.id.gender_edit_text);
        final String[] subjets = getResources().getStringArray(R.array.gender);

        ArrayAdapter dataAdapterForSpinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,subjets);
        dataAdapterForSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(dataAdapterForSpinner);

        gender_input.setKeyListener(null);

        gender_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinnerSubject.setVisibility(View.VISIBLE);
                spinnerSubject.performClick();
            }
        });

        gender_input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    spinnerSubject.setVisibility(View.VISIBLE);
                    spinnerSubject.performClick();
                }
                else{
                    spinnerSubject.setVisibility(View.GONE);
                }
            }
        });

        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                gender_input.setText(spinnerSubject.getSelectedItem().toString()); //this is taking the first value of the spinner by default.

                File path = new File(getFilesDir(), "pollr_images/");
                if (!path.exists()) path.mkdirs();
                File imageFile = new File(path, "image.jpg");
                // use imageFile to open your image
                Log.d("ProfileEditActivity", "" + imageFile.exists());
                if(!imageFile.exists()) {
                    if (gender_input.getText().toString().equals("Male"))
                        imageView.setImageDrawable((getResources().getDrawable(R.drawable.male)));
                    else if (gender_input.getText().toString().equals("Female"))
                        imageView.setImageDrawable((getResources().getDrawable(R.drawable.female)));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
                gender_input.setText("");
            }
        });
    }

    public void setupRaceSpinner() {
        final Spinner spinnerSubject = (Spinner) findViewById(R.id.race_spinner);
        final EditText race_input = (EditText) findViewById(R.id.race_edit_text);
        final String[] subjets = getResources().getStringArray(R.array.race);

        ArrayAdapter dataAdapterForSpinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,subjets);
        dataAdapterForSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(dataAdapterForSpinner);

        race_input.setKeyListener(null);

        race_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinnerSubject.setVisibility(View.VISIBLE);
                spinnerSubject.performClick();
            }
        });

        race_input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    spinnerSubject.setVisibility(View.VISIBLE);
                    spinnerSubject.performClick();
                }
                else{
                    spinnerSubject.setVisibility(View.GONE);
                }
            }
        });

        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                race_input.setText(spinnerSubject.getSelectedItem().toString()); //this is taking the first value of the spinner by default.
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
                race_input.setText("");
            }
        });
    }

    public void setupIncomeSpinner() {
        final Spinner spinnerSubject = (Spinner) findViewById(R.id.income_spinner);
        final EditText income_input = (EditText) findViewById(R.id.income_edit_text);
        final String[] subjets = getResources().getStringArray(R.array.income);

        ArrayAdapter dataAdapterForSpinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,subjets);
        dataAdapterForSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(dataAdapterForSpinner);

        income_input.setKeyListener(null);

        income_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinnerSubject.setVisibility(View.VISIBLE);
                spinnerSubject.performClick();
            }
        });

        income_input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    spinnerSubject.setVisibility(View.VISIBLE);
                    spinnerSubject.performClick();
                }
                else{
                    spinnerSubject.setVisibility(View.GONE);
                }
            }
        });

        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                income_input.setText(spinnerSubject.getSelectedItem().toString()); //this is taking the first value of the spinner by default.
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
                income_input.setText("");
            }
        });
    }
}
