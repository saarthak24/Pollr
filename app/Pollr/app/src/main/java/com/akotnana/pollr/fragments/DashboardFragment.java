package com.akotnana.pollr.fragments;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.akotnana.pollr.R;
import com.akotnana.pollr.activities.CustomVerificationFlowActivity;
import com.akotnana.pollr.activities.SignInActivity;
import com.akotnana.pollr.utils.BackendUtils;
import com.akotnana.pollr.utils.Config;
import com.akotnana.pollr.utils.DataStorage;
import com.akotnana.pollr.utils.Poll;
import com.akotnana.pollr.utils.RVAdapter;
import com.akotnana.pollr.utils.VolleyCallback;
import com.android.volley.VolleyError;
import com.microblink.activity.ScanActivity;
import com.microblink.hardware.camera.CameraType;
import com.microblink.recognizers.BaseRecognitionResult;
import com.microblink.recognizers.RecognitionResults;
import com.microblink.recognizers.blinkbarcode.usdl.USDLScanResult;
import com.microblink.recognizers.blinkid.CombinedRecognizerSettings;
import com.microblink.recognizers.blinkid.usdl.combined.USDLCombinedRecognizerSettings;
import com.microblink.view.recognition.RecognitionType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.view.View.GONE;

/**
 * Created by anees on 11/12/2017.
 */

public class DashboardFragment extends Fragment {

    public static boolean submitted = false;

    public String TAG = "DashboardFragment";

    private SwipeRefreshLayout swipeContainer;
    private List<Poll> polls = new ArrayList<Poll>();
    private static RecyclerView rv;
    private static RVAdapter adapter;
    private String output = "";

    private SwipeRefreshLayout.OnRefreshListener swipeRefreshListner;

    private OnFragmentInteractionListener mListener;

    private TextView mEmptyText;

    private Snackbar errorSnack;

    private boolean verified = false;

    public static final int MY_BLINKID_REQUEST_CODE = 0x101;

