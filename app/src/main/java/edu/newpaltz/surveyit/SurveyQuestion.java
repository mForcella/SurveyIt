/*
 A survey question.
*/

package edu.newpaltz.surveyit;

public class SurveyQuestion {

    String id, sid, question, resType, resVal;

    public SurveyQuestion(String id, String sid, String question, String resType, String resVal) {
        this.id = id;
        this.sid = sid;
        this.question = question;
        this.resType = resType;
        this.resVal = resVal;
    }

    public String getId() {        return id;    }

    public String getSid() {        return sid;    }

    public String getQuestion() {        return question;    }

    public String getResType() {        return resType;    }

    public String getResVal() {        return resVal;    }
}
