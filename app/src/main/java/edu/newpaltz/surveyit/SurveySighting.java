/*
 A survey sighting.
*/

package edu.newpaltz.surveyit;

public class SurveySighting {

    String id, siId, oId, jpg, lat, lng, date;

    public SurveySighting(String siId, String oId, String jpg,
                          String lat, String lng, String date) {
        this.siId = siId;
        this.oId = oId;
        this.jpg = jpg;
        this.lat = lat;
        this.lng = lng;
        this.date = date;
    }

    public SurveySighting(String id, String siId, String oId, String jpg,
                          String lat, String lng, String date) {
        this.id = id;
        this.siId = siId;
        this.oId = oId;
        this.jpg = jpg;
        this.lat = lat;
        this.lng = lng;
        this.date = date;
    }

    public String getId() {        return id;    }

    public String getSiId() {        return siId;    }

    public String getoId() {        return oId;    }

    public String getJpg() {        return jpg;    }

    public String getLat() {        return lat;    }

    public String getLng() {        return lng;    }

    public String getDate() {        return date;    }
}
