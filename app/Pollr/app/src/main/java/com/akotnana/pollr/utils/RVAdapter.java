package com.akotnana.pollr.utils;

/**
 * Created by anees on 11/12/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.akotnana.pollr.R;

import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PollViewHolder> {

    public static class PollViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView pollPolitician;
        TextView pollShortQuestion;
        TextView pollQuestionType;

        PollViewHolder(View pollView) {
            super(pollView);
            cv = (CardView) pollView.findViewById(R.id.cv);
            pollPolitician = (TextView) pollView.findViewById(R.id.politician_name);
            pollShortQuestion = (TextView) pollView.findViewById(R.id.poll_short_question);
            pollQuestionType = (TextView) pollView.findViewById(R.id.poll_type);
        }
    }

    List<Poll> polls;
    Context context;

    public RVAdapter(List<Poll> polls, Context con) {
        this.context = con;
        this.polls = polls;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void clear() {
        polls.clear();
        notifyDataSetChanged();
    }

    // Add a list of polls
    public void addAll(List<Poll> list) {
        polls.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public PollViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.poll, viewGroup, false);
        PollViewHolder pvh = new PollViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final PollViewHolder pollViewHolder, int i) {
        final int jj = i;
        pollViewHolder.pollPolitician.setText("from " + polls.get(i).getPolitician());
        pollViewHolder.pollShortQuestion.setText(polls.get(i).getShortQuestion());
        pollViewHolder.pollQuestionType.setText(polls.get(i).getPollType());
        pollViewHolder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Intent intent = new Intent(context, WaitActivity.class);
                intent.putExtra("id", Integer.toString(polls.get(jj).id));
                intent.putExtra("name", (polls.get(jj).pollName));
                intent.putExtra("distance", (polls.get(jj).distance));
                intent.putExtra("description", (polls.get(jj).description));
                intent.putExtra("location", new String[]{Double.toString(polls.get(jj).dd.getLatitude()), Double.toString(polls.get(jj).dd.getLongitude())});
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                */

            }
        });
    }

    @Override
    public int getItemCount() {
        return polls.size();
    }
}
