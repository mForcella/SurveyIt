/*
 A survey object.
*/

package edu.newpaltz.surveyit;

public class Survey {

    String id, name, desc, date;
    int opNum;

    public Survey(String id, String name, String desc, String date, int opNum) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.date = date;
        this.opNum = opNum;
    }

    public String getId() {       return id;    }

    public String getName() {        return name;    }

    public String getDesc() {        return desc;    }

    public String getDate() {        return date;    }

    public int getOpNum() {         return opNum;   }
}
