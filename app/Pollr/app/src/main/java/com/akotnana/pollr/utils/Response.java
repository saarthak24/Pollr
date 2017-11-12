package com.akotnana.pollr.utils;

/**
 * Created by anees on 11/12/2017.
 */

public class Response {
    String question;
    String answer;

    public Response(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }


    public String getFullQuestion() {
        return this.question;
    }

    public String getShortQuestion() {
        if(this.question.length() < 60) {
            return this.question;
        }
        return this.question.substring(0, 60) + "...";
    }

    public String getAnswer() {
        return this.answer;
    }
}
