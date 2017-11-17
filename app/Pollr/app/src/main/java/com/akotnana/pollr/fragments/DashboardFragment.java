package com.akotnana.pollr.fragments;


import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.akotnana.pollr.utils.Poll;
import com.akotnana.pollr.utils.RVAdapter;
import com.akotnana.pollr.utils.VolleyCallback;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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

                                while(output.equals("")) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

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
        if(input.equals("") || input.length() < 5) {
            errorSnack = Snackbar.make(((Activity) getContext()).findViewById(android.R.id.content), "No polls currently available. Swipe down to check!", Snackbar.LENGTH_INDEFINITE);
            errorSnack.setAction("Dismiss", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    errorSnack.dismiss();
                }
            });
            errorSnack.show();
        } else {
            if(errorSnack != null)
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
                if(!id.equals(bad))
                    polls.add(new Poll(question, type, id));
            }
        }
    }

    private void initializeAdapter(){
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
        if(errorSnack != null)
            errorSnack.dismiss();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

