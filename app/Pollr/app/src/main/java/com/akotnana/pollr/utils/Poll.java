package com.akotnana.pollr.utils;

/**
 * Created by anees on 11/12/2017.
 */

public class Poll {
    String question;
    String pollType;
    String pollID;
    boolean verified;

    public Poll(String question, String pollType, String id, boolean verified) {
        this.question = question;
        this.pollID = id;
        this.pollType = pollType;
        this.verified = verified;
    }


    public String getFullQuestion() {
        return this.question;
    }

    public String getShortQuestion() {
        if(this.question.length() < 30) {
            return this.question;
        }
        return this.question.substring(0, 30) + "...";
    }

    public String getPollType() {
        return pollType;
    }

    public boolean canAnswer() {
        return verified;
    }

    public String getPollID() {
        return pollID;
    }
}
