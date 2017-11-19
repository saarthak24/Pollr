package com.akotnana.pollr.utils;

/**
 * Created by anees on 11/12/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.akotnana.pollr.R;
import com.akotnana.pollr.utils.Response;

import java.util.List;

public class RVAdapterResponse extends RecyclerView.Adapter<RVAdapterResponse.ResponseViewHolder> {

    public static class ResponseViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView responseShortQuestion;
        TextView responseAnswer;

        ResponseViewHolder(View responseView) {
            super(responseView);
            cv = (CardView) responseView.findViewById(R.id.cv);
            responseShortQuestion = (TextView) responseView.findViewById(R.id.response_short_question);
            responseAnswer = (TextView) responseView.findViewById(R.id.response_answer);
        }
    }

    List<Response> responses;
    Context context;

    public RVAdapterResponse(List<Response> responses, Context con) {
        this.context = con;
        this.responses = responses;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void clear() {
        responses.clear();
        notifyDataSetChanged();
    }

    // Add a list of responses
    public void addAll(List<Response> list) {
        responses.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public ResponseViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.response, viewGroup, false);
        ResponseViewHolder pvh = new ResponseViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final ResponseViewHolder responseViewHolder, int i) {
        final int jj = i;
        responseViewHolder.responseShortQuestion.setText(responses.get(i).getFullQuestion());
        responseViewHolder.responseAnswer.setText(responses.get(i).getAnswer());
        
    }

    @Override
    public int getItemCount() {
        return responses.size();
    }
}
