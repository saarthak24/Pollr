package com.akotnana.pollr.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.akotnana.pollr.R;
import com.akotnana.pollr.activities.NavigationActivity;
import com.akotnana.pollr.activities.ProfileEditActivity;
import com.akotnana.pollr.utils.BackendUtils;
import com.akotnana.pollr.utils.DataStorage;
import com.akotnana.pollr.utils.VolleyCallback;
import com.android.volley.VolleyError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import static android.view.View.GONE;

/**
 * Created by anees on 11/12/2017.
 */

public class ProfileFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private OnFragmentInteractionListener mListener;

    EditText name;
    EditText dob;
    EditText gender;
    EditText race;
    EditText income;
    EditText zip_code;

    TextView responsesNum;
    TextView nameDisplay;
    
    int age;
    
    Button join;

    View v;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        this.v = v;
        name = (EditText) v.findViewById(R.id.name_edit_text);
        dob = (EditText) v.findViewById(R.id.dob_edit_text);
        gender = (EditText) v.findViewById(R.id.gender_edit_text);
        race = (EditText) v.findViewById(R.id.race_edit_text);
        income = (EditText) v.findViewById(R.id.income_edit_text);
        zip_code = (EditText) v.findViewById(R.id.location_edit_text);

        responsesNum = (TextView) v.findViewById(R.id.responsesNumber);
        nameDisplay = (TextView) v.findViewById(R.id.nameDisplay);
        
        join = (Button) v.findViewById(R.id.save_button); 

        zip_code.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    try {
                        PlacePicker.IntentBuilder intentBuilder =
                                new PlacePicker.IntentBuilder();
                        Intent intent = intentBuilder.build(getActivity());
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
                            ProfileFragment.this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                    dpd.setAccentColor(getResources().getColor(R.color.colorAccent));
                    dpd.setOkColor(getResources().getColor(R.color.colorPrimary));
                    dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
                }
            }
        });

        dob.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    Calendar now = Calendar.getInstance();
                    DatePickerDialog dpd = DatePickerDialog.newInstance(
                            ProfileFragment.this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                    dpd.setAccentColor(getResources().getColor(R.color.colorAccent));
                    dpd.setOkColor(getResources().getColor(R.color.colorPrimary));
                    dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
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
                        Intent intent = intentBuilder.build(getActivity());
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
                final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Joining...");
                progressDialog.show();
                BackendUtils.doPostRequest("/api/v1/user_profile", new HashMap<String, String>() {{
                    put("name", name.getText().toString());
                    put("race", race.getText().toString());
                    put("auth_token", new DataStorage(getContext()).getData("auth_token"));
                    put("district", zip_code.getText().toString());
                    put("age", String.valueOf(age));
                    put("income", income.getText().toString());
                    put("gender", gender.getText().toString());
                }}, new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        //new DataStorage(getApplicationContext()).storeData("auth_token", result.trim());
                        onSignUpSuccess();
                        progressDialog.dismiss();
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

                    }
                }, getContext());
            }
        });

        setupGenderSpinner();
        setupIncomeSpinner();
        setupRaceSpinner();



        join.setEnabled(false);
        instantiateEverything();
        join.setEnabled(true);


        return v;
    }

    @Override
    public void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == 1337
                && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(getActivity(), data);
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

                    Log.e("goat", body + "\n");

                }
            }, getContext());

        } else {
            super.onActivityResult(requestCode, resultCode, data);
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

    public static int getAge(Calendar dob) {
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
        Toast.makeText(getContext(), "Request failed", Toast.LENGTH_SHORT).show();
        join.setEnabled(true);
    }

    public void onSignUpSuccess() {
        join.setEnabled(true);
        Intent intent = new Intent(getContext(), NavigationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        getActivity().overridePendingTransition(0,0);
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
        final Spinner spinnerSubject = (Spinner) v.findViewById(R.id.gender_spinner);
        final EditText gender_input = (EditText) v.findViewById(R.id.gender_edit_text);
        final String[] subjets = getResources().getStringArray(R.array.gender);

        ArrayAdapter dataAdapterForSpinner = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,subjets);
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
                gender_input.setText("");
            }
        });
    }

    public String titleCase(String realName) {
        String space = " ";
        String[] names = realName.split(space);
        StringBuilder b = new StringBuilder();
        for (String name : names) {
            if (name == null || name.isEmpty()) {
                b.append(space);
                continue;
            }
            b.append(name.substring(0, 1).toUpperCase())
                    .append(name.substring(1).toLowerCase())
                    .append(space);
        }
        return b.toString();
    }

    public void setupRaceSpinner() {
        final Spinner spinnerSubject = (Spinner) v.findViewById(R.id.race_spinner);
        final EditText race_input = (EditText) v.findViewById(R.id.race_edit_text);
        final String[] subjets = getResources().getStringArray(R.array.race);

        ArrayAdapter dataAdapterForSpinner = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,subjets);
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
        final Spinner spinnerSubject = (Spinner) v.findViewById(R.id.income_spinner);
        final EditText income_input = (EditText) v.findViewById(R.id.income_edit_text);
        final String[] subjets = getResources().getStringArray(R.array.income);

        ArrayAdapter dataAdapterForSpinner = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,subjets);
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

    public void instantiateEverything() {
        BackendUtils.doPostRequest("/api/v1/user_profile_get", new HashMap<String, String>() {{
            put("auth_token", new DataStorage(getContext()).getData("auth_token"));
        }}, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d("goat", result);
                JSONObject object;

                try {
                    object = new JSONObject(result);
                    name.setText(titleCase(object.getString("name")));
                    nameDisplay.setText(titleCase(object.getString("name")));
                    DateFormat dateFormat = new SimpleDateFormat("MM-dd-YYYY");
                    Calendar cal = Calendar.getInstance();
                    age = Integer.parseInt(object.getString("age"));
                    cal.add(Calendar.YEAR, -age);
                    dob.setText(dateFormat.format(cal.getTime()));
                    gender.setText(object.getString("gender"));
                    zip_code.setText(object.getString("district"));
                    income.setText(object.getString("income"));
                    race.setText(object.getString("race"));

                    responsesNum.setText(object.getString("pollResponse"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
            }
        }, getContext());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

