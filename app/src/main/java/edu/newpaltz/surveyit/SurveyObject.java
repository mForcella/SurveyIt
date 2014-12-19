/*
 A survey object.
*/

package edu.newpaltz.surveyit;

import java.util.ArrayList;

public class SurveyObject {

    String id, sid, jpg;
    ArrayList<String> options;

    public SurveyObject(String id, String sid, String jpg, ArrayList<String> options) {
        this.id = id;
        this.sid = sid;
        this.jpg = jpg;
        this.options = options;
    }

    public String getId() {        return id;    }

    public String getSid() {        return sid;    }

    public String getJpg() {        return jpg;     }

    public ArrayList<String> getOptions() {        return options;    }


}
