package edu.newpaltz.surveyit;

public class SurveyObjectImage {

    String id, oId, jpg, ssId, date, rank, flag;

    public SurveyObjectImage (String id, String oId, String jpg, String ssId, String date, String rank, String flag) {
        this.id = id;
        this.oId = oId;
        this.jpg = jpg;
        this.ssId = ssId;
        this.date = date;
        this.rank = rank;
        this.flag = flag;
    }

    public SurveyObjectImage (String oId, String jpg, String ssId, String date, String rank, String flag) {
        this.oId = oId;
        this.jpg = jpg;
        this.ssId = ssId;
        this.date = date;
        this.rank = rank;
        this.flag = flag;
    }

    public String getId() {        return id;    }

    public void setId(String id) {        this.id = id;    }

    public String getoId() {        return oId;    }

    public void setoId(String oId) {        this.oId = oId;    }

    public String getJpg() {        return jpg;    }

    public void setJpg(String jpg) {        this.jpg = jpg;    }

    public String getSsId() {        return ssId;    }

    public void setSsId(String ssId) {        this.ssId = ssId;    }

    public String getDate() {        return date;    }

    public void setDate(String date) {        this.date = date;    }

    public String getRank() {        return rank;    }

    public void setRank(String rank) {        this.rank = rank;    }

    public String getFlag() {        return flag;    }

    public void setFlag(String flag) {        this.flag = flag;    }
}
