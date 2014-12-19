/*
 A survey object value.
*/

package edu.newpaltz.surveyit;

public class SurveyObjectValue {

    String id, sid, opNum, descrip;

    public SurveyObjectValue (String id, String sid, String opNum, String descrip) {
        this.id = id;
        this.sid = sid;
        this.opNum = opNum;
        this.descrip = descrip;
    }

    public String getId() {        return id;    }

    public String getSid() {        return sid;    }

    public String getOpNum() {        return opNum;    }

    public String getDescrip() {        return descrip;    }
}