    public DashboardFragment() {
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
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);

        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeRefreshListner = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(
                        new Runnable() {
                            public void run() {

                                Log.d(TAG, "started runnable");
                                getActivity().runOnUiThread(
                                        new Runnable() {
                                            public void run() {
                                                adapter.clear();
                                            }
                                        });


                                Log.d(TAG, "Retrieving");
                                String gg = "";
                                BackendUtils.doGetRequest("/api/v1/dashboard", new HashMap<String, String>() {{
                                    put("auth_token", new DataStorage(getContext()).getData("auth_token"));
                                }}, new VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        //Log.d(TAG, result);
                                        output = result;
                                    }

                                    @Override
                                    public void onError(VolleyError error) {

                                    }
                                }, getContext());

                                int i = 0;
                                while (output.equals("") && i < 50) {
                                    try {
                                        Thread.sleep(100);
                                        i += 50;
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if(output.equals(""))
                                    output = "{}";

                                getActivity().runOnUiThread(
                                        new Runnable() {
                                            public void run() {
                                                Log.d(TAG, "refreshing everything");
                                                try {
                                                    initializeData(output);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                if (adapter != null)
                                                    initializeAdapter();
                                                swipeContainer.setRefreshing(false);
                                            }
                                        });

                            }
                        }).start();

            }
        };

        swipeContainer.setOnRefreshListener(swipeRefreshListner);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.myDialog));
        builder.setTitle("Verification needed");
        builder.setMessage("Pollr needs to verify your identity and personal details before you can use this app. A personal identification card (i.e Drivers License) is needed to verify your identity.");
        builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialogBox, int id) {
                getActivity().startActivityForResult(buildScanIntent(buildUSDLCombinedElement()), MY_BLINKID_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();

        //TODO: remove temp

        rv = (RecyclerView) v.findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        initializeAdapter();

        swipeContainer.post(new Runnable() {
            @Override
            public void run() {
                swipeContainer.setRefreshing(true);
                // directly call onRefresh() method
                swipeRefreshListner.onRefresh();
            }
        });


        return v;
    }

    private void initializeData(String input) throws JSONException {
        adapter.clear();
        adapter.notifyDataSetChanged();
        Log.d(TAG, input);
        if (input.equals("")) {
            errorSnack = Snackbar.make(((Activity) getContext()).findViewById(android.R.id.content), "No polls currently available. Swipe down to check!", Snackbar.LENGTH_INDEFINITE);
            errorSnack.setAction("Dismiss", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    errorSnack.dismiss();
                }
            });
            errorSnack.show();
        } else {
            if(output.equals("{}")) {
                polls.add(new Poll("The government should raise the federal minimum wage.", "sd", "1"));
                polls.add(new Poll("The government should make cuts to public spending in order to reduce the national debt.", "sd", "2"));
                polls.add(new Poll("Should police officers be required to wear body cameras?", "mc", "3"));
            } else {
                if (errorSnack != null)
                    errorSnack.dismiss();
                swipeContainer.setVisibility(View.VISIBLE);
                //parse input and add to polls
                //TEMP
                JSONArray jsonarray = null;
                try {
                    jsonarray = new JSONArray(input);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String bad = new DataStorage(getContext()).getData("lastPollAnswered");
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonobject = null;
                    try {
                        jsonobject = jsonarray.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String id = "";
                    String question = "";
                    String type = "";

                    try {
                        id = jsonobject.getString("id");
                        question = jsonobject.getString("question");
                        type = jsonobject.getString("type");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (!id.equals(bad))
                        polls.add(new Poll(question, type, id));
                }
            }
        }
    }

    private void initializeAdapter() {
        adapter = new RVAdapter(polls, getContext());
        rv.setAdapter(adapter);
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
    public void onResume() {
        Log.d(TAG, "HELLOOOOO");
        adapter.clear();
        adapter.notifyDataSetChanged();
        swipeContainer.post(new Runnable() {
            @Override
            public void run() {
                swipeContainer.setRefreshing(true);
                // directly call onRefresh() method
                swipeRefreshListner.onRefresh();
            }
        });
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (errorSnack != null)
            errorSnack.dismiss();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }



    ///////////////////////////////////////////////


    private Intent buildScanIntent(CombinedRecognizerSettings combinedRecognizerSettings) {
        Intent intent = new Intent(getActivity(), CustomVerificationFlowActivity.class);
        intent.putExtra(CustomVerificationFlowActivity.EXTRAS_LICENSE_KEY, Config.LICENSE_KEY);
        intent.putExtra(CustomVerificationFlowActivity.EXTRAS_COMBINED_RECOGNIZER_SETTINGS, combinedRecognizerSettings);
        intent.putExtra(CustomVerificationFlowActivity.EXTRAS_COMBINED_CAMERA_TYPE, (Parcelable) CameraType.CAMERA_BACKFACE);
        return intent;
    }

    private CombinedRecognizerSettings  buildUSDLCombinedElement() {
        USDLCombinedRecognizerSettings usdlCombined = new USDLCombinedRecognizerSettings();
        return usdlCombined;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // onActivityResult is called whenever we are returned from activity started
        // with startActivityForResult. We need to check request code to determine
        // that we have really returned from BlinkID activity.
        if (requestCode == MY_BLINKID_REQUEST_CODE) {

            // make sure BlinkID activity returned result
            if (resultCode == CustomVerificationFlowActivity.RESULT_OK && data != null) {

                USDLScanResult combinedResult = (USDLScanResult) data.getParcelableExtra(CustomVerificationFlowActivity.EXTRAS_COMBINED_RECOGNITION_RESULT);
                if (combinedResult != null) {
                    // prepare recognition results for ResultActivity, it accepts RecognitionResults extra
                    data.putExtra(ScanActivity.EXTRAS_RECOGNITION_RESULTS, new RecognitionResults(new BaseRecognitionResult[]{combinedResult}, RecognitionType.SUCCESSFUL));
                } else {
                    Log.e("DashboardFragment", "Unable to retrieve recognition results!");
                }
                Log.e("DashboardFragment", "Got recognition results!");

                String name = combinedResult.getField(USDLScanResult.kCustomerFullName);
                String dob = combinedResult.getField(USDLScanResult.kDateOfBirth);
                String gender = combinedResult.getField(USDLScanResult.kSex);
                String address = combinedResult.getField(USDLScanResult.kFullAddress);

                Toast.makeText(getContext(), name + "\n" + dob + "\n" + gender + "\n" + address, Toast.LENGTH_LONG).show();

                // set intent's component to ResultActivity and pass its contents
                // to ResultActivity. ResultActivity will show how to extract
                // data from result.
                //data.setComponent(new ComponentName(this, ResultActivity.class));
                //startActivity(data);
            } else {
                // if BlinkID activity did not return result, user has probably
                // pressed Back button and cancelled scanning
                Toast.makeText(getActivity().getApplicationContext(), "Scan cancelled!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

