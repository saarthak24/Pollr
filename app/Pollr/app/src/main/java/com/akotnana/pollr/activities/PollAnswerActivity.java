package com.akotnana.pollr.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

    Button submitButton;

    TextView question;

    RadioGroup radioGroup;

    ProgressBar bore;

    String answer = "Neutral";

    public String TAG = "PollAnswerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_answer);

        bore = (ProgressBar) findViewById(R.id.progress_bore);

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

        String fromNotification = "";
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                fromNotification = null;
            } else {
                fromNotification = extras.getString("fromNotification");
            }
        } else {
            fromNotification = (String) savedInstanceState.getSerializable("fromNotification");
        }

        submitButton = (Button) findViewById(R.id.submit_button);

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

        Log.d(TAG, type + "|" + id + "|" + question);

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

            Log.d(TAG, "slider startme");

            discreteSlider.setOnDiscreteSliderChangeListener(new DiscreteSlider.OnDiscreteSliderChangeListener() {
                @Override
                public void onPositionChanged(int position) {
                    int childCount = tickMarkLabelsRelativeLayout.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        TextView tv = (TextView) tickMarkLabelsRelativeLayout.getChildAt(i);
                        if (i == position) {
                            tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                            answer = tv.getText().toString();
                        }
                        else
                            tv.setTextColor(getResources().getColor(R.color.grey_400));
                    }
                }
            });

            discreteSlider.setPosition(2);

            tickMarkLabelsRelativeLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    tickMarkLabelsRelativeLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    addTickMarkTextLabels();
                }
            });
            bore.setVisibility(GONE);
            slider.setVisibility(View.VISIBLE);
            multipleChoice.setVisibility(GONE);
        } else {
            if(String.valueOf(id).length() < 2) {
                String[] arr = new String[]{"Yes", "No", "Other option"};

                for (int i = 0; i < arr.length; i++) {
                    RadioButton butt = new RadioButton(getApplicationContext());
                    butt.setText(arr[i]);
                    butt.setTextSize(18f);
                    butt.setPadding(0, 30, 0, 30);
                    ColorStateList colorStateList = new ColorStateList(
                            new int[][]{
                                    new int[]{-android.R.attr.state_checked},
                                    new int[]{android.R.attr.state_checked}
                            },
                            new int[]{

                                    Color.DKGRAY
                                    , getResources().getColor(R.color.colorPrimary),
                            }
                    );
                    butt.setButtonTintList(colorStateList);
                    if (!arr[i].isEmpty())
                        radioGroup.addView(butt);
                }
                bore.setVisibility(GONE);
                slider.setVisibility(GONE);
                multipleChoice.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "MC startme");
                final String finalId = id;
                BackendUtils.doGetRequest("/api/v1/getpoll", new HashMap<String, String>() {{
                    Log.d(TAG, new DataStorage(getApplicationContext()).getData("auth_token"));
                    put("auth_token", new DataStorage(getApplicationContext()).getData("auth_token"));
                    put("poll_id", finalId);
                }}, new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.d(TAG, result);
                        JSONObject object = null;
                        String hello = null;
                        try {
                            object = new JSONObject(result);
                            hello = object.getString("choices");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String[] arr = hello.substring(1, hello.length() - 1).split(", ");

                        for (int i = 0; i < arr.length; i++) {
                            RadioButton butt = new RadioButton(getApplicationContext());
                            butt.setText(arr[i]);
                            butt.setTextSize(18f);
                            butt.setPadding(0, 30, 0, 30);
                            ColorStateList colorStateList = new ColorStateList(
                                    new int[][]{
                                            new int[]{-android.R.attr.state_checked},
                                            new int[]{android.R.attr.state_checked}
                                    },
                                    new int[]{

                                            Color.DKGRAY
                                            , getResources().getColor(R.color.colorPrimary),
                                    }
                            );
                            butt.setButtonTintList(colorStateList);
                            if (!arr[i].isEmpty())
                                radioGroup.addView(butt);
                        }
                        bore.setVisibility(GONE);
                        slider.setVisibility(GONE);
                        multipleChoice.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(VolleyError error) {

                    }
                }, getApplicationContext());
            }

        }

        final String finalId1 = id;
        final String finalId2 = id;
        final String finalFromNotification = fromNotification;
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitButton.setEnabled(false);
                if(answer.equals("")) {
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    // find the radiobutton by returned id
                    RadioButton radioButton = (RadioButton) findViewById(selectedId);
                    answer = radioButton.getText().toString();
                }
                BackendUtils.doPostRequest("/api/v1/answer", new HashMap<String, String>() {{
                    put("auth_token", new DataStorage(getApplicationContext()).getData("auth_token"));
                    put("poll_id", finalId1);
                    put("answer", answer);
                }}, new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.d(TAG, result);
                        new DataStorage(getApplicationContext()).storeData("lastPollAnswered", finalId1);
                        if(finalFromNotification.equals(0))
                            finish();
                        else {
                            Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                            overridePendingTransition(0,0);
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {
                        submitButton.setEnabled(true);
                    }
                }, getApplicationContext());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0,0);
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
