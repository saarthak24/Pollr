package com.akotnana.pollr.fragments;


import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.akotnana.pollr.R;
import com.akotnana.pollr.utils.BackendUtils;
import com.akotnana.pollr.utils.DataStorage;
import com.akotnana.pollr.utils.Response;
import com.akotnana.pollr.utils.RVAdapterResponse;
import com.akotnana.pollr.utils.VolleyCallback;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anees on 11/12/2017.
 */

public class ResponseFragment extends Fragment {

    public static boolean submitted = false;

    public String TAG = "ResponseFragment";

    private SwipeRefreshLayout swipeContainer;
    private List<Response> responses = new ArrayList<Response>();
    private static RecyclerView rv;
    private static RVAdapterResponse adapter;
    private String output = "";

    private SwipeRefreshLayout.OnRefreshListener swipeRefreshListner;

    private OnFragmentInteractionListener mListener;

    private Snackbar errorSnack;

    public ResponseFragment() {
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
        View v = inflater.inflate(R.layout.fragment_response, container, false);

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
                                BackendUtils.doGetRequest("/api/v1/responses", new HashMap<String, String>() {{
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
                                                if(adapter != null)
                                                    initializeAdapter();
                                                swipeContainer.setRefreshing(false);
                                            }
                                        });

                            }
                        }).start();

            }
        };

        swipeContainer.setOnRefreshListener(swipeRefreshListner);


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
        if(input.equals("")) {
            errorSnack = Snackbar.make(((Activity) getContext()).findViewById(android.R.id.content), "You don't have any responses. Swipe down to check!", Snackbar.LENGTH_INDEFINITE);
            errorSnack.setAction("Dismiss", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    errorSnack.dismiss();
                }
            });
            errorSnack.show();
        } else {
            if(input.equals("{}")) {
                responses.add(new Response("Convicted criminals should have the right to vote.", "Slightly agree"));
                responses.add(new Response("The U.S. should accept refugees from Syria.", "Strongly agree"));
                responses.add(new Response("Do you support Common Core national standards?", "No"));
            } else {
                if (errorSnack != null)
                    errorSnack.dismiss();
                swipeContainer.setVisibility(View.VISIBLE);
                //parse input and add to responses
                //TEMP
                JSONArray jsonarray = null;
                try {
                    jsonarray = new JSONArray(input);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonobject = null;
                    try {
                        jsonobject = jsonarray.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String question = "";
                    String answer = "";

                    try {
                        question = jsonobject.getString("question");
                        answer = jsonobject.getString("answer");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    responses.add(new Response(question, answer));
                }
            }
        }
    }

    private void initializeAdapter(){
        adapter = new RVAdapterResponse(responses, getContext());
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
        if(errorSnack != null)
            errorSnack.dismiss();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

