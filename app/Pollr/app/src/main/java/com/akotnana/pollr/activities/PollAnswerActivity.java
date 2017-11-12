package com.akotnana.pollr.activities;

import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.akotnana.pollr.R;
import com.akotnana.pollr.fragments.DashboardFragment;
import com.akotnana.pollr.utils.BackendUtils;
import com.akotnana.pollr.utils.DataStorage;
import com.akotnana.pollr.utils.VolleyCallback;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.etiennelawlor.discreteslider.library.ui.DiscreteSlider;
import com.etiennelawlor.discreteslider.library.utilities.DisplayUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.RunnableFuture;

import static android.view.View.GONE;

public class PollAnswerActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private TextView toolbarTitle;

    RelativeLayout tickMarkLabelsRelativeLayout;
    DiscreteSlider discreteSlider;

    LinearLayout multipleChoice;
    LinearLayout slider;

    TextView question;

    RadioGroup radioGroup;

    ArrayList<String> choices = new ArrayList<>();

    public String TAG = "PollAnswerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_answer);

        String type = "";
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                type = null;
            } else {
                type = extras.getString("type");
            }
        } else {
            type = (String) savedInstanceState.getSerializable("type");
        }

        String id = "";
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                id = null;
            } else {
                id = extras.getString("id");
            }
        } else {
            id = (String) savedInstanceState.getSerializable("id");
        }

        String question = "";
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                question = null;
            } else {
                question = extras.getString("question");
            }
        } else {
            question = (String) savedInstanceState.getSerializable("question");
        }

        this.question = (TextView) findViewById(R.id.question);
        this.question.setText(question);

        discreteSlider = (DiscreteSlider) findViewById(R.id.discrete_slider);
        tickMarkLabelsRelativeLayout = (RelativeLayout) findViewById(R.id.tick_mark_labels_rl);

        multipleChoice = (LinearLayout) findViewById(R.id.multiple_choice_view);
        slider = (LinearLayout) findViewById(R.id.slider_view) ;

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbarTitle.setText("Answer Poll");

        if(type.equals("sd")) {


            discreteSlider.setOnDiscreteSliderChangeListener(new DiscreteSlider.OnDiscreteSliderChangeListener() {
                @Override
                public void onPositionChanged(int position) {
                    int childCount = tickMarkLabelsRelativeLayout.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        TextView tv = (TextView) tickMarkLabelsRelativeLayout.getChildAt(i);
                        if (i == position)
                            tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        else
                            tv.setTextColor(getResources().getColor(R.color.grey_400));
                    }
                }
            });

            tickMarkLabelsRelativeLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    tickMarkLabelsRelativeLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    addTickMarkTextLabels();
                }
            });
            slider.setVisibility(View.VISIBLE);
            multipleChoice.setVisibility(GONE);
        } else {
            final String finalId = id;
            BackendUtils.doGetRequest("/api/v1/pokemon", new HashMap<String, String>() {{
                put("auth_token", new DataStorage(getApplicationContext()).getData("auth_token"));
                put("poll_id", finalId);
            }}, new VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    JSONObject object = null;
                    JSONArray arr = null;
                    try {
                        object = new JSONObject(result);
                        arr = object.getJSONArray("choices");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < arr.length(); i++) {

                        try {
                            choices.add((String) arr.get(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    for(String a : choices) {
                        RadioButton butt = new RadioButton(getApplicationContext());
                        butt.setText(a);
                        radioGroup.addView(butt);
                    }
                    slider.setVisibility(GONE);
                    multipleChoice.setVisibility(View.VISIBLE);
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
                }
            }, getApplicationContext());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addTickMarkTextLabels(){
        int tickMarkCount = discreteSlider.getTickMarkCount();
        float tickMarkRadius = discreteSlider.getTickMarkRadius();
        int width = tickMarkLabelsRelativeLayout.getMeasuredWidth();

        int discreteSliderBackdropLeftMargin = DisplayUtility.dp2px(getApplicationContext(), 32);
        int discreteSliderBackdropRightMargin = DisplayUtility.dp2px(getApplicationContext(), 32);
        float firstTickMarkRadius = tickMarkRadius;
        float lastTickMarkRadius = tickMarkRadius;
        int interval = (width - (discreteSliderBackdropLeftMargin+discreteSliderBackdropRightMargin) - ((int)(firstTickMarkRadius+lastTickMarkRadius)) )
                / (tickMarkCount-1);

        String[] tickMarkLabels = {"Strongly\nDisagree", "Disagree", "Neutral", "Agree", "Strongly\nAgree"};
        int tickMarkLabelWidth = DisplayUtility.dp2px(getApplicationContext(), 60);

        for(int i=0; i<tickMarkCount; i++) {
            TextView tv = new TextView(getApplicationContext());

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    tickMarkLabelWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);

            tv.setText(tickMarkLabels[i]);
            tv.setGravity(Gravity.CENTER);
            if(i==discreteSlider.getPosition())
                tv.setTextColor(getResources().getColor(R.color.colorPrimary));
            else
                tv.setTextColor(getResources().getColor(R.color.grey_400));

//                    tv.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));

            int left = discreteSliderBackdropLeftMargin + (int)firstTickMarkRadius + (i * interval) - (tickMarkLabelWidth/2);

            layoutParams.setMargins(left,
                    0,
                    0,
                    0);
            tv.setLayoutParams(layoutParams);

            tickMarkLabelsRelativeLayout.addView(tv);
        }
    }

}
