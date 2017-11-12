package com.akotnana.pollr.utils;

/**
 * Created by anees on 11/12/2017.
 */

public class Poll {
    String politician;
    String question;
    String pollType;
    int pollID;

    public Poll(String pol, String question, String pollType, int id) {
        this.politician = pol;
        this.question = question;
        this.pollID = id;
        this.pollType = pollType;
    }

    public String getPolitician() {
        return this.politician;
    }

    public String getFullQuestion() {
        return this.question;
    }

    public String getShortQuestion() {
        return this.question.substring(0, 30) + "...";
    }

    public String getPollType() {
        return pollType;
    }

    public int getPollID() {
        return pollID;
    }
}
