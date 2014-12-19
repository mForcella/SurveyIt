/*
A survey instance object
*/

package edu.newpaltz.surveyit;

public class SurveyInstance {

    String id, sid, location, observer, comment, date;

    public SurveyInstance(String sid, String location, String observer, String comment, String date) {
        this.sid = sid;
        this.location = location;
        this.observer = observer;
        this.comment = comment;
        this.date = date;
    }

    public SurveyInstance(String id, String sid, String location, String observer, String comment, String date) {
        this.id = id;
        this.sid = sid;
        this.location = location;
        this.observer = observer;
        this.comment = comment;
        this.date = date;
    }

    public String getId() {        return id;    }

    public String getSid() {        return sid;    }

    public String getLocation() {        return location;    }

    public String getObserver() {        return observer;    }

    public String getComment() {        return comment;    }

    public String getDate() {        return date;    }
}